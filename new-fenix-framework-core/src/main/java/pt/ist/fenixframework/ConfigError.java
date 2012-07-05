package pt.ist.fenixframework;

/**
 * Thrown when the FenixFramework encounters some configuration problem.
 */
public class ConfigError extends Error {
    public static final String MISSING_CONFIG = "Config is absent."
        + " Explicitly initialize the FenixFramework with FenixFramework.initialize(Config)"
        + " or via convention by providing a fenix-framework.properties resource before attempting getConfig().";
    public static final String MISSING_REQUIRED_FIELD = "A required field was not specified in the FenixFramework config: ";

    public ConfigError(String message) {
	super(message);
    }

    public ConfigError(String message, String param) {
	super(message + param);
    }
}
