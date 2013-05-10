/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.cluster;

import pt.ist.fenixframework.backend.jvstm.infinispan.JvstmIspnBackEnd;

import com.hazelcast.core.ILock;

public class ClusterUtils {

    private static final String FF_GLOBAL_LOCK_NAME = "ff.hzl.global.lock";

    public static ILock globalLock() {
        return JvstmIspnBackEnd.getInstance().getHazelcastInstance().getLock(FF_GLOBAL_LOCK_NAME);
    }
}
