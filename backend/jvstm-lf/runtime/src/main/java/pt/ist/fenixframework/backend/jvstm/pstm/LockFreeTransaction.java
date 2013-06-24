/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import pt.ist.fenixframework.backend.jvstm.lf.LocalLockFreeTransaction;

public interface LockFreeTransaction {

    /**
     * This is the commit algorithm that each LockFreeTransaction perform on each node, regardless of whether it is a
     * {@link LocalLockFreeTransaction} or a {@link RemoteLockFreeTransaction}. Note that {@link DistributedLockFreeTransaction}s
     * are wrapped in {@link LocalLockFreeTransaction}s.
     */
    public void localCommit();

}
