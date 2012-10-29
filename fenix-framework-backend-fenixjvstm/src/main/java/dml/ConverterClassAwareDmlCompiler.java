package dml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConverterClassAwareDmlCompiler {

    public static void main(String[] args) throws Exception {
	final int c = args.length;
	final int offset = c - 3; 
	final File dir = new File(args[offset]); 
	final String[] baseArgs; 
	if (dir.exists() && dir.isDirectory()) { 
	    final List<String> dmlFilenames = new ArrayList<String>(); 
	    for (final File file : dir.listFiles()) { 
		if (file.isFile() && file.getName().endsWith(".dml")) { 
		    dmlFilenames.add(file.getCanonicalPath()); 
		} 
	    } 
	    Collections.sort(dmlFilenames);
            List<URL> pluginDmlUrlList = PluginDmlUrlLoader.getPluginDmlUrlList();
	    baseArgs = new String[c - 3 + dmlFilenames.size()+pluginDmlUrlList.size()]; 
	    System.arraycopy(args, 0, baseArgs, 0, offset); 
	    int i = offset;
            for(URL pluginDmlUrl : pluginDmlUrlList) {
                baseArgs[i++] = pluginDmlUrl.toExternalForm();
            }
	    for (final String dmlFilename : dmlFilenames) { 
		baseArgs[i++] = dmlFilename; 
	    } 
	} else { 
	    baseArgs = Arrays.copyOfRange(args, 0, c - 2); 
	} 

        DmlCompiler.main(baseArgs);
    }

}
