package pt.ist.fenixframework;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        URL url = dml("package org.fenixedu.external.users.domain;");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
    }

    @Test
    public void testCommentEOF() throws IOException {
        URL url = dml("class stuff;\n//");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(model);
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
