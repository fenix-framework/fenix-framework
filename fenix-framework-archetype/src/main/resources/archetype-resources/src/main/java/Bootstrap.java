package ${package};

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;

public class Bootstrap {

  public static void init() {
    try {
      FenixFramework.initialize(new Config() {{
        domainModelPath = "/${artifactId}.dml";
        dbAlias = "//localhost:3306/${dbName}";
        dbUsername = "${dbUsername}";
        dbPassword = "";
        rootClass = ${package}.${rootClassname}.class;
      }});
    } catch(Error e) {
      
    }
  }
}