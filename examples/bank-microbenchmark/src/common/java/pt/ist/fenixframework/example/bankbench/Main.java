package pt.ist.fenixframework.example.bankbench;

import java.util.Set;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {

    public static void main(String[] args) throws Exception {
        int maxThreads = Integer.parseInt(args[0]);
        int maxClientId = Integer.parseInt(args[1]);
        int numCommands = Integer.parseInt(args[2]);
        
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);

        for (int i = 0; i < numCommands; i++) {
            doRandomGetBalance(executor, maxClientId);
            //doRandomTransfer(executor, maxClientId);
        }

        executor.shutdown();
        executor.awaitTermination(10000000, TimeUnit.SECONDS);

        float secs = (System.currentTimeMillis() - start) / 1000.0f;
        System.out.printf("Running %d commands took %f seconds\n", numCommands, secs);
    }

    private static void doRandomTransfer(Executor executor, int maxClientId) {
        int src = Math.max(1, randomInt(maxClientId));
        int dst = Math.max(1, randomInt(maxClientId));

        doCommand(executor, new TransferCommand(src, dst));
    }

    private static void doRandomGetBalance(Executor executor, int maxClientId) {
        int id = Math.max(1, randomInt(maxClientId));

        doCommand(executor, new GetBalanceCommand(id));
    }

    private static int randomInt(int max) {
        return (int)Math.floor(Math.random() * max);
    }


    private static void doCommand(Executor executor, Runnable cmd) {
        executor.execute(cmd);
    }

    static class TransferCommand extends TxCommand {
        private static final Money ZERO = Money.euros(0);

        private final int srcClientId;
        private final int dstClientId;

        TransferCommand(int srcId, int dstId) {
            super(false);
            this.srcClientId = srcId;
            this.dstClientId = dstId;
        }

        public void xaction(TxSystem txsys) {
            Client src = TxSystem.getInstance().getClient(srcClientId);
            Client dst = TxSystem.getInstance().getClient(dstClientId);
            
            // find an account on the src client with some money
            Account srcAccount = null;
            for (Account acc : src.getAccounts()) {
                if (ZERO.lessThan(acc.getBalance())) {
                    srcAccount = acc;
                    break;
                }
            }

            if (srcAccount != null) {
                Set<? extends Account> dstAccounts = dst.getAccounts();
                int numAcc = randomInt(dstAccounts.size());

                Account dstAccount = null;
                for (Account acc : dstAccounts) {
                    if (numAcc-- == 0) {
                        dstAccount = acc;
                        break;
                    }
                }

                srcAccount.transfer(dstAccount, srcAccount.getBalance().getHalfMoney());
            }
        }
    }

    static class GetBalanceCommand extends TxCommand {
        private static final Money ZERO = Money.euros(0);

        private final int clientId;

        GetBalanceCommand(int id) {
            super(true);
            this.clientId = id;
        }

        public void xaction(TxSystem txsys) {
            Client client = TxSystem.getInstance().getClient(clientId);

            client.getTotalBalance();
        }
    }

}
