package pt.ist.fenixframework.example.helloworld;

public class HelloWorldApplication extends HelloWorldApplication_Base {
    public void sayHello() {
        for (Person p : getPersonSet()) {
            System.out.println("Hello " + p.getName());
        }
    }    
}
