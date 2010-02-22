
package dml;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import antlr.*;
import antlr.collections.AST;

import antlr.debug.misc.ASTFrame;

public class DmlCompiler {

    public static void main(String[] args) throws Exception {
        CompilerArgs compArgs = new CompilerArgs(args);

        DomainModel model = getDomainModel(compArgs);

        CodeGenerator generator = 
            compArgs
            .generatorClass
            .getConstructor(CompilerArgs.class, DomainModel.class)
            .newInstance(compArgs, model);

        generator.generateCode();
    }

    public static DomainModel getDomainModel(CompilerArgs compArgs) throws ANTLRException {
        return getDomainModel(compArgs.domainModelClass, compArgs.domainSpecFilenames);
    }

//     public static DomainModel getFenixDomainModel(String[] dmlFiles) throws ANTLRException {
//         return getDomainModel(FenixDomainModel.class, Arrays.asList(dmlFiles));
//     }

//     public static DomainModel getFenixDomainModelForURL(URL dmlFileURL) throws ANTLRException {
//         return getDomainModelForURLs(FenixDomainModel.class, Collections.singletonList(dmlFileURL));
//     }

    public static DomainModel getDomainModel(Class<? extends DomainModel> modelClass, String[] dmlFiles) throws ANTLRException {
        return getDomainModel(modelClass, Arrays.asList(dmlFiles));
    }

    public static DomainModel getDomainModel(Class<? extends DomainModel> modelClass, 
                                             List<String> dmlFiles) throws ANTLRException {
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String filename : dmlFiles) {
            try {
                urls.add(new File(filename).toURI().toURL());
            } catch (MalformedURLException mue) {
                System.err.println("Cannot convert " + filename + " into an URL.  Ignoring it...");
            }
        }

        return getDomainModelForURLs(modelClass, urls);
    }

    public static DomainModel getDomainModelForURLs(Class<? extends DomainModel> modelClass, 
                                                    List<URL> dmlFilesURLs) throws ANTLRException {
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
                //System.out.println(t.toStringTree());
                
                //ASTFrame fr = new ASTFrame("Tree Viewer", t);
                //fr.setVisible(true);  
                
                walker.domainDefinitions(t, model, dmlFileURL);
                //System.out.println("Model = " + model);
            } catch (IOException ioe) {
                System.err.println("Cannot read " + dmlFileURL + ".  Ignoring it...");
                //System.exit(3);
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException ioe) {}
                }
                if (urlStream != null) {
                    try { urlStream.close(); } catch (IOException ioe) {}
                }
            }
        }

        model.finalizeDomain();
        return model;
    }
}
