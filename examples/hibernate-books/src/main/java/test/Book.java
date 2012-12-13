package test;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Indexed
public abstract class Book extends Book_Base {

    public  Book() {
        super();

        // commented out due to ogm initialization bug
        //getPublisher();
        //getAuthors();
    }

    @Field(name="bookName")
    @Override
    public String getBookName() {
        return super.getBookName();
    }

    @Field(name="price")
    @Override
    public double getPrice() {
        return super.getPrice();
    }

    //	@Override
    //	public String toString() {
    //		String authors = " authors";
    //		for (Author author : getAuthors()) {
    //			authors += " " + author.getName();
    //		}
    //		return "Book " + getBookName() + " price " + getPrice() + (getAuthors().isEmpty() ? "" : authors);
    //	}

    @Override
    public String toString() {
        return "Book " + getBookName();
    }

    @Override
    @IndexedEmbedded
    public test.Publisher getPublisher() {
        return super.getPublisher();
    }

    @Override
    @IndexedEmbedded
    public java.util.Set<test.Author> getAuthors() {
        return super.getAuthors();
    }
}
