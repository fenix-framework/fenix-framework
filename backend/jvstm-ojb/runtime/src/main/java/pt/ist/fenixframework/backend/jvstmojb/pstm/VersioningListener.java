package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.transaction.Status;

import org.apache.ojb.broker.accesslayer.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBTransaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

/**
 * Copyright © 2013 Quorum Born IT | www.qub-it.com
 *
 * This file is part of Fenix Framework.
 *
 * Fenix Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fenix Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fenix Framework. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Paulo Abrantes (paulo.abrantes@qub-it.com)
 */
public class VersioningListener implements CommitListener {

    private static final Logger logger = LoggerFactory.getLogger(VersioningListener.class);

    private static final String CONNECTION = VersioningListener.class.getSimpleName() + ".connection";
    private static final String TX_NUMBER = VersioningListener.class.getSimpleName() + ".txNumber";
    private static final String UPDATE_ENTITY = VersioningListener.class.getSimpleName() + ".updateEntity";
    private static final String LOGGED_CLASSES = VersioningListener.class.getSimpleName() + ".loggedClasses";
    private static final String CREATED = VersioningListener.class.getSimpleName() + ".created";
    private static final String MODIFIED = VersioningListener.class.getSimpleName() + ".modified";
    private static final String DELETED = VersioningListener.class.getSimpleName() + ".deleted";

    private static Field UNDERLYING_TRANSACTION;
    static {
        try {
            UNDERLYING_TRANSACTION = JvstmOJBTransaction.class.getDeclaredField("underlyingTransaction");
            UNDERLYING_TRANSACTION.setAccessible(true);
        } catch (Exception e) {
            UNDERLYING_TRANSACTION = null;
            e.printStackTrace();
        }
    }

    //
    // It seems FF2 no longer as a TX active in afterCommit so we have 
    // to gather all the information beforeCommit but only after commit
    // use the connection to write it. 
    //
    // Since the connection is also associated with the Tx and they clean
    // it we also have to keep the connection link from the beforeCommit
    // and hopefully be able to write to it through there.
    //
    // 21 February 2014 - Paulo Abrantes
    @Override
    public void beforeCommit(final Transaction transaction) {
        final TxIntrospector txIntrospector = transaction.getTxIntrospector();
        if (txIntrospector.isWriteTransaction()) {

            final TopLevelTransaction topLevel = getHackedUnderlyingTransaction(transaction);
            final Integer txNumber = topLevel.getNumber();

            // legidio, just for report purposes
            String updateEntity = "unknown:unknown";
            final Set<Class<? extends DomainObject>> loggedClasses = new HashSet<Class<? extends DomainObject>>();

            final Map<DomainObject, Map<String, Object>> modified = new HashMap<DomainObject, Map<String, Object>>();
            final Map<DomainObject, Map<String, Object>> deleted = new HashMap<DomainObject, Map<String, Object>>();
            for (final Iterator<DomainObject> iterator = txIntrospector.getModifiedObjects().iterator(); iterator.hasNext();) {
                // TODO: Casting to OneBoxDomainObject where getUpdateEntiy was inserted. Check best way to do it!
                final OneBoxDomainObject domainObject = (OneBoxDomainObject) iterator.next();

                if (isVersionedActive(domainObject.getClass())) {
                    if (domainObject.getUpdateEntity() != null) {
                        updateEntity = domainObject.getUpdateEntity().externalize();
                    }
                    loggedClasses.add(domainObject.getClass());

                    if (txIntrospector.isDeleted(domainObject)) {
                        deleted.put(domainObject, domainObject.get$auditInfo());
                    } else {
                        modified.put(domainObject, domainObject.get$auditInfo());
                    }
                }
            }

            final Map<DomainObject, Map<String, Object>> created = new HashMap<DomainObject, Map<String, Object>>();
            for (final Iterator<DomainObject> iterator = txIntrospector.getNewObjects().iterator(); iterator.hasNext();) {
                // TODO: Casting to OneBoxDomainObject where getUpdateEntiy was inserted. Check best way to do it!
                final OneBoxDomainObject domainObject = (OneBoxDomainObject) iterator.next();

                if (isVersionedActive(domainObject.getClass())) {
                    updateEntity = domainObject.getUpdateEntity().externalize();
                    loggedClasses.add(domainObject.getClass());

                    created.put(domainObject, domainObject.get$auditInfo());
                }
            }

            try {
                transaction.putInContext(CONNECTION, topLevel.getOJBBroker().serviceConnectionManager().getConnection());
            } catch (LookupException e) {
                e.printStackTrace();
            }
            transaction.putInContext(TX_NUMBER, txNumber);
            transaction.putInContext(UPDATE_ENTITY, updateEntity);
            transaction.putInContext(LOGGED_CLASSES, loggedClasses);
            transaction.putInContext(MODIFIED, modified);
            transaction.putInContext(CREATED, created);
            transaction.putInContext(DELETED, deleted);
        }
    }

