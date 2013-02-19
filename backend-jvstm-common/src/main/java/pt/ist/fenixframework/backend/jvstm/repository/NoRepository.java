package pt.ist.fenixframework.backend.jvstm.repository;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

public class NoRepository extends Repository {

    @Override
    public long getMaxIdForClass(Class domainClass, long upperLimitOID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void reloadPrimitiveAttribute(VBox box) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void reloadReferenceAttribute(VBox box) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void persistChanges(Set<Entry<jvstm.VBox, Object>> changes, int txNumber, Object nullObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public int getMaxCommittedTxNumber() {
        return 0;
    }

    @Override
    public void closeRepository() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void storeKeyValue(Serializable key, Serializable value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Serializable getValue(Serializable key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

}
