package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.NumericFeature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 5.3
 */
@Test
public class CloudtmFeatureManagerTest {

   private static final CloudtmFeatureManager TEST_MANAGER = new CloudtmFeatureManager() {
      @Override
      public Feature[] getAllKeyFeatures() {
         return new Feature[]{new NumericFeature("feature1"),
                              new NumericFeature("feature2"),
                              new NumericFeature("feature3")};
      }
   };
   
   public void testSimpleHints() {
      CloudtmFeatureManager manager = TEST_MANAGER;
      String randomOID = "random_oid";
      LocalityHints hints = new LocalityHints(Constants.GROUP_ID, "group", "feature1", "1", "feature2", "2");
      String key = StringUtils.encode(randomOID, hints.hints2String());
      
      Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

      Assert.assertEquals(featureValueMap.get(manager.getAllKeyFeatures()[0]).getValueAsString(), "1");
      Assert.assertEquals(featureValueMap.get(manager.getAllKeyFeatures()[1]).getValueAsString(), "2");
      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));      
   }

   public void testOnlyGroup() {
      CloudtmFeatureManager manager = TEST_MANAGER;
      String randomOID = "random_oid";
      LocalityHints hints = new LocalityHints(Constants.GROUP_ID, "group");
      String key = StringUtils.encode(randomOID, hints.hints2String());

      Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[0]));
      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[1]));
      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));
   }

   public void testNoHints() {
      CloudtmFeatureManager manager = TEST_MANAGER;
      String randomOID = "random_oid";
      String key = StringUtils.encode(randomOID);

      Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[0]));
      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[1]));
      Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));
   }
   
}
