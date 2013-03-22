package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.NumericFeature;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
@Test
public class LocalityHintsTest {
   
   private static final CloudtmFeatureManager TEST_MANAGER = new CloudtmFeatureManager() {
      @Override
      public Feature[] getAllKeyFeatures() {
         return new Feature[]{new NumericFeature("feature1"),
                              new NumericFeature("feature2"),
                              new NumericFeature("feature3")};
      }
   };

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testWrongParameters() {
      new LocalityHints("bla");
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfInitialized() {
      LocalityHints hints = new LocalityHints(Constants.GROUP_ID, "bla");
      hints.hints2String();
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfInitialized2() {
      LocalityHints.string2Hints("bla");
   }

   @Test(dependsOnMethods = {"testWrongParameters", "testIfInitialized", "testIfInitialized2"})
   public void testFeatureEquality() {
      LocalityHints.init(TEST_MANAGER);
      
      LocalityHints hints = new LocalityHints("feature1", "value1", "feature2", "value2", "feature3", "value3");

      Assert.assertEquals(hints.get("feature1"), "value1", "Error checking feature1.");
      Assert.assertEquals(hints.get("feature2"), "value2", "Error checking feature2.");
      Assert.assertEquals(hints.get("feature3"), "value3", "Error checking feature3.");
      Assert.assertNull(hints.get(Constants.GROUP_ID), "Error checking group.");

      String encoded = hints.hints2String();
      
      LocalityHints newHints = LocalityHints.string2Hints(encoded);

      Assert.assertEquals(newHints.get("feature1"), "value1", "Error checking feature1.");
      Assert.assertEquals(newHints.get("feature2"), "value2", "Error checking feature2.");
      Assert.assertEquals(newHints.get("feature3"), "value3", "Error checking feature3.");
      Assert.assertNull(newHints.get(Constants.GROUP_ID), "Error checking group.");
   }

   @Test(dependsOnMethods = {"testWrongParameters", "testIfInitialized", "testIfInitialized2"})
   public void testFeatureAndGroupEquality() {
      LocalityHints.init(TEST_MANAGER);

      LocalityHints hints = new LocalityHints("feature1", "value1", "feature2", "value2", Constants.GROUP_ID, "group");

      Assert.assertEquals(hints.get("feature1"), "value1", "Error checking feature1.");
      Assert.assertEquals(hints.get("feature2"), "value2", "Error checking feature2.");
      Assert.assertNull(hints.get("feature3"), "Error checking feature3.");
      Assert.assertEquals(hints.get(Constants.GROUP_ID), "group", "Error checking group.");

      String encoded = hints.hints2String();

      LocalityHints newHints = LocalityHints.string2Hints(encoded);

      Assert.assertEquals(newHints.get("feature1"), "value1", "Error checking feature1.");
      Assert.assertEquals(newHints.get("feature2"), "value2", "Error checking feature2.");
      Assert.assertNull(newHints.get("feature3"), "Error checking feature3.");
      Assert.assertEquals(newHints.get(Constants.GROUP_ID), "group", "Error checking group.");
   }

   @Test(dependsOnMethods = {"testWrongParameters", "testIfInitialized", "testIfInitialized2"})
   public void testEmptyHints() {
      LocalityHints.init(TEST_MANAGER);

      LocalityHints hints = new LocalityHints();

      Assert.assertNull(hints.get("feature1"), "Error checking feature1.");
      Assert.assertNull(hints.get("feature2"), "Error checking feature2.");
      Assert.assertNull(hints.get("feature3"), "Error checking feature3.");
      Assert.assertNull(hints.get(Constants.GROUP_ID), "Error checking group.");

      String encoded = hints.hints2String();

      LocalityHints newHints = LocalityHints.string2Hints(encoded);

      Assert.assertNull(newHints.get("feature1"), "Error checking feature1.");
      Assert.assertNull(newHints.get("feature2"), "Error checking feature2.");
      Assert.assertNull(newHints.get("feature3"), "Error checking feature3.");
      Assert.assertNull(newHints.get(Constants.GROUP_ID), "Error checking group.");
   }

}
