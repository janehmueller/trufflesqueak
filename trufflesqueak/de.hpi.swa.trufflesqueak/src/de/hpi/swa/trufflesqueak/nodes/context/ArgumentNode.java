package de.hpi.swa.trufflesqueak.nodes.context;

import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.SqueakBytecodeNode;

public class ArgumentNode extends SqueakBytecodeNode {
    private final int idx;

    public ArgumentNode(CompiledCodeObject code, int index) {
        super(code, 0);
        idx = index;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return push(frame, frame.getArguments()[idx]);
    }
}
