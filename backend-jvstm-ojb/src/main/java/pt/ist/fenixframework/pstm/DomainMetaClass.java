package pt.ist.fenixframework.pstm;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ojb.broker.accesslayer.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.adt.bplustree.BPlusTree;
import pt.ist.fenixframework.backend.jvstmojb.pstm.AbstractDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.OneBoxDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TransactionSupport;
import pt.ist.fenixframework.backend.jvstmojb.repository.DbUtil;
import pt.ist.fenixframework.pstm.consistencyPredicates.DomainConsistencyPredicate;
import pt.ist.fenixframework.pstm.consistencyPredicates.DomainDependenceRecord;
import pt.ist.fenixframework.pstm.consistencyPredicates.PrivateConsistencyPredicate;
import pt.ist.fenixframework.pstm.consistencyPredicates.PublicConsistencyPredicate;

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

    private static final Logger logger = LoggerFactory.getLogger(DomainMetaClass.class);

    private static final int MAX_NUMBER_OF_OBJECTS_TO_PROCESS = 10000;

    /**
     * Compares two classes according to their hierarchies, such that a
     * superclass always appears before any of its subclasses. Classes of
     * different hierarchies are sorted alphabetically according to the names of
     * their successive superclasses (from top to bottom).
     */
    public static Comparator<Class<? extends AbstractDomainObject>> COMPARATOR_BY_CLASS_HIERARCHY_TOP_DOWN =
            new Comparator<Class<? extends AbstractDomainObject>>() {
                @Override
                public int compare(Class<? extends AbstractDomainObject> class1, Class<? extends AbstractDomainObject> class2) {
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
        if (class1.equals(OneBoxDomainObject.class)) {
            return "";
        }
        return getHierarchyName(class1.getSuperclass()) + "<-" + class1.getName();
    }

    /**
     * Compares two <code>DomainMetaClasses</code> according to their
     * hierarchies, such that a superclass always appears before any of its
     * subclasses. <code>DomainMetaClasses</code> of different hierarchies are
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

    public DomainMetaClass(Class<? extends AbstractDomainObject> domainClass) {
        super();
        checkFrameworkNotInitialized();
        setDomainClass(domainClass);
        DomainFenixFrameworkRoot.getInstance().addDomainMetaClasses(this);
        setExistingDomainMetaObjects(new BPlusTree<DomainMetaObject>());
        setFinalized(false);

        logger.info("Creating new a DomainMetaClass: {}", domainClass);
    }

    public DomainMetaClass(Class<? extends AbstractDomainObject> domainClass, DomainMetaClass metaSuperclass) {
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

    @Override
    public void removeDomainFenixFrameworkRoot() {
        checkFrameworkNotInitialized();
        super.removeDomainFenixFrameworkRoot();
    }

    public Class<? extends AbstractDomainObject> getDomainClass() {
        String[] strings = getDomainClassName().split(" ");
        String fullyQualifiedClassName = strings[1];
        strings = fullyQualifiedClassName.split("[.]");

        try {
            Class<?> domainClass = Class.forName(fullyQualifiedClassName);
            return (Class<? extends AbstractDomainObject>) domainClass;
        } catch (ClassNotFoundException e) {
            logger.info("The following domain class has been removed: {}", e.getMessage());
        }
        return null;
    }

    public void setDomainClass(Class<? extends AbstractDomainObject> domainClass) {
        checkFrameworkNotInitialized();
        setDomainClassName(domainClass.toString());
    }

    @Override
    public void setDomainClassName(String domainClassName) {
        checkFrameworkNotInitialized();
        super.setDomainClassName(domainClassName);
    }

    /**
     * @return true if this <code>DomainMetaClass</code> was already fully
     *         initialized
     */
    protected boolean isFinalized() {
        return getFinalized();
    }

    @Override
    public void setFinalized(Boolean finalized) {
        checkFrameworkNotInitialized();
        super.setFinalized(finalized);
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
        metaObject.removeDomainMetaClass();
    }

    public <PredicateT extends DomainConsistencyPredicate> PredicateT getDeclaredConsistencyPredicate(Method predicateMethod) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicates()) {
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
    public void addDeclaredConsistencyPredicates(DomainConsistencyPredicate declaredConsistencyPredicates) {
        checkFrameworkNotInitialized();
        super.addDeclaredConsistencyPredicates(declaredConsistencyPredicates);
    }

    @Override
    public void removeDeclaredConsistencyPredicates(DomainConsistencyPredicate declaredConsistencyPredicates) {
        checkFrameworkNotInitialized();
        super.removeDeclaredConsistencyPredicates(declaredConsistencyPredicates);
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}, for each new
     * <code>DomainMetaClass</code>.
     * 
     * Creates a {@link DomainMetaObject} for each existing {@link AbstractDomainObject} of the specified class, and associates
     * the
     * new {@link DomainMetaObject} to its <code>DomainMetaClass</code>.
     */
    protected void initExistingDomainObjects() {
        checkFrameworkNotInitialized();

        while (true) {
            List<String> oids = getExistingOIDsWithoutMetaObject(getDomainClass());
            if (oids.isEmpty()) {
                break;
            }
            for (String oid : oids) {
                AbstractDomainObject existingDO = null;
                try {
                    existingDO = FenixFramework.getDomainObject(oid);
                } catch (Exception ex) {
                    logger.warn("An exception was thrown while allocating the object: {} - {}", getDomainClass(), oid);
                    ex.printStackTrace();
                    continue;
                }
                DomainMetaObject metaObject = new DomainMetaObject();
                metaObject.setDomainObject(existingDO);
                addExistingDomainMetaObject(metaObject);
            }

            logger.info("Transaction finished. Number of initialized " + getDomainClass().getSimpleName() + " objects: "
                    + getExistingDomainMetaObjectsCount());

            // Starts a new transaction to process a limited number of existing objects.
            // This is necessary to split the load of the mass creation of DomainMetaObjects among several transactions.
            // Each transaction processes a maximum of MAX_NUMBER_OF_OBJECTS_TO_PROCESS objects in order to avoid OutOfMemoryExceptions.
            // Because the JDBC query only returns objects that have no DomainMetaObjects, there is no problem with
            // processing only an incomplete part of the objects of this class.
            Transaction.beginTransaction();
        }
    }

    /**
     * Uses a JDBC query to obtain the OIDs of the existing {@link AbstractDomainObject}s of this class that do not yet have a
     * {@link DomainMetaObject}.<br>
     * <br>
     * This method only returns a <strong>maximum of
     * MAX_NUMBER_OF_OBJECTS_TO_PROCESS</strong> OIDs.
     * 
     * @param domainClass
     *            the <code>Class</code> for which to obtain the existing
     *            objects OIDs
     * 
     * @return the <code>List</code> of <code>Strings</code> containing the OIDs
     *         of all the {@link AbstractDomainObject}s of the given class,
     *         without {@link DomainMetaObject}.
     */
    private static List<String> getExistingOIDsWithoutMetaObject(Class<? extends AbstractDomainObject> domainClass) {
        String tableName = getTableName(domainClass);
        String className = domainClass.getName();

        String metaObjectOidsQuery = "select OID from FF$DOMAIN_META_OBJECT";

        String query =
                "select OID from " + tableName
                        + ", FF$DOMAIN_CLASS_INFO where OID >> 32 = DOMAIN_CLASS_ID and DOMAIN_CLASS_NAME = '" + className
                        + "' and (OID_DOMAIN_META_OBJECT is null or OID_DOMAIN_META_OBJECT not in (" + metaObjectOidsQuery
                        + ")) order by OID limit " + MAX_NUMBER_OF_OBJECTS_TO_PROCESS;

        ArrayList<String> oids = new ArrayList<String>();
        try {
            Statement statement = getCurrentJdbcConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                oids.add(resultSet.getString("OID"));
            }
        } catch (SQLException e) {
            throw new Error(e);
        }

        return oids;
    }

    private static String getTableName(Class<? extends AbstractDomainObject> domainClass) {
        Class<?> topSuperClass = domainClass;
        while (!topSuperClass.getSuperclass().getSuperclass().equals(OneBoxDomainObject.class)) {
            //skip to the next non-base superclass
            topSuperClass = topSuperClass.getSuperclass().getSuperclass();
        }
        return DbUtil.convertToDBStyle(topSuperClass.getSimpleName());
    }

    private String getTableName() {
        DomainMetaClass topMetaClass = this;
        while (topMetaClass.hasDomainMetaSuperclass()) {
            //skip to the next non-base superclass
            topMetaClass = topMetaClass.getDomainMetaSuperclass();
        }
        return DbUtil.convertToDBStyle(getSimpleClassName(topMetaClass.getDomainClassName()));
    }

    private String getSimpleClassName(String fullClassName) {
        String[] strings = fullClassName.split("\\.");
        return strings[strings.length - 1];
    }

    @Override
    public void addDomainMetaSubclasses(DomainMetaClass domainMetaSubclasses) {
        checkFrameworkNotInitialized();
        super.addDomainMetaSubclasses(domainMetaSubclasses);
    }

    @Override
    public void removeDomainMetaSubclasses(DomainMetaClass domainMetaSubclasses) {
        checkFrameworkNotInitialized();
        super.removeDomainMetaSubclasses(domainMetaSubclasses);
    }

    @Override
    public void setDomainMetaSuperclass(DomainMetaClass domainMetaSuperclass) {
        checkFrameworkNotInitialized();
        super.setDomainMetaSuperclass(domainMetaSuperclass);
    }

    @Override
    public void removeDomainMetaSuperclass() {
        checkFrameworkNotInitialized();
        super.removeDomainMetaSuperclass();
    }

    /**
     * This method should be called only during the initialization of the {@link FenixFramework}.
     * 
     * Sets the superclass of this DomainMetaClass to the metaSuperClass passed as argument.
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
     * for each existing object of this DomainMetaClass.
     */
    public void executeInheritedPredicates() {
        checkFrameworkNotInitialized();

        if (!hasDomainMetaSuperclass()) {
            return;
        }

        List<PrivateConsistencyPredicate> privatePredicates = new ArrayList<PrivateConsistencyPredicate>();
        Map<String, PublicConsistencyPredicate> publicPredicates = new HashMap<String, PublicConsistencyPredicate>();
        getDomainMetaSuperclass().fillConsistencyPredicatesOfThisClassAndSuperclasses(privatePredicates, publicPredicates);

        for (DomainConsistencyPredicate newPredicate : privatePredicates) {
            newPredicate.executeConsistencyPredicateForMetaClassAndSubclasses(this);
        }
        for (DomainConsistencyPredicate newPredicate : publicPredicates.values()) {
            newPredicate.executeConsistencyPredicateForMetaClassAndSubclasses(this);
        }
    }

    /**
     * Fills in the collections received as parameters with all the {@link DomainConsistencyPredicate}s that are either inherited,
     * or
     * declared by this DomainMetaClass.
     * 
     * @param privatePredicates
     *            the <code>List</code> of {@link PrivateConsistencyPredicate}s
     *            to fill in
     * @param publicPredicates
     *            the <code>Map</code> of <code>Strings</code> to {@link PublicConsistencyPredicate}s to fill in
     */
    private void fillConsistencyPredicatesOfThisClassAndSuperclasses(List<PrivateConsistencyPredicate> privatePredicates,
            Map<String, PublicConsistencyPredicate> publicPredicates) {
        DomainMetaClass metaSuperclass = getDomainMetaSuperclass();
        if (metaSuperclass != null) {
            metaSuperclass.fillConsistencyPredicatesOfThisClassAndSuperclasses(privatePredicates, publicPredicates);
        }
        getPrivateConsistencyPredicates(privatePredicates);
        getPublicConsistencyPredicates(publicPredicates);
    }

    /**
     * Adds to the <code>List</code> of {@link PrivateConsistencyPredicate}s
     * passed as argument, all the {@link PrivateConsistencyPredicate}s declared
     * directly in this class.
     */
    private void getPrivateConsistencyPredicates(List<PrivateConsistencyPredicate> privatePredicates) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicates()) {
            if (declaredConsistencyPredicate.isPrivate()) {
                privatePredicates.add((PrivateConsistencyPredicate) declaredConsistencyPredicate);
            }
        }
    }

    /**
     * Adds to the <code>Map</code> of <code>Strings</code> to {@link PublicConsistencyPredicate}s passed as argument, all the
     * {@link PublicConsistencyPredicate}s declared directly in this class,
     * associated to their method names.
     */
    private void getPublicConsistencyPredicates(Map<String, PublicConsistencyPredicate> publicPredicates) {
        for (DomainConsistencyPredicate declaredConsistencyPredicate : getDeclaredConsistencyPredicates()) {
            if (declaredConsistencyPredicate.isPublic()) {
                publicPredicates.put(declaredConsistencyPredicate.getPredicate().getName(),
                        (PublicConsistencyPredicate) declaredConsistencyPredicate);
            }
        }
    }

    /**
     * Returns a <code>Set</code> with all the {@link DomainConsistencyPredicate}s that affect the objects of this
     * class.
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
     * This method should be called only during the initialization of the {@link FenixFramework}. <br>
     * Deletes this <code>DomainMetaClass</code>, and all its metaSubclasses.
     * Also deletes all the declared {@link DomainConsistencyPredicate}s. <br>
     * A DomainMetaClass should be deleted only when the corresponding domain
     * class is removed from the DML, or the framework is configured not to
     * create meta objects.
     **/
    protected void delete() {
        checkFrameworkNotInitialized();
        removeAllMetaObjects();

        // If we are deleting this class, then the previous subclass will have changed its superclass
        // and should also be deleted.
        for (DomainMetaClass metaSubclass : getDomainMetaSubclasses()) {
            metaSubclass.delete();
        }

        logger.info("Deleted DomainMetaClass " + getDomainClassName());
        for (DomainConsistencyPredicate domainConsistencyPredicate : getDeclaredConsistencyPredicates()) {
            domainConsistencyPredicate.classDelete();
        }
        removeDomainMetaSuperclass();

        DomainFenixFrameworkRoot root = getDomainFenixFrameworkRoot();
        if (root != null) {
            root.removeDomainMetaClasses(this);
        }
        //Deletes THIS metaClass, which is also a Fenix-Framework DomainObject
        deleteDomainObject();
    }

    /**
     * Uses a JDBC query to obtain the delete all the {@link DomainMetaObject}s
     * of this class. It also sets to null any OIDs that used to point to the
     * deleted {@link DomainMetaObject}s, both in the {@link AbstractDomainObject}'s and in the {@link DomainDependenceRecord} 's
     * tables.
     */
    protected void removeAllMetaObjects() {
        String tableName = getTableName();

        String metaObjectsToDeleteQuery =
                "select OID from FF$DOMAIN_META_OBJECT where OID_DOMAIN_META_CLASS = '" + getExternalId() + "'";

        String clearDomainObjectsQuery =
                "update " + tableName + " set OID_DOMAIN_META_OBJECT = null " + "where OID_DOMAIN_META_OBJECT in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearDomainObjectsQuery);
        } catch (SQLException e) {
            logger.warn("The deleted DomainMetaClass " + getDomainClassName() + " had a table named " + tableName
                    + " that no longer exists.");
            e.printStackTrace();
        }

        String clearDependenceRecordsQuery =
                "delete from FF$DOMAIN_DEPENDENCE_RECORD where OID_DEPENDENT_DOMAIN_META_OBJECT " + "in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearDependenceRecordsQuery);
        } catch (SQLException e) {
            throw new Error(e);
        }

        String clearIndirectionTable =
                "delete from FF$DEPENDED_DOMAIN_META_OBJECTS_DEPENDING_DEPENDENCE_RECORDS" + " where OID_DOMAIN_META_OBJECT in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearIndirectionTable);
        } catch (SQLException e) {
            throw new Error(e);
        }

        // Delete the BPlusTree that linked to the existing DomainMetaObjects
        BPlusTree<DomainMetaObject> bPlusTree = getExistingDomainMetaObjects();
        bPlusTree.delete();
        removeExistingDomainMetaObjects();

        String deleteMetaObjectsQuery =
                "delete from FF$DOMAIN_META_OBJECT where OID_DOMAIN_META_CLASS = '" + getExternalId() + "'";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(deleteMetaObjectsQuery);
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    public static DomainMetaClass readDomainMetaClass(Class<? extends AbstractDomainObject> domainClass) {
        return DomainFenixFrameworkRoot.getInstance().getDomainMetaClass(domainClass);
    }

    private static Connection getCurrentJdbcConnection() {
        try {
            return TransactionSupport.getOJBBroker().serviceConnectionManager().getConnection();
        } catch (LookupException e) {
            throw new Error(e);
        }
    }
}
