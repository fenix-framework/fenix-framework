package x.y;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class A extends A_Base {
    
    public  A() {
        super();
    }
    
    public static void main(String[] args) {
        try {
            test();
            Thread.sleep(3000);
            show();
            Thread.sleep(3000);
            show();
            Thread.sleep(3000);
            show();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            FenixFramework.shutdown();
        }
    }

    @Atomic
    static void test() {
        System.out.println(FenixFramework.getTransactionManager());
        A a = new A();
        a.setName("xpto");
        a.setPai(FenixFramework.getDomainRoot());
    }

    @Atomic
    static void show() {
        for (A a : FenixFramework.getDomainRoot().getTheAs()) {
            System.out.println("NAME: " + a.getName());
        }
    }

}
