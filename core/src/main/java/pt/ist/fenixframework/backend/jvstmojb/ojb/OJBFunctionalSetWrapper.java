package pt.ist.fenixframework.backend.jvstmojb.ojb;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.ojb.broker.ManageableCollection;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;

import pt.ist.fenixframework.backend.jvstmojb.dml.runtime.FunctionalSet;

public class OJBFunctionalSetWrapper implements ManageableCollection {
    private static final Iterator EMPTY_ITER = new Iterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private FunctionalSet elements = FunctionalSet.EMPTY;

    public OJBFunctionalSetWrapper() {
    }

    public FunctionalSet getElements() {
        return elements;
    }

    @Override
    public void ojbAdd(Object anObject) {
        elements = elements.addUnique(anObject);
    }

    @Override
    public void ojbAddAll(ManageableCollection otherCollection) {
        Iterator iter = ((OJBFunctionalSetWrapper) otherCollection).getElements().iterator();
        while (iter.hasNext()) {
            ojbAdd(iter.next());
        }
    }

    @Override
    public Iterator ojbIterator() {
        return EMPTY_ITER;
    }

    @Override
    public void afterStore(PersistenceBroker broker) throws PersistenceBrokerException {
        // empty
    }
}
