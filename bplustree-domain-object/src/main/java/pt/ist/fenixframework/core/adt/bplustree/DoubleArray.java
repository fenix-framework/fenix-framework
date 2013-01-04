package pt.ist.fenixframework.core.adt.bplustree;

import java.io.Serializable;
import java.util.Arrays;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class DoubleArray<T extends AbstractDomainObject> implements Serializable {

    // Can this be final? Is it a problem due to deserialization?
    public Comparable[] keys;
    public T[] values;
    
    public DoubleArray(Comparable key, T value, T lastValue) {
	this.keys = new Comparable[2];
	this.values = (T[])new Object[2];
	
	this.keys[0] = key;
	this.values[0] = value;
	
	this.keys[1] = BPlusTreeArray.LAST_KEY;
	this.values[1] = lastValue;
    }
    
    public DoubleArray(Comparable[] keys, T[] values) {
	this.keys = keys;
	this.values = values;
    }
    
    public int length() {
	return keys.length;
    }
    
    public DoubleArray<T> duplicateAndAddKey(Comparable midKey, T left, T right) {
	Comparable[] newKeys = new Comparable[keys.length + 1];
	T[] newValues = (T[])new Object[values.length + 1];
	
	int midKeyIndex = Arrays.binarySearch(newKeys, midKey, null);
	// copy as many items as up to the new insertion spot 
	System.arraycopy(keys, 0, newKeys, 0, midKeyIndex);
	System.arraycopy(values, 0, newValues, 0, midKeyIndex);
	newKeys[midKeyIndex] = midKey;
	newValues[midKeyIndex] = left;
	
	// the midKey can never be the last index, because of the LAST_KEY
	System.arraycopy(keys, midKeyIndex, newKeys, midKeyIndex + 1, keys.length - midKeyIndex);
	System.arraycopy(values, midKeyIndex, newValues, midKeyIndex + 1, values.length - midKeyIndex);
	
	newValues[midKeyIndex + 1] = right;
	
	return new DoubleArray<T>(newKeys, newValues);
    }

    // Up to and excluding key at 'index' (and we place the LAST_KEY on its spot)
    public DoubleArray<T> duplicateLeftOf(int index, T val) {
	Comparable[] newKeys = new Comparable[index];
	T[] newValues = (T[]) new Object[index];
	
	System.arraycopy(keys, 0, newKeys, 0, index - 1);
	System.arraycopy(values, 0, newValues, 0, index - 1);
	
	newKeys[index - 1] = BPlusTreeArray.LAST_KEY;
	newValues[index - 1] = val;
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    // Up to and including key at 'index'
    public DoubleArray<T> duplicateRightOf(int index) {
	Comparable[] newKeys = new Comparable[keys.length - index];
	T[] newValues = (T[]) new Object[values.length - index];
	
	System.arraycopy(keys, index, newKeys, 0, keys.length - index);
	System.arraycopy(values, index, newValues, 0, values.length - index);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    public T get(Comparable key) {
	int index = Arrays.binarySearch(keys, key, null);
	if (index >= 0) {
	    return values[index];
	} else {
	    return null;
	}
    }
    
    // assumes the deletedKey exists
    public DoubleArray<T> replaceKey(Comparable deletedKey, Comparable replacementKey, T subNode) {
	int remKeyIndex = Arrays.binarySearch(keys, deletedKey, null);
	int newKeyIndex = Arrays.binarySearch(keys, replacementKey, null);
	
	int lowerIndex = remKeyIndex < newKeyIndex ? remKeyIndex : newKeyIndex;
	int higherIndex = remKeyIndex > newKeyIndex ? remKeyIndex : newKeyIndex;
	
	Comparable[] newKeys = new Comparable[keys.length];
	T[] newValues = (T[]) new Object[values.length];
	
	// copy up to (excluding) the lower index changed
	// does nothing if the lower index is zero
	System.arraycopy(keys, 0, newKeys, 0, lowerIndex);
	System.arraycopy(values, 0, newValues, 0, lowerIndex);
	
	if (remKeyIndex == newKeyIndex) {
	    // both changes take place in the same index
	    // so copy the right side of the changed index to the new arrays
	    System.arraycopy(keys, lowerIndex + 1, newKeys, lowerIndex + 1, keys.length - lowerIndex - 1);
	    System.arraycopy(values, lowerIndex + 1, newValues, lowerIndex + 1, values.length - lowerIndex - 1);
	    
	    // and modify the index left out for the replacement
	    newKeys[lowerIndex] = replacementKey;
	    newValues[lowerIndex] = subNode;
	    
	} else {
	    // copy between the two changed indexes in case they are different
	    if (remKeyIndex == lowerIndex) {
		// the removed key is the lower index, so we skip that copy from the original arrays
		// we copy as many as up to the replacement key index (excluding) because it is the higher index
		System.arraycopy(keys, lowerIndex + 1, newKeys, lowerIndex, higherIndex - lowerIndex - 1);
		System.arraycopy(values, lowerIndex + 1, newValues, lowerIndex, higherIndex - lowerIndex - 1);
		
		// and we insert the replacement key in the higher index
		// note that the higher index was calculated in the original array, form which
		// an element has been subtracted (the lower index in the original array)
		newKeys[higherIndex - 1] = replacementKey;
		newValues[higherIndex - 1] = subNode;
		
		// copy the rest of the array
		System.arraycopy(keys, higherIndex, newKeys, higherIndex, keys.length - higherIndex);
		System.arraycopy(values, higherIndex, newValues, higherIndex, values.length - higherIndex);
		
	    } else {
		// the replacement key is the lower index, so we save one slot on the new arrays for it
		// we copy as many as up to the removed key index (which is the higher index in this case)
		System.arraycopy(keys, lowerIndex, newKeys, lowerIndex + 1, higherIndex - lowerIndex);
		System.arraycopy(values, lowerIndex, newValues, lowerIndex + 1, higherIndex - lowerIndex);
		
		// and we insert the replacement key in the lower index
		newKeys[lowerIndex] = replacementKey;
		newValues[lowerIndex] = subNode;
		
		// copy the rest of the array, by skipping the removed index (which is the higher) from the
		// original array and taking into account that we also added the replacement key in the 
		// new array
		System.arraycopy(keys, higherIndex + 1, newKeys, higherIndex + 1, keys.length - higherIndex);
		System.arraycopy(values, higherIndex + 1, newValues, higherIndex + 1, values.length - higherIndex);
	    }
	}
	
	return new DoubleArray<T>(keys, values);
    }
}
