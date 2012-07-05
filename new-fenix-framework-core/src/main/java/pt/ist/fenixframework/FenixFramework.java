package pt.ist.fenixframework;

// import jvstm.TransactionalCommand;
// import pt.ist.fenixframework.pstm.DataAccessPatterns;
// import pt.ist.fenixframework.pstm.MetadataManager;
// import pt.ist.fenixframework.core.PersistentRoot;
// import pt.ist.fenixframework.pstm.Transaction;
// import pt.ist.fenixframework.pstm.repository.RepositoryBootstrap;
// import pt.ist.fenixframework.core.Repository;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.core.DefaultConfig;

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

    /**
     * @return whether the <code>FenixFramework.initialize</code> method has already been invoked
     */
    public static boolean isInitialized() {
	synchronized (INIT_LOCK) {
            return initialized;
        }
    }

    /** This method initializes the FenixFramework.  It must be the first method to be called, and
     * it should be invoked only once.  It needs to be called before starting to access any
     * Transactions/DomainObjects, etc.
     *
     * @param config The configuration that will be used by this instance of the framework.
     */
    public static void initialize(Config config) {
	synchronized (INIT_LOCK) {
	    if (initialized) {
		throw new Error("Fenix framework already initialized");
	    }

	    FenixFramework.config = ((config != null) ? config
                                     : new DefaultConfig() {{ this.domainModelURLs = Config.resourceToURL("empty.dml"); }});
            FenixFramework.config.initialize();

	    // DataAccessPatterns.init(FenixFramework.config);
	    initialized = true;
	}

        // bootStrap(config);
        // initialize();
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
        if (config == null) {
            throw new ConfigError(ConfigError.MISSING_CONFIG);
        }
	return config;
    }

    public static TransactionManager getTransactionManager() {
        return getConfig().getTransactionManager();
    }

    // public static DomainModel getDomainModel() {
    //     return MetadataManager.getDomainModel();
    // }

    // public static <T extends DomainObject> T getRoot() {
    //     return (T) PersistentRoot.getRoot();
    // }


    public static <T extends DomainObject> T getDomainObject(String externalId) {
        return getConfig().getDomainObject(externalId);
    }
}
