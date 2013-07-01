package pt.ist.fenixframework.backend.jvstm.repository;

import java.util.UUID;

import pt.ist.fenixframework.backend.jvstm.lf.RemoteWriteSet;

public interface ExtendedRepository extends Repository {

    void persistWriteSet(UUID commitId, RemoteWriteSet writeSet, Object nullObject);

}
