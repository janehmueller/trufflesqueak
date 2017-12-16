package de.hpi.swa.trufflesqueak.nodes.bytecodes.jump;

import de.hpi.swa.trufflesqueak.model.CompiledCodeObject;

public class UnconditionalJumpNode extends AbstractJump {

    public UnconditionalJumpNode(CompiledCodeObject code, int index, int numBytecodes, int bytecode) {
        super(code, index, numBytecodes, shortJumpOffset(bytecode));
        successors[0] = successorIndex + offset;
    }

    public UnconditionalJumpNode(CompiledCodeObject code, int index, int numBytecodes, int bytecode, int parameter) {
        super(code, index, numBytecodes, longUnconditionalJumpOffset(bytecode, parameter));
        successors[0] = successorIndex + offset;
    }

    @Override
    public String toString() {
        return "jumpTo: " + offset;
    }
}
