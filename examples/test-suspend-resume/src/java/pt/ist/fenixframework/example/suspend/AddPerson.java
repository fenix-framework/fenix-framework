package pt.ist.fenixframework.example.suspend;

import pt.ist.fenixframework.FenixFramework;

import pt.ist.fenixframework.example.suspend.domain.App;
import pt.ist.fenixframework.example.suspend.domain.Person;

import jvstm.Atomic;
import java.util.Set;

public class AddPerson {

    public static void main(final String[] args) {
        Init.init();
        addPerson();
    }

    @Atomic
    private static void addPerson() {
        App app = FenixFramework.getRoot();

        Set<Person> people = app.getPersonSet();

        for (Person p : people) {
            System.out.println(p.getName());
        }

        app.addPerson(new Person("John Doe " + people.size()));
    }
}
