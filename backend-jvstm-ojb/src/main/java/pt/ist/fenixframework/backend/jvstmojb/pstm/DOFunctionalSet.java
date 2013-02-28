package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;

import jvstm.util.Cons;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.dml.runtime.FunctionalSet;

public class DOFunctionalSet<E extends AbstractDomainObject> extends FunctionalSet<E> implements Serializable {
    public static final DOFunctionalSet EMPTY = new DOFunctionalSet(0, Cons.empty());

    private DOFunctionalSet(int size, Cons<E> elems) {
        super(size, elems);
    }

    @Override
    protected DOFunctionalSet<E> makeFunctionalSet(int size, Cons<E> elems) {
        return new DOFunctionalSet<E>(size, elems);
    }

    // serialization code

    protected Object writeReplace() throws ObjectStreamException {
        SerializedForm s = new SerializedForm(this.size);
        Iterator<E> it = this.iterator();
        while (it.hasNext()) {
            s.add(it.next().getOid());
        }

        return s;
    }

    private static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 1L;

        private long[] oids;
        private transient int pos;

        SerializedForm(int size) {
            this.oids = new long[size];
            pos = 0;
        }

        void add(long oid) {
            oids[pos++] = oid;
        }

        Object readResolve() throws ObjectStreamException {
            FunctionalSet<DomainObject> set = DOFunctionalSet.EMPTY;
            for (int i = oids.length - 1; i >= 0; i--) { // this respects the original order, for whatever that is worth
                set = set.addUnique(FenixFramework.getConfig().getBackEnd().fromOid(oids[i]));
            }
            return set;
        }
    }
}
