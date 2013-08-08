package pt.ist.fenixframework.dap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.dap.implementation.DAPConfig;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.dml.DAPCodeGenerator;
import pt.ist.fenixframework.jmx.JmxUtil;

import javax.management.ObjectName;
import java.net.URL;

// ATTENTION: If you ever introduce a class between this class and Config, please make sure that you
// call super.init() inside init(). Also, please kindly move this warning to the new superclass (the
// one that directly extends from Config).
public abstract class FFDAPConfig extends Config {
    private static final Logger logger = LoggerFactory.getLogger(FFDAPConfig.class);

    /**
     * This well-known name specifies the location of the properties file used to configure the
     * <strong>Data Access Patterns</strong> framework.  This file should be available in the
     * application's classpath.
     */
    public static final String CONFIG_FILE = "dap.properties";

    protected boolean dapEnabled = isDAPEnabled();

    private boolean isDAPEnabled() {
        String dap = BackEndId.getBackEndId().getParam(DAPCodeGenerator.DAP_ON_CONFIG_KEY);
        return (dap != null) && dap.trim().equalsIgnoreCase(DAPCodeGenerator.DAP_ON_CONFIG_VALUE);
    }
    
    /*
     * Registers the DAP JMX interface so that it is available for invocation from external sources.
     */
    @Override
    protected void init() {
        try {
            if (!dapEnabled) return;

            if (logger.isDebugEnabled()) {
                logger.debug("Initialing Data Access Patterns module.");
            }
            DAPConfig dapConfig;
            URL dapConfigURL = Thread.currentThread().getContextClassLoader().
                getResource(CONFIG_FILE);
            if (dapConfigURL == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Resource '" + CONFIG_FILE + "' not found.  Using default values.");
                }
                dapConfig = new DAPConfig();
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Using config resource '" + dapConfigURL + "'.");
                }
                dapConfig = DAPConfig.loadConfig(dapConfigURL);
            }

            ObjectName name = JmxUtil.buildObjectName(getAppName(), "DAP", JmxUtil.COMMON_MODULE, "DapRemoteManager", null);
            // Create the MBean
            pt.ist.dap.jmx.DapRemoteManager mbean = new pt.ist.dap.jmx.DapRemoteManager();
            mbean.initDap(dapConfig, FenixFramework.getDomainModel());
            mbean.enableDap();
            JmxUtil.registerMBean(name, mbean);
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to initialize Data Access Patterns module", ex);
            }
            dapEnabled = false;
        }
    }
    
}
