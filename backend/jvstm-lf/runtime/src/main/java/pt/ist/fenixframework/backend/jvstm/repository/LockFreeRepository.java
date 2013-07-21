/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.repository;

import static jvstm.UtilUnsafe.UNSAFE;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import jvstm.Transaction;
import jvstm.UtilUnsafe;
import jvstm.VBox.Offsets;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeConfig;
import pt.ist.fenixframework.backend.jvstm.lf.SimpleWriteSet;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VersionedValue;
import pt.ist.fenixframework.core.Externalization;

/**
 * This class implements the Repository interface using a pluggable dataGrid. It extends the Repository with additional methods.
 */

/*
 * Format for the keys stored:
 * 
 * * <vboxid>":"<commitId>: contains the value written for that commit.  It also holds de version and the previous version
 * * <txVersion>: contains the commit id for the given txVersion (the key is an integer!)
 * * <vboxid>: the most recent consolidated version for *this* vboxid
 * * ":LCV:": The absolute last consolidated tx version (always >= than the consolidated version on any vbox)
 * * ":"<commitId>":": The write set of such commit Id (list of vbox ids)
 * * <serverId>":"<classId>: The highest counter for class instances of the given class in the given node (updated upon commits from this node only)
 * 
 *  Commit Ids are uuids, txVersions are integer numbers. vboxids should not contains the character ':'.
 */
public class LockFreeRepository implements ExtendedRepository {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeRepository.class);

    // this is a marker key, so that when bootstrapping the repository, we can identify whether it already exists 
    private static final String CACHE_IS_NEW = ":CacheAlreadExists:";

    // the key used to store the DomainClassInfo instances.
    private static final String DOMAIN_CLASS_INFO = ":DomainClassInfo:";

    /**
     * Key for storing the last consolidated transaction version. All transaction versions up to and including this one, have been
     * consolidated in their respective keys.
     */
    private static final String LAST_CONSOLIDATED_VERSION = ":LCV:";

    /**
     * Number of attempts to perform a repository transaction
     */
    private static final int MAX_TX_ATEMPTS = 10;

    private static final long bodyValueOffset = UtilUnsafe.objectFieldOffset(VBoxBody.class, "value");

    /* this is replaced with a method that needs to search the existing entries to decide the nonCommitted tx id*/
//    // the key used to store the nonCommitted committed transaction number
//    private static final String MAX_COMMITTED_TX_ID = "maxTxId";

//    // key to store context information in the (JVSTM's) Transaction
//    private static final String KEY_INSTANTIATED_CLASSES = ":Set<DomainClassInfo>:";

    private DataGrid dataGrid;

