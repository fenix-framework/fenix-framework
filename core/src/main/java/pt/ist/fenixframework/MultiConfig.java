package pt.ist.fenixframework;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores multiple Fenix Framework {@link Config}urations.  Each configuration is automatically
 * associated with a key obtained by invoking {@link
 * pt.ist.fenixframework.backend.BackEnd#getName()} on the config's BackEnd.
 *
 * Setting up multiple configurations allows the Fenix Framework to automatically choose the
 * configuration to use, according to the backend that generated the domain model.
 *
 * For this mechanism to work as expected when using explicit initialization (see {@link
 * FenixFramework}), the programmer must initialize the framework by invoking the {@link
 * FenixFramework#initialize(MultiConfig)} method (instead of {@link
 * FenixFramework#initialize(Config)}).  Before doing so, however, the programmer must create an
 * instance of MultiConfig and add each of the available configurations (via {@link #add(Config)}).
 *
 * NOTE: This class is not thread-safe.  It is not expectable to have multiple threads attempting to
 * concurrently add configurations the same MultiConfig instance.  However, it is possible (albeit
 * of questionable usefulness) to attempt initialization of the framework from multiple threads,
 * each using a different instance of MultiConfig.
 *
 * @see FenixFramework
 */
public class MultiConfig {
    private static final Logger logger = LoggerFactory.getLogger(MultiConfig.class);
    private final Map<String,Config> configs = new HashMap<String,Config>();

    public static final String UNKNOWN_BACKEND = "Unknown backend: ";

    /**
     * Add a configuration to the set of available configurations.  Only one configuration per
     * backend is supported.  If more than one configuration per backend is added, the last will
     * replace the previous.
     *
     * @return the previous configuration for the same backend, or null if there was none
     */
    public Config add(Config config) {
        return configs.put(config.getBackEnd().getName(), config);
    }


    public Config get(String backEndName) {
        Config config = configs.get(backEndName);
        if (config == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(UNKNOWN_BACKEND + backEndName);
            }
        }
        return config;
    }
}
