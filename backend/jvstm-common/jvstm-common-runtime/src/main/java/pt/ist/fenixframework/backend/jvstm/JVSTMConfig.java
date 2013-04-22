/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchConfig;

/**
 * This is the JVSTM configuration manager common for all JVSTM-based backends.
 * 
 * @see Config
 * 
 */
public class JVSTMConfig extends HibernateSearchConfig {

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

        this.backEnd.init(this);

        super.init();
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }

    @Override
    public String getBackEndName() {
        return JVSTMBackEnd.BACKEND_NAME;
    }

}
