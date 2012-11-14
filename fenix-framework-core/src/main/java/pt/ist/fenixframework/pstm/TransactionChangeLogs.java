package pt.ist.fenixframework.pstm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import jvstm.ActiveTransactionsRecord;
import jvstm.VBoxBody;
import jvstm.util.Cons;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.accesslayer.LookupException;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import dml.DomainClass;

public class TransactionChangeLogs {
    
    static final String SQL_STMT_FORMAT = "SELECT OID from %s where ID_INTERNAL = %s";

    static DomainObject readDomainObject(PersistenceBroker pb, String className, int idInternal) {
	// This method now goes to DB directly through JDBC to get the OID
	// corresponding to className and idInternal.
	// It uses the standard way to load an OID calling fromExternalId which
	// goes through the lookup cache.

	final DomainClass domainClass = FenixFramework.getDomainModel().findClass(className);
	final String tableName = OJBMetadataGenerator.getExpectedTableName(domainClass);
	String oid = null;
	ResultSet result = null;
	Statement statement = null;
	try {
	    final Connection connection = pb.serviceConnectionManager().getConnection();
	    connection.commit();
	    statement = connection.createStatement();
	    result = statement.executeQuery(String.format(SQL_STMT_FORMAT, tableName, idInternal));
	    if (result.next()) {
		oid = result.getString(1);
	    }
	} catch (SQLException se) {
	    throw new Error(se);
	} catch (LookupException e) {
	    throw new Error(e);
	} finally {
	    try {
		if (result != null) {
		    result.close();
		}
		if (statement != null) {
		    statement.close();
		}
	    } catch (SQLException se) {
		throw new Error(se);
	    }
	}

	if (oid == null) {
	    return null;
	} else {
	    return AbstractDomainObject.fromExternalId(oid);
	}

    }


    // ------------------------------------------------------------

    private static class AlienTransaction {
	final int txNumber;

        // the set of objects is kept so that a strong reference exists 
        // for each of the objects modified by another server until no running 
        // transaction in the current VM may need to access it
	private Map<AbstractDomainObject,List<String>> objectAttrChanges = new HashMap<AbstractDomainObject,List<String>>();

	AlienTransaction(int txNumber) {
	    this.txNumber = txNumber;
	}

        void register(AbstractDomainObject obj, String attrName) {
            List<String> allAttrs = objectAttrChanges.get(obj);

            if (allAttrs == null) {
                allAttrs = new LinkedList<String>();
                objectAttrChanges.put(obj, allAttrs);
            }

            allAttrs.add(attrName);
        }

        Cons<VBoxBody> commit() {
            Cons<VBoxBody> newBodies = Cons.empty();

            for (Map.Entry<AbstractDomainObject,List<String>> entry : objectAttrChanges.entrySet()) {
                AbstractDomainObject obj = entry.getKey();
                List<String> allAttrs = entry.getValue();

                for (String attr : allAttrs) {
                    VBoxBody newBody = obj.addNewVersion(attr, txNumber);
                    // the body may be null in some cases: see the 
                    // comment on the VBox.addNewVersion method
                    if (newBody != null) {
                        newBodies = newBodies.cons(newBody);
                    }
                }
            }

            return newBodies;
        }
    }


    public static ActiveTransactionsRecord updateFromTxLogsOnDatabase(PersistenceBroker pb, 
                                                                      ActiveTransactionsRecord record) 
            throws SQLException,LookupException {

	return updateFromTxLogsOnDatabase(pb, record, false);
    }

