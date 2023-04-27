package pt.ist.fenixframework;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import pt.ist.fenixframework.dml.DomainModel;

public class DomainModelParserTest {

    @Test
    @Disabled
    /*
     * Allowing keywords in the DML is likely not to be possible.
     * 
     * Test ignored until a better error can be reported.
     */
    public void testPackageExternal() throws IOException {
        URL url = dml("package pt.ist.fenixframework.test;");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
    }

    @Test
    public void testCommentEOF() throws IOException {
        URL url = dml("class stuff;\n//");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
    }

    @Test
    public void testValidDML() {
        URL url = getResource("TestResource1.dml");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
    }

    @Test
    public void testInvalidDML() {
        URL url = getResource("TestResource2.dml");
        assertThrows(Error.class, () -> DomainModelParser.getDomainModel(Collections.singletonList(url)));
    }

    @Test
    public void testInvalidURL() throws IOException {
        File file = File.createTempFile("dml", "tmp");
        URL url = file.toURI().toURL();
        file.delete();
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
    }

    // Note: i know there is a resource to url function but i want to keep package's tests separated
    private URL getResource(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    private URL dml(String contents) throws IOException {
        File file = File.createTempFile("dml", "tmp");
        file.deleteOnExit();
        try (Writer writer = new FileWriter(file)) {
            writer.write(contents);
        }
        return file.toURI().toURL();
    }
}
