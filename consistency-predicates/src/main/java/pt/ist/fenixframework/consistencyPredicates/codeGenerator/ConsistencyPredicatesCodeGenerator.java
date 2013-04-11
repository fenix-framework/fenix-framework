package pt.ist.fenixframework.consistencyPredicates.codeGenerator;

import java.io.PrintWriter;

import pt.ist.fenixframework.consistencyPredicates.runtime.ConsistencyChecks;
import pt.ist.fenixframework.dml.CodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

public abstract class ConsistencyPredicatesCodeGenerator extends CodeGenerator {

    public ConsistencyPredicatesCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        super.generateBaseClassBody(domClass, out);
        generateSlotConsistencyPredicates(domClass, out);
    }

    @Override
    protected void generateRoleSlotMethods(Role role, PrintWriter out) {
        super.generateRoleSlotMethods(role, out);

        if (role.needsMultiplicityChecks()) {
            generateMultiplicityConsistencyPredicate(role, out);
        }
    }

    protected void generateSlotConsistencyPredicates(DomainClass domClass, PrintWriter out) {
        if (domClass.hasSlotWithOption(Slot.Option.REQUIRED)) {
            generateRequiredConsistencyPredicate(domClass, out);
        }
    }

    protected void generateRequiredConsistencyPredicate(DomainClass domClass, PrintWriter out) {
        newline(out);
        println(out, "@pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicate");
        printMethod(out, "private", "boolean", "checkRequiredSlots");
        startMethodBody(out);

        for (Slot slot : domClass.getSlotsList()) {
            if (slot.hasOption(Slot.Option.REQUIRED)) {
                String slotName = slot.getName();

                print(out, ConsistencyChecks.class.getName() + ".checkRequired(this, \"");
                print(out, slotName);
                print(out, "\", get");
                print(out, capitalize(slotName));
                println(out, "());");
            }
        }
        print(out, "return true;");
        endMethodBody(out);
    }

    protected void generateMultiplicityConsistencyPredicate(Role role, PrintWriter out) {
        String slotName = role.getName();
        String slotAccessExpression = getSlotExpression(slotName);
        String capitalizedSlotName = capitalize(slotName);

        newline(out);
        println(out, "@pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicate");
        printMethod(out, "public final", "boolean", "checkMultiplicityOf" + capitalizedSlotName);
        startMethodBody(out);

        int lower = role.getMultiplicityLower();
        int upper = role.getMultiplicityUpper();

        if (lower > 0) {
            print(out, "if (");
            if (upper == 1) {
                print(out, "get");
                print(out, capitalizedSlotName);
                print(out, "() == null");
            } else {
                print(out, slotAccessExpression);
                print(out, ".size() < " + lower);
            }
            println(out, ") return false;");
        }

        if ((upper > 1) && (upper != Role.MULTIPLICITY_MANY)) {
            print(out, "if (");
            print(out, slotAccessExpression);
            print(out, ".size() > " + upper);
            println(out, ") return false;");
        }

        print(out, "return true;");
        endMethodBody(out);
    }

}
