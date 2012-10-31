package dml;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import antlr.ANTLRException;
import antlr.collections.AST;

public class DmlCompiler {

    public static void main(String[] args) throws Exception {
	CompilerArgs compArgs = new CompilerArgs(args);

	DomainModel model = getDomainModel(compArgs);

	CodeGenerator generator = compArgs.generatorClass.getConstructor(CompilerArgs.class, DomainModel.class).newInstance(
		compArgs, model);

	generator.generateCode();
    }

    /**
     * Use {@link #getDomainModelForURLs(Class, List)}
     */
    @Deprecated
    public static DomainModel getDomainModel(CompilerArgs compArgs) throws ANTLRException {
	return getDomainModelForURLs(compArgs.domainModelClass, compArgs.dmls);
    }

    /**
     * Use {@link #getDomainModelForURLs(Class, List)}
     */
    @Deprecated
    public static DomainModel getDomainModel(Class<? extends DomainModel> modelClass, String[] dmlFiles) throws ANTLRException {
	return getDomainModel(modelClass, Arrays.asList(dmlFiles));
    }

    /**
     * Use {@link #getDomainModelForURLs(Class, List)}
     */
    @Deprecated
    public static DomainModel getDomainModel(Class<? extends DomainModel> modelClass, List<String> dmlFiles)
	    throws ANTLRException {
	ArrayList<URL> urls = new ArrayList<URL>();
	for (String filename : dmlFiles) {
	    try {
		if (filename.startsWith("jar:file")) {
		    urls.add(new URL(filename));
		} else {
		    urls.add(new File(filename).toURI().toURL());
		}
	    } catch (MalformedURLException mue) {
		System.err.println("Cannot convert " + filename + " into an URL.  Ignoring it...");
	    }
	}

	return getDomainModelForURLs(modelClass, urls);
    }

    public static DomainModel getDomainModelForURLs(Class<? extends DomainModel> modelClass, List<URL> dmlFilesURLs)
	    throws ANTLRException {
	return getDomainModelForURLs(modelClass, dmlFilesURLs, false);
    }

    public static DomainModel getDomainModelForURLs(Class<? extends DomainModel> modelClass, List<URL> dmlFilesURLs,
	    boolean checkForMissingExternals) throws ANTLRException {
	DmlTreeParser walker = new DmlTreeParser();
	DomainModel model = null;

	try {
	    model = modelClass.newInstance();
	} catch (Exception exc) {
	    throw new Error("Could not create an instance of the domain model class", exc);
	}

	for (URL dmlFileURL : dmlFilesURLs) {
	    InputStream urlStream = null;
	    DataInputStream in = null;
	    try {
		urlStream = dmlFileURL.openStream();
		in = new DataInputStream(new BufferedInputStream(urlStream));

		DmlLexer lexer = new DmlLexer(in);
		DmlParser parser = new DmlParser(lexer);
		parser.domainDefinitions();
		AST t = parser.getAST();
		// System.out.println(t.toStringTree());

		// ASTFrame fr = new ASTFrame("Tree Viewer", t);
		// fr.setVisible(true);

		walker.domainDefinitions(t, model, dmlFileURL);
		// System.out.println("Model = " + model);
	    } catch (IOException ioe) {
		System.err.println("Cannot read " + dmlFileURL + ".  Ignoring it...");
		// System.exit(3);
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (IOException ioe) {
		    }
		}
		if (urlStream != null) {
		    try {
			urlStream.close();
		    } catch (IOException ioe) {
		    }
		}
	    }
	}

	model.finalizeDomain(checkForMissingExternals);
	return model;
    }
}
