/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;

import com.hazelcast.config.ClasspathXmlConfig;

/**
 * This is the configuration manager used by the fenix-framework-backend-jvstm-lf-cluster project.
 * 
 * @see Config
 */
public class JvstmLockFreeConfig extends JVSTMConfig {
    private static final Logger logger = LoggerFactory.getLogger(JvstmLockFreeConfig.class);

    protected static final String HAZELCAST_FF_GROUP_NAME = "FenixFrameworkGroup";

    /**
     * This <strong>optional</strong> parameter specifies the Hazelcast configuration file. This
     * configuration will used to create a group communication system between Fenix Framework nodes. The default value
     * for this parameter is <code>fenix-framework-hazelcast-default.xml</code>, which is the default
     * configuration file that ships with the framework.
     */
    protected String hazelcastConfigFile = "fenix-framework-lf-hazelcast-default.xml";

    public String getHazelcastConfigFile() {
        return hazelcastConfigFile;
    }

    @Override
    protected void init() {
        JvstmLockFreeBackEnd thisBackEnd = new JvstmLockFreeBackEnd();
        super.backEnd = thisBackEnd;
        super.init(); // this will in turn initialize our backend
    }

    @Override
    public JvstmLockFreeBackEnd getBackEnd() {
        return (JvstmLockFreeBackEnd) this.backEnd;
    }

    public com.hazelcast.config.Config getHazelcastConfig() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        com.hazelcast.config.Config hzlCfg = new ClasspathXmlConfig(getHazelcastConfigFile());
        hzlCfg.getGroupConfig().setName(HAZELCAST_FF_GROUP_NAME);
        return hzlCfg;
    }

}
