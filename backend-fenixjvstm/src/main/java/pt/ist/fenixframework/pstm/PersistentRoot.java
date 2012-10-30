package pt.ist.fenixframework.pstm;

import jvstm.TransactionalCommand;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DomainObject;

public class PersistentRoot extends PersistentRoot_Base {

    private static final String initialRootKey = "pt.ist.fenixframework.root";

    private PersistentRoot() {
	super();
    }

    private void setRoot(String key, AbstractDomainObject obj) {
	setRootKey(key);
	setRootObject(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DomainObject> T getRoot(String key) {
	PersistentRoot root = getFirstInChain();
	while (root != null) {
	    if (root.getRootKey().equals(key)) {
		return (T) root.getRootObject();
	    }
	    root = root.hasNext() ? root.getNext() : null;
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends DomainObject> T getRoot() {
	return (T) getRoot(initialRootKey);
    }

    public static void addRoot(String key, AbstractDomainObject domainObject) {
	if (key == null) {
	    throw new IllegalArgumentException("argument key cannot be null");
	}
	if (getRoot(key) == null) {
	    PersistentRoot endOfChain = getFirstInChain().goToEndOfChain();
	    PersistentRoot newRoot = new PersistentRoot();
	    newRoot.setRoot(key, domainObject);
	    endOfChain.setNext(newRoot);
	}
    }

    private PersistentRoot goToEndOfChain() {
	return hasNext() ? getNext().goToEndOfChain() : this;
    }

    public static void initRootIfNeeded(final Config config) {
	if ((config != null) && (config.getRootClass() != null)) {
	    Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    PersistentRoot persRoot = getFirstInChain();
		    if (persRoot.getRootObject() == null) {
			try {
			    persRoot.setRoot(initialRootKey, (AbstractDomainObject) config.getRootClass().newInstance());
			} catch (Exception exc) {
			    throw new Error(exc);
			}
		    }
		}
	    });
	}
    }

    private static PersistentRoot getFirstInChain() {
	return AbstractDomainObject.fromOID(1L);
    }

}
