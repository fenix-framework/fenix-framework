package pt.ist.fenixframework.example.bankbench.ff;

import dml.runtime.RelationAdapter;

public class BankBenchApp extends BankBenchApp_Base {

    public  BankBenchApp() {
        super();
    }

    public FFClient createClient(String name) {
        FFClient c = new FFClient(name);

        int clientId = this.getLastClientId() + 1;
        this.setLastClientId(clientId);
        c.setId(clientId);

        addClient(c);

        return c;
    }

    public FFClient getClient(int id) {
        for (FFClient c : getClientSet()) {
            if (c.getId() == id) {
                return c;
            }
        }

        return null;
    }
}
