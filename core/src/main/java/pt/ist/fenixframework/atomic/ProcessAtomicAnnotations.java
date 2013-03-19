package pt.ist.fenixframework.atomic;

import java.io.File;
import java.util.ArrayList;

import pt.ist.esw.advice.ProcessAnnotations;
import pt.ist.fenixframework.Atomic;

/**
 * This class provides a command-line API to process atomic annotations. This will typically be used by programmers that do not
 * use the provided plugins (currently available for Maven).
 */
public class ProcessAtomicAnnotations {
    /**
     * Command-line API to start the processing.
     * 
     * @param args one or more class files or class-containing directories.
     * 
     */
    public static void main(String args[]) throws Exception {
        if (args.length == 0) {
            System.err.println("Syntax: ProcessAtomicAnnotations <class files or dirs>");
            System.exit(1);
        }

        ArrayList<File> files = new ArrayList<File>();
        for (String arg : args) {
            files.add(new File(arg));
        }

        new ProcessAnnotations(new ProcessAnnotations.ProgramArgs(Atomic.class, AtomicContextFactory.class, files)).process();
    }

}
