package test;

public class Book extends Book_Base {

    public Book(int id, double price) {
	super();
        setId(id);
        setPrice(price);

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
        return "Book " + getId();
    }
}
