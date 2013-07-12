package pt.ist.fenixframework.backend.infinispan;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;
import org.infinispan.distribution.group.Grouper;
import pt.ist.fenixframework.FenixFramework;

/**
 * {@link Grouper} implementation that knows how to groups Infinispan backend keys.
 *
 * @author Pedro Ruivo
 * @since 2.6
 */
public class FenixFrameworkGrouper implements Grouper<String> {

    private final InfinispanBackEnd backEnd;

    public FenixFrameworkGrouper(InfinispanBackEnd backEnd) {
        this.backEnd = backEnd;
    }

    @Override
    public String computeGroup(String key, String group) {
        //ignore the group parameter. It is != null when @Group is used in a key, but the keys are string.
        assert group == null;
        LocalityHints localityHints = backEnd.getLocalityHints(key);
        //it is not a problem to return null. Null means no grouping for the key.
        return localityHints == null ? null : localityHints.get(Constants.GROUP_ID);
    }

    @Override
    public Class<String> getKeyType() {
        return String.class;
    }
}
