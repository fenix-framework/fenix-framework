package pt.ist.fenixframework.backend.infinispan;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import pt.ist.fenixframework.adt.linkedlist.LinkedList;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.RelationMulValuesIndexedAwareSet;
import pt.ist.fenixframework.dml.runtime.DomainBasedMap;
import pt.ist.fenixframework.dml.runtime.KeyFunction;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.indexes.UnmodifiableDomainBaseSet;

public class RelationMulValuesIndexedCacheableAwareSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends RelationMulValuesIndexedAwareSet<E1, E2>  {

    public RelationMulValuesIndexedCacheableAwareSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap<LinkedList<E2>> multiValueMap, KeyFunction<? extends Comparable<?>, E2> mapKey) {
	super(owner, relation, multiValueMap, mapKey);
    }
    
    public Set<E2> getValues(Comparable<?> key) {
	return new UnmodifiableDomainBaseSet<E2>(multiValueMap.getCached(false, key));
    }
    
    public Set<E2> getValuesCached(boolean forceMiss, Comparable<?> key) {
	return new UnmodifiableDomainBaseSet<E2>(multiValueMap.getCached(forceMiss, key));
    }
    
    @Override
    public Iterator<E2> iterator() {
	return iteratorCached(false);
    }
    
    protected class RelationMulValuesIndexedCacheableAwareIterator implements Iterator<E2> {
	private Iterator<LinkedList<E2>> keyIterator;
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;
        private boolean forceMiss;

        RelationMulValuesIndexedCacheableAwareIterator(boolean forceMiss, DomainBasedMap<LinkedList<E2>> multiValueMap) {
            this.forceMiss = forceMiss;
            this.keyIterator = multiValueMap.iteratorCached(forceMiss);
            this.iterator = new EmptyIterator();
        }

        @Override
        public boolean hasNext() {
            while (!iterator.hasNext()) {
        	if (!keyIterator.hasNext()) {
        	    return false;
        	}
        	iterator = keyIterator.next().iteratorCached(forceMiss);
            }
            return true;
        }

        @Override
        public E2 next() {
            while (!iterator.hasNext()) {
        	if (!keyIterator.hasNext()) {
        	    throw new NoSuchElementException();
        	}
        	iterator = keyIterator.next().iteratorCached(forceMiss);
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
    
    public Iterator<E2> iteratorCached(boolean forceMiss) {
	return new RelationMulValuesIndexedCacheableAwareIterator(forceMiss, this.multiValueMap);
    }
    
}
