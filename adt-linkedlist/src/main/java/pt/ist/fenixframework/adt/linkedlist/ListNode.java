package pt.ist.fenixframework.adt.linkedlist;

import java.io.Serializable;

import pt.ist.fenixframework.adt.linkedlist.ListNode_Base;

public class ListNode<T extends Serializable> extends ListNode_Base {
    
    public ListNode(Comparable key, T value, ListNode<T> next) {
        super();
        setKeyValue(new KeyValue(key, value));
        setNext(next);
    }
    
}
