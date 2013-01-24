package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;

public class SkipListNodeShadow<T extends Serializable> extends SkipListNodeShadow_Base {
    
    public SkipListNodeShadow() {
        super();
    }
    
    public SkipListNodeShadow(int maxLevel, Comparable key, T value) {
	this();
	setForward(new ForwardArrayShadow(maxLevel));
	setKeyValue(new KeyValue(key, value));
    }
    
    public void setForward(int level, SkipListNodeShadow next) {
	SkipListNodeShadow[] arr = copyForward();
	arr[level] = next;
	setForward(new ForwardArrayShadow(arr));
    }
    
    public SkipListNodeShadow getForward(int level) {
	return getForwardShadow().forward[level];
    }
    
    private SkipListNodeShadow[] copyForward() {
	SkipListNodeShadow[] arr = getForward().forward;
	int size = arr.length;
	SkipListNodeShadow[] newArr = new SkipListNodeShadow[arr.length];
	System.arraycopy(arr, 0, newArr, 0, size);
	return newArr;
    }
    
}
