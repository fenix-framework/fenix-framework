package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.Transaction;

public class OO7Application extends OO7Application_Base {

    private interface DomainObjectReader {
        public DomainObject readDomainObjectByOID();
    }

    public static OO7Application getInstance() {
        return FenixFramework.getRoot();
    }

    public OO7Application() {
    }

    protected OO7Application getOO7Application() {
        return this;
    }
}
