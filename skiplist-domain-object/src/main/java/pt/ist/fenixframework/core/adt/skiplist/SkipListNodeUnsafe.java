package pt.ist.fenixframework.core.adt.skiplist;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class SkipListNodeUnsafe extends SkipListNodeUnsafe_Base {
    
    public SkipListNodeUnsafe() {
        super();
    }
    
    public SkipListNodeUnsafe(int maxLevel, AbstractDomainObject value) {
	this();
	setForward(new ForwardArrayUnsafe(maxLevel));
	setValue(value);
    }
    
    public void setForward(int level, SkipListNodeUnsafe next) {
	SkipListNodeUnsafe[] arr = copyForward();
	arr[level] = next;
	setForward(new ForwardArrayUnsafe(arr));
    }
    
    public SkipListNodeUnsafe getForward(int level) {
	return getForwardUnsafe().forward[level];
    }
    
    private SkipListNodeUnsafe[] copyForward() {
	SkipListNodeUnsafe[] arr = getForward().forward;
	int size = arr.length;
	SkipListNodeUnsafe[] newArr = new SkipListNodeUnsafe[arr.length];
	System.arraycopy(arr, 0, newArr, 0, size);
	return newArr;
    }
    
}
