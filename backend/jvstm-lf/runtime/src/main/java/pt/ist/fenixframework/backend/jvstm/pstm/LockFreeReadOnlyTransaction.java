/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockFreeReadOnlyTransaction extends LockFreeTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeReadOnlyTransaction.class);

    public LockFreeReadOnlyTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    // repeats the code from PersistentReadOnlyTransaction. We definitely need
    // to untangle the transaction hierarchy mess...

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        numBoxReads++;
        VBoxBody<T> body = vbox.getBody(number);
        if (body.value == VBox.NOT_LOADED_VALUE) {
            vbox.reload();
            // after the reload, the (new) body should have the required loaded value
            // if not, then something went wrong and it's better to abort
            // body = vbox.body.getBody(number);
            body = vbox.getBody(number);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                logger.error("Couldn't load the VBox: {}", vbox.getId());
                throw new VersionNotAvailableException();
            }
        }

        return body.value;
    }

    @Override
    public boolean txAllowsWrite() {
        return false;
    }

    @Override
    public void setReadOnly() {
        // nothing to do, LockFreeReadOnlyTransaction is already read-only :-)
    }

}