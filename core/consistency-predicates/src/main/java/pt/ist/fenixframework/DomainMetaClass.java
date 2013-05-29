package pt.ist.fenixframework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.adt.bplustree.BPlusTree;
import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicateSupport;
import pt.ist.fenixframework.consistencyPredicates.DomainConsistencyPredicate;
import pt.ist.fenixframework.consistencyPredicates.PrivateConsistencyPredicate;
import pt.ist.fenixframework.consistencyPredicates.PublicConsistencyPredicate;
import pt.ist.fenixframework.core.AbstractDomainObject;

/**
 * A <code>DomainMetaClass</code> is the domain entity that represents a class
 * existing in the application's domain model, declared in the DML.<br>
 * 
 * These DomainMetaClasses are created or deleted only during the {@link FenixFramework} initialization. A
 * <code>DomainMetaClass</code> is
 * linked to one superclass and many subclasses, which are also <code>DomainMetaClasses</code>.<br>
 * 
 * Each <code>DomainMetaClass</code> stores a set of all existing {@link DomainMetaObject}s of it's class. Furthermore, a
 * <code>DomainMetaClass</code> contains a set of all {@link DomainConsistencyPredicate}s that are declared in its code.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 **/
@NoDomainMetaObjects
public class DomainMetaClass extends DomainMetaClass_Base {

    private static final Class<? extends AbstractDomainObject> BOTTOM_GENERATED_SUPERCLASS = BackEndId.getBackEndId()
            .getDomainClassRoot();

    private static final Logger logger = LoggerFactory.getLogger(DomainMetaClass.class);

    /**
     * Compares two classes according to their hierarchies, such that a
     * superclass always appears before any of its subclasses. Classes of
     * different hierarchies are sorted alphabetically according to the names of
     * their successive superclasses (from top to bottom).
     */
    public static Comparator<Class<? extends DomainObject>> COMPARATOR_BY_CLASS_HIERARCHY_TOP_DOWN =
            new Comparator<Class<? extends DomainObject>>() {
                @Override
                public int compare(Class<? extends DomainObject> class1, Class<? extends DomainObject> class2) {
                    if (class1.equals(class2)) {
                        return 0;
                    }
                    if (class1.isAssignableFrom(class2)) {
                        return -1;
                    }
                    if (class2.isAssignableFrom(class1)) {
                        return 1;
                    }
                    return getHierarchyName(class1).compareTo(getHierarchyName(class2));
                }
            };

    /**
     * @return the fully qualified names of this class' successive superclasses
     *         (from top to bottom).
     */
    private static String getHierarchyName(Class<?> class1) {
        if (class1.equals(BOTTOM_GENERATED_SUPERCLASS)) {
            return "";
        }
        return getHierarchyName(class1.getSuperclass()) + "<-" + class1.getName();
    }

    /**
     * Compares two {@link DomainMetaClass}es according to their
     * hierarchies, such that a superclass always appears before any of its
     * subclasses. {@link DomainMetaClass}es of different hierarchies are
     * sorted alphabetically according to the names of their successive
     * superclasses (from top to bottom).
     * 
     * @see DomainMetaClass#COMPARATOR_BY_CLASS_HIERARCHY_TOP_DOWN
     */
    public static Comparator<DomainMetaClass> COMPARATOR_BY_META_CLASS_HIERARCHY_TOP_DOWN = new Comparator<DomainMetaClass>() {
        @Override
        public int compare(DomainMetaClass metaClass1, DomainMetaClass metaClass2) {
            if (metaClass1 == metaClass2) {
                return 0;
            }
            return COMPARATOR_BY_CLASS_HIERARCHY_TOP_DOWN.compare(metaClass1.getDomainClass(), metaClass2.getDomainClass());
        }
    };

    public DomainMetaClass(Class<? extends DomainObject> domainClass) {
        super();
        checkFrameworkNotInitialized();
        setDomainClass(domainClass);
        DomainFenixFrameworkRoot.getInstance().addDomainMetaClass(this);
        setExistingDomainMetaObjects(new BPlusTree<DomainMetaObject>());
        setInitialized(false);

        logger.info("Creating new a DomainMetaClass: " + domainClass);
    }

