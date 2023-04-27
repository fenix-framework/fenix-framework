package pt.ist.fenixframework.consistencyPredicates;

import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

import jvstm.cps.ConsistencyCheckTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;

public abstract class ConsistencyPredicateSupport {

    private static final Logger logger = LoggerFactory.getLogger(ConsistencyPredicateSupport.class);

    private static final class Holder {

        private static final ConsistencyPredicateSupport instance = resolveInstance();

        private static ConsistencyPredicateSupport resolveInstance() {
            Iterator<ConsistencyPredicateSupport> iterator = ServiceLoader.load(ConsistencyPredicateSupport.class).iterator();
            if (!iterator.hasNext()) {
                throw new Error(
                        "No implementations of " + ConsistencyPredicateSupport.class.getName() + " was found in the classpath");
            }
            ConsistencyPredicateSupport support = iterator.next();
            if (iterator.hasNext()) {
                logger.error("More than one instance of {} was found in the classpath\nUsing {}",
                        ConsistencyPredicateSupport.class, support);
            }
            logger.debug("ConsistencyPredicateSupport is provided by {}", support);
            return support;
        }

    }

    public static ConsistencyPredicateSupport getInstance() {
        return Holder.instance;
    }

    public abstract DomainMetaObject getDomainMetaObjectFor(DomainObject obj);

    public abstract void justSetMetaObjectForDomainObject(DomainObject domainObject, DomainMetaObject metaObject);

    public abstract ConsistencyCheckTransaction<?> createNewConsistencyCheckTransactionForObject(DomainObject obj);

    public abstract void removeAllMetaObjectsForMetaClass(DomainMetaClass domainMetaClass);

    public abstract Collection<String> getIDsWithoutMetaObjectBatch(Class<? extends DomainObject> domainClass);

    public abstract int getBatchSize();

}