    public static ActiveTransactionsRecord updateFromTxLogsOnDatabase(PersistenceBroker pb, 
                                                                      ActiveTransactionsRecord record, 
                                                                      boolean forUpdate) 
            throws SQLException,LookupException {

	Connection conn = pb.serviceConnectionManager().getConnection();

	// ensure that the connection is up-to-date
	conn.commit();
        
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
        
            // read tx logs
            int maxTxNumber = record.transactionNumber;

            rs = stmt.executeQuery("SELECT OBJ_OID,OBJ_ATTR,TX_NUMBER FROM FF$TX_CHANGE_LOGS WHERE TX_NUMBER > " 
                                   + (forUpdate ? (maxTxNumber - 1) : maxTxNumber)
                                   + " ORDER BY TX_NUMBER"
                                   + (forUpdate ? " FOR UPDATE" : ""));

            // if there are any results to be processed, process them
            if (rs.next()) {
                return processAlienTransaction(pb, rs, record);
            } else {
                return record;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }

            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private static ActiveTransactionsRecord processAlienTransaction(PersistenceBroker pb, ResultSet rs, ActiveTransactionsRecord record) 
            throws SQLException {

        // Acquire the JVSTM commit lock to process the result set, as
        // doing so is semantically similar to commiting transactions.
        // During the processing of an alien transaction, we may write
        // back to boxes, as well as update the most recent committed
        // record, so, as those things are supposed to be done one at
        // a time, during the commit, we must ensure that we acquire
        // the commit lock before proceeding.
        // This may be changed in the future, when the new version of
        // the JVSTM with a parallel commit becomes used in the
        // fenix-framework.
        Lock commitLock = TopLevelTransaction.getCommitlock();
        commitLock.lock();

        try {
            // Here, after acquiring the lock, we know that no new transactions can start, because
            // all transactions must call the updateFromTxLogsOnDatabase method with a number which 
            // is necessarily less than the number we are processing, and, therefore, will have to
            // come into this method, blocking in the lock.
            // Likewise for a commit of a write transaction.
            
            int currentCommittedNumber = Transaction.getMostRecentCommitedNumber();

            int txNum = rs.getInt(3);

            // skip all the records already processed
            while ((txNum <= currentCommittedNumber) && rs.next()) {
                txNum = rs.getInt(3);
            }

            if (txNum <= currentCommittedNumber) {
                // the records ended, so simply get out of here, with
                // the record corresponding to the higher number that
                // we got
                return findActiveRecordForNumber(record, txNum);
            }
            
            // now, it's time to process the new changeLog records

            AlienTransaction alienTx = new AlienTransaction(txNum);

            while (alienTx != null) {
                long oid = rs.getLong(1);
                String attr = rs.getString(2);

                if (oid != 0) {
                    // if the oid is 0, then this line
                    // doesn't represent a real change (see the
                    // comment on the DbChanges.writeAttrChangeLogs
                    // method)
                    AbstractDomainObject obj = AbstractDomainObject.fromOID(oid);
                    alienTx.register(obj, attr);
                }

                int nextTxNum = -1;
                if (rs.next()) {
                    nextTxNum = rs.getInt(3);
                }

                if (nextTxNum != txNum) {
                    // finished the records for an alien transaction, so "commit" it
                    Cons<VBoxBody> newBodies = alienTx.commit();

                    // add it to the queue of CommitRecords to be GCed later
                    TransactionCommitRecords.addCommitRecord(alienTx.txNumber, alienTx);

                    ActiveTransactionsRecord newRecord = new ActiveTransactionsRecord(txNum, newBodies);
                    Transaction.setMostRecentActiveRecord(newRecord);

                    if (nextTxNum != -1) {
                        // there are more to process, create a new alien transaction
                        txNum = nextTxNum;
                        alienTx = new AlienTransaction(txNum);
                    } else {
                        // finish the loop
                        alienTx = null;
                    }
                }
            }

            return findActiveRecordForNumber(record, txNum);
        } finally {
            commitLock.unlock();
        }
    }
    
    private static ActiveTransactionsRecord findActiveRecordForNumber(ActiveTransactionsRecord rec, int number) {
        while (rec.transactionNumber < number) {
            rec = rec.getNext();
        }

        return rec;
    }


    public static int initializeTransactionSystem() {
	// find the last committed transaction
	PersistenceBroker broker = null;

	try {
	    broker = PersistenceBrokerFactory.defaultPersistenceBroker();
	    broker.beginTransaction();

	    Connection conn = broker.serviceConnectionManager().getConnection();
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT MAX(TX_NUMBER) FROM FF$TX_CHANGE_LOGS");
	    int maxTx = (rs.next() ? rs.getInt(1) : -1);

	    broker.commitTransaction();
            broker.close();
            broker = null;

            stmt.close();
            rs.close();
            
            new CleanThread(maxTx).start();
            new StatisticsThread().start();

	    return maxTx;
	} catch (Exception e) {
	    throw new Error("Couldn't initialize the transaction system");
	} finally {
	    if (broker != null) {
		broker.close();
	    }
	}
    }

    private static class CleanThread extends Thread {
	private static final long SECONDS_BETWEEN_UPDATES = 120;

	private String server;
	private int lastTxNumber = -1;
	
	CleanThread(int lastTxNumber) {
            this.server = Util.getServerName();
            this.lastTxNumber = lastTxNumber;

	    setDaemon(true);
	}
	
        public void run() {
            try {
        	while (! initializeServerRecord()) {
        	    // intentionally empty
        	}
	    
        	while (true) {
        	    try {
        		sleep(SECONDS_BETWEEN_UPDATES * 1000);
        	    } catch (InterruptedException ie) {
        		return;
        	    }
        	    updateServerRecord();
        	}
            } finally {
        	System.out.println("Exiting CleanThread!");
        	System.err.flush();
        	System.out.flush();
            }
	}
	
	private boolean initializeServerRecord() {
	    PersistenceBroker broker = null;
	    
	    try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		broker.beginTransaction();
		
		Connection conn = broker.serviceConnectionManager().getConnection();
		Statement stmt = conn.createStatement();

		// delete previous record for this server and insert a new one
		stmt.executeUpdate("DELETE FROM FF$LAST_TX_PROCESSED WHERE SERVER = '" + server + "' or LAST_UPDATE < (NOW() - 3600)");
		stmt.executeUpdate("INSERT INTO FF$LAST_TX_PROCESSED VALUES ('" + server + "'," + lastTxNumber + ",null)");
		
		broker.commitTransaction();

                return true;
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("Couldn't initialize the clean thread");
		//throw new Error("Couldn't initialize the clean thread");
	    } finally {
		if (broker != null) {
		    broker.close();
		}
	    }

            return false;
	}
	
	private void updateServerRecord() {
	    int currentTxNumber = Transaction.getMostRecentCommitedNumber();

	    PersistenceBroker broker = null;
	    
	    try {
		broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		broker.beginTransaction();
		
		Connection conn = broker.serviceConnectionManager().getConnection();
		Statement stmt = conn.createStatement();

		// update record for this server
		stmt.executeUpdate("UPDATE FF$LAST_TX_PROCESSED SET LAST_TX=" 
				   + currentTxNumber 
				   + ",LAST_UPDATE=NULL WHERE SERVER = '" 
				   + server + "'");
		
		// delete obsolete values
		ResultSet rs = stmt.executeQuery("SELECT MIN(LAST_TX) FROM FF$LAST_TX_PROCESSED WHERE LAST_UPDATE > NOW() - " 
						 + (2 * SECONDS_BETWEEN_UPDATES));
		int min = (rs.next() ? rs.getInt(1) : 0);
		if (min > 0) {
		    stmt.executeUpdate("DELETE FROM FF$TX_CHANGE_LOGS WHERE TX_NUMBER < " + min);
		}

		broker.commitTransaction();

		this.lastTxNumber = currentTxNumber;
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("Couldn't update database in the clean thread");
		//throw new Error("Couldn't update database in the clean thread");
	    } catch (Throwable t) {
		t.printStackTrace();
		System.out.println("Couldn't update database in the clean thread because of a Throwable.");
	    } finally {
		if (broker != null) {
		    broker.close();
		}
	    }
	}
    }
}
