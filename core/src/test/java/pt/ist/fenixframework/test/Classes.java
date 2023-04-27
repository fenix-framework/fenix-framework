package pt.ist.fenixframework.test;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.AbstractTransaction;
import pt.ist.fenixframework.core.AbstractTransactionManager;
import pt.ist.fenixframework.core.WriteOnReadError;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.txintrospector.TxIntrospector;
import pt.ist.fenixframework.txintrospector.TxStats;

import javax.transaction.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class Classes {
    public static class CustomTx implements Transaction {
        public void commit() throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException,
                javax.transaction.HeuristicRollbackException, java.lang.SecurityException, java.lang.IllegalStateException,
                javax.transaction.SystemException {
        }

        public boolean delistResource(javax.transaction.xa.XAResource xaResource, int i)
                throws java.lang.IllegalStateException, javax.transaction.SystemException {
            return true;
        }

        public boolean enlistResource(javax.transaction.xa.XAResource xaResource)
                throws javax.transaction.RollbackException, java.lang.IllegalStateException, javax.transaction.SystemException {
            return true;
        }

        public int getStatus() throws javax.transaction.SystemException {
            return 1;
        }

        public void registerSynchronization(javax.transaction.Synchronization synchronization)
                throws javax.transaction.RollbackException, java.lang.IllegalStateException, javax.transaction.SystemException {
        }

        public void rollback() throws java.lang.IllegalStateException, javax.transaction.SystemException {
        }

        public void setRollbackOnly() throws java.lang.IllegalStateException, javax.transaction.SystemException {
        }
    }

    public static final class MyDomainObject extends AbstractDomainObjectAdapter {

        private final DomainModel model;
        private java.lang.Comparable oid;

        public MyDomainObject(DomainModel model) {
            this.model = model;
            this.oid = new Comparable() {
                @Override
                public int compareTo(Object o) {
                    return 0;
                }
            };
        }

        @Override
        protected void ensureOid() {
        }

        @Override
        public java.lang.Comparable getOid() {
            return oid;
        }

        @Override
        protected void deleteDomainObject() {
            invokeDeletionListeners();
        }

        @Override
        protected DomainModel getDomainModel() {
            return model;
        }

        @Override
        public String toString() {
            return "CustomDomainObject" + model.toString();
        }

        @Override
        public String getExternalId() {
            return "CustomExternalId$" + oid.toString();
        }
    }

    public static class CustomSerializable implements Serializable {
        private String test;

        public CustomSerializable(String test) {
            this.test = test;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CustomSerializable) {
                return ((CustomSerializable) o).test.equals(this.test);
            }
            return false;
        }
    }

    public static class CustomTransaction extends AbstractTransaction {
        private TxStats introspector = TxStats.newInstance();
        private HashMap<String, Object> ctx = new HashMap<>();

        protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
                SecurityException, IllegalStateException, SystemException {
        }

        protected void backendRollback() throws IllegalStateException, SystemException {
        }

        public TxIntrospector getTxIntrospector() {
            return this.introspector;
        }

        public void putInContext(String key, Object value) {
            ctx.put(key, value);
        }

        public <T> T getFromContext(String key) {
            return (T) ctx.get(key);
        }
    }

    public static class CustomSync implements Synchronization {
        private int status = 0;

        public int getStatus() {
            return status;
        }

        public void beforeCompletion() {
            status = 1;
        }

        public void afterCompletion(int var1) {
            status = 2;
        }
    }

    public static class CustomTransactionManager extends AbstractTransactionManager {
        CustomTransaction tx;

        protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
                SecurityException, IllegalStateException, SystemException {
            tx.commit();
        }

        protected void backendRollback() throws IllegalStateException, SystemException {
            tx.rollback();
        }

        public pt.ist.fenixframework.Transaction getTransaction() {
            return this.tx;
        }

        public <T> T withTransaction(CallableWithoutException<T> command) {
            return command.call();
        }

        public <T> T withTransaction(Callable<T> command) throws Exception {
            return command.call();
        }

        public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
            return command.call();
        }

        public void begin(boolean readOnly) throws NotSupportedException, SystemException, WriteOnReadError {
            this.tx = new CustomTransaction();
        }

        public void resume(Transaction tx) {
        }

        public Transaction suspend() {
            return tx;
        }

        public void setTransactionTimeout(int timeout) {
        }
    }

    public static class CustomCommitListener implements CommitListener {
        private int status = 0;

        public void beforeCommit(pt.ist.fenixframework.Transaction transaction) {
            status = 1;
        }

        public void afterCommit(pt.ist.fenixframework.Transaction transaction) {
            status = 2;
        }

        public int getStatus() {
            return status;
        }
    }
}
