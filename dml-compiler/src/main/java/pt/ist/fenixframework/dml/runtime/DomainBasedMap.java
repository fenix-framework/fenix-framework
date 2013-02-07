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
    
    public Iterator<T> iterator();
}
