package pt.ist.fenixframework.indexes;

import java.io.Serializable;

import pt.ist.fenixframework.DomainObject;

/** 
 * For a given Serializable V retrieves the Comparable K which is used as 
 * the key for V in the indexing collection.
 * 
 * @author sfbs
 *
 * @param <K>
 * @param <V>
 */
public abstract class KeyFunction<K extends Comparable<?>,V extends Serializable> {

	public abstract K getKey(V obj);
	
	/**
	 * Returns true if getKey function can return the same value for different instances of V.
	 */
	public abstract boolean allowMultipleKeys();

}