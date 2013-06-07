/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;

import com.hazelcast.config.ClasspathXmlConfig;

/**
 * This is the configuration manager used by the fenix-framework-backend-jvstm-cluster project.
 * 
 * @see Config
 */
public abstract class JvstmClusterConfig extends JVSTMConfig {
    private static final Logger logger = LoggerFactory.getLogger(JvstmClusterConfig.class);

    protected static final String HAZELCAST_FF_GROUP_NAME = "FenixFrameworkGroup";

    /**
     * This <strong>optional</strong> parameter specifies the Hazelcast configuration file. This
     * configuration will used to create a group communication system between Fenix Framework nodes. The default value
     * for this parameter is <code>fenix-framework-hazelcast-default.xml</code>, which is the default
     * configuration file that ships with the framework.
     */
    protected String hazelcastConfigFile = "fenix-framework-hazelcast-default.xml";

    public String getHazelcastConfigFile() {
        return hazelcastConfigFile;
    }

    @Override
    public JvstmClusterBackEnd getBackEnd() {
        return (JvstmClusterBackEnd) this.backEnd;
    }

    public com.hazelcast.config.Config getHazelcastConfig() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        com.hazelcast.config.Config hzlCfg = new ClasspathXmlConfig(getHazelcastConfigFile());
        hzlCfg.getGroupConfig().setName(HAZELCAST_FF_GROUP_NAME);
        return hzlCfg;
    }

}
