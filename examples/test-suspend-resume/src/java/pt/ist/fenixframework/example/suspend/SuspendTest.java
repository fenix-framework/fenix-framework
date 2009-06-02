package pt.ist.fenixframework.example.suspend;

import pt.ist.fenixframework.FenixFramework;

import pt.ist.fenixframework.example.suspend.domain.App;
import pt.ist.fenixframework.example.suspend.domain.Person;

import jvstm.Atomic;
import jvstm.Transaction;
import java.util.Scanner;
import java.util.Set;

public class SuspendTest {

    public static void main(final String[] args) {
        Init.init();
        new SuspendTest().repl();
    }


    private Transaction tx;
    private App app;

    SuspendTest() {
        initApp();
    }

    @Atomic
    private void initApp() {
        this.app = FenixFramework.getRoot();
    }

    private void repl() {
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String cmd = scanner.next();

            try {
                if (cmd.equals("quit")) {
                    abort();
                    return;
                } else if (cmd.equals("list")) {
                    listPeople();
                } else if (cmd.equals("add")) {
                    addPerson(scanner.nextLine().trim());
                } else if (cmd.equals("done")) {
                    commit();
                } else if (cmd.equals("cancel")) {
                    abort();
                } else if (cmd.equals("rename")) {
                    rename(scanner.nextLine().trim(), scanner.nextLine().trim());
                } else if (cmd.equals("show")) {
                    showPerson(scanner.nextLine().trim());
                } else {
                    System.out.println("Unknown command: '" + cmd + "'");
                }
            } catch (Exception ex) {
                System.out.println("Error during processing of command: " + ex);
                ex.printStackTrace();
            }
        }
    }

    private void resumeTx() {
        if (tx == null) {
            tx = Transaction.begin();
        } else {
            boolean resumed = false;
            try {
                Transaction.resume(tx);
                resumed = true;
            } catch (Exception exc) {
                System.out.println("Resume failed: " + exc);
                throw new RuntimeException("Aborting operation, because resume failed");
            } finally {
                if (! resumed) {
                    Transaction.abort();
                    tx = null;
                }
            }
        }
    }


    private void listPeople() {
        resumeTx();

        for (Person p : app.getPersonSet()) {
            System.out.println(p.getName() + " (oid=" + p.getOid() + ", " + p + ")");
        }

        Transaction.suspend();
    }

    private void addPerson(String name) {
        resumeTx();
        app.addPerson(new Person(name));
        Transaction.suspend();        
    }

    private void rename(String from, String to) {
        resumeTx();

        for (Person p : app.getPersonSet()) {
            if (p.getName().equals(from)) {
                p.setName(to);
            }
        }

        Transaction.suspend();
    }

    private void showPerson(String oidTxt) {
        resumeTx();

        try {
            long oid = Long.parseLong(oidTxt);
            Person p = Person.fromOID(oid);
            System.out.println(p.getName() + " (oid=" + p.getOid() + ", " + p + ")");
        } catch (Exception ex) {
            System.out.println("Can't show person with oid = " + oidTxt);
        }
        
        Transaction.suspend();
    }

    private void commit() {
        resumeTx();
        boolean finished = false;
        try {
            Transaction.commit();
            finished = true;
        } catch (Exception ex) {
            System.out.println("Commit failed: " + ex);
        } finally {
            if (! finished) {
                Transaction.abort();
            }
            this.tx = null;
        }
    }

    private void abort() {
        resumeTx();
        try {
            Transaction.abort();
        } catch (Exception ex) {
            System.out.println("Abort failed: " + ex);
        } finally {
            this.tx = null;
        }
    }
}
