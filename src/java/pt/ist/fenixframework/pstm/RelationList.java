package pt.ist.fenixframework.pstm;

import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.Set;

import jvstm.PerTxBox;
import pt.ist.fenixframework.DomainObject;
import dml.runtime.Relation;
import dml.runtime.FunctionalSet;

public class RelationList<E1 extends DomainObject,E2 extends DomainObject> extends AbstractList<E2> implements VersionedSubject,Set<E2>,dml.runtime.RelationBaseSet<E2> {
    private E1 listHolder;
    private Relation<E1,E2> relation;
    private String attributeName;

    private SoftReference<VBox<FunctionalSet<E2>>> elementsRef;

    private PerTxBox<FunctionalSet<E2>> elementsToAdd = new PerTxBox<FunctionalSet<E2>>(DOFunctionalSet.EMPTY) {
        public void commit(FunctionalSet<E2> toAdd) {
	    consolidateElementsIfLoaded();
        }
    };

    private PerTxBox<FunctionalSet<E2>> elementsToRemove = new PerTxBox<FunctionalSet<E2>>(DOFunctionalSet.EMPTY) {
        public void commit(FunctionalSet<E2> toRemove) {
	    consolidateElementsIfLoaded();
        }
    };

    public RelationList(E1 listHolder, Relation<E1,E2> relation, String attributeName, boolean allocateOnly) {
	this.listHolder = listHolder;
        this.relation = relation;
	this.attributeName = attributeName;

	VBox elementsBox = null;
	if (allocateOnly) {
	    elementsBox = SoftReferencedVBox.makeNew(listHolder, attributeName, allocateOnly);
	} else {
	    elementsBox = new SoftReferencedVBox<FunctionalSet<E2>>(listHolder, attributeName, DOFunctionalSet.EMPTY);
	}
	this.elementsRef = new SoftReference<VBox<FunctionalSet<E2>>>(elementsBox);
    }

    // The access to the elementsRef field should be synchronized
    private synchronized VBox<FunctionalSet<E2>> getElementsBox() {
	VBox<FunctionalSet<E2>> box = elementsRef.get();
	if (box == null) {
	    box = SoftReferencedVBox.makeNew(this.listHolder, this.attributeName, true);
	    this.elementsRef = new SoftReference<VBox<FunctionalSet<E2>>>(box);
	}
	return box;
    }


    public jvstm.VBoxBody addNewVersion(String attr, int txNumber) {
        return getElementsBox().addNewVersion(attr, txNumber);
    }

    public Object getCurrentValue(Object obj, String attrName) {
        // what's the correct value to return here?  should it be the
        // RelationList instance or the FunctionalSet within it?  I'll
        // go with the RelationList for now, but maybe this will
        // change later.  Either way, this code should be considered
        // experimental.
        return this;
    }

    private FunctionalSet<E2> elementSet() {
	consolidateElements();
	return getElementsBox().get(listHolder, attributeName);
    }

    protected void consolidateElementsIfLoaded() {
	if (elementsToAdd.get().size() + elementsToRemove.get().size() > 0) {
	    VBox<FunctionalSet<E2>> box = getElementsBox();
	    if (box.hasValue()) {
		consolidateElements();
	    } else {
		// here we write the NOT_LOADED_VALUE to force the box to go to the write-set
		box.putNotLoadedValue();
	    }
	}
    }

    private void consolidateElements() {
	VBox<FunctionalSet<E2>> box = getElementsBox();
	FunctionalSet<E2> origSet = box.get(listHolder, attributeName);
	FunctionalSet<E2> newSet = origSet;

	if (elementsToRemove.get().size() > 0) {
	    Iterator<E2> iter = elementsToRemove.get().iterator();
	    while (iter.hasNext()) {
		newSet = newSet.remove(iter.next());
	    }
	    elementsToRemove.put(DOFunctionalSet.EMPTY);
	}

	if (elementsToAdd.get().size() > 0) {
	    Iterator<E2> iter = elementsToAdd.get().iterator();
	    while (iter.hasNext()) {
		newSet = newSet.add(iter.next());
	    }
	    elementsToAdd.put(DOFunctionalSet.EMPTY);
	}

	if (newSet != origSet) {
	    box.put(newSet);
	}
    }

    public void setFromOJB(Object obj, String attr, OJBFunctionalSetWrapper ojbList) {
	getElementsBox().setFromOJB(obj, attr, ojbList.getElements());
    }

    public void justAdd(E2 obj) {
        Transaction.logAttrChange((DomainObject)listHolder, attributeName);
	elementsToAdd.put(elementsToAdd.get().add(obj));
	elementsToRemove.put(elementsToRemove.get().remove(obj));
    }

    public void justRemove(E2 obj) {
        Transaction.logAttrChange((DomainObject)listHolder, attributeName);
	elementsToRemove.put(elementsToRemove.get().add(obj));
	elementsToAdd.put(elementsToAdd.get().remove(obj));
    }


    public int size() {
	return elementSet().size();
    }

    public E2 get(int index) {
        return elementSet().get(index);
    }

    public E2 set(int index, E2 element) {
        E2 oldElement = get(index);

        int oldModCount = modCount;
        if (oldElement != element) {
            remove(oldElement);
            add(index, element);
        }
        // After the remove and add the modCount would have been incremented twice
        modCount = oldModCount + 1;
        return oldElement;
    }

    public void add(int index, E2 element) {
        relation.add(listHolder, element);
        modCount++;
    }

    public E2 remove(int index) {
        E2 elemToRemove = get(index);
        remove(elemToRemove);
	return elemToRemove;
    }

    public boolean remove(Object o) {
        modCount++;
        relation.remove(listHolder, (E2)o);
	// HACK!!! What to return here?
	// I wouldn't like to force a load of the list to be able to return the correct boolean value
	return true;
    }

    public Iterator<E2> iterator() {
	return new RelationListIterator<E2>(this);
    }

    private static class RelationListIterator<X extends DomainObject> implements Iterator<X> {
        private RelationList<?,X> list;
	private Iterator<X> iter;
	private boolean canRemove = false;
	private X previous = null;

	RelationListIterator(RelationList<?,X> list) {
            this.list = list;
	    this.iter = list.elementSet().iterator();
	}

	public boolean hasNext() {
	    return iter.hasNext();
	}

	public X next() {
	    X result = iter.next();
	    canRemove = true;
	    previous = result;
	    return result;
	}
	    
	public void remove() {
	    if (! canRemove) {
		throw new IllegalStateException();
	    } else {
		canRemove = false;
		list.remove(previous);
	    }
	}
    }
}
