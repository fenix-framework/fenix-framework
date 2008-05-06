package pt.ist.fenixframework.pstm;

public interface VersionedSubject {
    public jvstm.VBoxBody addNewVersion(String attr, int txNumber);
}
