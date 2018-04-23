package de.hpi.swa.graal.squeak.nodes.bytecodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;

import de.hpi.swa.graal.squeak.SqueakImageContext;
import de.hpi.swa.graal.squeak.exceptions.PrimitiveExceptions.PrimitiveWithoutResultException;
import de.hpi.swa.graal.squeak.model.BaseSqueakObject;
import de.hpi.swa.graal.squeak.model.ClassObject;
import de.hpi.swa.graal.squeak.model.CompiledCodeObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.MESSAGE;
import de.hpi.swa.graal.squeak.model.ObjectLayouts.SPECIAL_OBJECT_INDEX;
import de.hpi.swa.graal.squeak.model.PointersObject;
import de.hpi.swa.graal.squeak.model.SpecialSelectorObject;
import de.hpi.swa.graal.squeak.nodes.AbstractNodeWithImage;
import de.hpi.swa.graal.squeak.nodes.DispatchNode;
import de.hpi.swa.graal.squeak.nodes.LookupNode;
import de.hpi.swa.graal.squeak.nodes.context.SqueakLookupClassNode;
import de.hpi.swa.graal.squeak.nodes.context.frame.FrameSlotReadNode;
import de.hpi.swa.graal.squeak.nodes.context.stack.StackPopNReversedNode;
import de.hpi.swa.graal.squeak.nodes.context.stack.StackPushNode;
import de.hpi.swa.graal.squeak.util.ArrayUtils;

public final class SendBytecodes {

    public abstract static class AbstractSendNode extends AbstractBytecodeNode {
        @CompilationFinal protected final Object selector;
        @CompilationFinal private final int argumentCount;
        @Child protected SqueakLookupClassNode lookupClassNode;
        @Child private LookupNode lookupNode = LookupNode.create();
        @Child private DispatchNode dispatchNode = DispatchNode.create();
        @Child private SendDoesNotUnderstandNode sendDoesNotUnderstandNode;
        @Child private SendObjectAsMethodNode sendObjectAsMethodNode;
        @Child private StackPopNReversedNode popNReversedNode;
        @Child private StackPushNode pushNode;
        @Child private FrameSlotReadNode readContextNode;

        private AbstractSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object sel, final int argcount) {
            super(code, index, numBytecodes);
            selector = sel;
            argumentCount = argcount;
            lookupClassNode = SqueakLookupClassNode.create(code.image);
            pushNode = StackPushNode.create(code);
            popNReversedNode = StackPopNReversedNode.create(code, 1 + argumentCount);
            readContextNode = FrameSlotReadNode.create(code.thisContextOrMarkerSlot);
            sendDoesNotUnderstandNode = SendDoesNotUnderstandNode.create(code.image);
            sendObjectAsMethodNode = SendObjectAsMethodNode.create(code.image);
        }

        @Override
        public void executeVoid(final VirtualFrame frame) {
            final Object result;
            try {
                result = executeSend(frame);
            } catch (PrimitiveWithoutResultException e) {
                return; // ignoring result
            }
            pushNode.executeWrite(frame, result);
        }

        public Object executeSend(final VirtualFrame frame) {
            final Object[] rcvrAndArgs = (Object[]) popNReversedNode.executeRead(frame);
            final ClassObject rcvrClass = lookupClassNode.executeLookup(rcvrAndArgs[0]);
            final Object lookupResult = lookupNode.executeLookup(rcvrClass, selector);
            final Object contextOrMarker = readContextNode.executeRead(frame);
            if (!(lookupResult instanceof CompiledCodeObject)) {
                return sendObjectAsMethodNode.execute(frame, selector, rcvrAndArgs, lookupResult, contextOrMarker);
            } else if (((CompiledCodeObject) lookupResult).isDoesNotUnderstand()) {
                return sendDoesNotUnderstandNode.execute(frame, selector, rcvrAndArgs, rcvrClass, lookupResult, contextOrMarker);
            } else {
                return dispatchNode.executeDispatch(frame, lookupResult, rcvrAndArgs, contextOrMarker);
            }
        }

        public Object getSelector() {
            return selector;
        }

        @Override
        public boolean hasTag(final Class<? extends Tag> tag) {
            return ((tag == StandardTags.StatementTag.class) || (tag == StandardTags.CallTag.class));
        }

