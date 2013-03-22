package eu.cloudtm;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class AppUtils {

    @Atomic
    public void mymethod() {

        System.out.println(FenixFramework.isInitialized());
        System.out.println(FenixFramework.getTransaction());
        System.out.println("HEREHEHERHEHREHERHREH");
        System.out.println("HEREHEHERHEHREHERHREH");
        System.out.println("HEREHEHERHEHREHERHREH");
        System.out.println("HEREHEHERHEHREHERHREH");
    }

}
