package pt.ist.fenixframework.backend.jvstmojb;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.core.AbstractTransactionManager;

public class JvstmOJBTransactionManager extends AbstractTransactionManager {

	private static final Logger logger = LoggerFactory.getLogger(JvstmOJBTransactionManager.class);

	/*
	 * JvstmOJBTransactions are stored in a thread local, because JVSTM's are also.
	 */
	private final ThreadLocal<JvstmOJBTransaction> transactions = new ThreadLocal<JvstmOJBTransaction>();

	@Override
	public void begin(boolean readOnly) throws NotSupportedException {
		if (transactions.get() != null) {
			throw new NotSupportedException("Nesting is not yet supported in JVSTM based backends");
		}

		logger.trace("Begin Transaction. Read Only: {}", readOnly);

		Transaction underlying = Transaction.begin(readOnly);

		transactions.set(new JvstmOJBTransaction((pt.ist.fenixframework.pstm.Transaction) underlying));
	}

	@Override
	public pt.ist.fenixframework.Transaction getTransaction() {
		return transactions.get();
	}

	@Override
	public void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
		try {
			logger.trace("Committing Transaction");

			// Note that no null check is needed, because a
			// check has been made in the super-class.
			transactions.get().commit();
		} finally {
			transactions.remove();
		}
	}

	@Override
	public void backendRollback() throws SystemException {
		try {
			logger.trace("Rolling Back Transaction");

			// Note that no null check is needed, because a
			// check has been made in the super-class.
			transactions.get().rollback();
		} finally {
			transactions.remove();
		}

	}

	@Override
	public <T> T withTransaction(CallableWithoutException<T> command) {
		try {
			return withTransaction((Callable<T>) command);
		} catch (Exception e) {
			throw new RuntimeException("Exception ocurred while running transaction", e);
		}
	}

	@Override
	public <T> T withTransaction(Callable<T> command) throws Exception {
		return withTransaction(command, null /* new AtomicInstance()*/);
	}

	@Override
	public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
		if (pt.ist.fenixframework.pstm.Transaction.isInTransaction()) {
			logger.trace("Flattening call to withTransaction - Already inside a transaction");
			return command.call();
		}

		pt.ist.fenixframework.pstm.Transaction.begin();
		try {
			return command.call();
		} finally {
			pt.ist.fenixframework.pstm.Transaction.commit();
		}
	}

	@Override
	public void resume(javax.transaction.Transaction tobj) throws InvalidTransactionException, IllegalStateException,
	SystemException {
		if (!(tobj instanceof JvstmOJBTransaction)) {
			throw new InvalidTransactionException("Expected JvstmOJBTransaction, got " + tobj);
		}

		if (transactions.get() != null) {
			throw new IllegalStateException("Already associated with a transaction!");
		}

		JvstmOJBTransaction tx = (JvstmOJBTransaction) tobj;

		Transaction.resume(tx.getUnderlyingTransaction());
		transactions.set(tx);
	}

	@Override
	public JvstmOJBTransaction suspend() {
		JvstmOJBTransaction current = transactions.get();

		if (current == null) {
			return current;
		}

		Transaction.suspend();
		transactions.remove();

		return current;
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		throw new UnsupportedOperationException("Transaction timeouts are not supported in JVSTM");
	}

}