        @Override
        public String toString() {
            return "send: " + selector.toString();
        }
    }

    public static class SecondExtendedSendNode extends AbstractSendNode {
        public SecondExtendedSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int i) {
            super(code, index, numBytecodes, code.getLiteral(i & 63), i >> 6);
        }
    }

    public static class SendLiteralSelectorNode extends AbstractSendNode {
        public static AbstractBytecodeNode create(final CompiledCodeObject code, final int index, final int numBytecodes, final int literalIndex, final int argCount) {
            final Object selector = code.getLiteral(literalIndex);
            // if (selector != null && selector.toString().equals("halt")) {
            // return new HaltNode(code, index);
            // }
            return new SendLiteralSelectorNode(code, index, numBytecodes, selector, argCount);
        }

        public SendLiteralSelectorNode(final CompiledCodeObject code, final int index, final int numBytecodes, final Object selector, final int argCount) {
            super(code, index, numBytecodes, selector, argCount);
        }
    }

    public static class SendSelectorNode extends AbstractSendNode {
        public static SendSelectorNode createForSpecialSelector(final CompiledCodeObject code, final int index, final int selectorIndex) {
            final SpecialSelectorObject specialSelector = code.image.specialSelectorsArray[selectorIndex];
            return new SendSelectorNode(code, index, 1, specialSelector, specialSelector.getNumArguments());
        }

        public SendSelectorNode(final CompiledCodeObject code, final int index, final int numBytecodes, final BaseSqueakObject sel, final int argcount) {
            super(code, index, numBytecodes, sel, argcount);
        }
    }

    public static class SendSelfSelector extends AbstractSendNode {
        public SendSelfSelector(final CompiledCodeObject code, final int index, final int numBytecodes, final Object selector, final int numArgs) {
            super(code, index, numBytecodes, selector, numArgs);
        }
    }

    public static class SingleExtendedSendNode extends AbstractSendNode {
        public SingleExtendedSendNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int param) {
            super(code, index, numBytecodes, code.getLiteral(param & 31), param >> 5);
        }
    }

    public static class SingleExtendedSuperNode extends AbstractSendNode {
        protected static class SqueakLookupClassSuperNode extends SqueakLookupClassNode {
            @CompilationFinal private final CompiledCodeObject code;

            public SqueakLookupClassSuperNode(final CompiledCodeObject code) {
                super(code.image);
                this.code = code; // storing both, image and code, because of class hierarchy
            }

            @Override
            public ClassObject executeLookup(final Object receiver) {
                final ClassObject compiledInClass = code.getCompiledInClass();
                final Object superclass = compiledInClass.getSuperclass();
                if (superclass == code.image.nil) {
                    return compiledInClass;
                } else {
                    return (ClassObject) superclass;
                }
            }
        }

        public SingleExtendedSuperNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int rawByte) {
            this(code, index, numBytecodes, rawByte & 31, rawByte >> 5);
        }

        public SingleExtendedSuperNode(final CompiledCodeObject code, final int index, final int numBytecodes, final int literalIndex, final int numArgs) {
            super(code, index, numBytecodes, code.getLiteral(literalIndex), numArgs);
            lookupClassNode = new SqueakLookupClassSuperNode(code);
        }

        @Override
        public String toString() {
            return "sendSuper: " + selector.toString();
        }
    }

    public static final class SendDoesNotUnderstandNode extends AbstractNodeWithImage {
        @Child private DispatchNode dispatchNode = DispatchNode.create();
        @CompilationFinal private ClassObject messageClass;

        public static SendDoesNotUnderstandNode create(final SqueakImageContext image) {
            return new SendDoesNotUnderstandNode(image);
        }

        private SendDoesNotUnderstandNode(final SqueakImageContext image) {
            super(image);
        }

        public Object execute(final VirtualFrame frame, final Object selector, final Object[] rcvrAndArgs, final ClassObject rcvrClass, final Object lookupDNU, final Object contextOrMarker) {
            final PointersObject message = (PointersObject) getMessageClass().newInstance();
            message.atput0(MESSAGE.SELECTOR, selector);
            final Object[] arguments = ArrayUtils.allButFirst(rcvrAndArgs);
            message.atput0(MESSAGE.ARGUMENTS, image.newList(arguments));
            if (message.instsize() > MESSAGE.LOOKUP_CLASS) { // early versions do not have
                                                             // lookupClass
                message.atput0(MESSAGE.LOOKUP_CLASS, rcvrClass);
            }
            return dispatchNode.executeDispatch(frame, lookupDNU, new Object[]{rcvrAndArgs[0], message}, contextOrMarker);
        }

        private ClassObject getMessageClass() {
            if (messageClass == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                messageClass = (ClassObject) image.specialObjectsArray.at0(SPECIAL_OBJECT_INDEX.ClassMessage);
            }
            return messageClass;
        }
    }

    public static final class SendObjectAsMethodNode extends AbstractNodeWithImage {
        @Child private DispatchNode dispatchNode = DispatchNode.create();
        @Child private LookupNode lookupNode = LookupNode.create();
        @Child protected SqueakLookupClassNode lookupClassNode;
        @CompilationFinal private NativeObject runWithIn;

        public static SendObjectAsMethodNode create(final SqueakImageContext image) {
            return new SendObjectAsMethodNode(image);
        }

        private SendObjectAsMethodNode(final SqueakImageContext image) {
            super(image);
            lookupClassNode = SqueakLookupClassNode.create(image);
        }

        public Object execute(final VirtualFrame frame, final Object selector, final Object[] rcvrAndArgs, final Object lookupResult, final Object contextOrMarker) {
            final Object[] arguments = ArrayUtils.allButFirst(rcvrAndArgs);
            final ClassObject rcvrClass = lookupClassNode.executeLookup(lookupResult);
            final Object newLookupResult = lookupNode.executeLookup(rcvrClass, getRunWithIn());
            return dispatchNode.executeDispatch(frame, newLookupResult, new Object[]{lookupResult, selector, image.newList(arguments), rcvrAndArgs[0]}, contextOrMarker);
        }

        private NativeObject getRunWithIn() {
            if (runWithIn == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                runWithIn = (NativeObject) image.specialObjectsArray.at0(SPECIAL_OBJECT_INDEX.SelectorRunWithIn);
            }
            return runWithIn;
        }
    }
}