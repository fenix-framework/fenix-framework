package pt.ist.fenixframework.pstm.dml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import dml.CompilerArgs;
import dml.DomainModel;

public class FenixCodeGeneratorReadFromRsWithConverterClassParam extends FenixCodeGenerator {

    private static final Map<String, File> packageMapper = new HashMap<String, File>();

    public FenixCodeGeneratorReadFromRsWithConverterClassParam(final CompilerArgs compilerArgs, final DomainModel domainModel) {
	super(compilerArgs, domainModel);
	InputStream inputStream;
	try {
	    inputStream = getClass().getResourceAsStream("/.dmlProjectPackageMapper");
	    final String contents = FenixCodeGeneratorReadFromRsWithConverterClassParamOneBoxPerObject.read(new InputStreamReader(inputStream));
	    for (final String line : contents.split("\n")) {
		final int sindex = line.indexOf(' ');
		final String packageName = line.substring(0, sindex);
		final String packageDir = packageName.replace('.', File.separatorChar);
		final String srcDir = line.substring(sindex + 1);
		final String domainSrcDir = srcDir + File.separatorChar + packageDir;
		final File file = new File(domainSrcDir);
		packageMapper.put(packageName, file);
	    }
	} catch (IOException e) {
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
