package pt.ist.fenixframework.backend.ogm;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationBaseSet;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.adt.bplustree.BPlusTree;

public class RelationSet<E1 extends AbstractDomainObject,E2 extends AbstractDomainObject> extends AbstractSet<E2> implements RelationBaseSet<E2> {
    private E1 owner;
    private Relation<E1,E2> relation;

    private BPlusTree<E2> elements;

    public RelationSet(E1 owner, Relation<E1,E2> relation, BPlusTree<E2> elements) {
        this.owner = owner;
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
	relation.remove(owner, (E2)o);
    }

    @Override
    public Iterator<E2> iterator() {
        return new RelationSetIterator<E2>();
    }

    private class RelationSetIterator<X extends AbstractDomainObject> implements Iterator<X> {
        private Iterator<X> iterator;
        private boolean canRemove = false;
        private X previous = null;

        RelationSetIterator() {
            this.iterator = RelationSet.this.elements.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public X next() {
            X result = iterator.next();
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
                RelationSet.this.internalRemove(previous);
            }
        }
    }

    // smf: I think that this was only necessary when modelling relations-to-many in OGM.  Now that
    // they're just modelled as relations-to-one BPlusTree, I think that this is no longer used.
    // public boolean add(E2 o) {
    //     if (theSet.contains(o)) {
    //         return false;
    //     } else {
    //         // if (this.addedObjects.contains(o)) { // then we're done

    //         System.out.println("theSet is " + theSet.getClass());
    //         relation.add(owner, o);
    //         return true;
    //     }
    // }

    // public void setFromHibernate(Set<E2> set) {
    //     this.theSet = set;
    // }

    // public Set<E2> getToHibernate() {
    //     return this.theSet;
    // }

}
