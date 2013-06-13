package pt.ist.fenixframework.backend.jvstm.repository;

import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

public class NoRepository implements Repository {

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
    public int getMaxCounterForClass(DomainClassInfo domainClassInfo) {
        return -1;
    }

    @Override
    public void updateMaxCounterForClass(DomainClassInfo domainClassInfo, int newCounterValue) {
        // no-op
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
    public void reloadAttribute(VBox box) {
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
    public boolean init(JVSTMConfig jvstmConfig) {
        return true;
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
