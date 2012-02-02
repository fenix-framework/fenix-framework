package dml.runtime;

public interface FenixVBox<E> {
    public E get(Object obj, String attrName);
    public void put(Object obj, String attrName, E newValue);
}
