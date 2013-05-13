/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;

import com.hazelcast.config.ClasspathXmlConfig;
//import com.hazelcast.core.AtomicNumber;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.core.HazelcastInstance;

/**
 * This is the configuration manager used by the fenix-framework-backend-jvstm-infinispan project.
 * 
 * @see Config
 */
public class JvstmIspnConfig extends JVSTMConfig {
    private static final Logger logger = LoggerFactory.getLogger(JvstmIspnConfig.class);

    private static final String FAILED_INIT = "Failed to initialize Backend JVSTM-Infinispan";

    protected static final String HAZELCAST_FF_GROUP_NAME = "FenixFrameworkGroup";

    /**
     * This <strong>optional</strong> parameter specifies the Hazelcast configuration file. This
     * configuration will used to create a group communication system between Fenix Framework nodes. The default value
     * for this parameter is <code>fenix-framework-hazelcast-default.xml</code>, which is the default
     * configuration file that ships with the framework.
     */
    protected String hazelcastConfigFile = "fenix-framework-hazelcast-default.xml";

    /**
     * This <strong>optional</strong> parameter specifies the location of the XML file used to configure Infinispan. This file
     * should be available in the application's classpath.
     */
    protected String ispnConfigFile = null;

    public String getHazelcastConfigFile() {
        return hazelcastConfigFile;
    }

    public String getIspnConfigFile() {
        return this.ispnConfigFile;
    }

    @Override
    protected void init() {
        JvstmIspnBackEnd thisBackEnd = new JvstmIspnBackEnd();
        super.backEnd = thisBackEnd;
        super.init(); // this will in turn initialize our backend 
    }

    @Override
    public JvstmIspnBackEnd getBackEnd() {
        return (JvstmIspnBackEnd) this.backEnd;
    }

    @Override
    public String getBackEndName() {
        return JvstmIspnBackEnd.BACKEND_NAME;
    }

    public com.hazelcast.config.Config getHazelcastConfig() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        com.hazelcast.config.Config hzlCfg = new ClasspathXmlConfig(getHazelcastConfigFile());
        hzlCfg.getGroupConfig().setName(HAZELCAST_FF_GROUP_NAME);
        return hzlCfg;
    }

}
