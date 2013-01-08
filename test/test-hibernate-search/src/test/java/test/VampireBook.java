package test;

import org.hibernate.search.annotations.Indexed;

@Indexed
public class VampireBook extends VampireBook_Base {

    public  VampireBook() {
        super();

        toString();
    }

    public VampireBook(String bookName, double price, boolean hasGlowingVampires) {
        this();
        setBookName(bookName);
        setPrice(price);
        setHasGlowingVampires(hasGlowingVampires);
    }

    //	@Override
    //	public String toString() {
    //		return super.toString() + " glows? " + getHasGlowingVampires()
    //				+ (getPrequel() == null ? "" : " prequel " + getPrequel().getBookName())
    //				+ (getSequel() == null ? "" : " sequel " + getSequel().getBookName());
    //	}

    @Override
    public String toString() {
        return "VampireBook " + getBookName();
    }
}
