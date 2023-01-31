package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.util.Converter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static pt.ist.fenixframework.dml.TestUtils.compareDir;
import static pt.ist.fenixframework.dml.TestUtils.createRandomDir;

public class CustomCodeGeneratorTest {
    @Test
    public void emptyClassTest() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        DmlCompiler.main(args);
        Path correct = (new File(Converter.resourceToURL("DMLTest1Custom").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void emptyClassNegativeTest() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        DmlCompiler.main(args);
        Path correct = (new File(Converter.resourceToURL("DMLTest2Custom").toURI())).toPath();
        assertFalse(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest2() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest2.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        DmlCompiler.main(args);
        Path correct = (new File(Converter.resourceToURL("DMLTest2Custom").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest3() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest3.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        DmlCompiler.main(args);
        Path correct = (new File(Converter.resourceToURL("DMLTest3Custom").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest4() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest4.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        DmlCompiler.main(args);
        Path correct = (new File(Converter.resourceToURL("DMLTest4Custom").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTestInvalidGeneratorClass() throws IOException {
        String dir = createRandomDir();
        String[] args =
                { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest4.dml").getPath(), "-generator", "invalid" };
        assertThrows(DmlCompilerException.class, () -> DmlCompiler.compile(new CompilerArgs(args)));
    }

}
