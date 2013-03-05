package pt.ist.fenixframework;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import jvstm.TransactionalCommand;
import pt.ist.fenixframework.pstm.DataAccessPatterns;
import pt.ist.fenixframework.pstm.DomainFenixFrameworkRoot;
import pt.ist.fenixframework.pstm.DomainMetaClass;
import pt.ist.fenixframework.pstm.DomainMetaObject;
import pt.ist.fenixframework.pstm.MetadataManager;
import pt.ist.fenixframework.pstm.PersistentRoot;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.repository.RepositoryBootstrap;
import dml.DomainModel;

/**
 * This class provides a method to initialize the entire Fenix Framework. To do
 * it, programmers should call the static <code>initialize(Config)</code> method
 * with a proper instance of the <code>Config</code> class.
 * 
 * After initialization, it is possible to get an instance of the <code>DomainModel</code> class representing the
 * structure of the
 * application's domain.
 * 
 * @see Config
 * @see dml.DomainModel
 */
public class FenixFramework {

    private static final Object INIT_LOCK = new Object();
    private static boolean bootstrapped = false;
    private static boolean initialized = false;

    private static Config config;

    public static void initialize(Config config) {
        config.checkIsValid();

        bootStrap(config);
        initialize();
    }

    public static void bootStrap(Config config) {
        synchronized (INIT_LOCK) {
            if (bootstrapped) {
                throw new Error("Fenix framework already bootstrapped");
            }

            System.out.println("[FenixFramework] Bootstrap started.");
            FenixFramework.config = ((config != null) ? config : new Config());
            config.checkConfig();
            MetadataManager.init(config);
            new RepositoryBootstrap(config).updateDataRepositoryStructureIfNeeded();
            DataAccessPatterns.init(config);
            bootstrapped = true;
            System.out.println("[FenixFramework] Bootstrap finished.");
        }
    }

    public static void initialize() {
        synchronized (INIT_LOCK) {
            if (isInitialized()) {
                throw new Error("Fenix framework already initialized");
            }

            System.out.println("[FenixFramework] Initialization started.");
            initDomainFenixFrameworkRoot();
            PersistentRoot.initRootIfNeeded(config);

            FenixFrameworkPlugin[] plugins = config.getPlugins();
            if (plugins != null) {
                for (final FenixFrameworkPlugin plugin : plugins) {
                    Transaction.withTransaction(new TransactionalCommand() {

                        @Override
                        public void doIt() {
                            plugin.initialize();
                        }

                    });
                }
            }
            initialized = true;
            System.out.println("[FenixFramework] Initialization finished.");
        }
    }

    /**
     * @return <code>true</code> if the framework was already initialized. <br>
     *         <code>false</code> if the framework was not yet initialized, or
     *         the initialization is still in progress.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Creates a {@link PersistentRoot} for the {@link DomainFenixFrameworkRoot} and then initializes the
     * {@link DomainFenixFrameworkRoot}.
     */
    private static void initDomainFenixFrameworkRoot() {
        try {
            Transaction.withTransaction(new TransactionalCommand() {
                @Override
                public void doIt() {
                    getLockAndInitDomainFenixFrameworkRoot();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("ERROR: An exception was thrown during the initialization of the DomainFenixFrameworkRoot.");
        } finally {
            if (jvstm.Transaction.isInTransaction()) {
                Transaction.abort();
            }
        }
    }

    public static void getLockAndInitDomainFenixFrameworkRoot() {
        //Most of this connection- and lock-related code was copied from RepositoryBootstrap.updateDataRepositoryStructureIfNeeded()
        Connection connection = null;
        try {
            connection = Transaction.getCurrentJdbcConnection();

            Statement statement = null;
            ResultSet resultSet = null;

            String lockName = RepositoryBootstrap.getDbLockName();
            try {
                int iterations = 0;
                while (true) {
                    iterations++;
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery("SELECT GET_LOCK('" + lockName + "', 60)");
                    if (resultSet.next() && (resultSet.getInt(1) == 1)) {
                        break;
                    }
                    if ((iterations % 10) == 0) {
                        System.out.println("[DomainFenixFrameworkRoot] Warning: Could not yet obtain the " + lockName
                                + " lock. Number of retries: " + iterations);
                    }
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }

            try {

                if (getDomainFenixFrameworkRoot() == null) {
                    DomainFenixFrameworkRoot fenixFrameworkRoot = new DomainFenixFrameworkRoot();
                    PersistentRoot.addRoot(DomainFenixFrameworkRoot.ROOT_KEY, fenixFrameworkRoot);
                }
                getDomainFenixFrameworkRoot().initialize(getDomainModel());

            } finally {
                Statement statementUnlock = null;
                try {
                    statementUnlock = connection.createStatement();
                    statementUnlock.executeUpdate("DO RELEASE_LOCK('" + lockName + "')");
                } finally {
                    if (statementUnlock != null) {
                        statementUnlock.close();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex);
        }
    }

    public static DomainFenixFrameworkRoot getDomainFenixFrameworkRoot() {
        return PersistentRoot.getRoot(DomainFenixFrameworkRoot.ROOT_KEY);
    }

    public static Config getConfig() {
        return config;
    }

    public static DomainModel getDomainModel() {
        return MetadataManager.getDomainModel();
    }

    public static <T extends DomainObject> T getRoot() {
        return (T) PersistentRoot.getRoot();
    }

    /**
     * Indicates whether the framework was configured to allow the automatic
     * creation of {@link DomainMetaObject}s and {@link DomainMetaClass}es. Only
     * if this method returns <code>true</code> will a consistency predicate of
     * a domain object be allowed to read values from other objects.
     * 
     * @return the value of {@link Config}.canCreateDomainMetaObjects
     */
    public static boolean canCreateDomainMetaObjects() {
        return getConfig().canCreateDomainMetaObjects;
    }
}
