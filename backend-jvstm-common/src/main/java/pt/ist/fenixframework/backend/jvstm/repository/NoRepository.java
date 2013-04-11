package pt.ist.fenixframework.backend.jvstm.repository;

import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.core.AbstractDomainObject;

public class NoRepository extends Repository {

    private static final Logger logger = LoggerFactory.getLogger(NoRepository.class);

    @Override
    public DomainClassInfo[] getDomainClassInfos() {
        return new DomainClassInfo[0];
    }

    @Override
    public void storeDomainClassInfos(DomainClassInfo[] domainClassInfos) {
        // no-op
    }

    @Override
    public long getMaxOidForClass(Class<? extends AbstractDomainObject> domainClass, long lowerLimitOid, long upperLimitOid) {
        return lowerLimitOid;
    }

    @Override
    public void reloadPrimitiveAttribute(VBox box) {
        throw new UnsupportedOperationException("should not be invoked when using the NoRepository implementation");
    }

    @Override
    public void reloadReferenceAttribute(VBox box) {
        throw new UnsupportedOperationException("should not be invoked when using the NoRepository implementation");
    }

    @Override
    public void persistChanges(Set<Entry<jvstm.VBox, Object>> changes, int txNumber, Object nullObject) {
        // no-op
    }

    @Override
    public int getMaxCommittedTxNumber() {
        return 0;
    }

    @Override
    public void closeRepository() {
        // no-op
    }

    @Override
    @Atomic(mode = TxMode.WRITE)
    public void ensureDomainRoot() {
        logger.info("Creating the singleton DomainRoot instance");
        new DomainRoot();
    }

//    @Override
//    public void storeKeyValue(Serializable key, Serializable value) {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    @Override
//    public Serializable getValue(Serializable key) {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("not yet implemented");
//    }

}
