package pt.ist.fenixframework.example.oo7.extra;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pt.ist.fenixframework.example.oo7.domain.*;
import pt.ist.fenixframework.example.oo7.extra.Stopwatch;
/**
 * Base class for OO7 benchmark implementations.
 *
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public abstract class OO7Benchmark {

    private int scale;

    public OO7Benchmark(int scale) {
        this.scale = scale;
    }

    public void runQueries() {
        Stopwatch sw = new Stopwatch();
        Long[] ids = chooseAtomicParts(10);
        sw.start();
        System.out.println("Query 1");
        query1(ids);
        sw.stop();
        System.out.println("Query 1 time : " + sw.getElapsedTime());

        sw.reset();
        sw.start();
        System.out.println("Query 2");
        query2();
        sw.stop();
        System.out.println("Query 2 time : " + sw.getElapsedTime());

        sw.reset();
        sw.start();
        System.out.println("Query 3");
        query3();
        sw.stop();
        System.out.println("Query 3 time : " + sw.getElapsedTime());

        sw.reset();
        if (scale != OO7Database.TINY) {
            Long[] docIds = getRandomDocIds(100);
            sw.start();
            System.out.println("Query 4");
            query4(docIds);
            sw.stop();
            System.out.println("Query 4 time : " + sw.getElapsedTime());
            sw.reset();
        }

        sw.start();
        System.out.println("Query 5");
        query5();
        sw.stop();
        System.out.println("Query 5 time : " + sw.getElapsedTime());

        sw.reset();
        sw.start();
        System.out.println("Query 7");
        query7();
        sw.stop();
        System.out.println("Query 7 time : " + sw.getElapsedTime());

        sw.reset();
        sw.start();
        System.out.println("Query 8");
        query8();
        sw.stop();
        System.out.println("Query 8 time : " + sw.getElapsedTime());
    }

    public abstract Long[] chooseAtomicParts(int count);

    public abstract void query1(Long[] atomicPartIds);

    public abstract void query2();

    public abstract void query3();

    public abstract Long[] getRandomDocIds(int count);

    public abstract void query4(Long[] docIds);

    public abstract void query5();

    public abstract void query7();

    public abstract void query8();

    public void runTraversals() {
        Stopwatch sw = new Stopwatch();
        sw.start();
        System.out.println("Traversal 1");
        traversal1();
        sw.stop();
        System.out.println("Traversal 1 time : " + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal2a();
         sw.stop();
         System.out.println("Traversal 2a time : "
         + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal2b();
         sw.stop();
         System.out.println("Traversal 2b time : "
         + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal2c();
         sw.stop();
         System.out.println("Traversal 2c time : "
         + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal6();
         sw.stop();
         System.out.println("Traversal 6 time : "
         + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal8();
         sw.stop();
         System.out.println("Traversal 8 time : "
         + sw.getElapsedTime());

         sw.reset();
         sw.start();
         traversal9();
         sw.stop();
         System.out.println("Traversal 9 time : "
         + sw.getElapsedTime());
         sw.reset();
    }

    public abstract void startTransaction();

    public abstract void endTransaction();

    public abstract List<Module> getModules();

    public abstract List<Manual> getManuals();

    protected void traversal1() {
        startTransaction();
        for (Module m : getModules()) {
            traversal(m, TraversalInfo.T1);
        }
        endTransaction();
    }

    protected void traversal2a() {
        startTransaction();
        for (Module m : getModules()) {
            TraversalInfo ti = TraversalInfo.t2a();
            traversal(m, ti);
            System.out.println("Traversal 2A updated " + ti.getNumUpdated()
                    + " parts.");
        }
        endTransaction();
    }

    protected void traversal2b() {
        startTransaction();
        for (Module m : getModules()) {
            TraversalInfo ti = TraversalInfo.t2b();
            traversal(m, ti);
            System.out.println("Traversal 2B updated " + ti.getNumUpdated()
                    + " parts.");
        }
        endTransaction();
    }

    protected void traversal2c() {
        startTransaction();
        for (Module m : getModules()) {
            TraversalInfo ti = TraversalInfo.t2c();
            traversal(m, ti);
            System.out.println("Traversal 2C updated " + ti.getNumUpdated()
                    + " parts.");
        }
        endTransaction();
    }

    protected void traversal6() {
        startTransaction();
        for (Module m : getModules()) {
            traversal(m, TraversalInfo.T6);
        }
        endTransaction();
    }

    /**
     * Traverse an entire module's object graph
     *
     */
    protected void traversal(Module module, TraversalInfo ti) {
        ComplexAssembly root = module.getDesignRoot();
        traverseAssemblies(root, new HashSet<AtomicPart>(), ti);
    }

    int numAssembliesTraversed = 0;

    protected void traverseAssemblies(ComplexAssembly root, Set<AtomicPart> visitedParts,
            TraversalInfo ti) {
        Iterator<Assembly> subAssembliesIter =
            root.getSubAssemblies().iterator();
        while (subAssembliesIter.hasNext()) {
            Assembly assm = subAssembliesIter.next();
            if (assm instanceof ComplexAssembly) {
                traverseAssemblies((ComplexAssembly) assm, visitedParts, ti);
            } else {
                traverseParts((BaseAssembly) assm, visitedParts, ti);
            }
        }
    }

    protected void traverseParts(BaseAssembly assm, Set<AtomicPart> visitedParts,
            TraversalInfo ti) {
        Iterator<CompositePart> unsharedPartsIter =
            assm.getUnsharedPartIterator();
        while (unsharedPartsIter.hasNext()) {
            CompositePart cp = unsharedPartsIter.next();
            AtomicPart rootPart = cp.getRootPart();
            rootPart.getX(); // just to instantiate it if it is a proxy
            if (ti.isUpdateAtomicParts()) {
                updatePart(rootPart);
                ti.updatedPart();
            }
            if (!visitedParts.contains(rootPart)) {
                visitedParts.add(rootPart);
            }
            if (ti.isTraverseSubParts()) {
                traverseAtomicParts(rootPart, visitedParts, ti);
            }
        }
    }

    protected void traverseAtomicParts(AtomicPart atomicPart,
            Set<AtomicPart> visitedParts,
            TraversalInfo ti) {
        Iterator<Conn> connectionsIter =
            atomicPart.getConnections().iterator();
        while (connectionsIter.hasNext()) {
            Conn conn = connectionsIter.next();
            AtomicPart newPart = conn.getTo();
            if (!visitedParts.contains(newPart)) {
                if (ti.isUpdateAllAtomicParts()) {
                    updatePart(newPart);
                    if (ti.isUpdateAtomicPartMultipleTimes()) {
                        for (int i = 0; i < 3; i++) {
                            updatePart(newPart);
                            ti.updatedPart();
                        }
                    }
                    ti.updatedPart();
                }
                visitedParts.add(newPart);
                traverseAtomicParts(newPart, visitedParts, ti);
            }
        }
    }

    protected void updatePart(AtomicPart ap) {
        Integer tmp = ap.getX();
        ap.setX(ap.getY());
        ap.setY(tmp);
    }

    protected int traversal8() {
        startTransaction();
        int numIs = 0;
        for (Manual m : getManuals()) {
            String text = m.getText();
            int pos = -1;
            while ((pos = text.indexOf('i', pos + 1)) != -1) {
                numIs++;
            }
        }
        endTransaction();
        return numIs;
    }

    protected boolean traversal9() {
    	startTransaction();
        boolean cond = true;
        for (Manual m : getManuals()) {
            String text = m.getText();
            cond = cond && text.charAt(0) == text.charAt(text.length() - 1);
        }
        endTransaction();
        return cond;
    }

    protected static class TraversalInfo {
        private boolean traverseSubParts;

        private boolean updateAtomicParts;

        private boolean updateAllAtomicParts;

        private boolean updateAtomicPartMultipleTimes;

        private int numUpdated = 0;

        private TraversalInfo(boolean traverseSubParts) {
            this(traverseSubParts, false, false, false);
        }

        private TraversalInfo(boolean traverseSubParts,
                boolean updateAtomicParts, boolean updateAllAtomicParts,
                boolean updateAtomicPartMultipleTimes) {
            this.traverseSubParts = traverseSubParts;
            this.updateAtomicParts = updateAtomicParts;
            this.updateAllAtomicParts = updateAllAtomicParts;
            this.updateAtomicPartMultipleTimes = updateAtomicPartMultipleTimes;
        }

        public static TraversalInfo T1 = new TraversalInfo(true);

        public static TraversalInfo T6 = new TraversalInfo(false);

        public static TraversalInfo t2a() {
            return new TraversalInfo(true, true, false, false);
        }

        public static TraversalInfo t2b() {
            return new TraversalInfo(true, true, true, false);
        }

        public static TraversalInfo t2c() {
            return new TraversalInfo(true, true, true, true);
        }

        public boolean isTraverseSubParts() {
            return traverseSubParts;
        }

        public boolean isUpdateAtomicPartMultipleTimes() {
            return updateAtomicPartMultipleTimes;
        }

        public boolean isUpdateAllAtomicParts() {
            return updateAllAtomicParts;
        }

        public boolean isUpdateAtomicParts() {
            return updateAtomicParts;
        }

        public int getNumUpdated() {
            return numUpdated;
        }

        public void updatedPart() {
            numUpdated++;
        }
    }
}
