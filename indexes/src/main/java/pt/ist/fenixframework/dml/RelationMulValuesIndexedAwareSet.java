package pt.ist.fenixframework.dml;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import pt.ist.fenixframework.adt.linkedlist.LinkedList;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.runtime.DomainBasedMap;
import pt.ist.fenixframework.dml.runtime.KeyFunction;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationAwareSet;
import pt.ist.fenixframework.indexes.UnmodifiableDomainBaseSet;

public class RelationMulValuesIndexedAwareSet<E1 extends AbstractDomainObject, E2 extends AbstractDomainObject> extends
        RelationAwareSet<E1, E2> {

    // All accesses to the internalMap should go through the getInternalMap() method!
    private DomainBasedMap<LinkedList<E2>> multiValueMap;
    protected final DomainBasedMap.Getter<LinkedList<E2>> multiValueMapGetter;

    public RelationMulValuesIndexedAwareSet(E1 owner, Relation<E1, E2> relation, KeyFunction<? extends Comparable<?>, E2> mapKey,
            DomainBasedMap<LinkedList<E2>> multiValueMap, DomainBasedMap.Getter<LinkedList<E2>> multiValueMapGetter) {
        super(owner, relation, (DomainBasedMap<E2>) null, mapKey);
        this.multiValueMap = multiValueMap;
        this.multiValueMapGetter = multiValueMapGetter;
    }

    public RelationMulValuesIndexedAwareSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap<LinkedList<E2>> multiValueMap,
            KeyFunction<? extends Comparable<?>, E2> mapKey) {
        this(owner, relation, mapKey, multiValueMap, null);
    }

    public RelationMulValuesIndexedAwareSet(E1 owner, Relation<E1, E2> relation,
            DomainBasedMap.Getter<LinkedList<E2>> multiValueMapGetter, KeyFunction<? extends Comparable<?>, E2> mapKey) {
        this(owner, relation, mapKey, null, multiValueMapGetter);
    }

    /**
     * Provide access to the multiValuelMap. This method should be used by any other method that needs to access the
     * multiValueMap.
     * 
     * @return The reference to the map to use
     */
    // This method replicates behavior equivalent to that of the getInternalMap() in the super class.  Please see comments there.  
    protected DomainBasedMap<LinkedList<E2>> getMultiValueMap() {
        DomainBasedMap<LinkedList<E2>> localRef = multiValueMap;
        if (localRef == null) {
            localRef = reloadMultiValueMap();
            // here we assume that reloadMultiValueMap will always return the same instance, so at most we're just setting the
            // same thing repeatedly.
            multiValueMap = localRef;
        }
        return localRef;
    }

    // This method replicates behavior equivalent to that of the reloadInternalMap() in the super class.  Please see comments there.  
    private DomainBasedMap<LinkedList<E2>> reloadMultiValueMap() {
        return multiValueMapGetter.get();
    }

    @Override
    public boolean justAdd(E2 elem) {
        DomainBasedMap<E2> subMap = checkIfExists(mapKey.getKey(elem));
        return subMap.putIfMissing(elem.getOid(), elem);
    }

    @Override
    public boolean justRemove(E2 elem) {
        DomainBasedMap<E2> subMap = checkIfExists(mapKey.getKey(elem));
        return subMap.remove(elem.getOid());
    }

    protected DomainBasedMap<E2> checkIfExists(Comparable<?> key) {
        LinkedList<E2> subMap = getMultiValueMap().get(key);
        if (subMap == null) {
            // Note that this Collection is attached here, we can make it dynamic, but is it worth it?
            subMap = new LinkedList<E2>();
            getMultiValueMap().put(key, subMap);
        }
        return subMap;
    }

    @Override
    public E2 get(Comparable<?> key) {
        throw new UnsupportedOperationException();
    }

    public Set<E2> getValues(Comparable<?> key) {
        return new UnmodifiableDomainBaseSet<E2>(getMultiValueMap().get(key));
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof AbstractDomainObject) {
            E2 obj = (E2) o;
            DomainBasedMap<E2> subMap = checkIfExists(mapKey.getKey(obj));
            return subMap.contains(obj.getOid());
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        int sum = 0;
        for (DomainBasedMap<E2> subMap : getMultiValueMap()) {
            sum += subMap.size();
        }
        return sum;
    }

    @Override
    public Iterator<E2> iterator() {
        return new RelationMulValuesIndexedAwareIterator(getMultiValueMap());
    }

    protected class RelationMulValuesIndexedAwareIterator implements Iterator<E2> {
        private final Iterator<LinkedList<E2>> keyIterator;
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;

        RelationMulValuesIndexedAwareIterator(DomainBasedMap<LinkedList<E2>> multiValueMap) {
            this.keyIterator = multiValueMap.iterator();
            this.iterator = new EmptyIterator();
        }

        @Override
        public boolean hasNext() {
            while (!iterator.hasNext()) {
                if (!keyIterator.hasNext()) {
                    return false;
                }
                iterator = keyIterator.next().iterator();
            }
            return true;
        }

        @Override
        public E2 next() {
            while (!iterator.hasNext()) {
                if (!keyIterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                iterator = keyIterator.next().iterator();
            }
            E2 result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                relation.remove(owner, current);
            }
        }

        private final class EmptyIterator implements Iterator<E2> {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E2 next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

}
