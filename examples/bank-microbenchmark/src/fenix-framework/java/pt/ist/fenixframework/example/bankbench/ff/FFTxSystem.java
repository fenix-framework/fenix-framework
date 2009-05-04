package pt.ist.fenixframework.example.bankbench.ff;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.DomainFactory;
import pt.ist.fenixframework.example.bankbench.TxCommand;
import pt.ist.fenixframework.example.bankbench.TxSystem;


public class FFTxSystem extends TxSystem {

    public FFTxSystem() {
        Config config = new Config() {{ 
            domainModelPath = "/bank.dml";
            dbAlias = "//localhost:3306/ff-bank"; 
            dbUsername = "test";
            dbPassword = "test";
            rootClass = BankBenchApp.class;
        }};
        FenixFramework.initialize(config);
    }


    public void doIt(final TxCommand cmd, boolean readOnly) {
        Transaction.withTransaction(readOnly, new jvstm.TransactionalCommand() {
                public void doIt() {
                    cmd.xaction(FFTxSystem.this);
                }
            });
    }

    public Client getClient(int id){
        BankBenchApp app = FenixFramework.getRoot();
        return app.getClient(id);
    }

    public DomainFactory makeDomainFactory() {
        return new FFFactory();
    }
}
