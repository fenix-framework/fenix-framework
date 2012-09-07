package pt.ist.fenixframework.dml;

/**
 *  Generic API that all code generators must implement.  These methods are invoked by the {@link
 *  pt.ist.fenixframework.DmlCompiler} during its execution.
 */
public interface CodeGenerator {
    public final String BACKEND_PACKAGE = "pt.ist.fenixframework.backend";
    public final String ABSTRACT_BACKEND_ID_CLASS = "BackEndId";
    public final String CURRENT_BACKEND_ID_CLASS = "CurrentBackEndId";
    // public final String CURRENT_BACKEND_ID_FULL_CLASS = BACKEND_PACKAGE + "." + CURRENT_BACKEND_ID_CLASS;

    /**
     * Generate the backend-specific code for the domain model.
     */
    public abstract void generateCode();
    /**
     *  Generate the class that identifies the backend to which this code generator creates the
     *  code.  The generated class must be named {@link
     *  pt.ist.fenixframework.backend.CurrentBackEndId} and extend the {@link
     *  pt.ist.fenixframework.backend.BackEndId} class.
     */
    public abstract void generateBackEndId();

}