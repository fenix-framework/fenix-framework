package pt.ist.fenixframework.backend.jvstmojb.pstm;

public interface VersionedSubject {
    public jvstm.VBoxBody addNewVersion(String attr, int txNumber);

    public Object getCurrentValue(Object obj, String attrName);
}
