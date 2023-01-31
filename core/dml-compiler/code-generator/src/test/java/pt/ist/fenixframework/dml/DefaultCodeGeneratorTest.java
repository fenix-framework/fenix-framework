package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.util.Converter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.ist.fenixframework.dml.TestUtils.compareDir;
import static pt.ist.fenixframework.dml.TestUtils.createRandomDir;

public class DefaultCodeGeneratorTest {
    @Test
    public void emptyClassTest() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath() };
        DmlCompiler.compile(new CompilerArgs(args));
        Path correct = (new File(Converter.resourceToURL("DMLTest1").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void emptyClassNegativeTest() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath() };
        DmlCompiler.compile(new CompilerArgs(args));
        Path correct = (new File(Converter.resourceToURL("DMLTest2").toURI())).toPath();
        assertFalse(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest2() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest2.dml").getPath() };
        DmlCompiler.compile(new CompilerArgs(args));
        Path correct = (new File(Converter.resourceToURL("DMLTest2").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest3() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest3.dml").getPath() };
        DmlCompiler.compile(new CompilerArgs(args));
        Path correct = (new File(Converter.resourceToURL("DMLTest3").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

    @Test
    public void DMLTest4() throws IOException, DmlCompilerException, URISyntaxException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest4.dml").getPath() };
        DmlCompiler.compile(new CompilerArgs(args));
        Path correct = (new File(Converter.resourceToURL("DMLTest4").toURI())).toPath();
        assertTrue(compareDir(Paths.get(dir), correct));
    }

}
