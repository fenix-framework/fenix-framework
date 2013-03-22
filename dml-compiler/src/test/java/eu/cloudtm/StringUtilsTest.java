package eu.cloudtm;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
@Test
public class StringUtilsTest {

   private static final Random RANDOM = new Random();
   private static final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxz!#$%&/()='«+`º~-.,;:_^ª`*»?}][{¬½§£@\\|";
   private static final int SYMBOLS_LENGTH = SYMBOLS.length();
   private static final int MAX_STRING_LENGTH = 255;

   public void testSingleString() {
      String randomString = generateRandomString();
      String[] decoded = StringUtils.decode(StringUtils.encode(randomString));
      Assert.assertEquals(decoded.length, 1, "Decode array size mismatch for " + randomString + ".");
      Assert.assertEquals(decoded[0], randomString, "Decode string equals mismatch for " + randomString + ".");
   }

   public void testMultipleStrings() {
      for (int i = 2; i < 10; ++i) {
         String[] randomStringToEncode = new String[i];
         for (int j = 0; j < i; ++j) {
            randomStringToEncode[j] = generateRandomString();
         }

         String[] decoded = StringUtils.decode(StringUtils.encode(randomStringToEncode));
         Assert.assertEquals(decoded.length, i, "Decode array size mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         for (int j = 0; j < i; ++j) {
            Assert.assertEquals(decoded[j], randomStringToEncode[j], "Decode string equals mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         }
      }
   }

   public void testNullString() {
      String[] decoded = StringUtils.decode(StringUtils.encode(new String[]{null}));
      Assert.assertEquals(decoded.length, 1, "Decode array size mismatch for null.");
      Assert.assertEquals(decoded[0], null, "Decode string equals mismatch for.");
   }

   public void testMultipleNullString() {
      String[]  toEncode = new String[]{"bla", null, "bla1", null, "", "null"};
      String[] decoded = StringUtils.decode(StringUtils.encode(toEncode));
      Assert.assertEquals(decoded.length, toEncode.length, "Decode array size mismatch for " + Arrays.toString(toEncode) + ".");
      for (int i = 0; i < toEncode.length; i++) {
      Assert.assertEquals(decoded[i], toEncode[i], "Decode string equals mismatch for " + Arrays.toString(toEncode) + ".");
      }
   }
   
   public void testOverheadWithOneCharString() {
      for (int i = 1; i < 10; ++i) {
         String[] randomStringToEncode = new String[i];
         for (int j = 0; j < i; ++j) {
            randomStringToEncode[j] = generateRandomString(1);
         }

         String encoded = StringUtils.encode(randomStringToEncode);

         System.out.println("Overhead for " + i + " strings with 1 char is " + (i * 1.0 / encoded.length()));
         
         String[] decoded = StringUtils.decode(encoded);
         Assert.assertEquals(decoded.length, i, "Decode array size mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         for (int j = 0; j < i; ++j) {
            Assert.assertEquals(decoded[j], randomStringToEncode[j], "Decode string equals mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         }
      }
   }

   public void testOverheadWithEmptyString() {
      for (int i = 1; i < 10; ++i) {
         String[] randomStringToEncode = new String[i];
         for (int j = 0; j < i; ++j) {
            randomStringToEncode[j] = "";
         }

         String encoded = StringUtils.encode(randomStringToEncode);

         System.out.println("Overhead for " + i + " empty strings is " + (i * 1.0 / encoded.length()));

         String[] decoded = StringUtils.decode(encoded);
         Assert.assertEquals(decoded.length, i, "Decode array size mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         for (int j = 0; j < i; ++j) {
            Assert.assertEquals(decoded[j], randomStringToEncode[j], "Decode string equals mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         }
      }
   }

   public void testOverheadWithTenCharString() {
      for (int i = 1; i < 10; ++i) {
         String[] randomStringToEncode = new String[i];
         for (int j = 0; j < i; ++j) {
            randomStringToEncode[j] = generateRandomString(10);
         }

         String encoded = StringUtils.encode(randomStringToEncode);

         System.out.println("Overhead for " + i + " strings with 10 chars is " + (i * 1.0 / encoded.length()));
         
         String[] decoded = StringUtils.decode(encoded);
         Assert.assertEquals(decoded.length, i, "Decode array size mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         for (int j = 0; j < i; ++j) {
            Assert.assertEquals(decoded[j], randomStringToEncode[j], "Decode string equals mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         }
      }
   }

   public void testOverheadWithOneHundredCharString() {
      for (int i = 1; i < 10; ++i) {
         String[] randomStringToEncode = new String[i];
         for (int j = 0; j < i; ++j) {
            randomStringToEncode[j] = generateRandomString(100);
         }

         String encoded = StringUtils.encode(randomStringToEncode);

         System.out.println("Overhead for " + i + " strings with 100 chars is " + (i * 1.0 / encoded.length()));

         String[] decoded = StringUtils.decode(encoded);
         Assert.assertEquals(decoded.length, i, "Decode array size mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         for (int j = 0; j < i; ++j) {
            Assert.assertEquals(decoded[j], randomStringToEncode[j], "Decode string equals mismatch for " + Arrays.toString(randomStringToEncode) + ".");
         }
      }
   }

   public static String generateRandomString() {
      return generateRandomString(RANDOM.nextInt(MAX_STRING_LENGTH) + 1);
   }

   public static String generateRandomString(int length) {
      StringBuilder builder = new StringBuilder(256);
      while (length-- > 0) {
         builder.append(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS_LENGTH)));
      }
      return builder.toString();
   }

}
