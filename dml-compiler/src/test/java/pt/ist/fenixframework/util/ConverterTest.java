package pt.ist.fenixframework.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static pt.ist.fenixframework.util.Converter.*;

public class ConverterTest {
    private File createFile() throws IOException {
        File file = File.createTempFile("filetourl", "tmp");
        file.deleteOnExit();
        try (Writer writer = new FileWriter(file)) {
            writer.write("package org.fenixedu.external.users.domain;");
        }
        return file;
    }

    @Test
    public void filenameToUrlTest() throws IOException {
        File file = createFile();
        URL url = filenameToURL(file.getAbsolutePath());
        assertEquals(file.toURI().toURL(), url);
    }

    @Test
    public void filenameToUrlNonExistentFileTest() throws IOException {
        URL url = filenameToURL("abc");
        assertNull(url);
    }

    @Test
    public void filenameToUrlArrayTest() throws IOException {
        File file = createFile();
        URL[] url = filenameToURLArray(file.getAbsolutePath());
        assertEquals(1, url.length);
        assertEquals(file.toURI().toURL(), url[0]);
    }

    @Test
    public void filenamesToUrlArrayTest() throws IOException {
        File file1 = createFile();
        File file2 = createFile();
        URL[] urls = filenamesToURLArray(new String[] { file1.getAbsolutePath(), file2.getAbsolutePath() });
        assertEquals(2, urls.length);
        assertEquals(file1.toURI().toURL(), urls[0]);
        assertEquals(file2.toURI().toURL(), urls[1]);
    }

    @Test
    public void filenameToUrlArrayExceptionTest() throws IOException {
        assertThrows(RuntimeException.class, () -> filenameToURLArray("abc"));
    }

    @Test
    public void filenamesToUrlArrayExceptionTest() throws IOException {
        assertThrows(RuntimeException.class, () -> filenamesToURLArray(new String[] { "abc" }));
    }

    @Test
    public void resourceToURLTest() {
        URL expected = Thread.currentThread().getContextClassLoader().getResource("TestResource1.dml");
        assertEquals(expected, resourceToURL("TestResource1.dml"));
    }

    @Test
    public void resourceToURLNonExistentFileTest() {
        assertNull(resourceToURL("abc"));
    }

    @Test
    public void resourceToURLArrayTest() {
        URL expected = Thread.currentThread().getContextClassLoader().getResource("TestResource1.dml");
        URL[] url = resourceToURLArray("TestResource1.dml");
        assertEquals(1, url.length);
        assertEquals(expected, url[0]);
    }

    @Test
    public void resourceToUrlArrayExceptionTest() {
        assertThrows(RuntimeException.class, () -> resourceToURLArray("abc"));
    }

    @Test
    public void resourcesToURLArrayTest() {
        URL expected1 = Thread.currentThread().getContextClassLoader().getResource("TestResource1.dml");
        URL expected2 = Thread.currentThread().getContextClassLoader().getResource("TestResource2.dml");
        URL[] urls = resourcesToURLArray(new String[] { "TestResource1.dml", "TestResource2.dml" });
        assertEquals(2, urls.length);
        assertEquals(expected1, urls[0]);
        assertEquals(expected2, urls[1]);
    }

    @Test
    public void resourcesToUrlArrayExceptionTest() {
        assertThrows(RuntimeException.class, () -> resourcesToURLArray(new String[] { "abc" }));
    }
}
