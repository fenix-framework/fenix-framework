package pt.ist.fenixframework.backend.jvstm.datagrid;

public interface DataGrid {

    public void init(JvstmDataGridConfig config);

    public void stop();

    public Object get(Object key);

    public void put(Object key, Object value);

    /** Start a new data grid transaction */
    public void beginTransaction();

    /** Commit the existing data grid transaction */
    public void commitTransaction();

    /** Rollback the existing data grid transaction */
    public void rollbackTransaction();

    /** Get whether there is an active data grid transaction. */
    public boolean inTransaction();
}
