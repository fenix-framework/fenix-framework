package pt.ist.fenixframework.dml.runtime;

import java.io.Serializable;
import java.util.Iterator;

import pt.ist.fenixframework.DomainObject;

public interface DomainBasedMap<T extends Serializable> extends DomainObject, Iterable<T> {

    public T get(Comparable key);

    public boolean putIfMissing(Comparable key, T value);

    public void put(Comparable key, T value);

    public boolean remove(Comparable key);

    public boolean contains(Comparable key);

    public int size();

    @Override
    public Iterator<T> iterator();

    /**
     * This interface provides to the {@link RelationAwareSet} a means to ask for a instance of its internal DomainBasedMap,
     * when it is not yet loaded. An implementation of this interface should be provided (by the code generator) when creating
     * instances of RelationAwareSet and NOT providing the internalMap.
     */
    public interface Getter<Y extends Serializable> {
        public DomainBasedMap<Y> get();
    }

}
