package org.apache.ojb.broker.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.ManageableCollection;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.accesslayer.JdbcAccess;
import org.apache.ojb.broker.accesslayer.OJBIterator;
import org.apache.ojb.broker.accesslayer.PagingIterator;
import org.apache.ojb.broker.accesslayer.RsIterator;
import org.apache.ojb.broker.accesslayer.RsQueryObject;
import org.apache.ojb.broker.accesslayer.ChainingIterator;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryBySQL;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;
import org.apache.ojb.broker.util.ClassHelper;

import pt.ist.fenixframework.pstm.ojb.FenixJdbcAccessImpl;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.DomainObjectAllocator;


public class FenixPersistenceBroker extends PersistenceBrokerImpl {

    public FenixPersistenceBroker(PBKey key, PersistenceBrokerFactoryIF pbf) {
        super(key, pbf);
    }


    // copied and adapted from PersistenceBrokerImpl.doGetObjectByIdentity and getDBObject methods
    @Override
    public Object doGetObjectByIdentity(Identity oid) throws PersistenceBrokerException {
        Class c = oid.getObjectsRealClass();

        if (c == null) {
            c = oid.getObjectsTopLevelClass();
        }

        ClassDescriptor cld = getClassDescriptor(c);
        JdbcAccess dbAccess = serviceJdbcAccess();
        Object newObj = dbAccess.materializeObject(cld, oid);

        // if we did not find the object yet AND if the cld represents an Extent,
        // we can lookup all tables of the extent classes:
        if (newObj == null && cld.isExtent()) {
            Iterator extents = getDescriptorRepository().getAllConcreteSubclassDescriptors(cld).iterator();

            while (extents.hasNext()) {
                ClassDescriptor extCld = (ClassDescriptor) extents.next();

                newObj = dbAccess.materializeObject(extCld, oid);
                if (newObj != null) {
                    break;
                }
            }
        }

        if (newObj != null) {
            if (oid.getObjectsRealClass() == null) {
                oid.setObjectsRealClass(newObj.getClass());
            }
        }

        return newObj;
    }


    // copied and adapted from PersistenceBrokerImpl's
    // "retrieveReference" and QueryReferenceBroker's
    // "retrieveReference", "getReferencedObjectIdentity" and
    // "getReferencedObject" methods
    @Override
    public void retrieveReference(Object obj, String pAttributeName) throws PersistenceBrokerException {
        // In the new version of the fenix-framework, where we use OIDs allover as foreign keys
        // this method is no longer called for retrieving references to single objects
        // (those described by an ObjectReferenceDescriptor).
        // Now, this method should be used only for retrieving collections.
        //
        // So, the code here is the merging of the original
        // PersistenceBrokerImpl.retrieveReference with the
        // QueryReferenceBroker.retrieveCollection method, changed to
        // use OIDs rather the primary keys (which remain to be,
        // still, the idInternal, to create the SQL queries.

        ClassDescriptor cld = getClassDescriptor(obj.getClass());
        CollectionDescriptor cds = cld.getCollectionDescriptorByName(pAttributeName);
        if (cds == null) {
            throw new PersistenceBrokerException("In the Fenix Framework retrieveReference should be called only for collections");
        }

        // this collection type will be used:
        Class collectionClass = cds.getCollectionClass();
        Query fkQuery = getFKQuery((AbstractDomainObject)obj, cld, cds);

        ManageableCollection result = referencesBroker.getCollectionByQuery(collectionClass, fkQuery, false);
        cds.getPersistentField().set(obj, result);
    }


