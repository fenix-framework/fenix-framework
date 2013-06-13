/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.datagrid.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.datagrid.JvstmDataGridConfig;

/**
 * This is an extension to the configuration manager used by the fenix-framework-backend-jvstm-datagrid backend. It add the
 * infinispan specific config properties.
 * 
 * @see Config
 */
public class DataGridConfig extends JvstmDataGridConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataGridConfig.class);

    private static final String ISPN_DATA_GRID_CLASS_NAME =
            "pt.ist.fenixframework.backend.jvstm.datagrid.infinispan.InfinispanDataGrid";

    /**
     * This <strong>optional</strong> parameter specifies the location of the XML file used to configure Infinispan. This file
     * should be available in the application's classpath.
     */
    protected String dataGridConfigFile = null;

    public String getDataGridConfigFile() {
        return this.dataGridConfigFile;
    }

    @Override
    protected void checkConfig() {
        if (this.dataGridClassName == null) {
            this.dataGridClassName = ISPN_DATA_GRID_CLASS_NAME;
        }
        super.checkConfig();
    }
}
