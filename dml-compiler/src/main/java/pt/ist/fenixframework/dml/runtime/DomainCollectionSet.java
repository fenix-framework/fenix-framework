package pt.ist.fenixframework.dml.runtime;

import java.util.Iterator;

public interface DomainCollectionSet<E> {

    public void add(E e);
    
    public void remove(E e);
    
    public int size();
    
    public boolean contains(Object o);

    public Iterator<E> iterator();
    
}