    // this method results from the merging and simplification of the
    // getFKQuery, getFKQueryMtoN, and getFKQuery1toN methods that
    // exist in OJB's QueryReferenceBroker class.
    private Query getFKQuery(AbstractDomainObject obj, ClassDescriptor cld, CollectionDescriptor cod) {
        if (cod.isMtoNRelation()) {
            // each of the following arrays have one element only
            Object[] thisClassFks = cod.getFksToThisClass();
            Object[] itemClassFks = cod.getFksToItemClass();
            String table = cod.getIndirectionTable();

            Criteria criteria = new Criteria();
            criteria.addColumnEqualTo(table + "." + thisClassFks[0], obj.getOid());
            criteria.addColumnEqualToField(table + "." + itemClassFks[0], "OID");

            ClassDescriptor refCld = getClassDescriptor(cod.getItemClass());
            return QueryFactory.newQuery(refCld.getClassOfObject(), table, criteria);
        } else {
            ClassDescriptor refCld = getClassDescriptor(cod.getItemClass());
            // the following array will have only one element
            FieldDescriptor[] fields = cod.getForeignKeyFieldDescriptors(refCld);

            Criteria criteria = new Criteria();
            criteria.addEqualTo(fields[0].getAttributeName(), obj.getOid());

            return QueryFactory.newQuery(refCld.getClassOfObject(), criteria);
        }
    }

    // copied from PersistenceBrokerImpl, to change the RsIteratorFactory used
    protected OJBIterator getIteratorFromQuery(Query query, ClassDescriptor cld) throws PersistenceBrokerException {
        RsIteratorFactory factory = FenixRsIteratorFactory.getInstance();
        OJBIterator result = getRsIteratorFromQuery(query, cld, factory);

        if (query.usePaging()) {
            result = new PagingIterator(result, query.getStartAtIndex(), query.getEndAtIndex());
        }

        return result;
    }

    // verbatim copy from PersistenceBrokerImpl because the method was private there...
    private OJBIterator getRsIteratorFromQuery(Query query, ClassDescriptor cld, RsIteratorFactory factory)
        throws PersistenceBrokerException
    {
        if (query instanceof QueryBySQL)
        {
            return factory.createRsIterator((QueryBySQL) query, cld, this);
        }

        if (!cld.isExtent() || !query.getWithExtents())
        {
            // no extents just use the plain vanilla RsIterator
            return factory.createRsIterator(query, cld, this);
        }


        ChainingIterator chainingIter = new ChainingIterator();
        List<String> tablesRead = new LinkedList<String>();

        // BRJ: add base class iterator
        if (!cld.isInterface())
        {
            chainingIter.addIterator(factory.createRsIterator(query, cld, this));
            tablesRead.add(cld.getFullTableName());
        }

        Iterator extents = getDescriptorRepository().getAllConcreteSubclassDescriptors(cld).iterator();
        while (extents.hasNext())
        {
            ClassDescriptor extCld = (ClassDescriptor) extents.next();

            // read same table only once

            // JC: the following (original) test did not work when the
            // previously added iterator was empty (something that is
            // tested when the iterator is added to the chaining
            // iterator), because in that case the iterator added is
            // simply ignored.  So, the following test will return
            // false and, thus, make repeated queries to the same
            // table when all the classes are mapped to the same
            // table.  Instead, we keep the table names used so far in
            // the tablesRead list and check it here.
            //if (chainingIter.containsIteratorForTable(extCld.getFullTableName()))
            if (tablesRead.contains(extCld.getFullTableName()))
            {
            }
            else
            {
                // add the iterator to the chaining iterator.
                chainingIter.addIterator(factory.createRsIterator(query, extCld, this));
                tablesRead.add(extCld.getFullTableName());
            }
        }

        return chainingIter;
    }


    static class FenixRsIteratorFactory extends RsIteratorFactoryImpl {
	private static RsIteratorFactory instance;

	synchronized static RsIteratorFactory getInstance() {
            if (instance == null) {
                instance = new FenixRsIteratorFactory();
            }

            return instance;
	}

	public RsIterator createRsIterator(Query query, ClassDescriptor cld, PersistenceBrokerImpl broker) {
            return new FenixRsIterator(RsQueryObject.get(cld, query), broker);
	}
    }

    static class FenixRsIterator extends RsIterator {
        FenixRsIterator(RsQueryObject queryObject, PersistenceBrokerImpl broker) {
            super(queryObject, broker);
        }

        protected Object getObjectFromResultSet() throws PersistenceBrokerException {
            ClassDescriptor cld = getQueryObject().getClassDescriptor();
            
            if (cld.getFactoryClass() != DomainObjectAllocator.class) {
                return super.getObjectFromResultSet();
            } else {
                ResultSet rs = getRsAndStmt().m_rs;
                return FenixJdbcAccessImpl.readObjectFromRs(rs);
            }
        }
    }
}
