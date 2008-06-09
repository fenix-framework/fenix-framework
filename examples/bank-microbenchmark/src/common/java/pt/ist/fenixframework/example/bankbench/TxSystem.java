package pt.ist.fenixframework.example.bankbench;

public abstract class TxSystem {

    private static final String TX_SYS_PROP_NAME = "txsystem.classname";

    private static TxSystem createTxSystemInstance() {
        String txSysClassname = System.getProperty(TX_SYS_PROP_NAME);

        if (txSysClassname == null) {
            System.err.printf("ERROR: The system property '%s' must have the name of a class implementing %s\n",
                              TX_SYS_PROP_NAME,
                              TxSystem.class.getName());
            System.exit(1);
        }

        try {
            Class<TxSystem> txsysClass = (Class<TxSystem>)Class.forName(txSysClassname);
            return txsysClass.newInstance();
        } catch (ClassNotFoundException cnfe) {
            System.err.printf("ERROR: Couldn't find the class '%s'\n", txSysClassname);
            System.exit(1);
        } catch (Exception e) {
            System.err.printf("ERROR: Couldn't create an instance of '%s'\n", txSysClassname);
            System.err.println("Cause: " + e);
            System.exit(1);
        }
        return null;
    }

    private static final TxSystem txsys = createTxSystemInstance();
    private static final DomainFactory domainFactory = txsys.makeDomainFactory();

    public static TxSystem getInstance() {
        return txsys;
    }

    public static DomainFactory getDomainFactory() {
        return domainFactory;
    }


    public void save(Object obj) {
        // do nothing by default
    }
    
    public abstract void doIt(TxCommand cmd, boolean readOnly);
    public abstract Client getClient(int id);
    public abstract DomainFactory makeDomainFactory();
}
