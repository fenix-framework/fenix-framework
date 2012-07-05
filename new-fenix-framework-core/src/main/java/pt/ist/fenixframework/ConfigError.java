package pt.ist.fenixframework;

/**
 * Thrown when the FenixFramework encounters some configuration problem.
 */
public class ConfigError extends Error {
    public static final String CONFIG_CLASS_NOT_FOUND = "Could not get the configuration class: ";

    public static final String MISSING_CONFIG = "Config is absent."
        + " Explicitly initialize the FenixFramework with FenixFramework.initialize(Config)"
        + " or via convention by providing a fenix-framework.properties resource before attempting getConfig().";

    public static final String MISSING_REQUIRED_FIELD = "A required field was not specified in the FenixFramework config: ";

    public static final String UNKNOWN_PROPERTY = "Unknown configuration property.";

    public static final String COULD_NOT_SET_PROPERTY = "No method <propName>" + Config.SETTER_FROM_STRING
        + "(String) given, and property is not assignable from String: ";

    // public static final String NO_PROPERTY_SETTER = "No setter method for property.";

    public ConfigError(String message) {
	super(message);
    }

    public ConfigError(String message, String param) {
	super(message + param);
    }

    public ConfigError(String message, Throwable cause) {
	super(message, cause);
    }

    public ConfigError(Throwable cause) {
	super(cause);
    }
}
