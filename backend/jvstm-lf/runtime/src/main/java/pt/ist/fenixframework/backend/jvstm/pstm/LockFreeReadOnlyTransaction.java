/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockFreeReadOnlyTransaction extends LockFreeTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeReadOnlyTransaction.class);

    public LockFreeReadOnlyTransaction(ActiveTransactionsRecord record) {
        super(record);
        setReadOnly();
    }

    // repeats the code from PersistentReadOnlyTransaction. We definitely need
    // to untangle the transaction hierarchy mess...

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        numBoxReads++;
        return getValueFromBody(vbox, vbox.getBody(number));
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