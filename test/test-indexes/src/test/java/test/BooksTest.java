package test;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

@RunWith(JUnit4.class)
public class BooksTest {

    private static final Logger logger = LoggerFactory.getLogger(BooksTest.class);

    private static final String STEPH = "Steph";
    private static final String MEH = "Meh";
    private static final String BRAM_STOKER = "Bram Stoker";

    private static final String LITTLE = "Little";
    private static final String ARCHIBALD = "Archibald";

    private static final String TWOLIGHTS = "TwoLights";
    private static final String FEW_MOON = "Few Moon";
    private static final String DRACULA = "Dracula";

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    @Test
    @Atomic
    public void test01() {
	Set<Author> stokers = FenixFramework.getDomainRoot().getTheAuthorsByName(BRAM_STOKER);
	
	assertTrue(stokers.size() == 2);
	assertTrue(stokers.contains(getAuthorByNameAndAge(BRAM_STOKER, 125)));
	assertTrue(stokers.contains(getAuthorByNameAndAge(BRAM_STOKER, 60)));
	
	Set<Author> meh = FenixFramework.getDomainRoot().getTheAuthorsByName(MEH);
	assertTrue(meh.size() == 1);
	assertTrue(meh.contains(getAuthorByName(MEH)));
	
	Set<Author> empty = FenixFramework.getDomainRoot().getTheAuthorsByName("EMPTY");
	assertTrue(empty.size() == 0);
    }

    @Test
    @Atomic
    public void test02() {
	Book dracula = FenixFramework.getDomainRoot().getTheBooksByBookName(DRACULA);
	
	assertTrue(dracula.equals(getBookByName(DRACULA)));
	
	Set<Author> stokers = dracula.getAuthorsByName(BRAM_STOKER);
	assertTrue(stokers.size() == 1);
	assertTrue(stokers.contains(getAuthorByNameAndAge(BRAM_STOKER, 125)));
	
	Set<Author> archibald = dracula.getAuthorsByName(ARCHIBALD);
	assertTrue(archibald.size() == 0);
	
	Publisher pub = dracula.getPublisher();
	assertTrue(pub.getPublisherName().equals(ARCHIBALD));
	
	assertTrue(pub.getBooksPublishedByBookName(DRACULA) == dracula);
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
	Publisher little = FenixFramework.getDomainRoot().getThePublishersByPublisherName(LITTLE);
	
	assertTrue(little.equals(getPublisherByName(LITTLE)));
	
	Set<Book> booksPublishedByLittle = little.getBooksPublished();
	assertTrue(booksPublishedByLittle.size() == 2);
	assertTrue(booksPublishedByLittle.contains(getBookByName(FEW_MOON)));
	assertTrue(booksPublishedByLittle.contains(getBookByName(TWOLIGHTS)));
	
	assertTrue(little.getBooksPublishedByBookName(TWOLIGHTS).equals(getBookByName(TWOLIGHTS)));
	assertTrue(little.getBooksPublishedByBookName(FEW_MOON).equals(getBookByName(FEW_MOON)));
	
	getBookByName(TWOLIGHTS).removePublisher();
    }
    
    @Atomic
    public void test03part2() {
	Publisher little = FenixFramework.getDomainRoot().getThePublishersByPublisherName(LITTLE);
	
	assertTrue(little.equals(getPublisherByName(LITTLE)));
	
	Set<Book> booksPublishedByLittle = little.getBooksPublished();
	assertTrue(booksPublishedByLittle.size() == 1);
	assertTrue(booksPublishedByLittle.contains(getBookByName(FEW_MOON)));

	assertTrue(little.getBooksPublishedByBookName(TWOLIGHTS) == null);
	assertTrue(little.getBooksPublishedByBookName(FEW_MOON).equals(getBookByName(FEW_MOON)));
	
	booksPublishedByLittle.clear();
    }
    
    @Atomic
    public void test03part3() {
	Publisher little = FenixFramework.getDomainRoot().getThePublishersByPublisherName(LITTLE);
	
	assertTrue(little.equals(getPublisherByName(LITTLE)));
	
	Set<Book> booksPublishedByLittle = little.getBooksPublished();
	assertTrue(booksPublishedByLittle.size() == 0);
	
	assertTrue(little.getBooksPublishedByBookName(TWOLIGHTS) == null);
	assertTrue(little.getBooksPublishedByBookName(FEW_MOON) == null);
    }
    
    @Before
    @Atomic
    public void init() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        // Authors
        Author steph = new Author(STEPH, 83);
        domainRoot.addTheAuthors(steph);
        Author meh = new Author(MEH, 83);
        domainRoot.addTheAuthors(meh);
        Author bStoker = new Author(BRAM_STOKER, 125);
        domainRoot.addTheAuthors(bStoker);
        Author bStokerSon = new Author(BRAM_STOKER, 60);
        domainRoot.addTheAuthors(bStokerSon);

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

    @After
    @Atomic
    public void reset() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

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

    @Atomic
    @SuppressWarnings("unused")
    private static void printAll() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

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
    public static Author getAuthorByNameAndAge(String authorName, int age) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getAge() == age && author.getName().equals(authorName)) {
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
