package pt.ist.fenixframework.core.adt.skiplist;

import java.io.Serializable;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class ForwardArrayUnsafe implements Serializable {

    private static final long serialVersionUID = 5348029777012836627L;
    
    public SkipListNodeUnsafe[] forward;
    
    private ForwardArrayUnsafe() {
	
    }
    
    public ForwardArrayUnsafe(int level) {
	this.forward = new SkipListNodeUnsafe[level + 1];
    }
    
    public ForwardArrayUnsafe(SkipListNodeUnsafe[] forward) {
	this.forward = forward;
    }
    
}
