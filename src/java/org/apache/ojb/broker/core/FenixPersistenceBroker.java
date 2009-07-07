package org.apache.ojb.broker.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.accesslayer.JdbcAccess;
import org.apache.ojb.broker.accesslayer.OJBIterator;
import org.apache.ojb.broker.accesslayer.PagingIterator;
import org.apache.ojb.broker.accesslayer.RsIterator;
import org.apache.ojb.broker.accesslayer.RsQueryObject;
import org.apache.ojb.broker.accesslayer.ChainingIterator;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryBySQL;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;
import org.apache.ojb.broker.util.ClassHelper;

import pt.ist.fenixframework.pstm.ojb.FenixJdbcAccessImpl;
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
    public void retrieveReference(Object pInstance, String pAttributeName) throws PersistenceBrokerException {
        ClassDescriptor cld = getClassDescriptor(pInstance.getClass());
        CollectionDescriptor cod = cld.getCollectionDescriptorByName(pAttributeName);
        if (cod != null) {
            super.retrieveReference(pInstance, pAttributeName);
            // when we dispatch to the super, then we're done
            return;
        }

        ObjectReferenceDescriptor ord = cld.getObjectReferenceDescriptorByName(pAttributeName);
        if (ord == null) {
            throw new PersistenceBrokerException("did not find attribute " + pAttributeName +
                                                 " for class " + pInstance.getClass().getName());
        }

        Object refObj = null;

        Object[] fkValues = ord.getForeignKeyValues(pInstance, cld);
        if (fkValues[0] != null) {
            // we have some non-null foreign key

            // ensure that top-level extents are used for Identities
            Identity id = new Identity(ord.getItemClass(), getTopLevelClass(ord.getItemClass()), fkValues);
            refObj = doGetObjectByIdentity(id);
        }

        // finally, set the field to the referenced object
        ord.getPersistentField().set(pInstance, refObj);
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

        // BRJ: add base class iterator
        if (!cld.isInterface())
        {

            chainingIter.addIterator(factory.createRsIterator(query, cld, this));
        }

        Iterator extents = getDescriptorRepository().getAllConcreteSubclassDescriptors(cld).iterator();
        while (extents.hasNext())
        {
            ClassDescriptor extCld = (ClassDescriptor) extents.next();

            // read same table only once
            if (chainingIter.containsIteratorForTable(extCld.getFullTableName()))
            {
            }
            else
            {
                // add the iterator to the chaining iterator.
                chainingIter.addIterator(factory.createRsIterator(query, extCld, this));
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
