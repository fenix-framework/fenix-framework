package pt.ist.fenixframework.hibernatesearch;

import java.util.*;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.search.cfg.SearchMapping;
import org.hibernate.search.cfg.spi.IndexManagerFactory;
import org.hibernate.search.cfg.spi.SearchConfigurationBase;
import org.hibernate.search.impl.DefaultIndexManagerFactory;
import org.hibernate.search.spi.ServiceProvider;

/**
 * This class is used to configure hibernate-search.
 */
class SearchConfiguration extends SearchConfigurationBase {

    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    private final Properties properties;
    private final SearchMapping searchMapping;
    private final Map<Class<? extends ServiceProvider<?>>, Object> providedServices =
            new HashMap<Class<? extends ServiceProvider<?>>, Object>();
    private final IndexManagerFactory indexManagerFactory = new DefaultIndexManagerFactory();

    public SearchConfiguration(Properties properties, SearchMapping searchMapping, Collection<Class<?>> classes) {
        this.properties = properties;
        this.searchMapping = searchMapping;

        for (Class<?> c : classes) {
            this.classes.put(c.getName(), c);
        }
    }

    @Override
    public Iterator<Class<?>> getClassMappings() {
        return classes.values().iterator();
    }

    @Override
    public Class<?> getClassMapping(String name) {
        return classes.get(name);
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public ReflectionManager getReflectionManager() {
        return null;
    }

    @Override
    public SearchMapping getProgrammaticMapping() {
        return searchMapping;
    }

    @Override
    public Map<Class<? extends ServiceProvider<?>>, Object> getProvidedServices() {
        return providedServices;
    }

    @Override
    public IndexManagerFactory getIndexManagerFactory() {
        return indexManagerFactory;
    }
}
