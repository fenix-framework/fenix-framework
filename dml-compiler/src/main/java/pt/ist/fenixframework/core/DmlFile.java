package pt.ist.fenixframework.core;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ist.fenixframework.core.exception.SpecifiedDmlFileNotFoundException;

public class DmlFile {
    private final URL url;

    private final String relative;

    public DmlFile(URL url, String relative) {
        this.url = url;
        this.relative = relative;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DmlFile) {
            return url.equals(((DmlFile) obj).url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return relative != null ? relative : url.toExternalForm();
    }

    public static List<DmlFile> parseDependencyDmlFiles(String dmlFilesField) throws SpecifiedDmlFileNotFoundException {
        return parseDependencyDmlFiles(dmlFilesField, Thread.currentThread().getContextClassLoader());
    }

    public static List<DmlFile> parseDependencyDmlFiles(String dmlFilesField, ClassLoader loader)
            throws SpecifiedDmlFileNotFoundException {
        List<DmlFile> dmlFileList = new ArrayList<DmlFile>();

        // As dmlFilesField comes from the project properties it can be either null or empty
        // if the property key or the property value does not exist (respectively).
        // In that case default to an empty list as there are no files to parse
        if (dmlFilesField != null && !dmlFilesField.isEmpty()) {
            for (String dmlFileName : dmlFilesField.trim().split("\\s*,\\s*")) {
                if (dmlFileName != null && !dmlFileName.isEmpty()) {
                    URL dmlFileUrl = loader.getResource(dmlFileName);
                    if (dmlFileUrl == null) {
                        throw new SpecifiedDmlFileNotFoundException(dmlFileName);
                    }
                    dmlFileList.add(new DmlFile(dmlFileUrl, dmlFileName));
                }
            }
        }

        return dmlFileList;
    }
}
