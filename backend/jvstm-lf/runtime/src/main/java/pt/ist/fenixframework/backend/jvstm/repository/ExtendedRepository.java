package pt.ist.fenixframework.backend.jvstm.repository;

import java.util.UUID;

import pt.ist.fenixframework.backend.jvstm.lf.RemoteWriteSet;

public interface ExtendedRepository extends Repository {

    void persistWriteSet(UUID commitId, RemoteWriteSet writeSet, Object nullObject);

    void mapTxVersionToCommitId(int txVersion, UUID commitId);

    /**
     * Get the String representation of the commitId belonging to the given version.
     * 
     * @param txVersion The version from which to get the commit id
     * @return The commitId or <code>null</code> if the requested version is not committed yet
     */
    String getCommitIdFromVersion(int txVersion);
}
