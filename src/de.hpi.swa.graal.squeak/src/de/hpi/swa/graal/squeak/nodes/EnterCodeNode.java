package de.hpi.swa.graal.squeak.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RootNode;

import de.hpi.swa.graal.squeak.SqueakLanguage;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.ContextObject;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameSlotWriteNode;
import de.hpi.swa.graal.squeak.nodes.context.stack.StackPushNode;
import de.hpi.swa.graal.squeak.util.FrameAccess;
import de.hpi.swa.graal.squeak.util.FrameMarker;

public abstract class EnterCodeNode extends RootNode {
    @CompilationFinal protected final CompiledCodeObject code;
    @Child private GetOrCreateContextNode createContextNode;
    @Child private ExecuteContextNode executeContextNode;
    @Child private FrameSlotWriteNode contextWriteNode;
    @Child private FrameSlotWriteNode instructionPointerWriteNode;
    @Child private FrameSlotWriteNode stackPointerWriteNode;
    @Child private StackPushNode pushStackNode;

    public static EnterCodeNode create(final SqueakLanguage language, final CompiledCodeObject code) {
        return EnterCodeNodeGen.create(language, code);
    }

    protected EnterCodeNode(final SqueakLanguage language, final CompiledCodeObject code) {
        super(language, code.getFrameDescriptor());
        this.code = code;
        createContextNode = GetOrCreateContextNode.create(code);
        executeContextNode = ExecuteContextNode.create(code);
        contextWriteNode = FrameSlotWriteNode.create(code.thisContextOrMarkerSlot);
        instructionPointerWriteNode = FrameSlotWriteNode.create(code.instructionPointerSlot);
        stackPointerWriteNode = FrameSlotWriteNode.create(code.stackPointerSlot);
        pushStackNode = StackPushNode.create(code);
    }

    private void initializeSlots(final VirtualFrame frame) {
        contextWriteNode.executeWrite(frame, new FrameMarker());
        instructionPointerWriteNode.executeWrite(frame, 0L);
        stackPointerWriteNode.executeWrite(frame, -1L);
    }

    @ExplodeLoop
    @Specialization(assumptions = {"code.getCanBeVirtualizedAssumption()"})
    protected Object enterVirtualized(final VirtualFrame frame) {
        CompilerDirectives.ensureVirtualized(frame);
        initializeSlots(frame);
        final int numArgsAndCopiedValues = code.getNumArgsAndCopiedValues();
        // Push arguments and copied values onto the newContext.
        final Object[] arguments = frame.getArguments();
        for (int i = 0; i < numArgsAndCopiedValues; i++) {
            pushStackNode.executeWrite(frame, arguments[FrameAccess.RCVR_AND_ARGS_START + 1 + i]);
        }
        // Initialize temps with nil in newContext.
        final int numTemps = code.getNumTemps();
        for (int i = 0; i < numTemps - numArgsAndCopiedValues; i++) {
            pushStackNode.executeWrite(frame, code.image.nil);
        }
        assert FrameUtil.getLongSafe(frame, code.stackPointerSlot) + 1 >= numTemps;
        return executeContextNode.executeVirtualized(frame);
    }

    @ExplodeLoop
    @Specialization(guards = {"!code.getCanBeVirtualizedAssumption().isValid()"})
    protected Object enter(final VirtualFrame frame) {
        initializeSlots(frame);
        final ContextObject newContext = createContextNode.executeGet(frame, true);
        contextWriteNode.executeWrite(frame, newContext);
        // Push arguments and copied values onto the newContext.
        final Object[] arguments = frame.getArguments();
        final int numArgsAndCopiedValues = code.getNumArgsAndCopiedValues();
        for (int i = 0; i < numArgsAndCopiedValues; i++) {
            newContext.push(arguments[FrameAccess.RCVR_AND_ARGS_START + 1 + i]);
        }
        // Initialize temps with nil in newContext.
        final int numTemps = code.getNumTemps();
        for (int i = 0; i < numTemps - numArgsAndCopiedValues; i++) {
            newContext.push(code.image.nil);
        }
        assert newContext.getStackPointer() >= numTemps;
        return executeContextNode.executeNonVirtualized(frame, newContext);
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        return code.toString();
    }
}