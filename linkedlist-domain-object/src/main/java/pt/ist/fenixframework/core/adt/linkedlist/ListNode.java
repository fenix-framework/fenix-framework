package pt.ist.fenixframework.core.adt.linkedlist;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class ListNode<T extends AbstractDomainObject> extends ListNode_Base {
    
    public ListNode(T value, ListNode<T> next) {
        super();
        setValue(value);
        setNext(next);
    }
    
}
