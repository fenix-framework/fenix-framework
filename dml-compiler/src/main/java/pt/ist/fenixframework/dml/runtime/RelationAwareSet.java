package pt.ist.fenixframework.dml.runtime;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import pt.ist.fenixframework.DomainObject;

public class RelationAwareSet<E1 extends DomainObject,E2 extends DomainObject> extends AbstractSet<E2> implements Set<E2>,RelationBaseSet<E2> {
    private Set<E2> set;
    private E1 owner;
    private Relation<E1,E2> relation;

    public RelationAwareSet(E1 owner, Relation<E1,E2> relation, Set<E2> set) {
        this.owner = owner;
        this.relation = relation;
        this.set = set;
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

    @Override
    public Iterator<E2> iterator() {
        return new RelationAwareIterator(set);
    }

    @Override
    public boolean add(E2 o) {
        if (set.contains(o)) {
            return false;
        } else {
            relation.add(owner, o);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        if (set.contains(o)) {
            relation.remove(owner, (E2)o);
            return true;
        } else {
            return false;
        }
    }

    private class RelationAwareIterator implements Iterator<E2> {
        private Iterator<E2> iterator;
        private E2 current = null;
        private boolean canRemove = false;

        RelationAwareIterator(Set<E2> set) {
            this.iterator = set.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E2 next() {
            E2 result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }

        @Override
        public void remove() {
            if (! canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                relation.remove(owner, current);
            }
        }
    }
}
