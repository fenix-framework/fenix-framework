package ${package};

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

public class Main {

    // FenixFramework will try automatic initialization when first accessed
    public static void main(String [] args) {
        try {
            applicationCodeGoesHere();
        } finally {
            // ensure an orderly shutdown
            FenixFramework.shutdown();
        }
    }

    public static void applicationCodeGoesHere() {
        someTransaction();
    }

    @Atomic
    public static void someTransaction() {
        System.out.println("FenixFramework's root object is: " + FenixFramework.getDomainRoot());
    }
}
