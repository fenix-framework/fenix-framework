package pt.ist.fenixframework.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import pt.ist.fenixframework.project.exception.SpecifiedDmlFileNotFoundException;

public class DmlFile {

    private static String SEPARATOR_CHAR = ",";
    private static String JAR_FILE_PREFIX = "jar:file:";

    private URL url;

    public DmlFile(String dmlFilePath) throws MalformedURLException {
	this(new URL(dmlFilePath));
    }

    public DmlFile(URL url) {
	this.url = url;
    }

    public URL getUrl() {
	return url;
    }

    public String getName() {
	return new File(url.getPath()).getName();
    }

    public boolean isPackaged() {
	return url.toExternalForm().startsWith(JAR_FILE_PREFIX);
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 83 * hash + (this.url != null ? this.url.hashCode() : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final DmlFile other = (DmlFile) obj;
	if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
	    return false;
	}
	return true;
    }

    public static LinkedHashSet<DmlFile> parseDependencyDmlFiles(String dmlFilesField) throws SpecifiedDmlFileNotFoundException {
	LinkedHashSet<DmlFile> dmlFileList = new LinkedHashSet<DmlFile>();
	if(dmlFilesField.contains(SEPARATOR_CHAR)) {
	    for(String dmlFile : dmlFilesField.split(SEPARATOR_CHAR)) {
		String dmlFileName = dmlFile.trim();
		URL dmlFileUrl = DmlFile.class.getResource("/"+dmlFileName);
		if(dmlFileUrl == null) {
		    throw new SpecifiedDmlFileNotFoundException(dmlFileName);
		}
		dmlFileList.add(new DmlFile(dmlFileUrl));
	    }
	} else {
	    String dmlFileName = dmlFilesField.trim();
	    URL dmlFileUrl = DmlFile.class.getResource("/"+dmlFileName);
	    dmlFileList.add(new DmlFile(dmlFileUrl));
	}
	return dmlFileList;
    }
    
    public String getString() {
        return getName();
    }
    
    public static LinkedHashSet<DmlFile> parseDmlFileList(List<String> dmlFilepathList) throws MalformedURLException {
        LinkedHashSet<DmlFile> dmlFileList = new LinkedHashSet<DmlFile>();
        for(String dmlFilepath : dmlFilepathList) {
            dmlFileList.add(new DmlFile(dmlFilepath));
        }
        return dmlFileList;
    }
    
}
