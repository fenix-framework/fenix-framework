package pt.ist.fenixframework.core.adt.skiplist;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class SkipListNodeShadow extends SkipListNodeShadow_Base {
    
    public SkipListNodeShadow() {
        super();
    }
    
    public SkipListNodeShadow(int maxLevel, AbstractDomainObject value) {
	this();
	setForward(new ForwardArrayShadow(maxLevel));
	setValue(value);
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
