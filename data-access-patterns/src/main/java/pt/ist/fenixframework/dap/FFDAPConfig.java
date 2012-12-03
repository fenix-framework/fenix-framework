package pt.ist.fenixframework.dap;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.management.*;
import javax.management.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.dml.DAPCodeGenerator;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

import pt.ist.dap.structure.*;
import pt.ist.dap.implementation.*;
import pt.ist.dap.implementation.simple.*;

// ATTENTION: If you ever introduce a class between this class and Config, please make sure that you
// call super.init() inside init(). Also, please kindly move this warning to the new superclass (the
// one that directly extends from Config).
public abstract class FFDAPConfig extends Config {
    private static final Logger logger = LoggerFactory.getLogger(FFDAPConfig.class);
    /**
     * This <strong>optional</strong> parameter specifies whether the DAP framework is enabled.  If
     * this configuration property is not explicitly set, its default value is obtained from the
     * parameters given to the DML compiler (off by default).
     */
    protected boolean ptIstDapEnable = decideDapOnFromBackendDefaults();

    private boolean decideDapOnFromBackendDefaults() {
        String param = BackEndId.getBackEndId().getParam(DAPCodeGenerator.DAP_ON_CONFIG_KEY);
        boolean result = (param != null) &&
            param.trim().equalsIgnoreCase(DAPCodeGenerator.DAP_ON_CONFIG_VALUE);
        return result;
    }
    
    /**
     * This <strong>optional</strong> parameter specifies the location of the properties file used
     * to configure DAP.  This file should be available in the application's classpath.
     */
    protected String dapConfigFile = null;

    // If the config property is explicitly provided, this method overrides the default definition
    // protected void dataAccessPatternsOnFromString(String value) {
    //     dataAccessPatternsOn = (value != null) && value.trim().equalsIgnoreCase("true");
    // }
    
    protected void ptIstDapEnableFromString(String value) {
        ptIstDapEnable = (value != null) && value.trim().equalsIgnoreCase("true");
    }

    /*
     * Registers the DAP JMX interface so that it is available for invocation from external sources.
     */
    @Override
    protected void init() {
        try {
            if (!ptIstDapEnable) return;

            logger.info("Initialing Data Access Patterns module.");
            DAPConfig dapConfig;
            if (dapConfigFile == null) {
                logger.warn("No property 'dapConfigFile' given. Will use default values.");
                dapConfig = new DAPConfig();
            } else {
                URL dapConfigURL = Thread.currentThread().getContextClassLoader().
                    getResource(dapConfigFile);
                if (dapConfigURL == null) {
                    logger.warn("DAP config file not found (" + dapConfigFile +
                                "). Will use default values.");
                    dapConfig = new DAPConfig();
                } else {
                    logger.trace("Using DAP config file: " + dapConfigURL);
                    dapConfig = DAPConfig.loadConfig(dapConfigURL);
                }
            }

            // Get the Platform MBean Server
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            // Construct the ObjectName for the MBean we will register
            ObjectName name = new ObjectName("pt.ist.dap.jmx:type=DapRemoteManager");
            // Create the MBean
            pt.ist.dap.jmx.DapRemoteManager mbean = new pt.ist.dap.jmx.DapRemoteManager();
            // Register the MBean
            mbs.registerMBean(mbean, name);
            //loadConfig(config.getClass().getResource("/dap.properties"));
            //DAPConfig dapConfig = DAPConfig.loadConfig((new File("./config/dap.properties")).toURI().toURL());
            // DAPConfig dapConfig = DAPConfig.loadConfig(Thread.currentThread().
            //                                            getContextClassLoader().
            //                                            getResource(dapConfigFile));
            mbean.initDap(dapConfig, FenixFramework.getDomainModel());
            mbean.enableDap();
        } catch (Exception ex) {
            logger.warn("MBeanRegistration (DapRemoteManager) threw exception", ex);
        }
    }
    
}
