package pt.ist.fenixframework.backend.jvstm.repository;

import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeConfig;

public interface DataGrid {

    public void init(JvstmLockFreeConfig config);

    public void stop();

    public <T> T get(Object key);

    public void put(Object key, Object value);

    public void putIfAbsent(Object key, Object value);

    /** Start a new data grid transaction */
    public void beginTransaction();

    /** Commit the existing data grid transaction */
    public void commitTransaction();

    /** Rollback the existing data grid transaction */
    public void rollbackTransaction();

    /** Get whether there is an active data grid transaction. */
    public boolean inTransaction();
}
