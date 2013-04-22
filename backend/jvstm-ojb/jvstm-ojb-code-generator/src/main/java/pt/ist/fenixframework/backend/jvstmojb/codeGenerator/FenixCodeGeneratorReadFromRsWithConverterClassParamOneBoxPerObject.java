package pt.ist.fenixframework.backend.jvstmojb.codeGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;

public class FenixCodeGeneratorReadFromRsWithConverterClassParamOneBoxPerObject extends FenixCodeGeneratorOneBoxPerObject {

    private static final Map<String, File> packageMapper = new HashMap<String, File>();

    public FenixCodeGeneratorReadFromRsWithConverterClassParamOneBoxPerObject(final CompilerArgs compilerArgs,
            final DomainModel domainModel) {
        super(compilerArgs, domainModel);
        InputStream inputStream;
        try {
            inputStream = getClass().getResourceAsStream("/.dmlProjectPackageMapper");
            final String contents = read(new InputStreamReader(inputStream));
            for (final String line : contents.split("\n")) {
                final int sindex = line.indexOf(' ');
                final String packageName = line.substring(0, sindex);
                final String packageDir = packageName.replace('.', File.separatorChar);
                final String srcDir = postProcessSrcDir(line.substring(sindex + 1));
                final String domainSrcDir = srcDir + File.separatorChar + packageDir;
                final File file = new File(domainSrcDir);
                packageMapper.put(packageName, file);
            }
        } catch (IOException e) {
        }
    }

    protected String postProcessSrcDir(final String srcDir) {
        return srcDir;
    }

    public static String read(final InputStreamReader fileReader) throws IOException {
        try {
            char[] buffer = new char[4096];
            final StringBuilder fileContents = new StringBuilder();
            for (int n = 0; (n = fileReader.read(buffer)) != -1; fileContents.append(buffer, 0, n)) {
                ;
            }
            return fileContents.toString();
        } finally {
            fileReader.close();
        }
    }

    @Override
    protected File getDirectoryFor(String packageName) {
        final File dir = getPackageDir(packageName);
        return dir == null ? super.getDirectoryFor(packageName) : dir;
    }

    private File getPackageDir(final String packageName) {
        final File file = packageMapper.get(packageName);
        if (file == null) {
            final int i = packageName.lastIndexOf('.');
            final String parentPackageName = packageName.substring(0, i);
            final File dir = getPackageDir(parentPackageName);
            return dir == null ? null : new File(dir.getAbsolutePath() + File.separator + packageName.substring(i + 1));
        }
        return file;
    }

}
