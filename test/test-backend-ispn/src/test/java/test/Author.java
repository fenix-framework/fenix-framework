package test;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;
import pt.ist.fenixframework.Atomic;

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

    protected Author(LocalityHints hints, int id, int age) {
        super(hints);
        setId(id);
        setAge(age);
    }

    @Atomic
    public static Author createAuthorGroupedByAge(int id, int age) {
        LocalityHints localityHints = new LocalityHints();
        localityHints.addHint(Constants.GROUP_ID, String.valueOf(age));
        return new Author(localityHints, id, age);
    }
    
    @Atomic
    public static Author createAuthor(int id, int age) {
	return new Author(id, age);
    }

    @Override
    public String toString() {
        return "Author " + getId();
    }
}
