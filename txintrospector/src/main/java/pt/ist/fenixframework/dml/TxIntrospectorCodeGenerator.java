package pt.ist.fenixframework.dml;

import java.io.PrintWriter;

/**
 * Code generator used for TxIntrospector information gathering.
 * Disabled by default. You can enable it by adding
 * <params>
 *     <ptIstTxIntrospectorEnable>true</ptIstTxIntrospectorEnable>
 * </params>
 * to the configuration section of the dml-maven-plugin plugin in your pom.xml.
 * 
 * Note that this code generator will only work with backends which use TxStats
 * as its TxIntrospector.
 * 
 */
public class TxIntrospectorCodeGenerator extends DAPCodeGenerator {
    public static final String TXINTROSPECTOR_ON_CONFIG_KEY =
        pt.ist.fenixframework.txintrospector.TxIntrospector.TXINTROSPECTOR_ON_CONFIG_KEY;
    public static final String TXINTROSPECTOR_ON_CONFIG_VALUE =
        pt.ist.fenixframework.txintrospector.TxIntrospector.TXINTROSPECTOR_ON_CONFIG_VALUE;

    private static final String TXSTATS_FULL_CLASS =
        pt.ist.fenixframework.txintrospector.TxStats.class.getName();
    private static final String TX_STATS_INSTANCE = "((" + TXSTATS_FULL_CLASS + ")"
	    + pt.ist.fenixframework.FenixFramework.class.getName() + ".getTransaction().getTxIntrospector())";

    private final boolean enabled;

    public TxIntrospectorCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
        String param = compArgs.getParams().get(TXINTROSPECTOR_ON_CONFIG_KEY);
        enabled = (param != null) && param.trim().equalsIgnoreCase(TXINTROSPECTOR_ON_CONFIG_VALUE);
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
    protected void generateSetterBody(String setterName, Slot slot, PrintWriter out) {
        super.generateSetterBody(setterName, slot, out);

        generateSetterTxIntrospectorStatement(slot, out);
    }

    protected void generateSetterTxIntrospectorStatement(Slot slot, PrintWriter out) {
        if (enabled) {
            onNewline(out);
            print(out, TX_STATS_INSTANCE + ".addModifiedObject(this);");
        }
    }

    @Override
    protected void generateBackEndIdClassBody(PrintWriter out) {
        if (enabled) {
            // add parameter in static initializer block
            newline(out);
            newBlock(out);
            print(out, "setParam(\"" + TXINTROSPECTOR_ON_CONFIG_KEY + "\", \"" +TXINTROSPECTOR_ON_CONFIG_VALUE + "\");");
            closeBlock(out);
        }
        super.generateBackEndIdClassBody(out);
    }
}
