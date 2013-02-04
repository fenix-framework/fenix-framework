package pt.ist.fenixframework.dml.runtime;

import java.io.Serializable;
import java.util.Iterator;

public interface DomainBasedMap<T extends Serializable> {

    public T get(Comparable key);
    
    public void put(Comparable key, T value);
    
    public boolean remove(Comparable key);
    
    public boolean contains(Comparable key);
 
    public int size();
    
    public Iterator<T> iterator();
}