//    private int maxCommittedTxId = -1;

    @Override
    public boolean init(JVSTMConfig jvstmConfig) {
        try {
            initConcreteDataGrid((JvstmLockFreeConfig) jvstmConfig);
        } catch (Exception e) {
            logger.error("Failed to initialize data grid: {}", e);
            throw new RuntimeException(e);
        }

        return bootstrapIfNeeded();
    }

    @Override
    public DomainClassInfo[] getDomainClassInfos() {
        return doWithinBackingTransactionIfNeeded(new Callable<DomainClassInfo[]>() {
            @Override
            public DomainClassInfo[] call() {
                DomainClassInfo infos[] = (DomainClassInfo[]) dataGrid.get(DOMAIN_CLASS_INFO);

                if (infos == null) {
                    return new DomainClassInfo[0];
                }

                return infos;
            }
        });
    }

    @Override
    public void storeDomainClassInfos(DomainClassInfo[] newDomainClassInfos) {
        if (newDomainClassInfos == null || newDomainClassInfos.length == 0) {
            return;
        }

        int i, j;
        final DomainClassInfo[] all, stored = getDomainClassInfos();
        all = new DomainClassInfo[stored.length + newDomainClassInfos.length];
        for (j = 0; j < stored.length; j++) {
            all[j] = stored[j];
        }

        for (i = 0; i < newDomainClassInfos.length; i++, j++) {
            all[j] = newDomainClassInfos[i];
        }

        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                dataGrid.put(DOMAIN_CLASS_INFO, all);
                return null;
            }
        });
    }

    @Override
    public int getMaxCounterForClass(DomainClassInfo domainClassInfo) {
        final String key = makeKeyForMaxCounter(domainClassInfo);

        return doWithinBackingTransactionIfNeeded(new Callable<Integer>() {
            @Override
            public Integer call() {
                Integer nonCommitted = (Integer) dataGrid.get(key);

                if (nonCommitted == null) {
                    return -1;
                }

                return nonCommitted;
            }
        });
    }

    @Override
    public void updateMaxCounterForClass(DomainClassInfo domainClassInfo, int newCounterValue) {
        // TODO Auto-generated method stub
        logger.warn("updateMaxCounterForClass() not yet implemented");
//        throw new UnsupportedOperationException("not yet implemented");

//        Transaction current = FenixFramework.getTransaction();
//
//        Set<DomainClassInfo> infos = current.getFromContext(KEY_INSTANTIATED_CLASSES);
//        if (infos == null) {
//            infos = new HashSet<DomainClassInfo>();
//            current.putInContext(KEY_INSTANTIATED_CLASSES, infos);
//        }
//
//        if (infos.add(domainClassInfo)) {
//            logger.debug("Will update counter for instances of {} upon commit.", domainClassInfo.domainClassName);
//        }
    }

    @Override
    public void reloadPrimitiveAttribute(VBox box) {
        reloadAttribute(box);
    }

    @Override
    public void reloadReferenceAttribute(VBox box) {
        reloadAttribute(box);
    }

    // stores persistently a set of changes
    // the third arguments represents the reference used by the stm to represent null objects.
    @Override
    public void persistChanges(final Set<Entry<jvstm.VBox, Object>> changes, final int txNumber, final Object nullObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");

//        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
//            @Override
//            public Void call() {
//                updatePersistentInstanceCounters();
//                persistCommittedTransactionNumber(txNumber);
//
//                for (Entry<jvstm.VBox, Object> entry : changes) {
//                    VBox vbox = (VBox) entry.getKey();
//                    Object newValue = entry.getValue();
//
//                    newValue = (newValue == nullObject) ? null : newValue;
//
//                    String key = makeKeyFor(vbox);
//                    DataVersionHolder current = (DataVersionHolder) dataGrid.get(key);
//                    DataVersionHolder newVersion;
//                    byte[] externalizedData = Externalization.externalizeObject(newValue);
//
//                    if (current != null) {
//                        dataGrid.put(makeVersionedKey(key, current.version), current); // TODO: colocar aqui um timeout ?
//                        newVersion = new DataVersionHolder(txNumber, current.version, externalizedData);
//                    } else {
//                        newVersion = new DataVersionHolder(txNumber, -1, externalizedData);
//                    }
//
//                    dataGrid.put(key, newVersion); // TODO: colocar aqui um timeout
//                }
//                return null;
//            }
//        });
    }

    // returns the highest committed transaction number stored in the data grid
    @Override
    public synchronized int getMaxCommittedTxNumber() {
        return doWithinBackingTransactionIfNeeded(new Callable<Integer>() {

            class Interval {
                int committed;
                int nonCommitted;

                Interval(int committed, int nonCommitted) {
                    this.committed = committed;
                    this.nonCommitted = nonCommitted;
                }
            }

            @Override
            public Integer call() {
                Integer lcv = LockFreeRepository.this.dataGrid.get(LAST_CONSOLIDATED_VERSION);
                int minValue = (lcv == null) ? 0 : lcv;

                Interval interval = new Interval(minValue, Integer.MAX_VALUE);

                logger.debug("looking up highest tx number in range [{};{}]", interval.committed, interval.nonCommitted);
                return computeHighestCommittedTxNumber(interval);
            }

            private int computeHighestCommittedTxNumber(Interval interval) {
                if (interval.committed + 1 == interval.nonCommitted) {
                    // they are already contiguous
                    logger.debug("Found highest committed tx number: {}", interval.committed);
                    return interval.committed;
                }

                int delta = 1;
                int lastCommitted = interval.committed;

                while (true) {
                    int attempt = lastCommitted + delta;
                    if (attempt < 0) { // overflow
                        logger.debug("overflow. restarting with [{};{}]", interval.committed, interval.nonCommitted);
                        return computeHighestCommittedTxNumber(interval);
                    }

                    Object value = LockFreeRepository.this.dataGrid.get(attempt);

                    if (value == null) {
                        interval.nonCommitted = attempt;

                        logger.debug("got null. reducing higher value [{}; {}]", interval.committed, interval.nonCommitted);

                        return computeHighestCommittedTxNumber(interval);
                    } else {
                        interval.committed = attempt;

                        logger.debug("still committed. increasing lower value [{}; {}]", interval.committed,
                                interval.nonCommitted);

                        int nextDelta = delta << 1;
                        if (nextDelta < 0) { // overflow
                            logger.debug("overflow. restarting with [{};{}]", interval.committed, interval.nonCommitted);

                            return computeHighestCommittedTxNumber(interval);
                        } else {
                            delta = nextDelta;
                        }
                    }
                }

            }
        });
    }

    // close the connection to the repository
    @Override
    public void closeRepository() {
        logger.info("closeRepository()");
        this.dataGrid.stop();
        this.dataGrid = null;
//        maxCommittedTxId = -1;
    }

    /* methods used by the implementation of the Repository interface methods */

    /* This method is used to wrap every access to the dataGrid in a transaction.
    There should never be a (transactional) conflict when trying to write to the
    repository.  However, transactions may fail spuriously, e.g. in Infinispan
    when a node leaves a tx may fail to commit.  So we retry a few times, before
    deciding to fail. */
    private <T> T doWithinBackingTransactionIfNeeded(Callable<T> command) {
        int attempts = 1;

        while (true) {

            boolean inTopLevel = false;
            boolean commandFinished = false;
            try {
                if (!dataGrid.inTransaction()) {
                    dataGrid.beginTransaction();
                    inTopLevel = true;
                }

                T result = command.call();
                commandFinished = true;

                return result;
            } catch (Exception e) {
                if (attempts++ < MAX_TX_ATEMPTS) {
                    logger.warn("Will retry (attemp {}) failed transaction: {}", attempts, e);
                } else {
                    logger.error("Giving up. Could not perform data grid operation.");
                    throw new PersistenceException(e);
                }
            } finally {
                if (inTopLevel) {
                    try {
                        if (commandFinished) {
                            dataGrid.commitTransaction();
                        } else {
                            dataGrid.rollbackTransaction();
                        }
                    } catch (Exception e) {
                        String op = (commandFinished ? "commit" : "rollback");

                        // don't increment attempts if they were already incremented in command's catch
                        if (commandFinished && (attempts++ < MAX_TX_ATEMPTS)) {
                            logger.warn("Will retry (attempt {}) failed {}: {}", attempts, op, e);
                        } else {
                            logger.error("Giving up. Could not {} to data grid", op);
                            throw new PersistenceException(e);
                        }
                    }
                }
            }

        }
    }

    private void initConcreteDataGrid(JvstmLockFreeConfig jvstmLockFreeConfig) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        String dataGridClassName = jvstmLockFreeConfig.getDatagridClassName();
        Class<? extends DataGrid> dataGridClass = Class.forName(dataGridClassName).asSubclass(DataGrid.class);
        this.dataGrid = dataGridClass.newInstance();

        this.dataGrid.init(jvstmLockFreeConfig);
    }

    private boolean bootstrapIfNeeded() {
        return doWithinBackingTransactionIfNeeded(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (dataGrid.get(CACHE_IS_NEW) == null) {
                    dataGrid.put(CACHE_IS_NEW, "false");
                    logger.info("Initialization marker not present. Data Drid is being initialized for the first time.");
                    return true;
                } else {
                    logger.info("Initialization marker is present. Data Grid already existed.");
                    return false;
                }
            }
        });
    }

    private String makeKeyForMaxCounter(DomainClassInfo domainClassInfo) {
        return String.valueOf(DomainClassInfo.getServerId()) + ":" + domainClassInfo.classId;
    }

    /* Algorithm: when reloadAttribute is invoked we know that at some point in
    the list of bodies there was a ref to <0,NOT_LOADED_VALUE,null>. Such ref
    will have to change to point to <requiredVersion,somevalue>--><0,NOT_LOADED_VALUE>.
    The invariant is:  the existing list of bodies never contains gaps between
    its elements; the last element is <0,NOT_LOADED_VALUE> to represent that
    from the last-but-one down to this, the values are not loaded. (Corollary,
    If <0,NOT_LOADED_VALUE> is at the head, then nothing is actually loaded).
    So, to maintain the invariant it is not enough to load the missing value:
    we also need to load all values from the last known down to the missing one.
    Only then can this newly created list be CAS-ed onto the previous ref to
    <0,NOT_LOADED_VALUE>. Note however, that a 'valid' version is different from
    a 'loaded' version.  We may allow bodies to contain the NOT_LOADED_VALUE if
    their version is not 0. */
    @Override
    public void reloadAttribute(VBox box) {
        int txNumber = jvstm.Transaction.current().getNumber();

        boolean success = false;

        VBoxBody oldestValidBody = box.getOldestValidBody();

        int highestVersionToLoad;
        if (oldestValidBody == null) {
            highestVersionToLoad = Transaction.mostRecentCommittedRecord.transactionNumber;
        } else {
            highestVersionToLoad = oldestValidBody.version - 1;
        }

        if (txNumber > highestVersionToLoad) {
            logger.debug("Version {} for vbox {} is already loaded", txNumber, box.getId());
            return;
        }

        logger.debug("Will load versions [{};{}] for vbox {}", highestVersionToLoad, txNumber, box.getId());

        VBoxBody tail = loadVersionsInRange(box, highestVersionToLoad, txNumber);

        replaceTail(box, oldestValidBody, tail);
    }

    @Override
    public void reloadAttributeSingleVersion(VBox box, jvstm.VBoxBody body) {
        int versionToLoad = body.version;

        // find the commitId of this committed version
        String commitId = getCommitIdForVersion(versionToLoad);

        // lookup an entry for this box in the given version
        String key = makeKeyWithCommitId(makeKeyFor(box), commitId);

        logger.debug("looking up key {} (tx version={})", key, versionToLoad);

        DataVersionHolder entry = LockFreeRepository.this.dataGrid.get(key);

        replaceBodyValue(body, Externalization.internalizeObject(entry.data));
    }

    @Override
    public void persistWriteSet(final UUID commitId, final SimpleWriteSet writeSet, final Object nullObject) {
        /* Store CommitId-->List<VBoxId> and VBoxId:CommitId-->Value */

        logger.debug("persistWriteSet with commitId={{}}, writeSet={{}}", commitId, writeSet);

        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
//                updatePersistentInstanceCounters();
//                persistCommittedTransactionNumber(txNumber);

                // store the list of vboxids in commitId

                String[] vBoxIds = writeSet.getVboxIds();
                int size = vBoxIds.length;
                LockFreeRepository.this.dataGrid.put(makeKeyFor(commitId), vBoxIds);

                // store each value associated with vboxid:commitId
                Object[] values = writeSet.getValues();

                for (int i = 0; i < size; i++) {
                    String vboxId = vBoxIds[i];
                    Object newValue = (values[i] == nullObject) ? null : values[i];

                    String key = makeKeyWithCommitId(vboxId, commitId.toString());

                    byte[] externalizedValue = Externalization.externalizeObject(newValue);
                    DataVersionHolder newVersion = new DataVersionHolder(externalizedValue);

                    LockFreeRepository.this.dataGrid.put(key, newVersion);
                }

                return null;
            }
        });
    }

    @Override
    public void mapTxVersionToCommitId(final int txVersion, final UUID commitId) {
        /* Store txVersion --> commitId */

        logger.debug("mapTxVersionToCommitId: {{}}->{{}}", txVersion, commitId);

        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                LockFreeRepository.this.dataGrid.putIfAbsent(txVersion, commitId.toString());
                return null;
            }
        });
    }

    @Override
    public String getCommitIdFromVersion(final int txVersion) {
        return doWithinBackingTransactionIfNeeded(new Callable<String>() {
            @Override
            public String call() {
                String commitId = getCommitIdForVersion(txVersion);
                logger.debug("getCommitIdFromVersion: {{}}->{{}}", txVersion, commitId);
                return commitId;
            }
        });
    };

    List<VersionedValue> getMostRecentVersions(final VBox vbox, final int desiredVersion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");

//        final String key = makeKeyFor(vbox);
//
//        return doWithinBackingTransactionIfNeeded(new Callable<List<VersionedValue>>() {
//            @Override
//            public List<VersionedValue> call() {
//                ArrayList<VersionedValue> result = new ArrayList<VersionedValue>();
//                DataVersionHolder current;
//
//                current = (DataVersionHolder) dataGrid.get(key);
//
//                if (current != null) {
//
//                    while (true) {
//                        result.add(new VersionedValue(Externalization.internalizeObject(current.data), current.version));
//
//                        if (current.version <= desiredVersion) {
//                            return result;
//                        }
//
//                        if (current.previousVersion == -1) {
//                            break;
//                        }
//
//                        current = (DataVersionHolder) dataGrid.get(makeVersionedKey(key, current.previousVersion));
//                    }
//                }
//                throw new PersistenceException("Version of vbox " + vbox.getId() + " not found for transaction number "
//                        + desiredVersion);
//            }
//        });
    }

    // persist the number of the committed transaction. Maybe this should be made differently.
    // It may abort transactions because they try to change this same slot.
    private void persistCommittedTransactionNumber(final int txNumber) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");

