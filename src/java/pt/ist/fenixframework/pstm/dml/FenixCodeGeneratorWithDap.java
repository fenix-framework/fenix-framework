package pt.ist.fenixframework.pstm.dml;

import java.io.PrintWriter;
import dml.CompilerArgs;
import dml.DomainModel;

public class FenixCodeGeneratorWithDap extends FenixCodeGenerator {
    public FenixCodeGeneratorWithDap (CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        print(out, "pt.ist.fenixframework.pstm.DataAccessPatterns.noteGetAccess(this, \"");
        print(out, slotName);
        println(out, "\");");
        super.generateGetterBody(slotName, typeName, out);
    }
	
}
