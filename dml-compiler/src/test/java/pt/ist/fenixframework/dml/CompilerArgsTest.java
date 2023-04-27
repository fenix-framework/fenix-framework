package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.util.Converter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.ist.fenixframework.dml.TestUtils.createRandomDir;

public class CompilerArgsTest {
    // Note: I cant really test bad inputs because they will halt the execution of the tests instead of throwing an exception

    @Test
    public void compilerArgsExternalDmlTest() throws IOException, DmlCompilerException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-externalDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath() };
        CompilerArgs ca = new CompilerArgs(args);
        assertTrue(ca.getLocalDomainSpecs().isEmpty());
        assertTrue(ca.getExternalDomainSpecs().contains(Converter.resourceToURL("DMLTest1.dml")));
        assertTrue(ca.isExternalDefinition(Converter.resourceToURL("DMLTest1.dml")));
    }

    @Test
    public void compilerArgsLocalDmlTest() throws IOException, DmlCompilerException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath() };
        CompilerArgs ca = new CompilerArgs(args);
        assertTrue(ca.getExternalDomainSpecs().isEmpty());
        assertTrue(ca.getLocalDomainSpecs().contains(Converter.resourceToURL("DMLTest1.dml")));
    }

    @Test
    public void compilerArgsDefaultLocalDmlTest() throws IOException, DmlCompilerException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, Converter.resourceToURL("DMLTest1.dml").getPath() };
        CompilerArgs ca = new CompilerArgs(args);
        assertTrue(ca.getExternalDomainSpecs().isEmpty());
        assertTrue(ca.getLocalDomainSpecs().contains(Converter.resourceToURL("DMLTest1.dml")));
    }

    // Note: cannot test project name because of directory weirdness

    @Test
    public void compilerParamTest() throws IOException, DmlCompilerException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath(), "-param", "test=123" };
        CompilerArgs ca = new CompilerArgs(args);
        assertEquals("123", ca.getParams().get("test"));
    }

    @Test
    public void codeGeneratorTest() throws IOException, DmlCompilerException {
        String dir = createRandomDir();
        String[] args = { "-d", dir, "-localDmlSpec", Converter.resourceToURL("DMLTest1.dml").getPath(), "-generator",
                "pt.ist.fenixframework.dml.CustomCodeGenerator" };
        CompilerArgs ca = new CompilerArgs(args);
        assertEquals(CustomCodeGenerator.class, ca.getCodeGenerator());
    }
}
