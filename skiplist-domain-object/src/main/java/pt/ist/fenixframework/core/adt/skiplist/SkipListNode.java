package pt.ist.fenixframework.core.adt.skiplist;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class SkipListNode extends SkipListNode_Base {
    
    public SkipListNode() {
        super();
    }
    
    public SkipListNode(int maxLevel, AbstractDomainObject value) {
	this();
	setForward(new ForwardArray(maxLevel));
	setValue(value);
    }
    
    public void setForward(int level, SkipListNode next) {
	SkipListNode[] arr = copyForward();
	arr[level] = next;
	setForward(new ForwardArray(arr));
    }
    
    public SkipListNode getForward(int level) {
	return getForward().forward[level];
    }
    
    private SkipListNode[] copyForward() {
	SkipListNode[] arr = getForward().forward;
	int size = arr.length;
	SkipListNode[] newArr = new SkipListNode[arr.length];
	System.arraycopy(arr, 0, newArr, 0, size);
	return newArr;
    }
    
}
