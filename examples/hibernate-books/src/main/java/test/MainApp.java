package test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.Transaction;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchSupport;
import pt.ist.fenixframework.txintrospector.TxStats;

public class MainApp {

    private static final String STEPH = "Steph";
    private static final String MAYOR = "Mayor";
    private static final String BRAM_STOKER = "Bram Stoker";

    private static final String LITTLE = "Little";
    private static final String ARCHIBALD = "Archibald";

    private static final String TWOLIGHTS = "TwoLights";
    private static final String FEW_MOON = "Few Moon";
    private static final String ECLIPSE = "Eclipse";
    private static final String DRACULA = "Dracula";

    @Atomic
    public static DomainRoot getDomainRoot() {
        return FenixFramework.getDomainRoot();
    }

    public static void main(String[] args) throws Exception {
        //try {
            DomainRoot domainRoot = getDomainRoot();
            for (Method method : MainApp.class.getMethods()) {
                reset(domainRoot);

                if (method.getName().startsWith("test")) {
                    method.invoke(null, domainRoot);
                }

                System.out.println("Query result: " + performQuery(Author.class, "name", "stoker"));
                System.out.println("Query result: " + performQuery(VampireBook.class, "bookName", "dracula"));

                System.out.println("\nQuery result: " + performQuery(VampireBook.class, "publisher.hibernate$primaryKey",
                        getPublisherByName(domainRoot, LITTLE).getExternalId()));

                operation1(domainRoot);

                System.out.println("\nQuery result: " + performQuery(VampireBook.class, "publisher.hibernate$primaryKey",
                        getPublisherByName(domainRoot, LITTLE).getExternalId()));

                operation2(domainRoot);

                System.out.println("\nQuery result: " + performQuery(VampireBook.class, "publisher.hibernate$primaryKey",
                        getPublisherByName(domainRoot, LITTLE).getExternalId()));

                break;
            }
        //} finally {
            FenixFramework.shutdown();
        //}
    }

    @Atomic
    public static void operation1(DomainRoot domainRoot) {
        getBookByName(domainRoot, TWOLIGHTS).removePublisher();
    }

    @Atomic
    public static void operation2(DomainRoot domainRoot) {
        getPublisherByName(domainRoot, LITTLE).getBooksPublished().clear();
    }

    @Atomic
    public static Collection<DomainObject> performQuery(Class<?> cls, String field, String queryString) {
        ArrayList<DomainObject> matchingObjects = new ArrayList<DomainObject>();

        QueryBuilder qb = HibernateSearchSupport.getSearchFactory().buildQueryBuilder().forEntity(cls).get();
        Query query = qb.keyword().onField(field).matching(queryString).createQuery();
        HSQuery hsQuery = HibernateSearchSupport.getSearchFactory().createHSQuery().luceneQuery(query)
                .targetedEntities(Arrays.<Class<?>>asList(cls));
        hsQuery.getTimeoutManager().start();
        for (EntityInfo ei : hsQuery.queryEntityInfos()) {
            matchingObjects.add(FenixFramework.getDomainObject((String) ei.getId()));
        }
        hsQuery.getTimeoutManager().stop();

        return matchingObjects;
    }

    // Tests new Objects
    @Atomic
    public static void test01(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book\n\t"
                + "(New: ['" + ECLIPSE + "']; DM: []; M: []; RCL: [nothing relevant])");

        createEclipse(txStats);

        assert txStats.getDirectlyModifiedObjects().isEmpty() : "Found " + txStats.getDirectlyModifiedObjects() + " but should be empty";
        assert txStats.getModifiedObjects().isEmpty() : "Found " + txStats.getModifiedObjects() + " but should be empty";

