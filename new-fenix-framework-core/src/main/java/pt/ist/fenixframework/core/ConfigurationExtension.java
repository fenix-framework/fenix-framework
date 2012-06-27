package pt.ist.fenixframework.core;

import pt.ist.fenixframework.Config;

/**
 * The concrete configuration extension will be invoked when the
 * <code>FenixFramework.initialize(Config)</code> method is invoked.
 */
public interface ConfigurationExtension {
    public void initialize(Config config);
}
