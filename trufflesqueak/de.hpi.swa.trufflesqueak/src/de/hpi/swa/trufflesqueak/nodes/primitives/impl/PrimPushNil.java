package de.hpi.swa.trufflesqueak.nodes.primitives.impl;

import com.oracle.truffle.api.frame.VirtualFrame;

import de.hpi.swa.trufflesqueak.model.CompiledMethodObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveQuickReturnNode;

public class PrimPushNil extends PrimitiveQuickReturnNode {
    public PrimPushNil(CompiledMethodObject code) {
        super(code);
    }

    @Override
    protected Object getConstant(VirtualFrame frame) {
        return code.image.nil;
    }
}