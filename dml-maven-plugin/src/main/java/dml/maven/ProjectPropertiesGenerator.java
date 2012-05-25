package dml.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ProjectPropertiesGenerator {
    
    public static void create(File output, Properties properties) throws IOException {
        output.getParentFile().mkdirs();
        properties.store(new FileWriter(output), "project.properties");
    }
    
}
