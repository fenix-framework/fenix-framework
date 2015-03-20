package pt.ist.fenixframework;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.dml.DomainModel;

@RunWith(JUnit4.class)
public class DomainModelParserTest {

    @Test
    public void testPackageExternal() throws IOException {
        URL url = dml("package org.fenixedu.external.users.domain;");
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
