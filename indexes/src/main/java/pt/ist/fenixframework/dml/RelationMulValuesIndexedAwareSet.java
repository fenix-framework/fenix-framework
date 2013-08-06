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

public class RelationMulValuesIndexedAwareSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends RelationAwareSet<E1, E2> {

    protected DomainBasedMap<LinkedList<E2>> multiValueMap;

    public RelationMulValuesIndexedAwareSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap<LinkedList<E2>> multiValueMap, KeyFunction<? extends Comparable<?>, E2> mapKey) {
	super(owner, relation, null, mapKey);
	this.multiValueMap = multiValueMap;
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
	LinkedList<E2> subMap = multiValueMap.get(key);
	if (subMap == null) {
	    // Note that this Collection is attached here, we can make it dynamic, but is it worth it?
	    subMap = new LinkedList<E2>();
	    multiValueMap.put(key, subMap);
	}
	return subMap;
    }

    @Override
    public E2 get(Comparable<?> key) {
 	throw new UnsupportedOperationException();
     }
    
    public Set<E2> getValues(Comparable<?> key) {
	return new UnmodifiableDomainBaseSet<E2>(multiValueMap.get(key));
    }
    
    public Set<E2> getValuesCached(boolean forceMiss, Comparable<?> key) {
	return new UnmodifiableDomainBaseSet<E2>(multiValueMap.getCached(forceMiss, key));
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
	for (DomainBasedMap<E2> subMap : multiValueMap) {
	    sum += subMap.size();
	}
	return sum;
    }

    @Override
    public Iterator<E2> iterator() {
	return new RelationMulValuesIndexedAwareIterator(this.multiValueMap);
    }
    
    protected class RelationMulValuesIndexedAwareIterator implements Iterator<E2> {
	private Iterator<LinkedList<E2>> keyIterator;
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
            if (! canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                relation.remove(owner, current);
            }
        }
        
	private final class EmptyIterator implements Iterator<E2> {
	    public boolean hasNext() {
	        return false;
	    }

	    public E2 next() {
	        throw new NoSuchElementException();
	    }

	    public void remove() {
	        throw new UnsupportedOperationException();
	    }
	}
    }

}
