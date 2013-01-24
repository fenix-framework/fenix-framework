package pt.ist.fenixframework.dml.runtime;

import java.io.Serializable;
import java.util.Iterator;

public interface DomainBasedSet<T extends Serializable> {

    public boolean add(Comparable key, T e);
    
    public boolean remove(Comparable key);
    
    public boolean contains(Comparable key);
 
    public int size();
    
    public Iterator<T> iterator();
}
