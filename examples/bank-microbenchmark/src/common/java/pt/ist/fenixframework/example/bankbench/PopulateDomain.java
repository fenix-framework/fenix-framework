package pt.ist.fenixframework.example.bankbench;

public class PopulateDomain {

    public static void main(String[] args) {
        int numClients = Integer.parseInt(args[0]);
        int accountsPerClient = Integer.parseInt(args[1]);
        int initialAmount = Integer.parseInt(args[2]);

        System.out.printf("Populating the domain with %d clients, each with %d accounts with an initial ammount of %d\n",
                          numClients,
                          accountsPerClient,
                          initialAmount);
        
        TxSystem txsys = TxSystem.getInstance();
        for (int i = 0; i < numClients; i++) {
            new CreateClientCommand(i, accountsPerClient, initialAmount).run();
        }
    }


    static class CreateClientCommand extends TxCommand {
        private int clientNumber;
        private int numAccounts;
        private int initialAmount;

        CreateClientCommand(int clientNumber, int numAccounts, int initialAmount) {
            super(false);
            this.clientNumber = clientNumber;
            this.numAccounts = numAccounts;
            this.initialAmount = initialAmount;
        }

        public void xaction(TxSystem txsys) {
            Client c = TxSystem.getDomainFactory().makeClient("Client no " + clientNumber);
            txsys.save(c);

            for (int i = 0; i < numAccounts; i++) {
                createAccount(txsys, c, (i == 0) ? initialAmount : 0);
            }
        }

        private void createAccount(TxSystem txsys, Client c, int initialAmount) {
            Account acc = TxSystem.getDomainFactory().makeAccount(c);
            acc.deposit(Money.euros(initialAmount));
            txsys.save(acc);
        }
    }
}
