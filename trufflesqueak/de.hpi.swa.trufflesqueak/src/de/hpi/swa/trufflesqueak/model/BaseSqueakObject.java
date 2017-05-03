package de.hpi.swa.trufflesqueak.model;

import de.hpi.swa.trufflesqueak.SqueakImageContext;
import de.hpi.swa.trufflesqueak.exceptions.UnwrappingError;
import de.hpi.swa.trufflesqueak.util.Chunk;

public abstract class BaseSqueakObject {
    public final SqueakImageContext image;

    public BaseSqueakObject(SqueakImageContext img) {
        image = img;
    }

    public abstract void fillin(Chunk chunk);

    @Override
    public String toString() {
        return "a " + getSqClassName();
    }

    public abstract BaseSqueakObject getSqClass();

    public void setSqClass(BaseSqueakObject newCls) {
        throw new RuntimeException("cannot do this");
    }

    public boolean isClass() {
        return false;
    }

    public String nameAsClass() {
        return "???NotAClass";
    }

    public String getSqClassName() {
        if (isClass()) {
            return nameAsClass() + " class";
        } else {
            return getSqClass().nameAsClass();
        }
    }

    /**
     * @param other The object to swap identities with
     */
    public boolean become(BaseSqueakObject other) {
        return false;
    }

    public abstract BaseSqueakObject at0(int idx);

    public abstract void atput0(int idx, BaseSqueakObject object) throws UnwrappingError;

    public abstract int size();

    public abstract int instsize();

    public int varsize() {
        return size() - instsize();
    }

    public int unwrapInt() throws UnwrappingError {
        throw new UnwrappingError();
    }

    public int unsafeUnwrapInt() {
        try {
            return unwrapInt();
        } catch (UnwrappingError e) {
            throw new RuntimeException(e);
        }
    }
}
