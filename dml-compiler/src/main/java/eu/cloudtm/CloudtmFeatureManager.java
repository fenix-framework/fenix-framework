package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
public abstract class CloudtmFeatureManager implements KeyFeatureManager {

   @Override
   public Map<Feature, FeatureValue> getFeatures(Object key) {
      if (!(key instanceof String)) {
         throw new IllegalArgumentException("Expected a String");
      }
      //obter os hints da key
      String[] decoded = StringUtils.decode((String) key);
      if (decoded.length > 1) {
         Map<Feature, FeatureValue> featureValueMap = new HashMap<Feature, FeatureValue>();
         /*

      LocalityHints may be stored in the keys, but each backend chooses how to
      generate the keys used to store data.  This means that recovering the LH
      required backend-specific knowledge.  The correct implementation should
      be to call FenixFramework.getOwnerDomainObject((String)key).getLocalityHints()
      which gets the LH for the DomainObject that owns the given key.

      As a temporary hack, for the Infinispan direct mapper we agree to store
      the LH in the decoded[1] field, it exists.

      For the OGM backend, since FenixFramework does not control the generation
      of the keys we will need support from Hibernate OGM to be able to get the
      object that owns such key.

         */
         LocalityHints hints = LocalityHints.string2Hints(decoded[1]);
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
