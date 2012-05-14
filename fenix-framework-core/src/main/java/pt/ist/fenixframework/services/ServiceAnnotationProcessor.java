package pt.ist.fenixframework.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
// cannot use ActiveObject.class.getName() the value must be a constant
// expression BLEAH!
@SupportedAnnotationTypes({ "pt.ist.fenixframework.services.Service" })
public class ServiceAnnotationProcessor extends AbstractProcessor {

    static final String LOG_FILENAME = ".serviceAnnotationLog";
    static final String FIELD_SEPERATOR = " ";
    static final String ENTRY_SEPERATOR = "\n";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

	FileWriter fileWriter = null;
	try {
	    fileWriter = new FileWriter(LOG_FILENAME, true);

	    final Set<MethodSymbol> annotatedElements = (Set<MethodSymbol>) roundEnv.getElementsAnnotatedWith(Service.class);

	    for (final MethodSymbol methodElement : annotatedElements) {
		final ClassSymbol classSymbol = (ClassSymbol) methodElement.getEnclosingElement();
		String className = processClassName(classSymbol);
		fileWriter.write(className);
		fileWriter.write(FIELD_SEPERATOR);
		fileWriter.write(methodElement.getSimpleName().toString());
		fileWriter.write(ENTRY_SEPERATOR);
	    }
	} catch (IOException e) {
	    throw new Error(e);
	} finally {
	    if (fileWriter != null) {
		try {
		    fileWriter.close();
		} catch (IOException e) {
		    throw new Error(e);
		}
	    }
	}

	return true;
    }

    private String processClassName(ClassSymbol classSymbol) {
	Symbol symbol = classSymbol.getEnclosingElement();
	if (symbol instanceof ClassSymbol) {
	    return processClassName((ClassSymbol) symbol) + "$" + classSymbol.getSimpleName();
	}
	return classSymbol.getQualifiedName().toString();
    }

}
