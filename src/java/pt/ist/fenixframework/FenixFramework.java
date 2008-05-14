package pt.ist.fenixframework;

import dml.DomainModel;

import pt.ist.fenixframework.pstm.MetadataManager;


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
            initialized = true;
        }
    }

    public static Config getConfig() {
        return config;
    }

    public static DomainModel getDomainModel() {
        return MetadataManager.getDomainModel();
    }
}
