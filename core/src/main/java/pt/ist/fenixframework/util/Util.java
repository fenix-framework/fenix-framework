package pt.ist.fenixframework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class Util {

    public static Class<?> loadClass(String name) {
        for (ClassLoader loader : classLoaders()) {
            Class cl = tryLoad(name, loader);
            if (cl != null) {
                return cl;
            }
        }
        return null;
    }

    public static InputStream loadResource(String name) {
        File file = new File(name);
        if (file.exists()) {
            try {
                return new FileInputStream(name);
            } catch (FileNotFoundException e) {
                //ignored
            }
        }
        for (ClassLoader loader : classLoaders()) {
            InputStream inputStream = tryLoadResource(name, loader);
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    public static Class<?> tryLoad(String name, ClassLoader loader) {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            //no-op
        }
        return null;
    }

    public static InputStream tryLoadResource(String name, ClassLoader loader) {
        return loader.getResourceAsStream(name);
    }

    private static final ClassLoader[] classLoaders() {
        return new ClassLoader[]{Thread.currentThread().getContextClassLoader(),
                Util.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()};
    }

}
