/*
 * JVSTM: a Java library for Software Transactional Memory
 * Copyright (C) 2005 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package jvstm.cps;

import jvstm.ActiveTransactionsRecord;
import jvstm.TopLevelTransaction;
import jvstm.Transaction;
import jvstm.VBox;
import jvstm.util.Cons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConsistentTopLevelTransaction extends TopLevelTransaction implements ConsistentTransaction {

    protected Cons newObjects = Cons.empty();
    protected Set alreadyChecked = null;

    public ConsistentTopLevelTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    public void registerNewObject(Object obj) {
        newObjects = newObjects.cons(obj);
    }

    public void registerNewObjects(Cons objs) {
        newObjects = objs.reverseInto(newObjects);
    }

    public Cons getNewObjectsRegister() {
        return newObjects;
    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        return new ConsistentNestedTransaction(this);
    }

    @Override
    protected void tryCommit() {
        if (isWriteTransaction()) {
            alreadyChecked = new HashSet();
            checkConsistencyPredicates();
            alreadyChecked = null; // allow gc of set
        }
        super.tryCommit();
    }

    protected void checkConsistencyPredicates() {
        // recheck all consistency predicates that may have changed
        Iterator<DependenceRecord> depRecIter = getDependenceRecordsToRecheck();
        while (depRecIter.hasNext()) {
            recheckDependenceRecord(depRecIter.next());
        }

        // check consistency predicates for all new objects
        for (Object obj : newObjects) {
            checkConsistencyPredicates(obj);
        }
    }

    protected void recheckDependenceRecord(DependenceRecord dependence) {
        Set<Depended> newDepended = checkOnePredicate(dependence.getDependent(), dependence.getPredicate());

        if (newDepended == null) {
            // a null return means that the predicate was already checked
            return;
        }

        Iterator<Depended> oldDeps = dependence.getDepended();
        while (oldDeps.hasNext()) {
            Depended dep = oldDeps.next();
            if (!newDepended.remove(dep)) {
                // if we didn't find the dep in the newDepended, it's
                // because it is no longer a depended, so remove the dependence
                oldDeps.remove();
                dep.removeDependence(dependence);
            }
        }

        // the elements remaining in the set newDepended are new and
        // should be added to the dependence record
        // likewise, the dependence record should be added to those depended
        for (Depended dep : newDepended) {
            dep.addDependence(dependence);
            dependence.addDepended(dep);
        }
    }

    protected void checkConsistencyPredicates(Object obj) {
        for (Method predicate : ConsistencyPredicateSystem.getPredicatesFor(obj)) {
            Set<Depended> depended = checkOnePredicate(obj, predicate);
            if ((depended != null) && (!depended.isEmpty())) {
                DependenceRecord dependence = makeDependenceRecord(obj, predicate, depended);
                for (Depended dep : depended) {
                    dep.addDependence(dependence);
                }
            }
        }
    }

    /*
     * This method checks one predicate, returning the set of objects
     * on which the check depended.  If this same predicate was
     * already checked for this object, the check is skipped and the
     * method returns null to indicate it.
     */
    protected Set<Depended> checkOnePredicate(Object obj, Method predicate) {
        Pair toCheck = new Pair(obj, predicate);

        if (alreadyChecked.contains(toCheck)) {
            // returning null means that no check was actually done, because it is repeated
            return null;
        }

        alreadyChecked.add(toCheck);

        ConsistencyCheckTransaction tx = makeConsistencyCheckTransaction(obj);
        tx.start();

        boolean predicateOk = false;
        boolean finished = false;

        Class<? extends ConsistencyException> excClass = predicate.getAnnotation(ConsistencyPredicate.class).value();
        try {
            predicateOk = (Boolean) (predicate.invoke(obj));
            Transaction.commit();
            finished = true;
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();

            ConsistencyException exc;

            // only wrap the cause if it is not a ConsistencyException already
            if (cause instanceof ConsistencyException) {
                exc = (ConsistencyException) cause;
            } else {
                try {
                    exc = excClass.newInstance();
                } catch (Throwable t) {
                    throw new Error(t);
                }
                exc.initCause(cause);
            }

            exc.init(obj, predicate);
            throw exc;
        } catch (Throwable t) {
            // any other kind of throwable is an Error in the JVSTM that should be fixed
            throw new Error(t);
        } finally {
            if (!finished) {
                Transaction.abort();
            }
        }

        if (predicateOk) {
            return tx.getDepended();
        } else {
            ConsistencyException exc;
            try {
                exc = excClass.newInstance();
            } catch (Throwable t) {
                throw new Error(t);
            }
            exc.init(obj, predicate);
            throw exc;
        }
    }

    protected ConsistencyCheckTransaction makeConsistencyCheckTransaction(Object obj) {
        return new DefaultConsistencyCheckTransaction(this);
    }

    protected DependenceRecord makeDependenceRecord(Object dependent, Method predicate, Set<Depended> depended) {
        return new DefaultDependenceRecord(dependent, predicate, depended);
    }

    protected Iterator<DependenceRecord> getDependenceRecordsToRecheck() {
        Cons<Iterator<DependenceRecord>> iteratorsList = Cons.empty();

        for (VBox box : boxesWritten.keySet()) {
            Depended dep = DependedVBoxes.getDependedForBoxIfExists(box);
            if (dep != null) {
                iteratorsList = iteratorsList.cons(dep.getDependenceRecords().iterator());
            }
        }

        return new ChainedIterator<DependenceRecord>(iteratorsList.iterator());
    }

    static final class Pair {
        final Object first;
        final Object second;

        Pair(Object first, Object second) {
            this.first = first;
            this.second = second;
        }

        public int hashCode() {
            return first.hashCode() + second.hashCode();
        }

        public boolean equals(Object other) {
            if (other.getClass() != Pair.class) {
                return false;
            }

            Pair p2 = (Pair) other;

            return (p2.first == first) && (p2.second == second);
        }
    }
}
