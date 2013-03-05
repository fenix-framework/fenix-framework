package pt.ist.fenixframework.dml.runtime;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.core.AbstractDomainObject;

public class RelationAwareSet<E1 extends AbstractDomainObject, E2 extends AbstractDomainObject> extends AbstractSet<E2> implements
        Set<E2>, RelationBaseSet<E2> {

    private static final Logger logger = LoggerFactory.getLogger(RelationAwareSet.class);

    // All accesses to the internalMap should go through the getInternalMap() method!
    private DomainBasedMap<E2> internalMap;
    protected final DomainBasedMap.Getter<E2> internalMapGetter;
    protected final KeyFunction<? extends Comparable<?>, E2> mapKey;
    protected final E1 owner;
    protected final Relation<E1, E2> relation;

    /**
     * Common constructor for the RelationAwareSet.
     * 
     * @param owner The {@link DomainObject} that owns this collection
     * @param relation The relation of which this set is a part of
     * @param mapKey The function that returns the key used to index this collection.
     * @param internalMap The backing collection for this side of the relation. If this is <code>null</code>, then the
     *            internalMapGetter will be used when the collection is needed.
     * @param internalMapGetter The function that returns the internalMap when it is needed. Can be <code>null</code> if it is
     *            never needed.
     */
    protected RelationAwareSet(E1 owner, Relation<E1, E2> relation, KeyFunction<? extends Comparable<?>, E2> mapKey,
            DomainBasedMap<E2> internalMap, DomainBasedMap.Getter<E2> internalMapGetter) {
        this.owner = owner;
        this.relation = relation;
        this.mapKey = mapKey;
        this.internalMap = internalMap;
        this.internalMapGetter = internalMapGetter;
    }

    /**
     * Standard creation of a RelationAwareSet when the internalMap instance is known.
     * 
     * @param owner The {@link DomainObject} that owns this collection
     * @param relation The relation of which this set is a part of
     * @param internalMap The backing collection for this side of the relation
     * @param mapKey The function that returns the key used to index this collection
     */
    public RelationAwareSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap<E2> internalMap,
            KeyFunction<? extends Comparable<?>, E2> mapKey) {
        this(owner, relation, mapKey, internalMap, null);
    }

    /**
     * Standard creation of a RelationAwareSet when the internalMap instance is unknown. When using this constructor the
     * internalMapGetter cannot be <code>null</code>.
     * 
     * @param owner The {@link DomainObject} that owns this collection
     * @param relation The relation of which this set is a part of
     * @param internalMapGetter The function that returns the internalMap when it is needed
     * @param mapKey The function that returns the key used to index this collection
     */
    public RelationAwareSet(E1 owner, Relation<E1, E2> relation, DomainBasedMap.Getter<E2> internalMapGetter,
            KeyFunction<? extends Comparable<?>, E2> mapKey) {
        this(owner, relation, mapKey, null, internalMapGetter);
        if (internalMapGetter == null) {
            String errorMsg = "internalMapGetter cannot be null when no internalMap is provided";
            logger.error(errorMsg);
            throw new NullPointerException(errorMsg);
        }
    }

    /**
     * Provide access to the internalMap. This method should be used by any other method that needs to access the internalMap.
     * 
     * @return The reference to the internal map to use
     */
    protected DomainBasedMap<E2> getInternalMap() {
        // the only time this can be null is when this RelationAwareSet belongs to an object that was materialized via the
        // DomainObjectAllocator.  In such case, we ask the object for the required collection.  Concurrent programming best
        // practices state that using the local variable is the correct way to do this, due to possible instruction reordering.
        DomainBasedMap<E2> localRef = internalMap;
        if (localRef == null) {
            localRef = reloadInternalMap();
            // here we assume that reloadInternalMap will always return the same instance, so at most we're just setting the
            // same thing repeatedly.
            internalMap = localRef;
        }
        return localRef;
    }

    /**
     * Ask the owner object to give us the collection.
     * 
     * @return The reference to the collection that holds the objects in this relation.
     */
    // This method can be synchronized to avoid multiple requests trying to reload the same map into memory, in which case it
    // should compare internalMap == null before hand.  Nevertheless, by not using synchronized we are allowing for a lock-free
    // algorithm, even though at the hypothetical cost of making additional request to the underlying storage system.
    private DomainBasedMap<E2> reloadInternalMap() {
        return internalMapGetter.get();
    }

    @Override
    public boolean justAdd(E2 elem) {
        return getInternalMap().putIfMissing(mapKey.getKey(elem), elem);
    }

    @Override
    public boolean justRemove(E2 elem) {
        return getInternalMap().remove(mapKey.getKey(elem));
    }

    @Override
    public int size() {
        return getInternalMap().size();
    }

    public E2 get(Comparable<?> key) {
        return getInternalMap().get(key);
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof AbstractDomainObject) {
            return getInternalMap().contains(mapKey.getKey((E2) o));
        } else {
            return false;
        }
    }

    @Override
    public Iterator<E2> iterator() {
        return new RelationAwareIterator(getInternalMap());
    }

    @Override
    public boolean add(E2 o) {
        return relation.add(owner, o);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof AbstractDomainObject) {
            return relation.remove(owner, (E2) o);
        } else {
            return false;
        }
    }

    protected class RelationAwareIterator implements Iterator<E2> {
        private final Iterator<E2> iterator;
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
            if (!canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                relation.remove(owner, current);
            }
        }
    }
}
