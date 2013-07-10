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

    private static final String WITHOUT_GROUP_HINTS = "&";
    private static final String WITH_GROUP_HINTS = "%";
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
        Map<String, String> keyValues = new HashMap<String, String>();

        if (WITH_GROUP_HINTS.equals(decode[0])) {
            keyValues.put(Constants.GROUP_ID, decode[1]);
            if (keyFeatureManager != null) {
                Feature[] features = keyFeatureManager.getAllKeyFeatures();
                for (int i = 2; i < decode.length; ++i) {
                    keyValues.put(features[i - 2].getName(), decode[i]);
                }
            }
        } else if (WITHOUT_GROUP_HINTS.equals(decode[0])) {
            if (keyFeatureManager != null) {
                Feature[] features = keyFeatureManager.getAllKeyFeatures();
                for (int i = 1; i < decode.length; ++i) {
                    keyValues.put(features[i - 1].getName(), decode[i]);
                }
            }
        }
        return new LocalityHints(keyValues);
    }

    public final synchronized void addHint(String key, String value) {
        this.keyValues.put(key, value);
    }

    public final String hints2String() {
        List<String> list = new ArrayList<String>(keyValues.size() + 2);

        String groupId = keyValues.get(Constants.GROUP_ID);
        if (groupId == null) {
            list.add(WITHOUT_GROUP_HINTS);
        } else {
            list.add(WITH_GROUP_HINTS);
            list.add(groupId);
        }

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
}
