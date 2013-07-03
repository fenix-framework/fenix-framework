package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.List;
import java.util.ListIterator;

import jvstm.Transaction;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.FenixVBox;

public abstract class VBox<E> extends jvstm.VBox<E> implements VersionedSubject, FenixVBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(VBox.class);

    static final Object NOT_LOADED_VALUE = new Object();
    public static final VBoxBody NOT_LOADED_BODY = new VBoxBody(notLoadedValue(), 0, null);

    public static <T> T notLoadedValue() {
        return (T) NOT_LOADED_VALUE;
    }

    protected VBox() {
        super();
    }

    protected VBox(E initial) {
        super(initial);
    }

    protected VBox(VBoxBody<E> body) {
        super(body);
    }

    /** Return this VBox's identifier */
    public abstract String getId();

    @Override
    public E get() {
        return ((JvstmInFenixTransaction) Transaction.current()).getBoxValue(this);
    }

    public void putNotLoadedValue() {
        this.put(VBox.<E> notLoadedValue());
    }

    @Override
    public synchronized VBoxBody addNewVersion(int txNumber) {
        if (body.version < txNumber) {
            return commit(VBox.<E> notLoadedValue(), txNumber);
        } else {
            /* when adding a version to the vbox it may happen that such version
            already exists.  That can happen if the box gets reloaded before the
            remote commit is applied, but after such remote commit data is already
            persisted.  In that case, just return null, because we are not
            responsible for what will happen to that vboxbody */
            logger.debug("tried to add an older version for a box: id=" + getId() + " -> " + body.version + " is not < "
                    + txNumber);
            return null;
        }
    }

    @Override
    public Object getCurrentValue() {
        return this.get();
    }

    public VBoxBody<E> getOldestLoadedBody() {
        if (this.body == VBox.NOT_LOADED_BODY || this.body == null) {
            return null;
        } else {
            VBoxBody<E> oldest = this.body;
            while ((oldest.next != VBox.NOT_LOADED_BODY) && (oldest.next != null)) {
                oldest = oldest.next;
            }
            return oldest;
        }
    }

    // synchronized here processes reloads of the same box one at a time, thus avoiding concurrent accesses to the persistence to
    // load the same box
    /* removed synchronized.

    It is not strictly necessary.  It only existed to prevent two reload
    requests regarding the same vbox to hit the repository.  However, the
    actual required synchronization is performed in the mergeVersions method.

    Now that VBox is used also in the lock-free implementation, the extraneous
    synchronized was removed so that it does not prevent the lock-free
    behavior.
     */
    /*synchronized */boolean reload() {
        return reload(Transaction.current().getNumber());
    }

    /* Reloads this box ensuring that it can provide at least information about 'requiredVersion'. */
    boolean reload(int requiredVersion) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reload VBox: {}", this.getId());
        }

        try {
            //Re-test to see whether some other thread already did the job for us.
            //This also requires the body's value slot to be final.

            //VBoxBody<E> body = this.body.getBody(Transaction.current().getNumber());
            VBoxBody<E> body = getBody(requiredVersion);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                doReload();
            }
            return true;
        } catch (Throwable e) {
            // what to do?
            logger.warn("Couldn't reload vbox '" + getId() + "': " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    public final VBoxBody<E> getBody(int maxVersion) {
        VBoxBody<E> current = body;

        while (current != null && current.version > maxVersion) {
            current = current.next;
        }
        if (current == null) {
            return NOT_LOADED_BODY; // VBox.NOT_LOADED_VALUE;
        }

        return current;
    }

    protected abstract void doReload();

    // merge the versions kept in this vbox with those stored in vvalues. There might
    // be duplicates.
    // synchronized because we need to ensure that the list of bodies does no change during merge
    synchronized public final void mergeVersions(List<VersionedValue> vvalues) {
        mergeBodiesIntoLoadedVersions(this.body, vvalues, 0);
        this.body = convertVersionedValuesToVBoxBodies(vvalues);
    }

    // this code picks from either the bodies or the Versioned values and inserts them in the correct position in the list of versioned values.
    protected void mergeBodiesIntoLoadedVersions(VBoxBody<E> boxBody, List<VersionedValue> vvalues, int pos) {
        if (boxBody != null && pos < vvalues.size()) {
            // must decide whether to pick the vboxbody or the vvalue
            if (boxBody.version > vvalues.get(pos).getVersion()) {
                // pick the vboxbody
                vvalues.add(pos, new VersionedValue(boxBody.value, boxBody.version));
                mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
            } else if (boxBody.version < vvalues.get(pos).getVersion()) {
                // pick the vvalue 
                mergeBodiesIntoLoadedVersions(boxBody, vvalues, pos + 1);
            } else {
                // both are the same version. advance on both lists
                mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
            }
        } else if (boxBody == null) { // all bodies have been put in vvalues
            return;  // we're done
        } else {
            // only bodies to append to the vvalues
            vvalues.add(new VersionedValue(boxBody.value, boxBody.version));
            mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
        }
    }

    private VBoxBody<E> convertVersionedValuesToVBoxBodies(List<VersionedValue> vvalues) {
        ListIterator<VersionedValue> iter = vvalues.listIterator(vvalues.size());

        VBoxBody<E> result = null;
        while (iter.hasPrevious()) {
            VersionedValue vvalue = iter.previous();
            result = VBox.makeNewBody((E) vvalue.getValue(), vvalue.getVersion(), result);
        }
        return result;
    }

}
