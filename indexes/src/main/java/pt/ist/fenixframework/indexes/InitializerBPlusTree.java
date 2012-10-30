package pt.ist.fenixframework.indexes;

import java.io.Serializable;
import java.lang.reflect.Field;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.adt.bplustree.BPlusTree;
import pt.ist.fenixframework.adt.bplustree.IBPlusTree;

public class InitializerBPlusTree<T extends Serializable> implements IBPlusTree<T>{

    private final String initializationKey;
    private final Field fieldToInitialize;

    public InitializerBPlusTree(String key, Class<?> domainClass, String slotName) {
	this.initializationKey = key;
	Field f = null;
	try {
	    f = domainClass.getDeclaredField(slotName);
	} catch (SecurityException e) {
	    e.printStackTrace();
	} catch (NoSuchFieldException e) {
	    e.printStackTrace();
	}
	this.fieldToInitialize = f;
    }

    @Override
    public T get(Comparable key) {
	return initialize().get(key);
    }

    @Override
    public void insert(Comparable key, T value) {
	initialize().insert(key, value);
    }

    @Override
    public void remove(Comparable key) {
	initialize().remove(key);
    }

    private BPlusTree<T> initialize() {
	BPlusTree<T> tree = (BPlusTree<T>)FenixFramework.getDomainRoot().getIndexRoot().get(initializationKey);
	try {
	    fieldToInitialize.set(null, tree);
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}
	return tree;
    }
}
