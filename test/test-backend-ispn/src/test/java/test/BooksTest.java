package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.InfinispanConfig;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class BooksTest {

    private static final int NUMBER_ELEMENTS = 600;
    private static final int DIVIDE_RATIO = 10;
    private static final int KEY_ONE = (NUMBER_ELEMENTS / 2) % (NUMBER_ELEMENTS / DIVIDE_RATIO);
    private static final int KEY_TWO = ((NUMBER_ELEMENTS / 2) - 1) % (NUMBER_ELEMENTS / DIVIDE_RATIO);
    private static final int KEY_THREE = NUMBER_ELEMENTS * 2;
    private static final String ISPN_CONFIG_FILE = "ispn.xml";

    @Test
    @Atomic
    public void test01() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        Set<Book> books = domainRoot.getTheBooks();
        Set<Author> authors = domainRoot.getTheAuthors();
        Set<Publisher> publishers = domainRoot.getThePublishers();

        assertTrue(books.size() == NUMBER_ELEMENTS);
        assertTrue(authors.size() == NUMBER_ELEMENTS);
        assertTrue(publishers.size() == NUMBER_ELEMENTS);

        int[] countBook = initArrayCount();
        for (Book book : books) {
            countBook[book.getId()] = countBook[book.getId()] + 1;
        }

        int[] countAuthor = initArrayCount();
        int[] countAuthorSecondary = initArrayCount();
        for (Author author : authors) {
            countAuthor[author.getId()] = countAuthor[author.getId()] + 1;
            countAuthorSecondary[author.getAge()] = countAuthorSecondary[author.getAge()] + 1;
        }

        int[] countPublishers = initArrayCount();
        for (Publisher publisher : publishers) {
            countPublishers[publisher.getId()] = countPublishers[publisher.getId()] + 1;
        }

        checkArrayCount(countBook);
        checkArrayCount(countAuthorSecondary);
        checkArrayCountClashes(countAuthor);
        checkArrayCount(countPublishers);
    }

    @Test
    public void test02() {
        int[] ids = doTest02part1();
        doTest02part2(ids);
    }

    @Test
    public void test03() {
        doTest03part01();
        doTest03part02();
    }

    @Before
    public void init() {
        InfinispanConfig config = new InfinispanConfig();
        config.setIspnConfigFile(ISPN_CONFIG_FILE);
        config.appNameFromString("fenix-framework-test-backend-ispn");
        FenixFramework.initialize(config);
        populate();
    }

    @After
    public void shutdown() {
        FenixFramework.shutdown();
    }

    @Atomic
    private void populate() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        for (int i = 0; i < NUMBER_ELEMENTS; i++) {
            domainRoot.addTheBooks(new Book(i, i));
            domainRoot.addTheAuthors(new Author(i % (NUMBER_ELEMENTS / DIVIDE_RATIO), i));
            domainRoot.addThePublishers(new Publisher(i));
        }
    }

    @Atomic
    private Book getBookById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Book book : domainRoot.getTheBooks()) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    @Atomic
    private Publisher getPublisherById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Publisher publisher : domainRoot.getThePublishers()) {
            if (publisher.getId() == id) {
                return publisher;
            }
        }
        return null;
    }

    @Atomic
    private void printAll() {
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
    private Author getAuthorById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getId() == id) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    private Author getAuthorByIdAndAge(int id, int age) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getAge() == age && author.getId() == id) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    private int[] doTest02part1() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        Book book = domainRoot.getTheBooksById(NUMBER_ELEMENTS / DIVIDE_RATIO);
        domainRoot.removeTheBooks(book);

        Publisher publisher = getPublisherById(0);
        domainRoot.removeThePublishers(publisher);

        Set<Author> authors = domainRoot.getTheAuthorsById(KEY_ONE);
        Author author = authors.iterator().next();
        domainRoot.removeTheAuthors(author);

        authors = domainRoot.getTheAuthorsById(KEY_TWO);

        int[] result = new int[2 + authors.size()];
        result[0] = publisher.getId();
        result[1] = author.getAge();

        Set<Author> copyAuthors = new HashSet<Author>();
        for (Author a : authors) {
            copyAuthors.add(a);
        }
        int upperBound = 2 + copyAuthors.size();

        Iterator<Author> iter = copyAuthors.iterator();
        for (int i = 2; i < upperBound; i++) {
            author = iter.next();
            result[i] = author.getAge();
            domainRoot.removeTheAuthors(author);
        }

        return result;
    }

    @Atomic
    private void doTest02part2(int[] ids) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        assertTrue(domainRoot.getTheBooksById(NUMBER_ELEMENTS / DIVIDE_RATIO) == null);
        assertTrue(getBookById(NUMBER_ELEMENTS / DIVIDE_RATIO) == null);

        Publisher publisher = getPublisherById(ids[0]);
        assertTrue(publisher == null);

        Set<Author> authors = domainRoot.getTheAuthorsById(KEY_ONE);
        assertTrue(authors.size() == (DIVIDE_RATIO - 1));
        for (Author author : authors) {
            assertTrue(author.getId() == KEY_ONE);
            assertTrue(author.getAge() != ids[1]);
        }
        assertTrue(getAuthorByIdAndAge(KEY_ONE, ids[1]) == null);

        authors = domainRoot.getTheAuthorsById(KEY_TWO);
        assertTrue(authors.size() == 0);
        for (int i = 2; i < ids.length; i++) {
            assertTrue(getAuthorByIdAndAge(KEY_TWO, ids[i]) == null);
        }
    }

    @Atomic
    private void doTest03part01() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        domainRoot.addTheBooks(new Book(KEY_THREE, KEY_THREE));
        domainRoot.addTheBooks(domainRoot.getTheBooksById(KEY_TWO + 1));

        domainRoot.addTheAuthors(new Author(KEY_THREE, NUMBER_ELEMENTS * 10));
        Iterator<Author> iter = domainRoot.getTheAuthorsById(KEY_TWO - 4).iterator();
        Author duplicate = null;
        for (int i = 0; i < DIVIDE_RATIO / 2; i++) {
            duplicate = iter.next();
        }
        domainRoot.addTheAuthors(duplicate);

        domainRoot.addThePublishers(new Publisher(KEY_THREE));
        domainRoot.addThePublishers(getPublisherById(KEY_TWO + 1));
    }

    @Atomic
    private void doTest03part02() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        assertTrue(domainRoot.getTheBooksById(KEY_THREE).getId() == KEY_THREE);
        assertTrue(domainRoot.getTheBooks().size() == (NUMBER_ELEMENTS + 1));

        assertTrue(domainRoot.getTheAuthorsById(KEY_THREE).contains(getAuthorByIdAndAge(KEY_THREE, NUMBER_ELEMENTS * 10)));
        assertTrue(domainRoot.getTheAuthors().size() == (NUMBER_ELEMENTS + 1));

        assertTrue(domainRoot.getThePublishers().contains(getPublisherById(KEY_THREE)));
        assertTrue(domainRoot.getThePublishers().size() == (NUMBER_ELEMENTS + 1));
    }

    private void checkArrayCount(int[] arrayCount) {
        for (int i = 0; i < arrayCount.length; i++) {
            assertTrue(arrayCount[i] == 1);
        }
    }

    private void checkArrayCountClashes(int[] arrayCount) {
        for (int i = 0; i < (NUMBER_ELEMENTS / DIVIDE_RATIO); i++) {
            assertTrue(arrayCount[i] == DIVIDE_RATIO);
        }
    }

    private int[] initArrayCount() {
        int[] count = new int[NUMBER_ELEMENTS];
        for (int i = 0; i < count.length; i++) {
            count[i] = 0;
        }
        return count;
    }
}
