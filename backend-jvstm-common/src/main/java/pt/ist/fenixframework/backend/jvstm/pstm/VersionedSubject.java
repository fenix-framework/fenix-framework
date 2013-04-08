package pt.ist.fenixframework.backend.jvstm.pstm;

public interface VersionedSubject {
    public jvstm.VBoxBody addNewVersion(/*String attr, */int txNumber);

    public Object getCurrentValue(/*Object obj, String attrName*/);
}
