package dml.runtime;

import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Set;

public class RelationAwareSet<E1,E2> extends AbstractSet<E2> implements Set<E2>,RelationBaseSet<E2> {
    private Set<E2> set = new VSet();

    private E1 owner;
    private Relation<E1,E2> relation;

    public RelationAwareSet(E1 owner, Relation<E1,E2> relation) {
        this.owner = owner;
        this.relation = relation;
    }

    public void justAdd(E2 elem) {
        set.add(elem);
    }

    public void justRemove(E2 elem) {
        set.remove(elem);
    }

    public int size() {
        return set.size();
    }

    public boolean contains(Object o) {
        return set.contains(o);
    }

    public Iterator<E2> iterator() {
        return new RelationAwareIterator<E2>(set);
    }

    public boolean add(E2 o) {
        if (set.contains(o)) {
            return false;
        } else {
            relation.add(owner, o);
            return true;
        }
    }

    public boolean remove(Object o) {
        if (set.contains(o)) {
            relation.remove(owner, (E2)o);
            return true;
        } else {
            return false;
        }
    }


    private class RelationAwareIterator<E2> implements Iterator<E2> {
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;

        RelationAwareIterator(Set<E2> set) {
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
                RelationAwareSet.this.remove(current);
            }
        }
    }
}
