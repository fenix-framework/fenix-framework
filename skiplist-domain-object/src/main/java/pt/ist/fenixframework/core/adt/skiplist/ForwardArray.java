package pt.ist.fenixframework.core.adt.skiplist;

import java.io.Serializable;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class ForwardArray implements Serializable {

    private static final long serialVersionUID = 5348029777012836627L;
    
    public SkipListNode[] forward;
    
    private ForwardArray() {
	
    }
    
    public ForwardArray(int level) {
	this.forward = new SkipListNode[level + 1];
    }
    
    public ForwardArray(SkipListNode[] forward) {
	this.forward = forward;
    }
    
}
