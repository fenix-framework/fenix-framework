package eu.cloudtm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;

import pt.ist.fenixframework.FenixFramework;

/**
 * // TODO: Document this
 * 
 * @author Pedro Ruivo
 * @since 2.3-cloudtm
 */
public abstract class CloudtmFeatureManager implements KeyFeatureManager {

    @Override
    public Map<Feature, FeatureValue> getFeatures(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException("Expected a String");
        }
        //obter os hints da key
        LocalityHints hints = FenixFramework.getOwnerDomainObject((String) key).getLocalityHints();

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
        return Collections.emptyMap();
    }

}
