package pt.ist.fenixframework.dml.runtime;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class RelationAwareSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends AbstractSet<E2> implements Set<E2>,RelationBaseSet<E2> {
    protected DomainBasedMap<E2> internalMap;
    protected KeyFunction<? extends Comparable<?>, E2> mapKey;
    protected E1 owner;
    protected Relation<E1,E2> relation;

    public RelationAwareSet(E1 owner, Relation<E1,E2> relation, DomainBasedMap<E2> internalMap, KeyFunction<? extends Comparable<?>, E2> mapKey) {
        this.owner = owner;
        this.relation = relation;
        this.internalMap = internalMap;
        this.mapKey = mapKey;
    }

    public boolean justAdd(E2 elem) {
        return internalMap.putIfMissing(mapKey.getKey(elem), elem);
    }

    public boolean justRemove(E2 elem) {
        return internalMap.remove(mapKey.getKey(elem));
    }

    public int size() {
        return internalMap.size();
    }

    public E2 get(Comparable<?> key) {
	return internalMap.get(key);
    }
    
    public E2 getCached(Comparable<?> key, boolean forceMiss) {
	return internalMap.getCached(forceMiss, key);
    }
    
    public boolean contains(Object o) {
	if (o instanceof AbstractDomainObject) {
	    return internalMap.contains(mapKey.getKey((E2)o));
	} else {
	    return false;
	}
    }

    @Override
    public Iterator<E2> iterator() {
        return new RelationAwareIterator(this.internalMap);
    }

    @Override
    public boolean add(E2 o) {
	return relation.add(owner, o);
    }

    @Override
    public boolean remove(Object o) {
	if (o instanceof AbstractDomainObject) {
	    return relation.remove(owner, (E2)o);
	} else {
	    return false;
	}
    }

    protected class RelationAwareIterator implements Iterator<E2> {
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;

        RelationAwareIterator(DomainBasedMap<E2> internalMap) {
            this.iterator = internalMap.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E2 next() {
            E2 result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }

        @Override
        public void remove() {
            if (! canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                relation.remove(owner, current);
            }
        }
    }
}
