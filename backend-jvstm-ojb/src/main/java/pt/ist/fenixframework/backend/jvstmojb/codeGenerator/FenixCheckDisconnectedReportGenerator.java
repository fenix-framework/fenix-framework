package pt.ist.fenixframework.backend.jvstmojb.codeGenerator;

import java.io.PrintWriter;
import java.util.Iterator;

import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;

public class FenixCheckDisconnectedReportGenerator extends FenixCodeGeneratorOneBoxPerObject {

    public FenixCheckDisconnectedReportGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected void generateCheckDisconnected(DomainClass domClass, PrintWriter out) {
        newline(out);
        printMethod(out, "protected", "boolean", "checkDisconnected");
        startMethodBody(out);
        println(out, "java.util.List<String> relationList = new java.util.ArrayList<String>();");
        println(out, "boolean result = checkDisconnected(relationList);");
        println(out, "doCheckDisconnectedAction(relationList);");
        println(out, "return result;");
        endMethodBody(out);

        newline(out);
        printMethod(out, "protected", "boolean", "checkDisconnected", "java.util.List connectedRelationList");
        startMethodBody(out);

        if (domClass.hasSuperclass()) {
            println(out, "super.checkDisconnected(connectedRelationList);");
        }

        Iterator<Role> roleSlotsIter = domClass.getRoleSlots();
        while (roleSlotsIter.hasNext()) {
            Role role = roleSlotsIter.next();

            if (role.getName() != null) {
                onNewline(out);

                print(out, "if (");
                if (role.getMultiplicityUpper() == 1) {
                    print(out, "has");
                } else {
                    print(out, "hasAny");
                }
                print(out, capitalize(role.getName()));
                println(out, String.format("()) connectedRelationList.add(\"%s\"); ", role.getName()));
            }
        }

        println(out, "return connectedRelationList.isEmpty();");
        endMethodBody(out);
    }

}
