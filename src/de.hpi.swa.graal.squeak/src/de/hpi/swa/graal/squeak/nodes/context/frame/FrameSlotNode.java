package de.hpi.swa.graal.squeak.nodes.context.frame;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;

public abstract class FrameSlotNode extends Node {
    @CompilationFinal protected final FrameSlot slot;

    protected FrameSlotNode(final FrameSlot frameSlot) {
        slot = frameSlot;
    }
}