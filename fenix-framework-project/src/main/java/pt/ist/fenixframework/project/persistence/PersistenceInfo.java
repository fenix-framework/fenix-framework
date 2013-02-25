package pt.ist.fenixframework.project.persistence;

import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import pt.ist.fenixframework.project.persistence.exception.PersistenceInfoException;

public abstract class PersistenceInfo {

    private static final String PERSISTENCE_ENGINE_KEY = "persistence-engine";

    public enum PersistenceEngine {
        MySQL, BerkleyDB
    };

    public abstract boolean isEngine(PersistenceEngine persistenceEngine);

    public BerkeleyDBPersistenceInfo asBerkeleyDBPersistenceInfo() {
        return (BerkeleyDBPersistenceInfo) this;
    }

    public MySQLPersistenceInfo asMySQLPersistenceInfo() {
        return (MySQLPersistenceInfo) this;
    }

    public static PersistenceInfo fromProperties(Properties properties) throws PersistenceInfoException {
        if (StringUtils.isBlank(properties.getProperty(PERSISTENCE_ENGINE_KEY)))
            throw new PersistenceInfoException("Missing parameter: " + PERSISTENCE_ENGINE_KEY);
        if (properties.getProperty(PERSISTENCE_ENGINE_KEY).toLowerCase().equals(PersistenceEngine.MySQL.toString().toLowerCase())) {
            return MySQLPersistenceInfo.load(properties);
        } else {
            throw new PersistenceInfoException("Specified persistence engine not supported");
        }
    }

    public abstract void validate() throws PersistenceInfoException;

}
