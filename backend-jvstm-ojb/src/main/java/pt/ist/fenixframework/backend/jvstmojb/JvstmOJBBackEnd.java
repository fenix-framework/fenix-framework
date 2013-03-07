package pt.ist.fenixframework.backend.jvstmojb;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstmojb.pstm.DomainClassInfo;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class JvstmOJBBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBBackEnd.class);

    public static final String BACKEND_NAME = "jvstm-ojb";

    private final TransactionManager transactionManager;

    public JvstmOJBBackEnd() {
        transactionManager = new JvstmOJBTransactionManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T fromOid(Object oid) {
        logger.trace("fromOid({})", oid);

        AbstractDomainObject obj = SharedIdentityMap.getCache().lookup(oid);

        if (obj == null) {
            obj = DomainObjectAllocator.allocateObject(DomainClassInfo.mapOidToClass(((Long) oid).longValue()), oid);
            obj = SharedIdentityMap.getCache().cache(obj);
        }

        return (T) obj;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        if (externalId == null) {
            return null;
        }
        return fromOid(Long.parseLong(externalId));
    }

    @Override
    public DomainRoot getDomainRoot() {
        return fromOid(1L);
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public void shutdown() {
        logger.info("Shutting Down Fenix Framework");
        deregisterDrivers();
        logger.info("Fenix Framework Shut Down sucessfully");
    }

    private void deregisterDrivers() {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();

            ClassLoader loader = driver.getClass().getClassLoader();

            if (loader != null && loader.equals(currentClassLoader)
                    && driver.getClass().getName().equals("com.mysql.jdbc.Driver")) {
                try {
                    DriverManager.deregisterDriver(driver);
                    logger.info("Successfully deregistered JDBC driver " + driver);
                } catch (SQLException e) {
                    logger.warn("Failed to deregister JDBC driver " + driver + ". This may cause a potential leak.", e);
                }
            }

        }
    }

}
