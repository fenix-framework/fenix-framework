package pt.ist.fenixframework.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixframework.core.exception.SpecifiedDmlFileNotFoundException;

public class DmlFile {
    private URL url;

    private String relative;

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
	List<DmlFile> dmlFileList = new ArrayList<DmlFile>();
	for (String dmlFileName : dmlFilesField.trim().split("\\s*,\\s*")) {
	    if (StringUtils.isNotEmpty(dmlFileName)) {
	    // if (dmlFileName != null && !dmlFileName.isEmpty()) { // alternative to not depend on StringUtils :-/
		URL dmlFileUrl = Thread.currentThread().getContextClassLoader().getResource(dmlFileName);
		if (dmlFileUrl == null) {
		    throw new SpecifiedDmlFileNotFoundException(dmlFileName);
		}
		dmlFileList.add(new DmlFile(dmlFileUrl, dmlFileName));
	    }
	}
	return dmlFileList;
    }
}
