package pt.ist.fenixframework.project.persistence;

import pt.ist.fenixframework.project.persistence.exception.PersistenceInfoException;

public class BerkeleyDBPersistenceInfo extends PersistenceInfo {

    @Override
    public boolean isEngine(PersistenceEngine persistenceEngine) {
        return persistenceEngine != null && persistenceEngine.equals(PersistenceEngine.BerkleyDB);
    }

    @Override
    public void validate() throws PersistenceInfoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
