package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;

import pt.ist.fenixframework.adt.skiplist.SkipListNode_Base;

public class SkipListNode<T extends Serializable> extends SkipListNode_Base {
    
    public SkipListNode() {
        super();
    }
    
    public SkipListNode(int maxLevel, Comparable key, T value) {
	this();
	setForward(new ForwardArray(maxLevel));
	setKeyValue(new KeyValue(key, value));
    }
    
    public void setForward(int level, SkipListNode next) {
	SkipListNode[] arr = copyForward();
	arr[level] = next;
	setForward(new ForwardArray(arr));
    }
    
    public SkipListNode getForward(int level) {
	return getForward().forward[level];
    }
    
    public SkipListNode getForwardCached(boolean forceMiss, int level) {
	return getForwardCached(forceMiss).forward[level];
    }
    
    private SkipListNode[] copyForward() {
	SkipListNode[] arr = getForward().forward;
	int size = arr.length;
	SkipListNode[] newArr = new SkipListNode[arr.length];
	System.arraycopy(arr, 0, newArr, 0, size);
	return newArr;
    }
    
}
