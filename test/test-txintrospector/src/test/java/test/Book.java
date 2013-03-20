package test;

public abstract class Book extends Book_Base {

    public Book() {
        this(null);
    }

    public Book(eu.cloudtm.LocalityHints hints) {
        super(hints);

        toString();
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
}
