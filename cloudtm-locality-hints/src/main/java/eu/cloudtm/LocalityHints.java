package eu.cloudtm;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 2.3-cloudtm
 */
public class LocalityHints {

    private static final String LOCALITY_HINT_STRING = "&";
    private static volatile KeyFeatureManager keyFeatureManager;
    private final Map<String, String> keyValues;

    public LocalityHints(String[] hints) {
        if (hints == null) {
            throw new NullPointerException("Hints array cannot be null.");
        }
        this.keyValues = new HashMap<String, String>(hints.length / 2);
        for (int i = 0; i < hints.length; ++i) {
            if (i + 1 >= hints.length) {
                throw new IllegalArgumentException("hints should be even");
            }
            keyValues.put(hints[i++], hints[i]);
        }
    }

    public LocalityHints() {
        this.keyValues = new HashMap<String, String>();
    }

    private LocalityHints(Map<String, String> keyValues) {
        this.keyValues = keyValues;
    }

    public static synchronized void init(KeyFeatureManager manager) {
        if (keyFeatureManager == null) {
            keyFeatureManager = manager;
        }
    }

    public static LocalityHints string2Hints(String hints) {
        String[] decode = StringUtils.decode(hints);
        if (decode.length == 0 || decode.length < 3) {
            return new LocalityHints();
        }else if (!LOCALITY_HINT_STRING.equals(decode[0])) {
            return new LocalityHints();
        }

        Map<String, String> keyValues = new HashMap<String, String>();

        if (decode[1] != null) {
            keyValues.put(Constants.GROUP_ID, decode[1]);
        }
        if (decode[2] != null) {
            keyValues.put(Constants.RELATION_NAME, decode[2]);
        }
        if (decode.length > 3) {
            if (keyFeatureManager != null) {
                Feature[] features = keyFeatureManager.getAllKeyFeatures();
                if (decode.length + 3 < features.length) {
                    //invalid string: more features than registered
                    return new LocalityHints();
                }
                for (int i = 0; i < features.length; ++i) {
                    keyValues.put(features[i].getName(), decode[i + 3]);
                }
            }
        }
        return new LocalityHints(keyValues);
    }

    public final synchronized LocalityHints addHint(String key, String value) {
        this.keyValues.put(key, value);
        return this;
    }

    public final String hints2String() {
        List<String> list = new ArrayList<String>(keyValues.size() + 2);

        list.add(LOCALITY_HINT_STRING);
        list.add(keyValues.get(Constants.GROUP_ID));
        list.add(keyValues.get(Constants.RELATION_NAME));

        if (keyFeatureManager != null) {
            for (Feature feature : keyFeatureManager.getAllKeyFeatures()) {
                list.add(keyValues.get(feature.getName()));
            }
        }

        return StringUtils.encode(list.toArray(new String[list.size()]));
    }

    public final String get(String key) {
        return keyValues.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalityHints that = (LocalityHints) o;

        return keyValues.equals(that.keyValues);

    }

    @Override
    public int hashCode() {
        return keyValues.hashCode();
    }

    @Override
    public String toString() {
        return "LocalityHints{" + "keyValues=" + keyValues + '}';
    }

    public final boolean isEmpty() {
        return keyValues.isEmpty();
    }

    @Override
    public LocalityHints clone() {
        return new LocalityHints(new HashMap<String, String>(keyValues));
    }
}
