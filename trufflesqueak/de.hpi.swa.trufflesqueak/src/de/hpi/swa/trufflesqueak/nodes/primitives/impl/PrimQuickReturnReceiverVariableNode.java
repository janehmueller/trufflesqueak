package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.bytecodes.PushReceiverVariableNode;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveQuickReturnNode;

public class PrimQuickReturnReceiverVariableNode extends PrimitiveQuickReturnNode {
    @Child PushReceiverVariableNode actual;

    public PrimQuickReturnReceiverVariableNode(CompiledMethodObject cm, int variableIdx) {
        super(cm);
        actual = new PushReceiverVariableNode(cm, -1, variableIdx);
    }

    @Override
    protected Object getConstant(VirtualFrame frame) {
        return actual.executeGeneric(frame);
    }
}
