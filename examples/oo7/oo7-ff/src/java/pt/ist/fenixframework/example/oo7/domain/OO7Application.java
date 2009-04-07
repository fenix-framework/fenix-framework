package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.Transaction;

public class OO7Application extends OO7Application_Base {

    private static OO7Application instance = null;

    private interface DomainObjectReader {
        public DomainObject readDomainObjectByOID();
    }

    public static synchronized void init() {
        if (instance == null) {
            //Transaction.withTransaction(new jvstm.TransactionalCommand() {
                    //public void doIt() {
                    instance = (OO7Application) Transaction.getDomainObject(OO7Application.class.getName(), 1);
                    //instance.initAccessClosures();
                    //}
                    //});
        }
    }

    public static OO7Application getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private OO7Application() {
    }

    public static void initTests() {
        instance = new OO7Application();
    }

    protected OO7Application getOO7Application() {
        return this;
    }
}
