package eu.cloudtm;

import java.util.ArrayList;
import java.util.List;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
public class StringUtils {

   private static final char SIZE_SEPARATOR = ':';
   private static final char INIT_CHAR = '#';

   public static String encode(String... strings) {
      StringBuilder builder = new StringBuilder(256);
      builder.append(INIT_CHAR);
      for (String s : strings) {
         if (s == null) {
            builder.append(SIZE_SEPARATOR);
         } else {
            builder.append(String.valueOf(s.length())).append(SIZE_SEPARATOR).append(s);
         }
      }

      return builder.toString();
   }

   public static String[] decode(String string) {
      if (string == null || !string.startsWith(Character.toString(INIT_CHAR))) {
         return new String[] {string};
      }
      List<String> list = new ArrayList<String>(8);
      int index = 1;

      while (index < string.length()) {
         int sizeStartIndex = index;
         while (SIZE_SEPARATOR != string.charAt(index)) {
            index++;
         }
         if (sizeStartIndex == index) {
            //null string
            list.add(null);
            index++;
         } else {
            int length = Integer.parseInt(string.substring(sizeStartIndex, index));
            list.add(string.substring(index + 1, index + 1 + length));
            index += length + 1;
         }
      }

      return list.toArray(new String[list.size()]);
   }
}
