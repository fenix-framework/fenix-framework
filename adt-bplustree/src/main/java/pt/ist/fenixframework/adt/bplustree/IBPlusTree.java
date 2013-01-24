package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;

/** Basic functionality of a BPlusTree */
public interface IBPlusTree<T extends Serializable> {

    public T get(Comparable key);
    
    public void insertKeyValue(Comparable key, T value);
    
    public boolean remove(Comparable key);
    
}
