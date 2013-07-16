package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.FenixFramework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pedro Ruivo
 * @since 2.3-cloudtm
 */
public abstract class CloudtmFeatureManager implements KeyFeatureManager {

    private static final Logger log = LoggerFactory.getLogger(CloudtmFeatureManager.class);

    @Override
    public Map<Feature, FeatureValue> getFeatures(Object key) {
        if (key == null || !(key instanceof String)) {
            getLog().error("Received an unknown key. Expected string but received " + key);
            return Collections.emptyMap();
        }
        //get the locality hints from the key
        LocalityHints hints = FenixFramework.localityHintsFromExternalId((String) key);

        if (hints != null) {
            Map<Feature, FeatureValue> featureValueMap = new HashMap<Feature, FeatureValue>();
            for (Feature feature : getAllKeyFeatures()) {
                String value = hints.get(feature.getName());
                if (value != null) {
                    featureValueMap.put(feature, feature.featureValueFromParser(value));
                }
            }
            return featureValueMap;
        }
        //hints are null. This means that it is not a key and maybe it is a group.
        return getFeatureFromGroup((String) key);
    }

    /**
     * @return the map of feature for this group. This method must return a non-null map!
     */
    protected abstract Map<Feature, FeatureValue> getFeatureFromGroup(String key);

    protected final Logger getLog() {
        return log;
    }

}
