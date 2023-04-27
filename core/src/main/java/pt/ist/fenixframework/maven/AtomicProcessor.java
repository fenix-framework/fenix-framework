package pt.ist.fenixframework.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.esw.advice.ProcessAnnotations;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.atomic.AtomicContextFactory;

import java.io.File;
import java.util.Arrays;

public class AtomicProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AtomicProcessor.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Wrong number of arguments for AtomicProcessor. Should be <projectType> <pathToClassDirectory>");
            logger.error("Got " + Arrays.toString(args));
            throw new RuntimeException("Wrong number of arguments for AtomicProcessor");
        }

        String projectType = args[0];
        File classesDirectory = new File(args[1]);

        if (projectType.equals("pom")) {
            logger.info("Cannot process pom projects");
            return;
        }

        try {
            new ProcessAnnotations(
                    new ProcessAnnotations.ProgramArgs(Atomic.class, AtomicContextFactory.class, classesDirectory)) {
                @Override
                protected void processClassFile(File classFile) {
                    if (!classFile.getName().contains("_Base")) {
                        super.processClassFile(classFile);
                    }
                };
            }.process();

        } catch (Exception e) {
            logger.error("Something went wrong with the post processing", e);
            throw new RuntimeException("Something went wrong with the post processing", e);
        }
    }

}
