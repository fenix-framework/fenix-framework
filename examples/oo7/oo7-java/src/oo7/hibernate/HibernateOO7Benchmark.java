package oo7.hibernate;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import oo7.AtomicPart;
import oo7.BaseAssembly;
import oo7.Document;
import oo7.Manual;
import oo7.Module;
import oo7.OO7Benchmark;
import oo7.OO7Database;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;

public class HibernateOO7Benchmark extends OO7Benchmark {

    private SessionFactory sf = null;

    private static Random rand = new Random();

    public static void main(String args[]) {

        if (args.length < 2) {
            System.out.println("This program takes 2 arguments:\n"
                    + "1. The scale of the database "
                    + "(0:Tiny, 1:Small, 2:Medium, 3:Large).\n"
                    + "2. The number of  iterations.\n");
            System.exit(-1);
        }

        int scale = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);
        HibernateOO7Benchmark b = new HibernateOO7Benchmark(scale);
        for (int i = 0; i < iterations; i++) {
            b.run();
        }
    }
    
    public void run() {
        sf.getStatistics().setStatisticsEnabled(true);

        runQueries();
        runTraversals();

    }

    public HibernateOO7Benchmark(int scale) {
        super(scale);
        sf = initializeSessionFactory();
    }
    
    protected SessionFactory initializeSessionFactory() {
        return new Configuration().configure().buildSessionFactory();
    }

    public void query1(final Long[] ids) {
        new HibernateAction() {
            protected Object performAction(Session sess) {
                for (int i = 0; i < ids.length; i++) {
                    AtomicPart part = (AtomicPart) sess.load(AtomicPart.class,
                            ids[i]);
                    part.getX(); // instantiate part
                }
                return null;
            }
        }.execute();
    }

    public Long[] chooseAtomicParts(final int numParts) {
        return (Long[]) new HibernateAction() {

            protected Object performAction(Session sess) {
                Long ids[] = new Long[numParts];
                Query q = sess.createQuery("from AtomicPart ap");
                q.setMaxResults(numParts);
                List atomicParts = q.list();
                if (atomicParts.size() < numParts) {
                    throw new IllegalStateException("Less than " + numParts
                            + " atomic parts in database");
                }
                for (int i = 0; i < numParts; i++) {
                    AtomicPart part = (AtomicPart) atomicParts.get(i);
                    ids[i] = part.getId();
                }
                return ids;
            }
        }.execute();
    }

    public void query2() {
        long dateThreshold = getRange(OO7Database.MIN_ATOMIC_DATE,
                OO7Database.MAX_ATOMIC_DATE, 0.99);
        String query = "from AtomicPart ap where ap.buildDate > "
                + dateThreshold;
        queryAtomicParts(query);
    }

    public void query3() {
        long dateThreshold = getRange(OO7Database.MIN_ATOMIC_DATE,
                OO7Database.MAX_ATOMIC_DATE, 0.9);
        String query = "from AtomicPart ap where ap.buildDate > "
                + dateThreshold;
        queryAtomicParts(query);
    }

    public void query7() {
        String query = "from AtomicPart ap";
        queryAtomicParts(query);
    }

    public int queryAtomicParts(final String query) {
        return ((Integer) new HibernateAction() {
            protected Object performAction(Session sess) {
                int numParts = 0;
                Query q = sess.createQuery(query);
                Iterator qIter = q.list().iterator();
                while (qIter.hasNext()) {
                    AtomicPart part = (AtomicPart) qIter.next();
                    part.getX();
                    numParts++;
                }
                return Integer.valueOf(numParts);
            }
        }.execute()).intValue();
    }

    public Long[] getRandomDocIds(final int count) {
        return (Long[]) new HibernateAction() {
            protected Object performAction(Session sess) {
                Long[] docIds = new Long[count];
                Query q = sess.createQuery("select d.id from Document d");
                List documentIds = q.list();
                if (documentIds.size() < count) {
                    throw new IllegalStateException(
                            "Too few documents for query 4.");
                }
                // Sample 100 documents without replacement
                for (int i = 0; i < count; i++) {
                    docIds[i] = (Long) documentIds.get(rand.nextInt(documentIds
                            .size()));
                    documentIds.remove(docIds[i]);
                }
                return docIds;
            }
        }.execute();
    }

    public void query4(final Long[] docIds) {
        new HibernateAction() {
            protected Object performAction(Session sess) {
                int numBaseAssemblies = 0;
                for (int i = 0; i < docIds.length; i++) {
                    Query q = sess
                            .createQuery("select ba from BaseAssembly ba left outer join ba.unsharedParts part where part.document.id = :docId");
                    q.setLong("docId", docIds[i].longValue());
                    Iterator qIter = q.list().iterator();
                    while (qIter.hasNext()) {
                        BaseAssembly ba = (BaseAssembly) qIter.next();
                        ba.getBuildDate();
                        numBaseAssemblies++;
                    }
                }
                return null;
            }
        }.execute();
    }

    public void query5() {
        new HibernateAction() {
            protected Object performAction(Session sess) {
                int numBaseAssemblies = 0;
                Query q = sess.createQuery("select ba from BaseAssembly ba "
                        + "left outer join ba.unsharedParts part "
                        + "where part.buildDate > ba.buildDate");
                Iterator qIter = q.list().iterator();
                while (qIter.hasNext()) {
                    BaseAssembly ba = (BaseAssembly) qIter.next();
                    ba.getBuildDate();
                    numBaseAssemblies++;
                }
                return null;
            }
        }.execute();
    }

    public void query8() {
        new HibernateAction() {
            protected Object performAction(Session sess) {
                int numPairs = 0;
                Query q = sess
                        .createQuery("from Document doc, AtomicPart part "
                                + "where doc.id = part.docId");
                Iterator qIter = q.list().iterator();
                while (qIter.hasNext()) {
                    Object[] tuple = (Object[]) qIter.next();
                    ((Document) tuple[0]).getTitle();
                    ((AtomicPart) tuple[1]).getDocId();
                    numPairs++;
                }
                return null;
            }
        }.execute();
    }
    
    protected Criteria getCriteria(Class clazz, Session sess) {
        return sess.createCriteria(clazz);
    }
    
    @Override
    public void startTransaction() {
        Session s = getSession();
        s.beginTransaction();
    }
    
    @Override
    public void endTransaction() {
        Session s = getSession();
        s.getTransaction().commit();
    }
    
    @Override
    public List<Manual> getManuals() {
        Criteria q = getCriteria(Manual.class, getSession());
        q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        @SuppressWarnings("unchecked")
        List<Manual> l = q.list();
        return l;
    }
    
    @Override
    public List<Module> getModules() {
        Criteria q = getCriteria(Module.class, getSession());
        q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        @SuppressWarnings("unchecked")
        List<Module> l = q.list();
        return l;
    }
    
    public abstract class HibernateAction {
        protected abstract Object performAction(Session sess);

        public Object execute() {
            Statistics st = sf.getStatistics();
            st.clear();
            Session sess = null;
            Transaction tx = null;
            try {
                sess = getSession();
                tx = sess.beginTransaction();
                Object o = performAction(sess);
                tx.commit();
                return o;
            } finally {
                if (tx.isActive()) { // we didn't commit
                    tx.rollback();
                }
                System.out.print("Entities fetched: "
                        + st.getEntityFetchCount());
                System.out.print(", Collections fetched: "
                        + st.getCollectionFetchCount());
                System.out.print(", Queries: " + st.getQueryExecutionCount());
                System.out.print(", Total: "
                        + (st.getEntityFetchCount() + st.getCollectionFetchCount() + st
                                .getQueryExecutionCount()));
                System.out.println(", Entities loaded: "
                        + st.getEntityLoadCount());
            }
        }
    }

    private long getRange(long min, long max, double percentage) {
        return (long) ((max - min) * percentage) + min;
    }

    private Session getSession() {
        return sf.getCurrentSession();
    }
}
