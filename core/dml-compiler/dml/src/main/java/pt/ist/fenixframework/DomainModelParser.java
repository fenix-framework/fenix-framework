package pt.ist.fenixframework;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.dml.DmlLexer;
import pt.ist.fenixframework.dml.DmlParser;
import pt.ist.fenixframework.dml.DmlTreeParser;
import pt.ist.fenixframework.dml.DomainModel;
import antlr.ANTLRException;
import antlr.collections.AST;

public class DomainModelParser {
    private static final Logger logger = LoggerFactory.getLogger(DomainModelParser.class);

    public static DomainModel getDomainModel(List<URL> dmlFilesURLs) {
        return getDomainModel(dmlFilesURLs, false);
    }

    public static DomainModel getDomainModel(List<URL> dmlFilesURLs, boolean checkForMissingExternals) {
        if (logger.isTraceEnabled()) {
            StringBuilder message = new StringBuilder();
            for (URL url : dmlFilesURLs) {
                message.append(url + "  ***  ");
            }

            logger.trace("dmlFilesUrls = " + message.toString());
        }

        DmlTreeParser walker = new DmlTreeParser();
        DomainModel model = new DomainModel();

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
            } catch (ANTLRException e) {
                throw new Error(e);
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