//        // there might be some synchronization issues concerning maxCommittedTxId
//        if (txNumber > this.maxCommittedTxId) {
//            this.maxCommittedTxId = txNumber;
//
//            doWithinBackingTransactionIfNeeded(new Callable<Void>() {
//                @Override
//                public Void call() {
//
//                    dataGrid.put(MAX_COMMITTED_TX_ID, new Integer(maxCommittedTxId));
//                    return null;
//                }
//            });
//        }
    }

    // only correct if invoked within a backing transaction.  Also this code
    // assumes a single global commit lock. Otherwise the counter might be set
    // backwards by a late-running thread.
    private void updatePersistentInstanceCounters() {
        // TODO Auto-generated method stub
        logger.warn("updatePersistentInstanceCounters() not yet implemented");
//        throw new UnsupportedOperationException("not yet implemented");

//        Transaction current = FenixFramework.getTransaction();
//
//        Set<DomainClassInfo> infos = current.getFromContext(KEY_INSTANTIATED_CLASSES);
//
//        if (infos != null) {
//            for (DomainClassInfo info : infos) {
//                String key = makeKeyForMaxCounter(info);
//                Integer nonCommitted = (Integer) dataGrid.get(key);
//
//                int newCounterValue = info.getLastKey();
//
//                if (nonCommitted == null || nonCommitted < newCounterValue) {
//                    dataGrid.put(key, newCounterValue);
//                    logger.debug("Update persistent counter for class {}: {}", info.domainClassName, newCounterValue);
//                }
//
//            }
//
//        }
    }

    /* Load existing versions between versionToLoad and txNumber.  The box may
    not have been written in the given versions.  Thus, if it is necessary to
    satisfy the requirements, the returned list of bodies must include a version
    lower than txNumber. */
    @SuppressWarnings("unchecked")
    private VBoxBody loadVersionsInRange(VBox box, int versionToLoad, int txNumber) {
        // find the commitId of this committed version
        String commitId = getCommitIdForVersion(versionToLoad);

//        logger.debug("version {} as commitId {}", versionToLoad, commitId);

        // lookup an entry for this box in the given version
        String key = makeKeyWithCommitId(makeKeyFor(box), commitId);

        logger.debug("looking up key {} (tx version={})", key, versionToLoad);

        DataVersionHolder entry = LockFreeRepository.this.dataGrid.get(key);
        if (entry != null) {
            logger.debug("Adding to list the value for key: {}", key);

            VBoxBody nextBody;

            if (versionToLoad <= txNumber) { // end recursion
                nextBody = VBox.notLoadedBody();
            } else {
                nextBody = loadVersionsInRange(box, versionToLoad - 1, txNumber);
            }

            return VBox.makeNewBody(Externalization.internalizeObject(entry.data), versionToLoad, nextBody);
        } else {
            if (versionToLoad == 0) {
                throw new PersistenceException("Version of vbox " + box.getId() + " not found for transaction number " + txNumber);
            }
            logger.debug("No such key: {}", key);
            return loadVersionsInRange(box, versionToLoad - 1, txNumber);
        }
    }

    private String getCommitIdForVersion(int versionToLoad) {
        String commitId = LockFreeRepository.this.dataGrid.get(versionToLoad);
        return commitId;
    }

    /* merge the given tail into the given body.  This method changes the final
    field that points to a VBody (either in the VBox.body or in VBoxBody.next.
    However, it only does so when the reference is to the NOT_LOADED_BODY.  In
    the worst case, if someone misses the change it will trigger a potentially
    unnecessary reload of the box. This method could be in the VBox class.  It's
    here because we're using JVSTM2 and pstm.VBox isn't aware of it.*/
    /**
     * Replace the reference to {@link VBox#NOT_LOADED_BODY} with a reference to the given tail. This method will only succeed
     * when replacing references to <code>null</code> or {@link VBox#NOT_LOADED_BODY} with some other tail.
     * 
     * @param body The body whose tail we need to replace. If it is null then, replace this VBox's body instead.
     * @param tail The reference to replace. Should end with {@link VBox#NOT_LOADED_BODY} (this method does not check for it).
     */
    protected void replaceTail(VBox box, VBoxBody body, VBoxBody tail) {
        if (body == null) {
            replaceTailInVBox(box, tail);
        } else {
            replaceTailInBody(body, tail);
        }
    }

    protected void replaceBodyValue(VBoxBody body, Object value) {
        if (UNSAFE.compareAndSwapObject(body, bodyValueOffset, VBox.notLoadedValue(), value)) {
            logger.debug("Set value for body in version {}", body.version);
        } else {
            logger.debug("Value for body in version {} was already set", body.version);
        }
    }

    private void replaceTailInVBox(VBox box, VBoxBody tail) {
        VBoxBody current = box.body;

        if (isBodyValid(current)) {
            updateAndReplaceTailInBody(current, tail);
        } else if (!UNSAFE.compareAndSwapObject(box, Offsets.bodyOffset, current, tail)) {
            /* we re-read box.body instead of using current because current might
            be null.  That's in theory.  In practice I think that it will always
            contains a NOT_LOADED_VALUE in version 0.*/
            updateAndReplaceTailInBody(box.body, tail);
        }
    }

    private boolean isBodyValid(VBoxBody body) {
        return !VBox.isBodyNullOrVersion0NotLoaded(body);
    }

    private static final long nextOffset = UtilUnsafe.objectFieldOffset(VBoxBody.class, "next");

    private void replaceTailInBody(VBoxBody body, VBoxBody tail) {
        VBoxBody current = body.next;

        if (isBodyValid(current)) {
            updateAndReplaceTailInBody(current, tail);
        } else if (!UNSAFE.compareAndSwapObject(body, nextOffset, current, tail)) {
            updateAndReplaceTailInBody(current, tail);
        }
    }

    private void updateAndReplaceTailInBody(VBoxBody current, VBoxBody tail) {
        current = getOldestValidBody(current);
        tail = findSubTail(current, tail);

        if (isBodyValid(tail)) {
            replaceTailInBody(current, tail);
        }
        // otherwise, everything from the tail was already found in the body 
    }

    /* Given a non-null reference to a single body, return the first element in
    tail that is not in the body list (the single body can have at most a next
    that is the NOT_LOADED_BODY. */
    private VBoxBody findSubTail(VBoxBody head, VBoxBody tail) {
        while (isBodyValid(tail)) {
            if (tail.version >= head.version) {
                tail = tail.next;
            } else {
                return tail;
            }
        }
        return null;
    }

    // Assumes !VBox.isBodyNullOrVersion0NotLoaded(head)
    private VBoxBody getOldestValidBody(VBoxBody head) {
        VBoxBody oldest = head;
        while (isBodyValid(oldest.next)) {
            oldest = oldest.next;
        }
        return oldest;
    }

    private static String makeKeyFor(UUID uuid) {
        return ":" + uuid.toString() + ":";
    }

    private static String makeKeyFor(VBox vbox) {
        return vbox.getId();
    }

    private static String makeKeyWithCommitId(String key, String commitId) {
        return key + ":" + commitId;
    }

//    private static String makeVersionedKey(String key, int version) {
//        return key + ":" + version;
//    }

    /* DataVersionHolder class. Ensures safe publication. */

//    private static class DataVersionHolder implements java.io.Serializable {
//        private static final long serialVersionUID = 1L;
//        public final int version;
//        public final int previousVersion;
//        public final byte[] data;
//
//        DataVersionHolder(int version, int previousVersion, byte[] data) {
//            this.version = version;
//            this.previousVersion = previousVersion;
//            this.data = data;
//        }
//    }

    private static class DataVersionHolder implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int version;
        public int previousVersion;
        public final byte[] data;

        DataVersionHolder(byte[] data) {
            this(-1, -1, data);
        }

        DataVersionHolder(int version, int previousVersion, byte[] data) {
            this.version = version;
            this.previousVersion = previousVersion;
            this.data = data;
        }
    }

}
