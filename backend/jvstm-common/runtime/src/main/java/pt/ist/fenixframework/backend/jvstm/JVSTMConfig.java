/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchConfig;

import com.hazelcast.config.ClasspathXmlConfig;
//import com.hazelcast.core.AtomicNumber;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.core.HazelcastInstance;

/**
 * This is the JVSTM configuration manager common for all JVSTM-based backends.
 * 
 * @see Config
 * 
 */
public class JVSTMConfig extends HibernateSearchConfig {

    private static final String FAILED_INIT = "Failed to initialize Backend Infinispan";

    protected static final String HAZELCAST_FF_GROUP_NAME = "FenixFrameworkGroup";

    /**
     * This <strong>optional</strong> parameter specifies the Hazelcast configuration file. This
     * configuration will used to create a group communication system between Fenix Framework nodes. The default value
     * for this parameter is <code>fenix-framework-hazelcast-default.xml</code>, which is the default
     * configuration file that ships with the framework.
     */
    protected String hazelcastConfigFile = "fenix-framework-hazelcast-default.xml";

    protected JVSTMBackEnd backEnd;

    /**
     * Subclasses of this config should set their own backEnd before calling this init. Otherwise, this method will set its own
     * backEnd (JVSTMBackEnd)
     */
    @Override
    protected void init() {
        // any sub-BackEnd with a concrete repository should have set it up by now
        // (and invoked super.init() only after doing so).  If a backend is not 
        // yet set, then we use the default JVSTMBackEnd, which has its own Repository.
        //      
        // We do not use the Config's constructor to pass the BackEnd instance,
        // because the config instance is created **before** being populated with
        // the configuration parameters, thus it may not be possible to create the
        // BackEnd instance.
        if (backEnd == null) {
            this.backEnd = new JVSTMBackEnd();
        }

        try {
            this.backEnd.init(this);
        } catch (Exception e) {
            throw new ConfigError(FAILED_INIT, e);
        }

        super.init();
    }

    @Override
    public JVSTMBackEnd getBackEnd() {
        return this.backEnd;
    }

    @Override
    public String getBackEndName() {
        return JVSTMBackEnd.BACKEND_NAME;
    }

    public String getHazelcastConfigFile() {
        return hazelcastConfigFile;
    }

    public com.hazelcast.config.Config getHazecastConfig() {
        System.setProperty("hazelcast.logging.type", "slf4j");
        com.hazelcast.config.Config hzlCfg = new ClasspathXmlConfig(getHazelcastConfigFile());
        hzlCfg.getGroupConfig().setName(HAZELCAST_FF_GROUP_NAME);
        return hzlCfg;
    }

}
