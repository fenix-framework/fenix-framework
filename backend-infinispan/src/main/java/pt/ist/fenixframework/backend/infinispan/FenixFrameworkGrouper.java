package pt.ist.fenixframework.backend.infinispan;

import org.infinispan.distribution.group.Grouper;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

/**
 * {@link Grouper} implementation that knows how to group Infinispan backend keys.
 *
 * @author Pedro Ruivo
 * @since 2.6-cloudtm
 */
public class FenixFrameworkGrouper implements Grouper<String> {

    private final InfinispanBackEnd backEnd;

    public FenixFrameworkGrouper(InfinispanBackEnd backEnd) {
        this.backEnd = backEnd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String computeGroup(String key, String group) {
        //ignore the group parameter. It is != null when @Group is used in a key, but the keys are string.
        assert group == null;
        LocalityHints localityHints = backEnd.getLocalityHints(key);
        return localityHints.get(Constants.GROUP_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<String> getKeyType() {
        return String.class;
    }
}
