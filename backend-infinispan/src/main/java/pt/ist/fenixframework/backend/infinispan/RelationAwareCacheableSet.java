package pt.ist.fenixframework.backend.infinispan;

import java.util.Iterator;

import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.runtime.DomainBasedMap;
import pt.ist.fenixframework.dml.runtime.KeyFunction;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationAwareSet;

public class RelationAwareCacheableSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends RelationAwareSet<E1, E2> {

    public RelationAwareCacheableSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap<E2> internalMap, KeyFunction<? extends Comparable<?>, E2> mapKey) {
	super(owner, relation, internalMap, mapKey);
    }

    public E2 get(Comparable<?> key) {
	// Here we cannot accept an input from the programmer with regard to the 
	// forced miss on the cache (to update the object) because the RelationAwareSet
	// is exposed as a normal JDK AbstractSet
	return internalMap.getCached(false, key);
    }
    
    public E2 getCached(boolean forceMiss, Comparable<?> key) {
	return internalMap.getCached(forceMiss, key);
    }
    
    @Override
    public Iterator<E2> iterator() {
        return iteratorCached(false);
    }
    
    public Iterator<E2> iteratorCached(boolean forceMiss) {
        return new RelationCacheableAwareIterator(forceMiss, this.internalMap);
    }
    
    protected class RelationCacheableAwareIterator implements Iterator<E2> {
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;
        private boolean forceMiss;

        RelationCacheableAwareIterator(boolean forceMiss, DomainBasedMap<E2> internalMap) {
            this.forceMiss = forceMiss;
            this.iterator = internalMap.iteratorCached(this.forceMiss);
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
