package test;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Indexed
public class Author extends Author_Base {

    public  Author() {
        super();

        toString();
    }

    public Author(String name, int age) {
        this();
        setName(name);
        setAge(age);
    }

    @Field
    @Override
    public String getName() {
        return super.getName();
    }

    //	@Override
    //	public String toString() {
    //		String books = " of";
    //		for (Book book : getBooks()) {
    //			books += " " + book.getBookName();
    //		}
    //		return "Author " + getName() + " age " + getAge() + books;
    //	}

    @Override
    public String toString() {
        return "Author " + getName();
    }

    @Override
    @ContainedIn
    public java.util.Set<test.Book> getBooks() {
        return super.getBooks();
    }
}
