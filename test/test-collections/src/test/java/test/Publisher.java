package test;

public class Publisher extends Publisher_Base {

    public  Publisher() {
        super();

        toString();
    }

    public Publisher(int id) {
        this();
        setId(id);
    }

    @Override
    public String toString() {
        return "Publisher " + getId();
    }
}
