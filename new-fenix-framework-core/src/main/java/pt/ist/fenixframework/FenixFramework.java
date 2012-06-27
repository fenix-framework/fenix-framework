package pt.ist.fenixframework;

// import jvstm.TransactionalCommand;
// import pt.ist.fenixframework.pstm.DataAccessPatterns;
// import pt.ist.fenixframework.pstm.MetadataManager;
// import pt.ist.fenixframework.core.PersistentRoot;
// import pt.ist.fenixframework.pstm.Transaction;
// import pt.ist.fenixframework.pstm.repository.RepositoryBootstrap;
import pt.ist.fenixframework.core.Repository;
import pt.ist.fenixframework.dml.DomainModel;

/**
 * This class provides a method to initialize the entire Fenix Framework. To do
 * it, programmers should call the static <code>initialize(Config)</code> method
 * with a proper instance of the <code>Config</code> class.
 * 
 * After initialization, it is possible to get an instance of the
 * <code>DomainModel</code> class representing the structure of the
 * application's domain.
 * 
 * @see Config
 * @see dml.DomainModel
 */
public class FenixFramework {

    private static final Object INIT_LOCK = new Object();
    // private static boolean bootstrapped = false;
    private static boolean initialized = false;

    private static Config config;

    private static TransactionManager transactionManager = null;
    private static Repository repository = null;

    public static void initialize(Config config) {
	synchronized (INIT_LOCK) {
	    if (initialized) {
		throw new Error("Fenix framework already initialized");
	    }

	    FenixFramework.config = ((config != null) ? config : new Config());
            config.initialize();

            // Because, the Config is an open extension point, we need to ensure the bare minimum,
            // e.g. a tx manager, an abstract domain object model, and a repository manager.
            ensureConfigExtensionRequirements();

	    // MetadataManager.init(config);
	    // new RepositoryBootstrap(config).updateDataRepositoryStructureIfNeeded();
	    // DataAccessPatterns.init(config);
	    initialized = true;
	}

        // bootStrap(config);
        // initialize();
    }

    // ensure that the minimum required components were setup
    private static void ensureConfigExtensionRequirements() {
        Config.checkRequired(transactionManager, "transactionManager");
        Config.checkRequired(repository, "repository");
    }

    public static void setTransactionManager(TransactionManager value) {
        // This method should only be invoked within FenixFramework.initialize(), but the
        // synchronized goes to ensure it.  Better safe than sorry.
        synchronized(INIT_LOCK) {
            if (transactionManager != null) {
                throw new Error("The 'transactionManager' is already set");
            }
            transactionManager = value;
        }
    }
    
    public static void setRepository(Repository value) {
        // This method should only be invoked within FenixFramework.initialize(), but the
        // synchronized goes to ensure it.  Better safe than sorry.
        synchronized(INIT_LOCK) {
            if (repository != null) {
                throw new Error("The 'repository' is already set");
            }
            repository = value;
        }
    }
    



    // private static void bootStrap(Config config) {
    //     synchronized (INIT_LOCK) {
    //         if (bootstrapped) {
    //     	throw new Error("Fenix framework already initialized");
    //         }

    //         FenixFramework.config = ((config != null) ? config : new Config());
    //         config.checkConfig();
    //         // MetadataManager.init(config);
    //         // new RepositoryBootstrap(config).updateDataRepositoryStructureIfNeeded();
    //         // DataAccessPatterns.init(config);
    //         bootstrapped = true;
    //     }
    // }

    // private static void initialize() {
    //     synchronized (INIT_LOCK) {
    //         if (initialized) {
    //     	throw new Error("Fenix framework already initialized");
    //         }
            

    //         // PersistentRoot.initRootIfNeeded(config);

    //         // FenixFrameworkPlugin[] plugins = config.getPlugins();
    //         // if (plugins != null) {
    //         //     for (final FenixFrameworkPlugin plugin : plugins) {
    //         //         Transaction.withTransaction(new TransactionalCommand() {
			
    //         //     	@Override
    //         //     	public void doIt() {
    //         //     	    plugin.initialize();
    //         //     	}

    //         //         });
    //         //     }
    //         // }
    //         initialized = true;
    //     }
    // }

    public static Config getConfig() {
	return config;
    }

    // public static DomainModel getDomainModel() {
    //     return MetadataManager.getDomainModel();
    // }

    // public static <T extends DomainObject> T getRoot() {
    //     return (T) PersistentRoot.getRoot();
    // }
}
