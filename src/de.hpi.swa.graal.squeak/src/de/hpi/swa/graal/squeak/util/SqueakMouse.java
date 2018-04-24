package de.hpi.swa.graal.squeak.util;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import de.hpi.swa.graal.squeak.exceptions.SqueakException;
import de.hpi.swa.graal.squeak.util.SqueakDisplay.EVENT_TYPE;
import de.hpi.swa.graal.squeak.util.SqueakDisplay.JavaDisplay;

public final class SqueakMouse extends MouseInputAdapter {
    @CompilationFinal private final JavaDisplay display;
    private Point position = new Point(0, 0);
    private int buttons = 0;

    private static final int RED = 4;
    private static final int YELLOW = 2;
    private static final int BLUE = 1;

    public SqueakMouse(final JavaDisplay display) {
        this.display = display;
    }

    public Point getPosition() {
        return position;
    }

    public int getButtons() {
        return buttons;
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        position = e.getPoint();
        addEvent(e);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        position = e.getPoint();
        addEvent(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        buttons |= mapButton(e);
        addEvent(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        buttons &= ~mapButton(e);
        addEvent(e);
    }

    private void addEvent(final MouseEvent e) {
        display.addEvent(new long[]{EVENT_TYPE.MOUSE, display.getEventTime(), e.getX(), e.getY(), buttons, display.keyboard.modifierKeys(), 0, 0});
    }

    private static int mapButton(final MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if (e.isControlDown()) {
                    return YELLOW;
                }
                if (e.isAltDown()) {
                    return BLUE;
                }
                return RED;
            case MouseEvent.BUTTON2:
                return BLUE;    // middle (frame menu)
            case MouseEvent.BUTTON3:
                return YELLOW;  // right (pane menu)
            case MouseEvent.NOBUTTON:
                return 0;
            default:
                throw new SqueakException("Unknown mouse button in event");
        }
    }
}
