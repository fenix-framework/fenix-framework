package test;

public class Author extends Author_Base {

    public  Author() {
        super();

        toString();
    }

    public Author(int id, int age) {
        this();
        setId(id);
        setAge(age);
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
        return "Author " + getId();
    }
}
