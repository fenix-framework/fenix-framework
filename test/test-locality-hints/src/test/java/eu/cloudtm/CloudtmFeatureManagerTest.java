package eu.cloudtm;

import eu.cloudtm.test.domain.Person;
import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.NumericFeature;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import java.util.Collections;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 2.3-cloudtm
 */
public class CloudtmFeatureManagerTest {

    private static final CloudtmFeatureManager TEST_MANAGER = new CloudtmFeatureManager() {
        @Override
        public Feature[] getAllKeyFeatures() {
            return new Feature[]{new NumericFeature("feature1"), new NumericFeature("feature2"), new NumericFeature("feature3")};
        }

        @Override
        protected Map<Feature, FeatureValue> getFeatureFromGroup(String key) {
            return Collections.emptyMap();
        }
    };
    Person person = null;

    @BeforeClass
    public void initLocalityHints() {
        LocalityHints.init(TEST_MANAGER);
    }

    @Atomic
    public void createTestPerson(LocalityHints hints) {
        this.person = new Person(hints);
    }

    @Test
    public void testSimpleHints() {
        LocalityHints hints = new LocalityHints(new String[]{Constants.GROUP_ID, "group", "feature1", "1", "feature2", "2"});
        createTestPerson(hints);

        CloudtmFeatureManager manager = TEST_MANAGER;

        String[] keys = FenixFramework.getStorageKeys(person);

        for (String key : keys) {
            Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

            Assert.assertNotNull(featureValueMap.get(manager.getAllKeyFeatures()[0]));
            Assert.assertEquals(featureValueMap.get(manager.getAllKeyFeatures()[0]).getValueAsString(), "1");

            Assert.assertNotNull(featureValueMap.get(manager.getAllKeyFeatures()[1]));
            Assert.assertEquals(featureValueMap.get(manager.getAllKeyFeatures()[1]).getValueAsString(), "2");

            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));
        }
    }

    @Test
    public void testOnlyGroup() {
        LocalityHints hints = new LocalityHints(new String[]{Constants.GROUP_ID, "group"});
        createTestPerson(hints);

        CloudtmFeatureManager manager = TEST_MANAGER;

        String[] keys = FenixFramework.getStorageKeys(person);

        for (String key : keys) {
            Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[0]));
            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[1]));
            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));
        }
    }

    @Test
    public void testNoHints() {
        createTestPerson(null);

        CloudtmFeatureManager manager = TEST_MANAGER;
        String[] keys = FenixFramework.getStorageKeys(person);

        for (String key : keys) {
            Map<Feature, FeatureValue> featureValueMap = manager.getFeatures(key);

            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[0]));
            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[1]));
            Assert.assertNull(featureValueMap.get(manager.getAllKeyFeatures()[2]));
        }
    }

}