    public DomainMetaClass(Class<? extends DomainObject> domainClass, DomainMetaClass metaSuperclass) {
        this(domainClass);
        setDomainMetaSuperclass(metaSuperclass);
    }

    /**
     * Checks that the {@link FenixFramework} is not initialized, throws an
     * exception otherwise. Should be called before any changes are made to {@link DomainMetaClass}es or to
     * {@link DomainConsistencyPredicate}s.
     * 
     * @throws RuntimeException
     *             if the framework was already initialized
     */
    private void checkFrameworkNotInitialized() {
        if (FenixFramework.isInitialized()) {
            throw new RuntimeException("Instances of " + getClass().getSimpleName()
                    + " cannot be edited after the FenixFramework has been initialized.");
        }
    }

    @Override
    public void setDomainFenixFrameworkRoot(DomainFenixFrameworkRoot domainFenixFrameworkRoot) {
        checkFrameworkNotInitialized();
        super.setDomainFenixFrameworkRoot(domainFenixFrameworkRoot);
    }

    public Class<? extends DomainObject> getDomainClass() {
        String[] strings = getDomainClassName().split(" ");
        String fullyQualifiedClassName = strings[1];
        strings = fullyQualifiedClassName.split("[.]");

        try {
            Class<?> domainClass = Class.forName(fullyQualifiedClassName);
            return (Class<? extends DomainObject>) domainClass;
        } catch (ClassNotFoundException e) {
            logger.info("The following domain class has been removed: " + e.getMessage());
        }
        return null;
    }

    public void setDomainClass(Class<? extends DomainObject> domainClass) {
        checkFrameworkNotInitialized();
        setDomainClassName(domainClass.toString());
    }

    @Override
    public void setDomainClassName(String domainClassName) {
        checkFrameworkNotInitialized();
        super.setDomainClassName(domainClassName);
    }

    /**
     * @return true if this {@link DomainMetaClass} was already fully initialized
     */
    protected boolean isInitialized() {
        return getInitialized();
    }

    @Override
    public void setInitialized(Boolean initialized) {
        checkFrameworkNotInitialized();
        super.setInitialized(initialized);
    }

    @Override
    public BPlusTree<DomainMetaObject> getExistingDomainMetaObjects() {
        return super.getExistingDomainMetaObjects();
    }

    public int getExistingDomainMetaObjectsCount() {
        return getExistingDomainMetaObjects().size();
    }

    public void addExistingDomainMetaObject(DomainMetaObject metaObject) {
        getExistingDomainMetaObjects().insert(metaObject.getOid(), metaObject);
        metaObject.setDomainMetaClass(this);
    }

    public void removeExistingDomainMetaObject(DomainMetaObject metaObject) {
        getExistingDomainMetaObjects().remove(metaObject.getOid());
        metaObject.setDomainMetaClass(null);
    }

