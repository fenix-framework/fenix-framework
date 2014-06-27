package pt.ist.fenixframework.backend.jvstmojb.ojb;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;
import org.apache.ojb.broker.metadata.fieldaccess.PersistentField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstmojb.repository.DbUtil;
import pt.ist.fenixframework.backend.jvstmojb.repository.database.JDBCTypeMap;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainEntity;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

/**
 * @author - Shezad Anavarali (shezad@ist.utl.pt)
 * 
 */
public class OJBMetadataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OJBMetadataGenerator.class);

    public static void updateOJBMappingFromDomainModel(DomainModel domainModel) throws Exception {

        final DescriptorRepository descriptorRepository = MetadataManager.getInstance().getGlobalRepository();
        @SuppressWarnings("unchecked")
        Map<String, ClassDescriptor> ojbMetadata = descriptorRepository.getDescriptorTable();

        for (DomainClass domClass : domainModel.getDomainClasses()) {
            final String classname = domClass.getFullName();
            final Class<?> clazz = Class.forName(classname);
            final ClassDescriptor classDescriptor = new ClassDescriptor(descriptorRepository);
            classDescriptor.setClassOfObject(clazz);
            classDescriptor.setTableName(getExpectedTableName(domClass));
            ojbMetadata.put(domClass.getFullName(), classDescriptor);
        }

        for (DomainClass domClass : domainModel.getDomainClasses()) {
            final String classname = domClass.getFullName();
            final Class<?> clazz = Class.forName(classname);
            final ClassDescriptor classDescriptor = ojbMetadata.get(classname);

            addClassExtentOfAncesterClassDescriptors(ojbMetadata, domClass.getSuperclass(), clazz);

            if (classDescriptor != null) {
                setFactoryMethodAndClass(classDescriptor);

                updateFields(domainModel, classDescriptor, domClass, ojbMetadata, clazz);
                if (!Modifier.isAbstract(clazz.getModifiers())) {
                    updateRelations(classDescriptor, domClass, ojbMetadata, clazz);
                }
            }
        }

    }

    private static void addClassExtentOfAncesterClassDescriptors(final Map<String, ClassDescriptor> ojbMetadata,
            final DomainEntity domainEntity, final Class<?> clazz) {
        if (domainEntity != null && domainEntity instanceof DomainClass) {
            final DomainClass domainClass = (DomainClass) domainEntity;
            final String ancesterClassname = domainClass.getFullName();
            final ClassDescriptor classDescriptor = ojbMetadata.get(ancesterClassname);
            classDescriptor.addExtentClass(clazz);
            addClassExtentOfAncesterClassDescriptors(ojbMetadata, domainClass.getSuperclass(), clazz);
        }
    }

    public static String getExpectedTableName(final DomainClass domainClass) {
        if (domainClass.getSuperclass() == null) {
            return getTableName(domainClass.getName());
        }
        return domainClass.getSuperclass() instanceof DomainClass ? getExpectedTableName((DomainClass) domainClass
                .getSuperclass()) : null;
    }

    private static String getTableName(final String name) {
        final StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (final char c : name.toCharArray()) {
            if (isFirst) {
                isFirst = false;
                stringBuilder.append(Character.toUpperCase(c));
            } else {
                if (Character.isUpperCase(c)) {
                    stringBuilder.append('_');
                    stringBuilder.append(c);
                } else {
                    stringBuilder.append(Character.toUpperCase(c));
                }
            }
        }
        return stringBuilder.toString();
    }

    private static void setFactoryMethodAndClass(ClassDescriptor cld) {
        // this will eventually disappear
        cld.setFactoryClass(DomainObjectAllocator.class);
    }

    protected static void updateFields(final DomainModel domainModel, final ClassDescriptor classDescriptor,
            final DomainClass domClass, final Map<String, ClassDescriptor> ojbMetadata, final Class<?> persistentFieldClass)
            throws Exception {

        DomainEntity domEntity = domClass;
        int fieldID = 1;

        addPrimaryFieldDescriptor(domainModel, "oid", "long", fieldID++, classDescriptor, persistentFieldClass);

        // write the domainMetaObject for all domain objects
        addFieldDescriptor(domainModel, "oidDomainMetaObject", "Long", fieldID++, classDescriptor, persistentFieldClass);

        while (domEntity instanceof DomainClass) {
            DomainClass dClass = (DomainClass) domEntity;

            Iterator<Slot> slots = dClass.getSlots();
            while (slots.hasNext()) {
                Slot slot = slots.next();

                String slotName = slot.getName();
                String slotType = slot.getSlotType().getDomainName();
                addFieldDescriptor(domainModel, slotName, slotType, fieldID++, classDescriptor, persistentFieldClass);
            }

            for (Role role : dClass.getRoleSlotsList()) {
                String roleName = role.getName();
                if ((role.getMultiplicityUpper() == 1) && (roleName != null)) {
                    String foreignOidField = "oid" + StringUtils.capitalize(roleName);
                    addFieldDescriptor(domainModel, foreignOidField, "Long", fieldID++, classDescriptor, persistentFieldClass);
                }
            }

            domEntity = dClass.getSuperclass();
        }

    }

    protected static void addPrimaryFieldDescriptor(DomainModel domainModel, String slotName, String slotType, int fieldID,
            ClassDescriptor classDescriptor, Class<?> persistentFieldClass) throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(classDescriptor, fieldID);
        fieldDescriptor.setColumnName(DbUtil.convertToDBStyle(slotName));
        fieldDescriptor.setAccess("readwrite");
        fieldDescriptor.setPrimaryKey(true);
        fieldDescriptor.setAutoIncrement(true);

        PersistentField persistentField = new ReadOnlyPersistentField(persistentFieldClass, slotName);
        fieldDescriptor.setPersistentField(persistentField);

        String sqlType = JDBCTypeMap.getJdbcTypeFor(domainModel, slotType);
        fieldDescriptor.setColumnType(sqlType);

        classDescriptor.addFieldDescriptor(fieldDescriptor);
    }

    protected static void addFieldDescriptor(DomainModel domainModel, String slotName, String slotType, int fieldID,
            ClassDescriptor classDescriptor, Class<?> persistentFieldClass) throws Exception {
        if (classDescriptor.getFieldDescriptorByName(slotName) == null) {
            FieldDescriptor fieldDescriptor = new FieldDescriptor(classDescriptor, fieldID);
            fieldDescriptor.setColumnName(DbUtil.convertToDBStyle(slotName));
            fieldDescriptor.setAccess("readwrite");

            PersistentField persistentField = new ReadOnlyPersistentField(persistentFieldClass, slotName);
            fieldDescriptor.setPersistentField(persistentField);

            String sqlType = JDBCTypeMap.getJdbcTypeFor(domainModel, slotType);
            fieldDescriptor.setColumnType(sqlType);

            classDescriptor.addFieldDescriptor(fieldDescriptor);
        }
    }

    protected static void updateRelations(final ClassDescriptor classDescriptor, final DomainClass domClass,
            Map<String, ClassDescriptor> ojbMetadata, Class<?> persistentFieldClass) throws Exception {

        DomainEntity domEntity = domClass;
        while (domEntity instanceof DomainClass) {
            DomainClass dClass = (DomainClass) domEntity;

            // roles
            Iterator<Role> roleSlots = dClass.getRoleSlots();
            while (roleSlots.hasNext()) {
                Role role = roleSlots.next();
                String roleName = role.getName();

                if (roleName == null) {
                    continue;
                }

                if (role.getMultiplicityUpper() != 1) {
                    // collection descriptors
                    if (classDescriptor.getCollectionDescriptorByName(roleName) == null) {

                        CollectionDescriptor collectionDescriptor = new CollectionDescriptor(classDescriptor);

                        if (role.getOtherRole().getMultiplicityUpper() == 1) {

                            String fkField = "oid" + StringUtils.capitalize(role.getOtherRole().getName());

                            ClassDescriptor otherClassDescriptor = ojbMetadata.get(((DomainClass) role.getType()).getFullName());

                            if (otherClassDescriptor == null) {
                                logger.warn("Ignoring {}", ((DomainClass) role.getType()).getFullName());
                                continue;
                            }

                            generateOneToManyCollectionDescriptor(collectionDescriptor, fkField);

                        } else {
                            generateManyToManyCollectionDescriptor(collectionDescriptor, role);

                        }
                        updateCollectionDescriptorWithCommonSettings(classDescriptor, persistentFieldClass, role, roleName,
                                collectionDescriptor);
                    }
                }
            }

            domEntity = dClass.getSuperclass();
        }
    }

    private static void updateCollectionDescriptorWithCommonSettings(final ClassDescriptor classDescriptor,
            Class<?> persistentFieldClass, Role role, String roleName, CollectionDescriptor collectionDescriptor)
            throws ClassNotFoundException {
        collectionDescriptor.setItemClass(Class.forName(role.getType().getFullName()));
        collectionDescriptor.setPersistentField(new WriteOnlyPersistentField(persistentFieldClass, roleName));
        collectionDescriptor.setRefresh(false);
        collectionDescriptor.setCascadingStore(ObjectReferenceDescriptor.CASCADE_NONE);
        collectionDescriptor.setCollectionClass(OJBFunctionalSetWrapper.class);
        collectionDescriptor.setCascadeRetrieve(false);
        collectionDescriptor.setLazy(false);
        classDescriptor.addCollectionDescriptor(collectionDescriptor);
    }

    private static void generateManyToManyCollectionDescriptor(CollectionDescriptor collectionDescriptor, Role role) {

        String indirectionTableName = DbUtil.convertToDBStyle(role.getRelation().getName());
        String fkToItemClass = DbUtil.getFkName(role.getType().getName());
        String fkToThisClass = DbUtil.getFkName(role.getOtherRole().getType().getName());

        if (fkToItemClass.equals(fkToThisClass)) {
            fkToItemClass = fkToItemClass + "_" + DbUtil.convertToDBStyle(role.getName());
            fkToThisClass = fkToThisClass + "_" + DbUtil.convertToDBStyle(role.getOtherRole().getName());
        }

        collectionDescriptor.setIndirectionTable(indirectionTableName);
        collectionDescriptor.addFkToItemClass(fkToItemClass);
        collectionDescriptor.addFkToThisClass(fkToThisClass);
        collectionDescriptor.setCascadingDelete(ObjectReferenceDescriptor.CASCADE_NONE);
    }

    private static void generateOneToManyCollectionDescriptor(CollectionDescriptor collectionDescriptor, String foreignKeyField) {
        collectionDescriptor.addForeignKeyField(foreignKeyField);
    }
}
