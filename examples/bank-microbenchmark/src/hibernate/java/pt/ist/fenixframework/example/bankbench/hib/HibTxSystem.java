package pt.ist.fenixframework.example.bankbench.hib;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.DomainFactory;
import pt.ist.fenixframework.example.bankbench.TxCommand;
import pt.ist.fenixframework.example.bankbench.TxSystem;


public class HibTxSystem extends TxSystem {
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public static Transaction beginTx() {
        return getSession().beginTransaction();
    }

    @Override
    public void save(Object obj) {
        getSession().save(obj);
    }

    public void doIt(final TxCommand cmd, boolean readOnly) {
        Transaction tx = beginTx();
        cmd.xaction(this);
        tx.commit();
    }

    public Client getClient(int id){
        return (Client)getSession().get(HClient.class, (long)id);
    }

    public DomainFactory makeDomainFactory() {
        return new HibFactory();
    }
}
