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

import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;

/**
 * This is the configuration manager used by the fenix-framework-backend-jvstm-infinispan project.
 * 
 * @see Config
 */
public class JvstmIspnConfig extends JVSTMConfig {
    private static final Logger logger = LoggerFactory.getLogger(JvstmIspnConfig.class);

    private static final String FAILED_INIT = "Failed to initialize Backend JVSTM-Infinispan";

    /**
     * This <strong>required</strong> parameter specifies the location of the XML file used to configure Infinispan. This file
     * should be available in the application's classpath.
     */
    protected String ispnConfigFile = null;

    protected final JvstmIspnBackEnd backEnd;

    public JvstmIspnConfig() {
        this.backEnd = JvstmIspnBackEnd.getInstance();
    }

    // process this config's parameters

    public String getIspnConfigFile() {
        return this.ispnConfigFile;
    }

    @Override
    protected void init() {
        try {
            this.backEnd.configJvstmIspn(this);
        } catch (Exception e) {
            throw new ConfigError(FAILED_INIT, e);
        }

        super.init(); // do this only after having set up transaction manager
    }

    @Override
    protected void checkConfig() {
        super.checkConfig();
        if (ispnConfigFile == null) {
            missingRequired("ispnConfigFile");
        }
    }

    @Override
    public JvstmIspnBackEnd getBackEnd() {
        return this.backEnd;
    }
}
