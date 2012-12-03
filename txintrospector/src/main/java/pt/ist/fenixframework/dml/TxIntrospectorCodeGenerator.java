package pt.ist.fenixframework.dml;

import java.io.PrintWriter;

/**
 * Code generator used for TxIntrospector information gathering.
 * Disabled by default. You can enable it by adding
 * <params>
 *     <pt.ist.fenixframework.txintrospector>on</pt.ist.fenixframework.txintrospector>
 * </params>
 * to the configuration section of the dml-maven-plugin plugin in your pom.xml.
 */
public class TxIntrospectorCodeGenerator extends DAPCodeGenerator {
    public static final String TXINTROSPECTOR_COMPILE_ARG = "pt.ist.fenixframework.txintrospector";
    public static final String TXINTROSPECTOR_COMPILE_ENABLE = "on";

    private static final String TXSTATS_FULL_CLASS =
        pt.ist.fenixframework.txintrospector.TxStats.class.getName();
    private static final String TX_STATS_INSTANCE =
        pt.ist.fenixframework.Transaction.TxLocal.class.getCanonicalName() + ".getTxLocal().getTxStats()";

    private final boolean enabled;

    public TxIntrospectorCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
        String state = compArgs.getParams().get(TXINTROSPECTOR_COMPILE_ARG);
        enabled = (state != null) && state.equalsIgnoreCase(TXINTROSPECTOR_COMPILE_ENABLE);
    }

    @Override
    protected void generateDefaultRelationListeners(Role role, PrintWriter out) {
        super.generateDefaultRelationListeners(role, out);

        if (enabled) {
            print(out, ", " + TXSTATS_FULL_CLASS + ".STATS_LISTENER");
        }
    }

    @Override
    protected void generateBaseClassConstructorsBody(DomainClass domClass, PrintWriter out) {
        super.generateBaseClassConstructorsBody(domClass, out);

        if (enabled) {
            if (domClass.getSuperclass() == null) {
                onNewline(out);
                print(out, TX_STATS_INSTANCE + ".addNewObject(this);");
            }
        }
    }

    @Override
    protected void generateSetterBody(DomainClass domainClass, String setterName, Slot slot, PrintWriter out) {
        super.generateSetterBody(domainClass, setterName, slot, out);

        generateSetterTxIntrospectorStatement(domainClass, slot, out);
    }

    protected void generateSetterTxIntrospectorStatement(DomainClass domainClass, Slot slot, PrintWriter out) {
        if (enabled) {
            onNewline(out);
            print(out, TX_STATS_INSTANCE + ".addModifiedObject(this);");
        }
    }
}
