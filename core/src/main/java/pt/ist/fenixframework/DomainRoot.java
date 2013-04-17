package pt.ist.fenixframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoDomainMetaObjects
public class DomainRoot extends DomainRoot_Base {

    private static final Logger logger = LoggerFactory.getLogger(DomainRoot.class);

    public DomainRoot() {
        super();
        logger.info("Created DomainRoot instance");
    }

}
