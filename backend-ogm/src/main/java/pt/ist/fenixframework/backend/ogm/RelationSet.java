package pt.ist.fenixframework.backend.ogm;

import java.util.Iterator;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationBaseSet;

public class RelationSet<E1 extends DomainObject,E2 extends DomainObject> extends AbstractSet<E2> implements Set<E2>,RelationBaseSet<E2> {
    // /* when theSet is an hibernate's PersistentSet then we need another place to store elements...  This is it */
    // private Set<E2> addedObjects = new HashSet<E2>();

    public Set<E2> theSet;

    private E1 owner;
    private Relation<E1,E2> relation;

    public RelationSet(Set<E2> theSet, E1 owner, Relation<E1,E2> relation) {
        this.theSet = theSet;
        this.owner = owner;
        this.relation = relation;
    }

    public void setFromHibernate(Set<E2> set) {
        this.theSet = set;
    }

    public Set<E2> getToHibernate() {
        return this.theSet;
    }

    public void justAdd(E2 elem) {
        theSet.add(elem);
    }

    public void justRemove(E2 elem) {
        theSet.remove(elem);
    }

    public int size() {
        return theSet.size();
    }

    public boolean contains(Object o) {
        return theSet.contains(o);
    }

    public Iterator<E2> iterator() {
        return new RelationIterator<E2>(theSet);
    }

    public boolean add(E2 o) {
        if (theSet.contains(o)) {
            return false;
        } else {
            // if (this.addedObjects.contains(o)) { // then we're done

            System.out.println("theSet is " + theSet.getClass());
            relation.add(owner, o);
            return true;
        }
    }

    public boolean remove(Object o) {
        if (theSet.contains(o)) {
            relation.remove(owner, (E2)o);
            return true;
        } else {
            return false;
        }
    }


    private class RelationIterator<E2> implements Iterator<E2> {
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;

        RelationIterator(Set<E2> set) {
            this.iterator = set.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E2 next() {
            E2 result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }
            
        public void remove() {
            if (! canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                // NOTE: first removing from "our" backing set avoids the justRemove (that is later
                // invoked by the remove() in the relation) to actually have to remove, thus not
                // incrementing the backing collection's modCount, hence not causing a
                // ConcurrentModificationException :-/
                this.iterator.remove();
                RelationSet.this.remove(current);
            }
        }
    }
}
