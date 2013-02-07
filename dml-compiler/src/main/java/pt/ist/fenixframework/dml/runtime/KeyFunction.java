package pt.ist.fenixframework.dml.runtime;

import java.io.Serializable;

/** 
 * For a given Serializable V retrieves the Comparable K which is used as 
 * the key for V in the indexing collection.
 */
public interface KeyFunction<K extends Comparable<?>,V extends Serializable> {

	public K getKey(V obj);
	
	/**
	 * Returns true if getKey function can return the same value for different instances of V.
	 */
	public boolean allowMultipleKeys();

}