        System.out.println(txStats);
    }

    private static VampireBook createEclipse(TxStats txStats) {
        VampireBook eclipse = new VampireBook(ECLIPSE, 0.11, true);

        assert txStats.getNewObjects().contains(eclipse) : "New objects should contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should not contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Modified objects should not contain " + eclipse;

        return eclipse;
    }

    @Atomic
    public static void test02(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and directly modify it\n\t"
                +"(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [nothing relevant])");

        VampireBook eclipse = createEclipse(txStats);
        eclipse.setBookName(eclipse.getBookName() + " SDK");
        eclipse.setPrice(3.12);
        eclipse.setHasGlowingVampires(false);

        assert txStats.getDirectlyModifiedObjects().isEmpty() : "Found " + txStats.getDirectlyModifiedObjects() + " but should be empty";
        assert txStats.getModifiedObjects().isEmpty() : "Found " + txStats.getModifiedObjects() + " but should be empty";

        System.out.println(txStats);
    }

    @Atomic
    public static void test03(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its 1-1 relation with '" + FEW_MOON + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");

        VampireBook eclipse = createEclipse(txStats);
        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        eclipse.setPrequel(fewMoon);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;

        System.out.println(txStats);
    }

    @Atomic
    public static void test04(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its 1-1 relation with '" + FEW_MOON + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");


        VampireBook eclipse = createEclipse(txStats);
        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(eclipse);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;

        System.out.println(txStats);
    }

    @Atomic
    public static void test05(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and 1) add a relation with '" + FEW_MOON + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");


        VampireBook eclipse = createEclipse(txStats);
        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(null);
        eclipse.setPrequel(fewMoon);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;

        System.out.println(txStats);
    }

    @Atomic
    public static void test06(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book, add a relation between the new book and '" + FEW_MOON + "', and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [])");


        VampireBook eclipse = createEclipse(txStats);
        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(null);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should NOT contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;

        System.out.println(txStats);
    }

    @Atomic
    public static void test07(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book, add a relation between the new book and '" + FEW_MOON + "', and then change relation to '" + DRACULA + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + DRACULA + "']; RCL: ['VampireBookToVampireBook'])");


        VampireBook eclipse = createEclipse(txStats);
        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        VampireBook dracula = getVampireBookByName(domainRoot, DRACULA);
        eclipse.setPrequel(fewMoon);
        eclipse.setPrequel(dracula);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should NOT contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;
        assert txStats.getModifiedObjects().contains(dracula) : "Modified objects should contain " + dracula;
        assert !txStats.getDirectlyModifiedObjects().contains(dracula) : "Directly modified objects should NOT contain " + dracula;

        System.out.println(txStats);
    }

    @Atomic
    public static void test08(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its 1-* relation with '" + LITTLE + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Publisher little = getPublisherByName(domainRoot, LITTLE);
        eclipse.setPublisher(little);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(little) : "Modified objects should contain " + little;
        assert !txStats.getDirectlyModifiedObjects().contains(little) : "Directly modified objects should NOT contain " + little;

        System.out.println(txStats);
    }

    @Atomic
    public static void test09(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its 1-* relation with '" + LITTLE + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Publisher little = getPublisherByName(domainRoot, LITTLE);
        eclipse.setPublisher(little);
        little.addBooksPublished(eclipse);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(little) : "Modified objects should contain " + little;
        assert !txStats.getDirectlyModifiedObjects().contains(little) : "Directly modified objects should NOT contain " + little;

        System.out.println(txStats);
    }

    @Atomic
    public static void test10(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and 1) add a 1-* relation with '" + LITTLE + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Publisher little = getPublisherByName(domainRoot, LITTLE);
        eclipse.setPublisher(little);
        little.removeBooksPublished(eclipse);
        little.addBooksPublished(eclipse);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(little) : "Modified objects should contain " + little;
        assert !txStats.getDirectlyModifiedObjects().contains(little) : "Directly modified objects should NOT contain " + little;

        System.out.println(txStats);
    }

    @Atomic
    public static void test11(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book, add a 1-* relation between the new book and '" + LITTLE + "', and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: [])");

        VampireBook eclipse = createEclipse(txStats);
        Publisher little = getPublisherByName(domainRoot, LITTLE);
        eclipse.setPublisher(little);
        little.removeBooksPublished(eclipse);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(little) : "Modified objects should NOT contain " + little;
        assert !txStats.getDirectlyModifiedObjects().contains(little) : "Directly modified objects should NOT contain " + little;

        System.out.println(txStats);
    }

    @Atomic
    public static void test12(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book, add a 1-* relation between the new book and '" + LITTLE + "', and then change relation to '" + ARCHIBALD + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + ARCHIBALD + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Publisher little = getPublisherByName(domainRoot, LITTLE);
        Publisher archibald = getPublisherByName(domainRoot, ARCHIBALD);
        eclipse.setPublisher(little);
        eclipse.setPublisher(archibald);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(little) : "Modified objects should NOT contain " + little;
        assert !txStats.getDirectlyModifiedObjects().contains(little) : "Directly modified objects should NOT contain " + little;
        assert txStats.getModifiedObjects().contains(archibald) : "Modified objects should contain " + archibald;
        assert !txStats.getDirectlyModifiedObjects().contains(archibald) : "Directly modified objects should NOT contain " + archibald;

        System.out.println(txStats);
    }

    @Atomic
    public static void test13(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its *-* relation with '" + STEPH + "' and '" + MAYOR + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MAYOR + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Author mayor = getAuthorByName(domainRoot, MAYOR);
        Author steph = getAuthorByName(domainRoot, STEPH);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(steph) : "Modified objects should contain " + steph;
        assert !txStats.getDirectlyModifiedObjects().contains(steph) : "Directly modified objects should NOT contain " + steph;
        assert txStats.getModifiedObjects().contains(mayor) : "Modified objects should contain " + mayor;
        assert !txStats.getDirectlyModifiedObjects().contains(mayor) : "Directly modified objects should NOT contain " + mayor;

        System.out.println(txStats);
    }

    @Atomic
    public static void test14(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and modify its *-* relation with '" + STEPH + "' and '" + MAYOR + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MAYOR + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Author mayor = getAuthorByName(domainRoot, MAYOR);
        Author steph = getAuthorByName(domainRoot, STEPH);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(steph) : "Modified objects should contain " + steph;
        assert !txStats.getDirectlyModifiedObjects().contains(steph) : "Directly modified objects should NOT contain " + steph;
        assert txStats.getModifiedObjects().contains(mayor) : "Modified objects should contain " + mayor;
        assert !txStats.getDirectlyModifiedObjects().contains(mayor) : "Directly modified objects should NOT contain " + mayor;

        System.out.println(txStats);
    }

    @Atomic
    public static void test15(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and 1) add a *-* relation with '" + STEPH + "' and '" + MAYOR + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MAYOR + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Author mayor = getAuthorByName(domainRoot, MAYOR);
        Author steph = getAuthorByName(domainRoot, STEPH);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(mayor);
        eclipse.removeAuthors(steph);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert txStats.getModifiedObjects().contains(steph) : "Modified objects should contain " + steph;
        assert !txStats.getDirectlyModifiedObjects().contains(steph) : "Directly modified objects should NOT contain " + steph;
        assert txStats.getModifiedObjects().contains(mayor) : "Modified objects should contain " + mayor;
        assert !txStats.getDirectlyModifiedObjects().contains(mayor) : "Directly modified objects should NOT contain " + mayor;

        System.out.println(txStats);
    }

    @Atomic
    public static void test16(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book and add a *-* relation with '" + STEPH + "' and '" + MAYOR + "' and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [])");

        VampireBook eclipse = createEclipse(txStats);
        Author mayor = getAuthorByName(domainRoot, MAYOR);
        Author steph = getAuthorByName(domainRoot, STEPH);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(mayor);
        eclipse.removeAuthors(steph);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(steph) : "Modified objects should contain " + steph;
        assert !txStats.getDirectlyModifiedObjects().contains(steph) : "Directly modified objects should NOT contain " + steph;
        assert !txStats.getModifiedObjects().contains(mayor) : "Modified objects should contain " + mayor;
        assert !txStats.getDirectlyModifiedObjects().contains(mayor) : "Directly modified objects should NOT contain " + mayor;

        System.out.println(txStats);
    }

    @Atomic
    public static void test17(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Create a new book, add a *-* relation with '" + STEPH + "' and '" + MAYOR + "', and then change relation to '" + BRAM_STOKER + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + BRAM_STOKER + "']; RCL: ['AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txStats);
        Author mayor = getAuthorByName(domainRoot, MAYOR);
        Author steph = getAuthorByName(domainRoot, STEPH);
        Author bStoker = getAuthorByName(domainRoot, BRAM_STOKER);
        eclipse.addAuthors(mayor);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(mayor);
        eclipse.removeAuthors(steph);
        eclipse.addAuthors(bStoker);

        assert !txStats.getModifiedObjects().contains(eclipse) : "Modified objects should NOT contain " + eclipse;
        assert !txStats.getDirectlyModifiedObjects().contains(eclipse) : "Directly modified objects should NOT contain " + eclipse;
        assert !txStats.getModifiedObjects().contains(steph) : "Modified objects should contain " + steph;
        assert !txStats.getDirectlyModifiedObjects().contains(steph) : "Directly modified objects should NOT contain " + steph;
        assert !txStats.getModifiedObjects().contains(mayor) : "Modified objects should contain " + mayor;
        assert !txStats.getDirectlyModifiedObjects().contains(mayor) : "Directly modified objects should NOT contain " + mayor;
        assert txStats.getModifiedObjects().contains(bStoker) : "Modified objects should contain " + bStoker;
        assert !txStats.getDirectlyModifiedObjects().contains(bStoker) : "Directly modified objects should NOT contain " + bStoker;

        System.out.println(txStats);
    }

    @Atomic
    public static void test18(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Change the relation between '" + FEW_MOON + "' and '" + TWOLIGHTS + "' to '" + FEW_MOON + "' and '" + DRACULA + "'\n\t"
                + "(New: []; DM: []; M: ['" + FEW_MOON + "', '" + DRACULA + "', '" + TWOLIGHTS + "']; "
                + "RCL: ['VampireBookToVampireBook' (removed), 'VampireBookToVampireBook' (changed)])");


        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        VampireBook twoLights = getVampireBookByName(domainRoot, TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(domainRoot, DRACULA);
        fewMoon.setPrequel(dracula);

        assert txStats.getNewObjects().isEmpty() : "New object should be empty";
        assert txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;
        assert txStats.getModifiedObjects().contains(twoLights) : "Modified objects should contain " + twoLights;
        assert !txStats.getDirectlyModifiedObjects().contains(twoLights) : "Directly modified objects should NOT contain " + twoLights;
        assert txStats.getModifiedObjects().contains(dracula) : "Modified objects should contain " + dracula;
        assert !txStats.getDirectlyModifiedObjects().contains(dracula) : "Directly modified objects should NOT contain " + dracula;

        System.out.println(txStats);
    }

    @Atomic
    public static void test19(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Change the relation between '" + FEW_MOON + "' and '" + TWOLIGHTS + "' to '" + FEW_MOON + "' and '" + DRACULA + "'. "
                + "Then change the name of '" + TWOLIGHTS + "' to tWoLiGhTs\n\t"
                + "(New: []; DM: []; M: ['" + FEW_MOON + "', '" + DRACULA + "', '" + TWOLIGHTS + "']; "
                + "RCL: ['VampireBookToVampireBook' (removed), 'VampireBookToVampireBook' (changed)])");


        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        VampireBook twoLights = getVampireBookByName(domainRoot, TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(domainRoot, DRACULA);
        fewMoon.setPrequel(dracula);
        twoLights.setBookName("tWoLiGhTs");

        assert txStats.getNewObjects().isEmpty() : "New object should be empty";
        assert txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;
        assert txStats.getModifiedObjects().contains(twoLights) : "Modified objects should contain " + twoLights;
        assert txStats.getDirectlyModifiedObjects().contains(twoLights) : "Directly modified objects should contain " + twoLights;
        assert txStats.getModifiedObjects().contains(dracula) : "Modified objects should contain " + dracula;
        assert !txStats.getDirectlyModifiedObjects().contains(dracula) : "Directly modified objects should NOT contain " + dracula;

        System.out.println(txStats);
    }

    @Atomic
    public static void test20(DomainRoot domainRoot) {
        TxStats txStats = Transaction.TxLocal.getTxLocal().getTxStats();
        pritTest(new Throwable().getStackTrace()[0].getMethodName().substring(7).toUpperCase(),
                "Change the relation '" + FEW_MOON + "' <-> '" + TWOLIGHTS + "' to '" + FEW_MOON + "' <-> '" + DRACULA + "' "
                + "and then back again to '" + FEW_MOON + "' <-> '" + TWOLIGHTS + "'. Finally change the name of '" + TWOLIGHTS + "' to tWoLiGhTs\n\t"
                + "(New: []; DM: ['" + TWOLIGHTS + "']; M: ['" + TWOLIGHTS + "']; RCL: [])");


        VampireBook fewMoon = getVampireBookByName(domainRoot, FEW_MOON);
        VampireBook twoLights = getVampireBookByName(domainRoot, TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(domainRoot, DRACULA);
        fewMoon.setPrequel(dracula);
        fewMoon.setPrequel(twoLights);
        twoLights.setBookName("tWoLiGhTs");

        assert txStats.getNewObjects().isEmpty() : "New object should be empty";
        assert !txStats.getModifiedObjects().contains(fewMoon) : "Modified objects should NOT contain " + fewMoon;
        assert !txStats.getDirectlyModifiedObjects().contains(fewMoon) : "Directly modified objects should NOT contain " + fewMoon;
        assert txStats.getModifiedObjects().contains(twoLights) : "Modified objects should contain " + twoLights;
        assert txStats.getDirectlyModifiedObjects().contains(twoLights) : "Directly modified objects should contain " + twoLights;
        assert !txStats.getModifiedObjects().contains(dracula) : "Modified objects should NOT contain " + dracula;
        assert !txStats.getDirectlyModifiedObjects().contains(dracula) : "Directly modified objects should NOT contain " + dracula;

        System.out.println(txStats);
    }

    private static void pritTest(String test, String desc) {
        System.out.println("############################ " + test + " ############################\n\t" + desc);
    }

    @Atomic
    public static void reset(DomainRoot domainRoot) {
        removeAll(domainRoot);
        init(domainRoot);
        //printAll(domainRoot);
    }

    @Atomic
    private static void init(DomainRoot domainRoot) {
        // Authors
        Author steph = new Author(STEPH, 83);
        domainRoot.addTheAuthors(steph);
        Author mayor = new Author(MAYOR, 83);
        domainRoot.addTheAuthors(mayor);
        Author bStoker = new Author(BRAM_STOKER, 125);
        domainRoot.addTheAuthors(bStoker);

        // Publishers
        Publisher little = new Publisher(LITTLE);
        domainRoot.addThePublishers(little);
        Publisher archibald = new Publisher(ARCHIBALD);
        domainRoot.addThePublishers(archibald);

        // Books
        VampireBook twolights = new VampireBook(TWOLIGHTS, 0.42, true);
        domainRoot.addTheBooks(twolights);
        VampireBook fewMoons = new VampireBook(FEW_MOON, 0.84, true);
        domainRoot.addTheBooks(fewMoons);
        Book dracula = new VampireBook(DRACULA, 12.42, false);
        domainRoot.addTheBooks(dracula);

        // Publisher with books
        little.addBooksPublished(twolights);
        little.addBooksPublished(fewMoons);
        archibald.addBooksPublished(dracula);

        // Authors with books
        steph.addBooks(twolights);
        steph.addBooks(fewMoons);
        mayor.addBooks(twolights);
        mayor.addBooks(fewMoons);
        bStoker.addBooks(dracula);

        // VampireBook to VampireBook
        twolights.setSequel(fewMoons);
    }

    @Atomic
    private static void removeAll(DomainRoot domainRoot) {
        for (Book book : domainRoot.getTheBooks()) {
            domainRoot.removeTheBooks(book);
        }

        for (Author author : domainRoot.getTheAuthors()) {
            domainRoot.removeTheAuthors(author);
        }

        for (Publisher publisher : domainRoot.getThePublishers()) {
            domainRoot.removeThePublishers(publisher);
        }
    }

    @SuppressWarnings("unused")
    @Atomic
    private static void printAll(DomainRoot domainRoot) {
        for (Book book : domainRoot.getTheBooks()) {
            System.out.println(book);
        }

        for (Author author : domainRoot.getTheAuthors()) {
            System.out.println(author);
        }

        for (Publisher publisher : domainRoot.getThePublishers()) {
            System.out.println(publisher);
        }
    }

    @Atomic
    public static Author getAuthorByName(DomainRoot domainRoot, String authorName) {
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getName().equals(authorName)) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    public static Book getBookByName(DomainRoot domainRoot, String bookName) {
        for (Book book : domainRoot.getTheBooks()) {
            if (book.getBookName().equals(bookName)) {
                return book;
            }
        }
        return null;
    }

    @Atomic
    public static VampireBook getVampireBookByName(DomainRoot domainRoot, String bookName) {
        return (VampireBook) getBookByName(domainRoot, bookName);
    }

    @Atomic
    public static Publisher getPublisherByName(DomainRoot domainRoot, String publisherName) {
        for (Publisher publisher : domainRoot.getThePublishers()) {
            if (publisher.getPublisherName().equals(publisherName)) {
                return publisher;
            }
        }
        return null;
    }
}
