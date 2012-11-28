package pt.ist.fenixframework.dap;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.management.*;
import javax.management.*;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

import pt.ist.dap.structure.*;
import pt.ist.dap.implementation.*;
import pt.ist.dap.implementation.simple.*;


public abstract class FFDAPConfig extends Config {
    /**
     * This <strong>optional</strong> parameter specifies the location of the properties file used to
     * configure DAP.  This file should be available in the application's classpath.
     */
    protected String dapConfigFile = null;
    
    /*
     * Registers the DAP JMX interface so that it is available for invocation from external sources.
     */
    @Override
    protected void init() {
        try {
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
            
            DAPConfig dapConfig = DAPConfig.loadConfig(Thread.currentThread().getContextClassLoader().getResource(dapConfigFile));
            mbean.initDap(dapConfig, FenixFramework.getDomainModel());
            mbean.enableDap();
        } catch (Exception ex) {
            System.err.println("MBeanRegistration (DapRemoteManager) threw exception");
            ex.printStackTrace();
        }
    }
    
}
