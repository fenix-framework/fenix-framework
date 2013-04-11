package test;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

public abstract class Book extends Book_Base {

    public Book() {
        super();

        toString();
    }

    @Field
    @Override
    public String getBookName() {
        return super.getBookName();
    }

    @Field
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
    public java.util.Set<test.Author> getAuthorSet() {
        return super.getAuthorSet();
    }
}
