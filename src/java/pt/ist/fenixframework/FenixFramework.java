package pt.ist.fenixframework;

import jvstm.TransactionalCommand;
import pt.ist.fenixframework.pstm.DataAccessPatterns;
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
 * After initialization, it is possible to get an instance of the
 * <code>DomainModel</code> class representing the structure of the
 * application's domain.
 * 
 * @see Config
 * @see dml.DomainModel
 */
public class FenixFramework {

    private static final Object INIT_LOCK = new Object();
    private static boolean initialized = false;

    private static Config config;

    public static void initialize(Config config) {
	synchronized (INIT_LOCK) {
	    if (initialized) {
		throw new Error("Fenix framework already initialized");
	    }

	    FenixFramework.config = ((config != null) ? config : new Config());
	    config.checkConfig();
	    MetadataManager.init(config);
	    new RepositoryBootstrap(config).updateDataRepositoryStructureIfNeeded();
	    DataAccessPatterns.init(config);
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
	}

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
}
