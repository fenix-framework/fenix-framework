package pt.ist.fenixframework.backend.jvstmmem;

import java.util.AbstractSet;
import java.util.Iterator;

import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.adt.bplustree.BPlusTree;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationBaseSet;

public class RelationSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends AbstractSet<E2> implements RelationBaseSet<E2> {
    private E1 listHolder;
    private Relation<E1,E2> relation;

    private BPlusTree<E2> elements;

    public RelationSet(E1 listHolder, Relation<E1,E2> relation, BPlusTree<E2> elements) {
	this.listHolder = listHolder;
        this.relation = relation;
        this.elements = elements;
    }

    @Override
    public void justAdd(E2 obj) {
	elements.insert(obj);
    }

    @Override
    public void justRemove(E2 obj) {
	elements.remove(obj.getOid());
    }

    @Override
    public int size() {
	return elements.size();
    }

    @Override
    public boolean contains(Object obj) {
        if (! (obj instanceof AbstractDomainObject)) {
            return false;
        }
        return elements.containsKey(((E2)obj).getOid());
    }

    @Override
    public boolean remove(Object o) {
	E2 elemToRemove = elements.get(((E2)o).getOid());
	if (elemToRemove == null) {
	    return false;
	}
	internalRemove(o);
	return true;
    }

    private void internalRemove(Object o) {
	relation.remove(listHolder, (E2)o);
    }

    @Override
    public Iterator<E2> iterator() {
	return new RelationSetIterator<E2>(this);
    }

    private static class RelationSetIterator<X extends AbstractDomainObject> implements Iterator<X> {
        private RelationSet<?,X> list;
    	private Iterator<X> iter;
    	private boolean canRemove = false;
    	private X previous = null;

    	RelationSetIterator(RelationSet<?,X> list) {
            this.list = list;
    	    this.iter = list.elements.iterator();
    	}

        @Override
    	public boolean hasNext() {
    	    return iter.hasNext();
    	}
        
        @Override
    	public X next() {
    	    X result = iter.next();
    	    canRemove = true;
    	    previous = result;
    	    return result;
    	}

        @Override
    	public void remove() {
    	    if (! canRemove) {
    		throw new IllegalStateException();
    	    } else {
    		canRemove = false;
    		list.internalRemove(previous);
    	    }
    	}
    }
}
