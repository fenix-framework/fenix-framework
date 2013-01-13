package pt.ist.fenixframework.util;

import java.net.URL;

/**
 * Looks in the current class path for a particular file
 *
 * @author Pedro Ruivo
 * @since 2.0
 */
public class FileLookup {

   /**
    * Looks in the class path for a file. The file name is returned by the property {@param propertyName} value and
    * if this value is not found, it uses the {@param defaultFileName}
    *
    * @param propertyName     the property name that contains the file name to find
    * @param defaultFileName  the default file name (if the properties is not found)
    * @return                 the URL for the file defined by {@param fileName}
    */
   public static URL find(String propertyName, String defaultFileName) {
      String fileName;
      if (propertyName != null) {
         fileName = System.getProperty(propertyName, defaultFileName);
      } else {
         fileName = defaultFileName;
      }
      return find(fileName);
   }

   /**
    * @param fileName   the file name
    * @return           the URL for the file defined by {@param fileName}
    */
   public static URL find(String fileName) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
      if (url == null) {
         url = ClassLoader.getSystemClassLoader().getResource(fileName);
      }
      return url;
   }

}
