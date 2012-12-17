package test;

import java.util.*;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchSupport;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class HibernateSearchBooksTest {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchBooksTest.class);

    private static final String STEPH = "Steph";
    private static final String MEH = "Meh";
    private static final String BRAM_STOKER = "Bram Stoker";

    private static final String LITTLE = "Little";
    private static final String ARCHIBALD = "Archibald";

    private static final String TWOLIGHTS = "TwoLights";
    private static final String FEW_MOON = "Few Moon";
    private static final String DRACULA = "Dracula";

    @Test
    @Atomic
    public void test01() {
        Collection<DomainObject> queryResults = performQuery(Author.class, "name", "stoker");

        assertTrue(queryResults.size() == 1);
        assertTrue(queryResults.contains(getAuthorByName(BRAM_STOKER)));
    }

    @Test
    @Atomic
    public void test02() {
        Collection<DomainObject> queryResults = performQuery(VampireBook.class, "bookName", "dracula");

        assertTrue(queryResults.size() == 1);
        assertTrue(queryResults.contains(getBookByName(DRACULA)));
    }

    @Test
    public void test03() {
        // Test done in multiple parts, to check that the indexes are being correctly updated after
        // each transaction
        test03part1();
        test03part2();
        test03part3();
    }

    @Atomic
    public void test03part1() {
        Collection<DomainObject> queryResults = performQuery(VampireBook.class, "publisher.id",
                getPublisherByName(LITTLE).getExternalId());

        assertTrue(queryResults.size() == 2);
        assertTrue(queryResults.contains(getBookByName(FEW_MOON)));
        assertTrue(queryResults.contains(getBookByName(TWOLIGHTS)));

        getBookByName(TWOLIGHTS).removePublisher();
    }

    @Atomic
    public void test03part2() {
        Collection<DomainObject> queryResults = performQuery(VampireBook.class, "publisher.id",
                getPublisherByName(LITTLE).getExternalId());

        assertTrue(queryResults.size() == 1);
        assertTrue(queryResults.contains(getBookByName(FEW_MOON)));

        getPublisherByName(LITTLE).getBooksPublished().clear();
    }

    @Atomic
    public void test03part3() {
        Collection<DomainObject> queryResults = performQuery(VampireBook.class, "publisher.id",
                getPublisherByName(LITTLE).getExternalId());

        assertTrue(queryResults.size() == 0);
    }

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

        logger.trace("performQuery result: " + matchingObjects);

        return matchingObjects;
    }

    @BeforeClass
    @Atomic
    public static void init() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        // Authors
        Author steph = new Author(STEPH, 83);
        domainRoot.addTheAuthors(steph);
        Author meh = new Author(MEH, 83);
        domainRoot.addTheAuthors(meh);
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
        meh.addBooks(twolights);
        meh.addBooks(fewMoons);
        bStoker.addBooks(dracula);

        // VampireBook to VampireBook
        twolights.setSequel(fewMoons);
    }

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    @Atomic
    public static Author getAuthorByName(String authorName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getName().equals(authorName)) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    public static Book getBookByName(String bookName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Book book : domainRoot.getTheBooks()) {
            if (book.getBookName().equals(bookName)) {
                return book;
            }
        }
        return null;
    }

    public static VampireBook getVampireBookByName(String bookName) {
        return (VampireBook) getBookByName(bookName);
    }

    @Atomic
    public static Publisher getPublisherByName(String publisherName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Publisher publisher : domainRoot.getThePublishers()) {
            if (publisher.getPublisherName().equals(publisherName)) {
                return publisher;
            }
        }
        return null;
    }
}
