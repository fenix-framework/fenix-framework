package pt.ist.fenixframework.example.helloworld;

import jvstm.Atomic;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;

public class Main {
    public static void main(final String[] args) {
        Config config = new Config() {{ 
            domainModelPath = "/helloworld.dml";
            dbAlias = "//localhost:3306/test"; 
            dbUsername = "test";
            dbPassword = "test";
            rootClass = HelloWorldApplication.class;
        }};
        FenixFramework.initialize(config);

        addNewPeople(args);

        greetAll();
    }

    @Atomic
    private static void addNewPeople(String[] args) {
        HelloWorldApplication app = FenixFramework.getRoot();
        for (String name : args) {
            app.addPerson(new Person(name, app));
        }
    }

    @Atomic
    private static void greetAll() {
        HelloWorldApplication app = FenixFramework.getRoot();
        app.sayHello();
    }
}
