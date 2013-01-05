package pt.ist.fenixframework.core.adt.bplustree;

import java.io.Serializable;
import java.util.Arrays;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class DoubleArray<T extends AbstractDomainObject> implements Serializable {

    // Can this be final? Is it a problem due to deserialization?
    // In terms of immutability of the ValueType it does not matter much anyway 
    // as the slots of the arrays are always mutable
    public Comparable[] keys;
    public T[] values;

    public DoubleArray() {
	this.keys = new Comparable[0];
	this.values = (T[]) new Object[0];
    }
    
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
    
    public boolean containsKey(Comparable key) {
	return get(key) != null;
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

    public T firstValue() {
	return values[0];
    }

    public Comparable firstKey() {
	return keys[0];
    }

    public T lastValue() {
	return values[values.length - 1];
    }
    
    // produces a new double array
    public DoubleArray<T> mergeWith(Comparable splitKey, DoubleArray<T> left) {
	int leftSize = left.length();
	int thisSize = this.length();
	
	Comparable[] newKeys = new Comparable[thisSize + leftSize];
	T[] newValues = (T[]) new Object[thisSize + leftSize];
	
	System.arraycopy(left.keys, 0, newKeys, 0, leftSize);
	System.arraycopy(left.values, 0, newValues, 0, leftSize);

	// remove the entry for left's LAST_KEY and add the higher left value associated with the split-key
	// this boils down to only updating the key, because the value was already copied
	newKeys[leftSize - 1] = splitKey;
    	
	// merge the remaining sub-nodes
	System.arraycopy(this.keys, 0, newKeys, leftSize, thisSize);
	System.arraycopy(this.values, 0, newValues, leftSize, thisSize);
	
	// sort both arrays according to the keys
	quicksort(newKeys, newValues);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    // produces a new double array
    public DoubleArray<T> mergeWith(DoubleArray<T> other) {
	int otherSize = other.length();
	int thisSize = this.length();
	
	Comparable[] newKeys = new Comparable[thisSize + otherSize];
	T[] newValues = (T[]) new Object[thisSize + otherSize];
	
	System.arraycopy(other.keys, 0, newKeys, 0, otherSize);
	System.arraycopy(other.values, 0, newValues, 0, otherSize);
	System.arraycopy(this.keys, 0, newKeys, otherSize, thisSize);
	System.arraycopy(this.values, 0, newValues, otherSize, thisSize);
	
	// sort both arrays according to the keys
	quicksort(newKeys, newValues);
	
	return new DoubleArray<T>(newKeys, newValues);
    }

    // adapted a quicksort algorithm to simultaneously sort both arrays based on the keys' order
    public void quicksort(Comparable[] main, T[] index) {
	quicksort(main, index, 0, index.length - 1);
    }

    // quicksort a[left] to a[right]
    public void quicksort(Comparable[] a, T[] index, int left, int right) {
	if (right <= left) return;
	int i = partition(a, index, left, right);
	quicksort(a, index, left, i-1);
	quicksort(a, index, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private int partition(Comparable[] a, T[] index, int left, int right) {
	int i = left - 1;
	int j = right;
	while (true) {
	    while (less(a[++i], a[right]))
		;                               
	    while (less(a[right], a[--j]))     
		if (j == left) break;          
	    if (i >= j) break;                 
	    exch(a, index, i, j);               
	}
	exch(a, index, i, right);               
	return i;
    }

    private boolean less(Comparable x, Comparable y) {
	return (x.compareTo(y) < 0);
    }
    
    private void exch(Comparable[] a, T[] index, int i, int j) {
	Comparable tmp = a[i];
	a[i] = a[j];
	a[j] = tmp;
	T tmpi = index[i];
	index[i] = index[j];
	index[j] = tmpi;
    }
    
    protected class KeyVal {
	public final Comparable key;
	public final T val;
	
	public KeyVal(Comparable key, T val) {
	    this.key = key;
	    this.val = val;
	}
    }
    
    public KeyVal getSmallestKeyValue() {
	return new KeyVal(keys[0], values[0]);
    }
    
    public KeyVal getBiggestKeyValue() {
	return new KeyVal(keys[keys.length - 1], values[values.length - 1]);
    }
    
    public DoubleArray<T> removeSmallestKeyValue() {
	Comparable[] newKeys = new Comparable[this.length() - 1];
	T[] newValues = (T[]) new Object[this.length() - 1];
	
	System.arraycopy(keys, 1, newKeys, 0, keys.length - 1);
	System.arraycopy(values, 1, newValues, 0, values.length - 1);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    public DoubleArray<T> removeBiggestKeyValue() {
	Comparable[] newKeys = new Comparable[this.length() - 1];
	T[] newValues = (T[]) new Object[this.length() - 1];
	
	System.arraycopy(keys, 0, newKeys, 0, keys.length - 1);
	System.arraycopy(values, 0, newValues, 0, values.length - 1);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    public DoubleArray<T> removeKey(Comparable key) {
	Comparable[] newKeys = new Comparable[this.length() - 1];
	T[] newValues = (T[]) new Object[this.length() - 1];
	
	int indexToRemove = Arrays.binarySearch(keys, key, null);
	
	System.arraycopy(keys, 0, newKeys, 0, indexToRemove);
	System.arraycopy(values, 0, newValues, 0, indexToRemove);
	
	System.arraycopy(keys, indexToRemove + 1, newKeys, indexToRemove, keys.length - indexToRemove - 1);
	System.arraycopy(values, indexToRemove + 1, newValues, indexToRemove, values.length - indexToRemove - 1);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    public DoubleArray<T> addKeyValue(KeyVal keyVal) {
	return addKeyValue(keyVal.key, keyVal.val);
    }
    
    public DoubleArray<T> addKeyValue(Comparable keyToInsert, T valToInsert) {
	Comparable[] newKeys = new Comparable[this.length() + 1];
	T[] newValues = (T[]) new Object[this.length() + 1];
	
	int indexToInsert = Arrays.binarySearch(keys, keyToInsert, null);
	
	System.arraycopy(keys, 0, newKeys, 0, indexToInsert);
	System.arraycopy(values, 0, newValues, 0, indexToInsert);
	
	newKeys[indexToInsert] = keyToInsert;
	newValues[indexToInsert] = valToInsert;
	
	System.arraycopy(keys, indexToInsert, newKeys, indexToInsert + 1, keys.length - indexToInsert);
	System.arraycopy(values, indexToInsert, newValues, indexToInsert + 1, values.length - indexToInsert);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    public Comparable lowerKeyThanHighest() {
	return keys[keys.length - 2];
    }
    
    public Comparable findRightMiddlePosition() {
	return keys[BPlusTreeArray.LOWER_BOUND + 1];
    }
    
    // Left part up to the "findRightMiddlePosition"
    public DoubleArray<T> leftPart() {
	Comparable[] newKeys = new Comparable[BPlusTreeArray.LOWER_BOUND];
	T[] newValues = (T[]) new Object[BPlusTreeArray.LOWER_BOUND];
	
	System.arraycopy(keys, 0, newKeys, 0, BPlusTreeArray.LOWER_BOUND);
	System.arraycopy(values, 0, newValues, 0, BPlusTreeArray.LOWER_BOUND);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
    
    // Left part from the "findRightMiddlePosition" (including) until the end
    public DoubleArray<T> rightPart() {
	Comparable[] newKeys = new Comparable[keys.length - BPlusTreeArray.LOWER_BOUND];
	T[] newValues = (T[]) new Object[values.length - BPlusTreeArray.LOWER_BOUND];
	
	System.arraycopy(keys, BPlusTreeArray.LOWER_BOUND, newKeys, 0, keys.length - BPlusTreeArray.LOWER_BOUND);
	System.arraycopy(values, BPlusTreeArray.LOWER_BOUND, newValues, 0, values.length - BPlusTreeArray.LOWER_BOUND);
	
	return new DoubleArray<T>(newKeys, newValues);
    }
}
