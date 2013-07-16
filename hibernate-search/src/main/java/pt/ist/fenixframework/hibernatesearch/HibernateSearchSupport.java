package pt.ist.fenixframework.hibernatesearch;

import java.lang.annotation.ElementType;
import java.util.*;

import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.cfg.*;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.spi.SearchFactoryBuilder;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.DomainClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for interactions with Hibernate-Search.
 *
 * This class provides a SearchFactory instance that can be used to perform searches using hibernate-search.
 */
public class HibernateSearchSupport {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchSupport.class);

    private static final Set<Class<?>> INDEXED_CLASSES = new HashSet<Class<?>>(getIndexedDomainClasses());
    private static SearchFactoryImplementor searchFactory;

    /**
     * Returns the SearchFactory instance. Null when HibernateSearchSupport is disabled.
     */
    public static SearchFactoryImplementor getSearchFactory() {
        return searchFactory;
    }

    static void initializeSearchFactory(Properties hibernateSearchConfiguration) {
        if (searchFactory != null) {
            throw new RuntimeException("Tried to initialize already initialized HibernateSearchSupport");
        }
        if (INDEXED_CLASSES.isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Hibernate Search enabled but no domain classes are marked with @Indexed");
            }
        }

        SearchConfiguration configuration =
                new SearchConfiguration(hibernateSearchConfiguration, getBuiltinMapping(), INDEXED_CLASSES);
        searchFactory = new SearchFactoryBuilder().configuration(configuration).buildSearchFactory();
    }

    protected static void updateIndex(TransactionContext context, Collection<DomainObject> objects, WorkType workType) {
        try {
            for (DomainObject obj : objects) {
                if (!INDEXED_CLASSES.contains(obj.getClass())) continue;
                searchFactory.getWorker().performWork(new Work<DomainObject>(obj, workType), context);
            }
        } catch (RuntimeException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Problem inside updateIndex", e);
            }
            throw e;
        }
    }

    /** Built-in default programatic mapping for hibernate-search **/
    private static SearchMapping getBuiltinMapping() {
        SearchMapping mapping = new SearchMapping();

        // Map the getExternalId() method as a documentId for all domain classes
        // Note that hibernate-search currently requires this method to really exist in the
        // AbstractDomainObject class
        mapping
            .entity(AbstractDomainObject.class)
                .property("externalId", ElementType.METHOD)
                    .documentId()
                    .name("id");

        return mapping;
    }

    /** Scans for domain classes annotated with @Indexed **/
    private static Set<Class<?>> getIndexedDomainClasses() {
        Set<Class<?>> classList = new HashSet<Class<?>>();

        for (DomainClass dc : FenixFramework.getDomainModel().getDomainClasses()) {
            try {
                Class<?> domainClass = Class.forName(dc.getFullName());

                if (domainClass.getAnnotation(org.hibernate.search.annotations.Indexed.class) != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Indexing " + dc.getFullName());
                    }
                    classList.add(domainClass);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return classList;
    }
}
