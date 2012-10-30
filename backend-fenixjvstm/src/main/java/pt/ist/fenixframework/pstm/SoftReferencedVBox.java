package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

import pt.ist.fenixframework.DomainObject;

// This version of the VBox exists only because the special needs of
// a RelationList which holds a SoftReference to its VBox.
// For further explanations see the comment on the class SpecialBody at the end of this file
class SoftReferencedVBox<E> extends ReferenceBox<E> {
    public SoftReferencedVBox(DomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    public SoftReferencedVBox(DomainObject ownerObj, String slotName, E initial) {
	super(ownerObj, slotName, initial);
    }

    protected SoftReferencedVBox(DomainObject ownerObj, String slotName, VBoxBody<E> body) {
	super(ownerObj, slotName, body);
    }

    @Override
    public VBoxBody commit(E newValue, int txNumber) {
        // see comment on the class SpecialBody at the end of this file
        VBoxBody<E> newBody = new SpecialBody(newValue, txNumber, this.body, this);
	this.body = newBody;
        return newBody;
    }

    public static <T> VBox<T> makeNew(DomainObject ownerObj, String slotName, boolean allocateOnly) {
        if (allocateOnly) {
            // when a box is allocated, it is safe 
            // to say that the version number is 0
            return new SoftReferencedVBox<T>(ownerObj, slotName, makeNewBody((T)NOT_LOADED_VALUE, 0, null));
        } else {
            return new SoftReferencedVBox<T>(ownerObj, slotName);
        }
    }

    /*
     * This SpecialBody class is a hack that will eventually disappear.
     * It is a simple extension of a MultiVersionBoxBody so that it holds a 
     * strong reference to the box which is owning it.  Instances of this class
     * are created only during the processing of an AlientTransaction
     * (see TransactionChangeLogs) when a new version is added to a RelationList.
     * Because RelationLists use a SoftReference to keep the VBox (so that the 
     * bi-directional relations do not prevent the GC from working), it could happen
     * that the VBox of a RelationList got GCed after the processing of an AlientTransaction 
     * but before older running transactions finished.  If meanwhile a more recent transaction
     * accessed the RelationList, it would load its value and associate it with version 0, which 
     * is wrong.  So, until the AlientTransaction gets cleaned up (see the 
     * TransactionChangeLogs.cleanOldAlienTxs method), we must prevent that the VBox be GCed.
     * This class ensures it, because the AlientTransaction keeps strong a reference to an instance of 
     * this class which has also a strong reference to the VBox.
     * Finally, when the cleanOldAlienTxs method runs and calls the freeResources on the AlienTransaction,
     * it calls the clearPrevious method of each body, which, in this case, also removes the reference to
     * the VBox.
     *
     * It's a *little* bit confusing, I know, but...
     */
    private static class SpecialBody<E> extends VBoxBody<E> {
        private VBox owner;

        SpecialBody(E value, int version, VBoxBody<E> next, VBox owner) {
            super(value, version, next);
            this.owner = owner;
        }

        public void clearPrevious() {
            super.clearPrevious();

            // loose the reference to the owner so that it may be GCed, if needed
            owner = null;
        }
    }
}
