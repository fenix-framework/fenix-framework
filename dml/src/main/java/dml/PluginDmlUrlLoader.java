package dml;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginDmlUrlLoader {
    
    final static String[] pluginDmlFilenamePaths = new String[] {
            "file-plugin.dml",
            "luceneSearch-plugin.dml",
            "remote-plugin.dml",
            "scheduler-plugin.dml"
        };
    
    public static List<URL> getPluginDmlUrlList() {
        List<URL> pluginDmlUrlList = new ArrayList<URL>();
        for(String pluginDmlFilenamePath : pluginDmlFilenamePaths) {
            URL filePluginDmlUrl = getPluginDmlUrl(pluginDmlFilenamePath);
            if(filePluginDmlUrl != null) {
                pluginDmlUrlList.add(filePluginDmlUrl);
            }
        }
        return pluginDmlUrlList;
    }

    public static URL getPluginDmlUrl(String dmlFilenamePath) {
        return PluginDmlUrlLoader.class.getClassLoader().getResource(dmlFilenamePath);
    }
}
