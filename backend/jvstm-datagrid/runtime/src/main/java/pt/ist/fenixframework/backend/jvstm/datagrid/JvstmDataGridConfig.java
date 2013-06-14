/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.datagrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.cluster.JvstmClusterConfig;

/**
 * This is the configuration manager used by the fenix-framework-backend-jvstm-datagrid project.
 * 
 * @see Config
 */
public class JvstmDataGridConfig extends JvstmClusterConfig {
    private static final Logger logger = LoggerFactory.getLogger(JvstmDataGridConfig.class);

    protected static final String FAILED_INIT = "Failed to initialize Backend JVSTM-DataGrid";

    /**
     * This <strong>required</strong> parameter specifies the classname of the datagrid implementation.
     */
    protected String dataGridClassName = null;

    public String getDatagridClassName() {
        return this.dataGridClassName;
    }

    @Override
    protected void init() {
        JvstmDataGridBackEnd thisBackEnd = new JvstmDataGridBackEnd();
        super.backEnd = thisBackEnd;
        super.init(); // this will in turn initialize our backend
    }

    @Override
    protected void checkConfig() {
        super.checkConfig();
        checkRequired(dataGridClassName, "dataGridClassName");
    }

    @Override
    public JvstmDataGridBackEnd getBackEnd() {
        return (JvstmDataGridBackEnd) this.backEnd;
    }

}
