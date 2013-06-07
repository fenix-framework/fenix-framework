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

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.cluster.JvstmClusterBackEnd;
import pt.ist.fenixframework.backend.jvstm.pstm.OwnedVBox;
import pt.ist.fenixframework.backend.jvstm.pstm.StandaloneVBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VBoxCache;

public class JvstmDataGridBackEnd extends JvstmClusterBackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmDataGridBackEnd.class);

    public static final String BACKEND_NAME = "jvstm-datagrid";

    JvstmDataGridBackEnd() {
        super(new DataGridRepository());
    }

    public static JvstmDataGridBackEnd getInstance() {
        return (JvstmDataGridBackEnd) FenixFramework.getConfig().getBackEnd();
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public VBox lookupCachedVBox(String vboxId) {
        VBox vbox = StandaloneVBox.lookupCachedVBox(vboxId);
        if (vbox != null) {
            return vbox;
        }
        // It may be an owned VBox
        return OwnedVBox.lookupCachedVBox(vboxId);
    }

    public VBox vboxFromId(String vboxId) {
        logger.debug("vboxFromId({})", vboxId);

        VBox vbox = lookupCachedVBox(vboxId);

        if (vbox == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("VBox not found after lookup: {}", vboxId);
            }

            /* we assume that well-written programs write to data grid entries
            before reading from them.  As such, when a StandaloneVBox is not
            cached, we simply allocate it. */
            vbox = StandaloneVBox.makeNew(vboxId, true);
            // cache vbox and return the canonical vbox
            vbox = VBoxCache.getCache().cache(vbox);
        }

        return vbox;
    }
}
