package pt.ist.fenixframework.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Converter {
    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - BEGIN */

    // REGARDING RESOURCES

    public static URL resourceToURL(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource);
    }

    public static URL[] resourceToURLArray(String resource) {
        return resourcesToURLArray(new String[]{resource});
    }

    public static URL[] resourcesToURLArray(String [] resources) {
        final URL[] urls = new URL[resources.length];
        for (int i = 0; i < resources.length; i++) {
            urls[i] = resourceToURL(resources[i]);
            if (urls[i] == null) {
                throw new RuntimeException("cannot find DML for resource '" + resources[i] + "'");
            }
        }
        return urls;
    }

    // REGARDING FILENAMES

    public static URL filenameToURL(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return null;
            }

            return file.toURI().toURL();
        } catch (MalformedURLException mue) {
            return null;
        }
    }

    public static URL[] filenameToURLArray(String filename) {
        return filenamesToURLArray(new String[]{filename});
    }

    public static URL[] filenamesToURLArray(String [] filenames) {
        final URL[] urls = new URL[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            urls[i] = filenameToURL(filenames[i]);
            if (urls[i] == null) {
                throw new RuntimeException("cannot find DML for file'" + filenames[i] + "'");
            }
        }
        return urls;
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - END */
}