    public <PredicateT extends DomainConsistencyPredicate> PredicateT getDeclaredConsistencyPredicate(Method predicateMethod) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicateSet()) {
            if (declaredConsistencyPredicate.getPredicate().equals(predicateMethod)) {
                return (PredicateT) declaredConsistencyPredicate;
            }
        }
        return null;
    }

    public <PredicateT extends DomainConsistencyPredicate> PredicateT getDeclaredConsistencyPredicate(String predicateName) {
        try {
            Method predicateMethod = getDomainClass().getDeclaredMethod(predicateName);
            return (PredicateT) getDeclaredConsistencyPredicate(predicateMethod);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public void addDeclaredConsistencyPredicate(DomainConsistencyPredicate declaredConsistencyPredicates) {
        checkFrameworkNotInitialized();
        super.addDeclaredConsistencyPredicate(declaredConsistencyPredicates);
    }

    @Override
    public void removeDeclaredConsistencyPredicate(DomainConsistencyPredicate declaredConsistencyPredicates) {
        checkFrameworkNotInitialized();
        super.removeDeclaredConsistencyPredicate(declaredConsistencyPredicates);
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}, for each new
     * {@link DomainMetaClass}.
     * 
     * Creates a {@link DomainMetaObject} for each existing {@link DomainObject} of the specified class, and associates
     * the
     * new {@link DomainMetaObject} to its {@link DomainMetaClass}.
     */
    protected void initExistingDomainObjects() {
        checkFrameworkNotInitialized();

        while (true) {
            Collection<String> ids = ConsistencyPredicateSupport.getInstance().getIDsWithoutMetaObjectBatch(getDomainClass());
            if (ids.isEmpty()) {
                break;
            }
            for (String id : ids) {
                DomainObject existingDO = null;
                try {
                    existingDO = FenixFramework.getDomainObject(id);
                } catch (Exception ex) {
                    logger.warn("An exception was thrown while allocating the object: {} - {}", getDomainClass(), id);
                    ex.printStackTrace();
                    continue;
                }
                DomainMetaObject metaObject = new DomainMetaObject();
                metaObject.setDomainObject(existingDO);
                addExistingDomainMetaObject(metaObject);
            }

            // Starts a new transaction to process a limited number of existing objects.
            // This is necessary to split the load of the mass creation of DomainMetaObjects among several transactions.
            // Each transaction processes a maximum of MAX_NUMBER_OF_OBJECTS_TO_PROCESS objects in order to avoid OutOfMemoryExceptions.
            // Because the JDBC query only returns objects that have no DomainMetaObjects, there is no problem with
            // processing only an incomplete part of the objects of this class.
            logger.info("Transaction finished. Number of initialized " + getDomainClass().getSimpleName() + " objects: "
                    + getExistingDomainMetaObjectsCount());
            DomainFenixFrameworkRoot.checkpointTransaction();
        }
    }

    @Override
    public void addDomainMetaSubclass(DomainMetaClass domainMetaSubclasses) {
        checkFrameworkNotInitialized();
        super.addDomainMetaSubclass(domainMetaSubclasses);
    }

    @Override
    public void removeDomainMetaSubclass(DomainMetaClass domainMetaSubclasses) {
        checkFrameworkNotInitialized();
        super.removeDomainMetaSubclass(domainMetaSubclasses);
    }

    @Override
    public void setDomainMetaSuperclass(DomainMetaClass domainMetaSuperclass) {
        checkFrameworkNotInitialized();
        super.setDomainMetaSuperclass(domainMetaSuperclass);
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}.
     * 
     * Sets the superclass of this {@link DomainMetaClass} to the metaSuperClass passed as argument.
     */
    public void initDomainMetaSuperclass(DomainMetaClass metaSuperclass) {
        checkFrameworkNotInitialized();
        setDomainMetaSuperclass(metaSuperclass);
        logger.info("Initializing the meta-superclass of " + getDomainClass() + " to " + metaSuperclass.getDomainClass());
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}.
     * 
     * Executes any inherited consistency predicates from the superclasses that are not being overridden,
     * for each existing object of this {@link DomainMetaClass}.
     */
    public void executeInheritedPredicates() {
        checkFrameworkNotInitialized();
        if (getDomainMetaSuperclass() == null) {
            return;
        }

        for (DomainConsistencyPredicate newPredicate : getDomainMetaSuperclass().getAllConsistencyPredicates()) {
            newPredicate.executeConsistencyPredicateForMetaClassAndSubclasses(this);
        }
    }

    /**
     * Fills in the collections received as parameters with all the {@link DomainConsistencyPredicate}s
     * that are either inherited or declared by this {@link DomainMetaClass}.<br>
     * This method executes recursively, in top-down order of the DomainMetaClass hierarchy.
     * 
     * @param privatePredicates
     *            the <code>List</code> of {@link PrivateConsistencyPredicate}s
     *            to fill in
     * @param publicPredicates
     *            the <code>Map</code> of <code>Strings</code> with method names, to {@link PublicConsistencyPredicate}s
     */
    private void fillConsistencyPredicatesOfThisClassAndSuperclasses(List<PrivateConsistencyPredicate> privatePredicates,
            Map<String, PublicConsistencyPredicate> publicPredicates) {
        DomainMetaClass metaSuperclass = getDomainMetaSuperclass();
        if (metaSuperclass != null) {
            metaSuperclass.fillConsistencyPredicatesOfThisClassAndSuperclasses(privatePredicates, publicPredicates);
        }
        fillDeclaredPrivatePredicates(privatePredicates);
        fillDeclaredPublicPredicates(publicPredicates);
    }

    /**
     * Adds to the <code>List</code> of {@link PrivateConsistencyPredicate}s
     * passed as argument, all the {@link PrivateConsistencyPredicate}s declared
     * directly in this class.
     */
    private void fillDeclaredPrivatePredicates(List<PrivateConsistencyPredicate> privatePredicates) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicateSet()) {
            if (declaredConsistencyPredicate.isPrivate()) {
                privatePredicates.add((PrivateConsistencyPredicate) declaredConsistencyPredicate);
            }
        }
    }

    /**
     * Adds to the <code>Map</code> of <code>Strings</code> to {@link PublicConsistencyPredicate}s passed as argument, all the
     * {@link PublicConsistencyPredicate}s declared directly in this class, associated to their method names.
     */
    private void fillDeclaredPublicPredicates(Map<String, PublicConsistencyPredicate> publicPredicates) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicateSet()) {
            if (declaredConsistencyPredicate.isPublic()) {
                // Overwrites previous values under the same key.
                // So, any overridden predicate with the same name will be replaced.  
                publicPredicates.put(declaredConsistencyPredicate.getPredicate().getName(),
                        (PublicConsistencyPredicate) declaredConsistencyPredicate);
            }
        }
    }

    /**
     * Returns a <code>Set</code> with all the {@link DomainConsistencyPredicate}s that affect the objects of this class.
     * 
     * @return a <code>Set</code> of all the {@link DomainConsistencyPredicate}s
     *         declared directly by this class, or inherited and not overridden.
     */
    public Set<DomainConsistencyPredicate> getAllConsistencyPredicates() {
        Set<DomainConsistencyPredicate> allPredicates = new HashSet<DomainConsistencyPredicate>();

        List<PrivateConsistencyPredicate> privatePredicates = new ArrayList<PrivateConsistencyPredicate>();
        Map<String, PublicConsistencyPredicate> publicPredicates = new HashMap<String, PublicConsistencyPredicate>();
        fillConsistencyPredicatesOfThisClassAndSuperclasses(privatePredicates, publicPredicates);

        allPredicates.addAll(privatePredicates);
        allPredicates.addAll(publicPredicates.values());
        return allPredicates;
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}.<br>
     * Deletes this <code>DomainMetaClass</code>, and all its metaSubclasses.
     * Also deletes all the declared {@link DomainConsistencyPredicate}s.<br>
     * A DomainMetaClass should be deleted only when the corresponding domain class is removed from the DML,
     * or the framework is configured not to create meta objects.
     **/
    protected void delete() {
        checkFrameworkNotInitialized();
        ConsistencyPredicateSupport.getInstance().removeAllMetaObjectsForMetaClass(this);

        // If we are deleting this class, then the previous subclass will have changed its superclass
        // and should also be deleted.
        for (DomainMetaClass metaSubclass : getDomainMetaSubclassSet()) {
            metaSubclass.delete();
        }

        logger.info("Deleted DomainMetaClass " + getDomainClassName());
        for (DomainConsistencyPredicate domainConsistencyPredicate : getDeclaredConsistencyPredicateSet()) {
            domainConsistencyPredicate.classDelete();
        }
        setDomainMetaSuperclass(null);

        DomainFenixFrameworkRoot root = getDomainFenixFrameworkRoot();
        if (root != null) {
            root.removeDomainMetaClass(this);
        }
        //Deletes THIS metaClass, which is also a Fenix-Framework DomainObject
        deleteDomainObject();
    }

    public static DomainMetaClass readDomainMetaClass(Class<? extends DomainObject> domainClass) {
        return DomainFenixFrameworkRoot.getInstance().getDomainMetaClass(domainClass);
    }

}
