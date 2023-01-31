package pt.ist.fenixframework.dml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static String createRandomDir() throws IOException {
        Path path = Files.createTempDirectory(Paths.get("/tmp"), "compilerTest");
        path.toFile().deleteOnExit();
        return path.toFile().getAbsolutePath();
    }

    public static boolean compareDir(Path path1, Path path2) throws IOException {
        if (!Files.exists(path1) || !Files.exists(path2)) {
            return false;
        }

        if (!Files.isDirectory(path1) || !Files.isDirectory(path2)) {
            return false;
        }

        for (Path file : listAllFiles(path1)) {
            Path relative = path1.relativize(file);
            Path current = Paths.get(path2.toString(), relative.toString());

            if (!Files.exists(current))
                return false;
            if (Files.isDirectory(current))
                return false;
            if (!compareFiles(file, current))
                return false;
        }

        return true;
    }

    public static List<Path> listAllFiles(Path root) {
        List<Path> found = new ArrayList<>();

        if (Files.isDirectory(root)) {
            for (String children : root.toFile().list()) {
                found.addAll(listAllFiles(Paths.get(root.toString() + "/" + children)));
            }
        } else {
            found.add(root);
        }

        return found;
    }

    public static boolean compareFiles(Path p1, Path p2) throws IOException {
        if (!Files.exists(p1) || !Files.exists(p2)) {
            return false;
        }

        if (Files.isDirectory(p1) || Files.isDirectory(p2)) {
            return false;
        }

        if (p1.equals(p2)) {
            return true;
        }

        if (Files.size(p1) != Files.size(p2)) {
            return false;
        }

        InputStream in1 = null;
        InputStream in2 = null;
        try {
            in1 = Files.newInputStream(p1);
            in2 = Files.newInputStream(p2);

            int expectedByte = in1.read();
            while (expectedByte != -1) {
                if (expectedByte != in2.read()) {
                    return false;
                }
                expectedByte = in1.read();
            }
            if (in2.read() != -1) {
                return false;
            }
            return true;
        } finally {
            if (in1 != null) {
                try {
                    in1.close();
                } catch (IOException e) {
                    return false;
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }
}
