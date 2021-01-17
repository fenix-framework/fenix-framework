package pt.ist.fenixframework;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.dml.CodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DmlCompilerException;
import pt.ist.fenixframework.dml.DomainModel;

public class DmlCompiler {
    /**
     * Runs the DML compiler
     * 
     * This is the main entry point for running the DML compiler, from the command line. This
     * method will create the {@link CompilerArgs} from the command line parameters, and then invoke {@link DmlCompiler#compile}.
     * 
     * @param args All the compiler's parameters
     * 
     * @see CompilerArgs
     */
    public static void main(String[] args) throws DmlCompilerException {
        CompilerArgs compArgs = new CompilerArgs(args);
        compile(compArgs);
    }

    /**
     * Runs the DML compiler
     * 
     * This is the main entry point for, programmatically, running the DML compiler. The compiler
     * will first create the domain model, and then run the code generator.
     * 
     * @param compArgs All the compiler's parameters
     * @return The {@link DomainModel}
     * 
     * @see CompilerArgs
     */
    public static DomainModel compile(CompilerArgs compArgs) throws DmlCompilerException {
        try {
            DomainModel model = getDomainModel(compArgs);
            CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class)
                    .newInstance(compArgs, model);
            generator.generateCode();
            return model;
        } catch (Exception e) {
            throw new DmlCompilerException(e);
        }
    }

    public static DomainModel getDomainModel(CompilerArgs compArgs) throws DmlCompilerException {
        // IMPORTANT: external specs must be first.  The order is important for the DmlCompiler
        List<URL> dmlSpecs = new ArrayList<URL>(compArgs.getExternalDomainSpecs());
        dmlSpecs.addAll(compArgs.getLocalDomainSpecs());
        return DomainModelParser.getDomainModel(dmlSpecs, false);
    }
}