    // TODO: fix the need for the versioning handler in ff
    //
    //
    private static boolean isVersionedActive(Class clazz) {
        return true;
    }

    static private TopLevelTransaction getHackedUnderlyingTransaction(final Transaction input) {
        TopLevelTransaction result = null;

        if (UNDERLYING_TRANSACTION != null) {
            try {
                result = (TopLevelTransaction) UNDERLYING_TRANSACTION.get(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public void afterCommit(final Transaction transaction) {
        final Integer txNumber = transaction.getFromContext(TX_NUMBER);
        if (txNumber != null) {

            try {
                if (transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ACTIVE) {
                    final Connection connection = transaction.getFromContext(CONNECTION);

                    String updateEntity = transaction.getFromContext(UPDATE_ENTITY);
                    final Set<Class<? extends DomainObject>> loggedClasses = transaction.getFromContext(LOGGED_CLASSES);

                    final Map<DomainObject, Map<String, Object>> modified = transaction.getFromContext(MODIFIED);
                    final Map<DomainObject, Map<String, Object>> created = transaction.getFromContext(CREATED);
                    final Map<DomainObject, Map<String, Object>> deleted = transaction.getFromContext(DELETED);
                    final int modifiedSize = modified != null ? modified.size() : 0;
                    final int createdSize = created != null ? created.size() : 0;
                    final int deletedSize = deleted != null ? deleted.size() : 0;

                    if (modifiedSize == 0 && createdSize == 0 && deletedSize == 0) {

                    } else if (connection == null || connection.isClosed()) {

                    } else {

                        VersioningHandler.startLog(connection);
                        for (Entry<DomainObject, Map<String, Object>> entry : modified.entrySet()) {
                            processEdit(entry.getKey(), entry.getValue(), txNumber);
                        }
                        for (Entry<DomainObject, Map<String, Object>> entry : created.entrySet()) {
                            processCreated(entry.getKey(), entry.getValue(), txNumber);
                        }
                        for (Entry<DomainObject, Map<String, Object>> entry : deleted.entrySet()) {
                            processDelete(entry.getKey(), entry.getValue(), txNumber);
                        }
                    }

                } else {
                }

            } catch (final Throwable t) {
                t.printStackTrace();
            } finally {
                VersioningHandler.endLog();

                transaction.putInContext(CONNECTION, null);
                transaction.putInContext(TX_NUMBER, null);
                transaction.putInContext(UPDATE_ENTITY, null);
                transaction.putInContext(LOGGED_CLASSES, null);
                transaction.putInContext(MODIFIED, null);
                transaction.putInContext(CREATED, null);
                transaction.putInContext(DELETED, null);
            }

        } else {
        }
    }

    private void processCreated(DomainObject domainObject, Map<String, Object> auditInfo, int txNumber) {
        VersioningHandler.logCreate(txNumber, domainObject.getClass().getName(), domainObject.getExternalId(), auditInfo);
    }

    private void processEdit(DomainObject domainObject, Map<String, Object> auditInfo, int txNumber) {
        VersioningHandler.logUpdate(txNumber, domainObject.getClass().getName(), domainObject.getExternalId(), auditInfo);
    }

    private void processDelete(DomainObject domainObject, Map<String, Object> auditInfo, int txNumber) {
        VersioningHandler.logDelete(txNumber, domainObject.getClass().getName(), domainObject.getExternalId(), auditInfo);
    }
}