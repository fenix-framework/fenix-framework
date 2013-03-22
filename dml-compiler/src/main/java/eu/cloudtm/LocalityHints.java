package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
public class LocalityHints {

   private static final String WITHOUT_GROUP_HINTS = "&";
   private static final String WITH_GROUP_HINTS = "%";
   private static volatile CloudtmFeatureManager cloudtmFeatureManager;
   private final Map<String, String> keyValues;

   public LocalityHints(String... hints) {
      this.keyValues = new HashMap<String, String>(hints.length / 2);
      for (int i = 0; i < hints.length; ++i) {
         if (i + 1 >= hints.length) {
            throw new IllegalArgumentException("hints should be even");
         }
         keyValues.put(hints[i++], hints[i]);
      }
   }

   private LocalityHints(Map<String, String> keyValues) {
      this.keyValues = keyValues;
   }

   public static synchronized void init(CloudtmFeatureManager manager) {
      if (cloudtmFeatureManager == null) {
         cloudtmFeatureManager = manager;
      }
   }

   public static LocalityHints string2Hints(String hints) {
      checkIfInitialized();
      String[] decode = StringUtils.decode(hints);
      Feature[] features = cloudtmFeatureManager.getAllKeyFeatures();
      Map<String, String> keyValues = new HashMap<String, String>();
      
      if (WITH_GROUP_HINTS.equals(decode[0])) {
         keyValues.put(Constants.GROUP_ID, decode[1]);
         for (int i = 2; i < decode.length; ++i) {
            keyValues.put(features[i - 2].getName(), decode[i]);
         }
      } else if (WITHOUT_GROUP_HINTS.equals(decode[0])) {
         for (int i = 1; i < decode.length; ++i) {
            keyValues.put(features[i - 1].getName(), decode[i]);
         }
      } else {
         throw new IllegalArgumentException("String " + hints + " is not a valid string");
      }
      return new LocalityHints(keyValues);
   }

   private static void checkIfInitialized() {
      if (cloudtmFeatureManager == null) {
         throw new IllegalStateException("Locality Hints is not initialized. Please invoke init(CloudtmFeatureManager)");
      }
   }

   public final String hints2String() {
      checkIfInitialized();
      List<String> list = new ArrayList<String>(keyValues.size() + 2);

      String groupId = keyValues.get(Constants.GROUP_ID);
      if (groupId == null) {
         list.add(WITHOUT_GROUP_HINTS);
      } else {
         list.add(WITH_GROUP_HINTS);
         list.add(groupId);
      }
      

      for (Feature feature : cloudtmFeatureManager.getAllKeyFeatures()) {
         list.add(keyValues.get(feature.getName()));
      }

      return StringUtils.encode(list.toArray(new String[list.size()]));
   }
   
   public final String get(String key) {
      return keyValues.get(key);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      LocalityHints that = (LocalityHints) o;

      return keyValues.equals(that.keyValues);

   }

   @Override
   public int hashCode() {
      return keyValues.hashCode();
   }

   @Override
   public String toString() {
      return "LocalityHints{" +
            "keyValues=" + keyValues +
            '}';
   }
}
