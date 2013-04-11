package pt.ist.fenixframework;

import java.util.Set;

import jvstm.cps.Depended;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicateSupport;
import pt.ist.fenixframework.consistencyPredicates.DomainConsistencyPredicate;
import pt.ist.fenixframework.consistencyPredicates.DomainDependenceRecord;

/**
 * Each domain object is associated with one <code>DomainMetaObject</code>,
 * which is a representation of the domain object inside the fenix-framework
 * domain. The <code>DomainMetaObject</code> is created when the domain object
 * is created.<br>
 * 
 * The <code>DomainMetaObject</code> stores the object's {@link DomainMetaClass} and all its dependencies as
 * {@link DomainDependenceRecord}s. The object's
 * <strong>own</strong> dependence records point to other objects on which the
 * consistency of this object depends. The object's <strong>depending</strong>
 * dependence records point to the other objects whose consistency depends on
 * this object.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 **/
@NoDomainMetaObjects
public class DomainMetaObject extends DomainMetaObject_Base implements Depended<DomainDependenceRecord> {

    public DomainMetaObject() {
        super();
    }

    /**
     * Deletes this <code>DomainMetaObject</code>, and all the object's own {@link DomainDependenceRecord}s.
     * It also removes the dependencies of other objects to this object.<br>
     * 
     * A DomainMetaObject should be deleted only when the associated DO is being deleted.
     * Therefore, we can remove it from its metaClass' existing objects list.<br>
     * 
     * This method assumes that an object must be disconnected from all other objects before being deleted.
     * Because all relations are bidireccional, the transaction that disconnects this object from all its
     * relations will have all of the objects at the end of those relations in its write set.
     * Thus, if any other object's consistency depends on the existence of this object,
     * its consistency predicate will still be checked, even after removing
     * the <strong>depending</strong> dependence records of this object.
     **/
    public void delete() {
        getDomainMetaClass().removeExistingDomainMetaObject(this);

        // Removes the dependencies from other objects.
        // Because the object was disconnected from all relations, any depending objects
        // will be checked by the objects on the other side of the relations. 
        for (DomainDependenceRecord dependingDependenceRecord : getDependingDependenceRecordSet()) {
            removeDependingDependenceRecord(dependingDependenceRecord);
        }

        // Removes this objects own records, of its own predicates.
        // The object is being deleted, so it no longer has to be consistent.
        for (DomainDependenceRecord ownDependenceRecord : getOwnDependenceRecordSet()) {
            ownDependenceRecord.delete();
        }

        // Removes the relation to the DomainObject that this meta object used to represent.
        setDomainObject(null);

        // Deletes THIS metaObject, which is also a fenix-framework DomainObject.
        deleteDomainObject();
    }

    @Override
    public void setDomainObject(DomainObject domainObject) {
        // These two sets are needed because the relation between a domainObject
        // and it's metaObject is only partially implemented in DML.
        super.setDomainObject(domainObject);
        justSetMetaObjectForDomainObject(domainObject, this);
    }

    /**
     * @return The {@link DomainDependenceRecord} of this <code>DomainMetaObject</code> for the {@link DomainConsistencyPredicate}
     *         passed as argument
     */
    public DomainDependenceRecord getOwnDependenceRecord(DomainConsistencyPredicate predicate) {
        for (DomainDependenceRecord dependenceRecord : getOwnDependenceRecordSet()) {
            if (dependenceRecord.getDomainConsistencyPredicate() == predicate) {
                return dependenceRecord;
            }
        }
        return null;
    }

    /**
     * @return true if this object is consistent according to all its {@link DomainConsistencyPredicate}s.
     */
    public boolean isConsistent() {
        for (DomainDependenceRecord dependenceRecord : getOwnDependenceRecordSet()) {
            if (!dependenceRecord.isConsistent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if this <code>DomainMetaObject</code> has a {@link DomainDependenceRecord} for the
     *         {@link DomainConsistencyPredicate} passed as argument
     */
    public boolean hasOwnDependenceRecord(DomainConsistencyPredicate predicate) {
        return getOwnDependenceRecord(predicate) != null;
    }

    // @Override
    public void removeDomainObject() {
        DomainObject domainObject = getDomainObject();

        // These two sets are needed because the relation between a domainObject
        // and it's metaObject is only partially implemented in DML.
        justSetMetaObjectForDomainObject(domainObject, null);
        super.setDomainObject(null);
    }

    public static DomainMetaObject getDomainMetaObjectFor(DomainObject obj) {
        return ConsistencyPredicateSupport.getInstance().getDomainMetaObjectFor(obj);
    }

    private static void justSetMetaObjectForDomainObject(DomainObject domainObject, DomainMetaObject metaObject) {
        ConsistencyPredicateSupport.getInstance().justSetMetaObjectForDomainObject(domainObject, metaObject);
    }

    // Depended interface implemented below:
    @Override
    public void addDependence(DomainDependenceRecord record) {
        addDependingDependenceRecord(record);
    }

    @Override
    public void removeDependence(DomainDependenceRecord record) {
        removeDependingDependenceRecord(record);
    }

    @Override
    public Set<DomainDependenceRecord> getDependenceRecords() {
        return getDependingDependenceRecordSet();
    }
}
