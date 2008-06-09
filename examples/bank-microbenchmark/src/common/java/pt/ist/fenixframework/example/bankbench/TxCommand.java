package pt.ist.fenixframework.example.bankbench;

public abstract class TxCommand implements Runnable {
    
    private boolean readOnly;

    TxCommand(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public abstract void xaction(TxSystem txsys);
    
    public final void run() {
        TxSystem.getInstance().doIt(this, readOnly);
    }
}

