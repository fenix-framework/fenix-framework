package pt.ist.fenixframework.pstm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

import pt.ist.fenixframework.pstm.ojb.ReadOnlyPersistentField;
import pt.ist.fenixframework.pstm.ojb.WriteOnlyPersistentField;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;
import org.apache.ojb.broker.metadata.fieldaccess.PersistentField;

import dml.DomainClass;
import dml.DomainEntity;
import dml.Role;
import dml.Slot;
import dml.ValueType;

/**
 * @author - Shezad Anavarali (shezad@ist.utl.pt)
 * 
 */
public class OJBMetadataGenerator {

    private static final String DOMAIN_OBJECT_CLASSNAME = "net.sourceforge.fenixedu.domain.DomainObject";

    private static String classToDebug = null;


    private static void addPersistentRootClassDescriptor(FenixDomainModel domainModel, 
                                                         DescriptorRepository repository) throws Exception {
        Class persRootClass = PersistentRoot.class;
        ClassDescriptor classDescriptor = new ClassDescriptor(repository);
        classDescriptor.setClassOfObject(persRootClass);
        classDescriptor.setTableName("FF$PERSISTENT_ROOT");
        setFactoryMethodAndClass(classDescriptor);

        addPrimaryFieldDescriptor(domainModel, "oid", "long", 1, classDescriptor, persRootClass);
        addFieldDescriptor(domainModel, PersistentRoot.SLOT_NAME, "long", 2, classDescriptor, persRootClass);

        repository.getDescriptorTable().put(PersistentRoot.class.getName(), classDescriptor);
    }

    public static void updateOJBMappingFromDomainModel(FenixDomainModel domainModel) throws Exception {

	final DescriptorRepository descriptorRepository = MetadataManager.getInstance().getGlobalRepository();
        Map ojbMetadata = descriptorRepository.getDescriptorTable();

        addPersistentRootClassDescriptor(domainModel, descriptorRepository);

        for (final Iterator iterator = domainModel.getClasses(); iterator.hasNext();) {
            final DomainClass domClass = (DomainClass) iterator.next();
            final String classname = domClass.getFullName();
            if (!classname.equals(DOMAIN_OBJECT_CLASSNAME)) {
        	final Class clazz = Class.forName(classname);
        	final ClassDescriptor classDescriptor = new ClassDescriptor(descriptorRepository);
        	classDescriptor.setClassOfObject(clazz);
        	classDescriptor.setTableName(getExpectedTableName(domClass));
        	ojbMetadata.put(domClass.getFullName(), classDescriptor);
            }
        }

       for (final Iterator iterator = domainModel.getClasses(); iterator.hasNext();) {
            final DomainClass domClass = (DomainClass) iterator.next();
            final String classname = domClass.getFullName();
            if (!classname.equals(DOMAIN_OBJECT_CLASSNAME)) {

        	final Class clazz = Class.forName(classname);
        	final ClassDescriptor classDescriptor = (ClassDescriptor) ojbMetadata.get(classname);

        	addClassExtentOfAncesterClassDescriptors(ojbMetadata, domClass.getSuperclass(), clazz);

        	if (classDescriptor != null) {
        	    setFactoryMethodAndClass(classDescriptor);

        	    updateFields(domainModel, classDescriptor, domClass, ojbMetadata, clazz);
        	    if (!Modifier.isAbstract(clazz.getModifiers())) {
        		updateRelations(classDescriptor, domClass, ojbMetadata, clazz);
        	    }
                
        	    if (classToDebug != null && classDescriptor.getClassNameOfObject().contains(classToDebug)) {
        		System.out.println(classDescriptor.toXML());
        	    }
        	}
            }

        }

    }

    private static void addClassExtentOfAncesterClassDescriptors(final Map ojbMetadata, final DomainEntity domainEntity, final Class clazz) {
	if (domainEntity != null && domainEntity instanceof DomainClass) {
	    final DomainClass domainClass = (DomainClass) domainEntity;
	    final String ancesterClassname = domainClass.getFullName();
	    if (!ancesterClassname.equals(DOMAIN_OBJECT_CLASSNAME)) {
		final ClassDescriptor classDescriptor = (ClassDescriptor) ojbMetadata.get(ancesterClassname);
		classDescriptor.addExtentClass(clazz);
		addClassExtentOfAncesterClassDescriptors(ojbMetadata, domainClass.getSuperclass(), clazz);
	    }
	}
    }

    private static String getExpectedTableName(final DomainClass domainClass) {
	if (domainClass.getFullName().equals(DOMAIN_OBJECT_CLASSNAME)) {
	    return null;
	}
	if (domainClass.getSuperclass() == null ||
		(domainClass.getSuperclass() instanceof DomainClass && domainClass.getSuperclass().getFullName().equals(DOMAIN_OBJECT_CLASSNAME))) {
	    return getTableName(domainClass.getName());
	}
	return domainClass.getSuperclass() instanceof DomainClass ? getExpectedTableName((DomainClass) domainClass.getSuperclass()) : null;
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


    protected static void updateFields(final FenixDomainModel domainModel,
                                       final ClassDescriptor classDescriptor,
                                       final DomainClass domClass, 
                                       final Map ojbMetadata, 
                                       final Class persistentFieldClass) throws Exception {

        DomainEntity domEntity = domClass;
        int fieldID = 1;

        addPrimaryFieldDescriptor(domainModel, "idInternal", "int", fieldID++, classDescriptor, persistentFieldClass);

        // write the OID also
        addFieldDescriptor(domainModel, "oid", "long", fieldID++, classDescriptor, persistentFieldClass);

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

    protected static void addPrimaryFieldDescriptor(FenixDomainModel domainModel,
                                                    String slotName,
                                                    String slotType,
                                                    int fieldID, 
                                                    ClassDescriptor classDescriptor,
                                                    Class persistentFieldClass) throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(classDescriptor, fieldID);
        fieldDescriptor.setColumnName(convertToDBStyle(slotName));
        fieldDescriptor.setAccess("readwrite");
        fieldDescriptor.setPrimaryKey(true);
        fieldDescriptor.setAutoIncrement(true);

        PersistentField persistentField = new ReadOnlyPersistentField(persistentFieldClass, slotName);
        fieldDescriptor.setPersistentField(persistentField);

        String sqlType = domainModel.getJdbcTypeFor(slotType);
        fieldDescriptor.setColumnType(sqlType);

        classDescriptor.addFieldDescriptor(fieldDescriptor);
    }


    protected static void addFieldDescriptor(FenixDomainModel domainModel,
                                             String slotName,
                                             String slotType,
                                             int fieldID, 
                                             ClassDescriptor classDescriptor,
                                             Class persistentFieldClass) throws Exception {
        if (classDescriptor.getFieldDescriptorByName(slotName) == null) {
            FieldDescriptor fieldDescriptor = new FieldDescriptor(classDescriptor, fieldID);
            fieldDescriptor.setColumnName(convertToDBStyle(slotName));
            fieldDescriptor.setAccess("readwrite");

            PersistentField persistentField = new ReadOnlyPersistentField(persistentFieldClass, slotName);
            fieldDescriptor.setPersistentField(persistentField);

            String sqlType = domainModel.getJdbcTypeFor(slotType);
            fieldDescriptor.setColumnType(sqlType);

            classDescriptor.addFieldDescriptor(fieldDescriptor);
        }
    }

    protected static void updateRelations(final ClassDescriptor classDescriptor,
            final DomainClass domClass, Map ojbMetadata, Class persistentFieldClass) throws Exception {

        DomainEntity domEntity = domClass;
        while (domEntity instanceof DomainClass) {
            DomainClass dClass = (DomainClass) domEntity;

            // roles
            Iterator roleSlots = dClass.getRoleSlots();
            while (roleSlots.hasNext()) {
                Role role = (Role) roleSlots.next();
                String roleName = role.getName();

                if (domClass.getFullName().equals("net.sourceforge.fenixedu.domain.RootDomainObject")
                        && roleName != null
                        && (roleName.equals("rootDomainObject") || roleName.equals("rootDomainObjects"))) {
                    continue;
                }

                if (role.getMultiplicityUpper() != 1) {
                    // collection descriptors
                    if (classDescriptor.getCollectionDescriptorByName(roleName) == null) {

                        CollectionDescriptor collectionDescriptor = new CollectionDescriptor(
                                classDescriptor);

                        if (role.getOtherRole().getMultiplicityUpper() == 1) {

                            String fkField = "oid" + StringUtils.capitalize(role.getOtherRole().getName());

                            ClassDescriptor otherClassDescriptor = (ClassDescriptor) ojbMetadata
                                    .get(((DomainClass) role.getType()).getFullName());

                            if (otherClassDescriptor == null) {
                                System.out.println("Ignoring "
                                        + ((DomainClass) role.getType()).getFullName());
                                continue;
                            }

                            generateOneToManyCollectionDescriptor(collectionDescriptor, fkField);

                        } else {
                            generateManyToManyCollectionDescriptor(collectionDescriptor, role);

                        }
                        updateCollectionDescriptorWithCommonSettings(classDescriptor,
                                persistentFieldClass, role, roleName, collectionDescriptor);
                    }
                }
            }

            domEntity = dClass.getSuperclass();
        }
    }

    private static void updateCollectionDescriptorWithCommonSettings(
            final ClassDescriptor classDescriptor, Class persistentFieldClass, Role role,
            String roleName, CollectionDescriptor collectionDescriptor) throws ClassNotFoundException {
        collectionDescriptor.setItemClass(Class.forName(role.getType().getFullName()));
        collectionDescriptor.setPersistentField(new WriteOnlyPersistentField(persistentFieldClass, roleName));
        collectionDescriptor.setRefresh(false);
        collectionDescriptor.setCascadingStore(ObjectReferenceDescriptor.CASCADE_NONE);
        collectionDescriptor.setCollectionClass(OJBFunctionalSetWrapper.class);
        collectionDescriptor.setCascadeRetrieve(false);
        collectionDescriptor.setLazy(false);
        classDescriptor.addCollectionDescriptor(collectionDescriptor);
    }

    private static void generateManyToManyCollectionDescriptor(
            CollectionDescriptor collectionDescriptor, Role role) {

        String indirectionTableName = convertToDBStyle(role.getRelation().getName());
        String fkToItemClass = "OID_" + convertToDBStyle(role.getType().getName());
        String fkToThisClass = "OID_" + convertToDBStyle(role.getOtherRole().getType().getName());

        if (fkToItemClass.equals(fkToThisClass)) {
            fkToItemClass = fkToItemClass + "_" + convertToDBStyle(role.getName());
            fkToThisClass = fkToThisClass + "_" + convertToDBStyle(role.getOtherRole().getName());
        }

        collectionDescriptor.setIndirectionTable(indirectionTableName);
        collectionDescriptor.addFkToItemClass(fkToItemClass);
        collectionDescriptor.addFkToThisClass(fkToThisClass);
        collectionDescriptor.setCascadingDelete(ObjectReferenceDescriptor.CASCADE_NONE);
    }

    private static void generateOneToManyCollectionDescriptor(CollectionDescriptor collectionDescriptor,
            String foreignKeyField) {
        collectionDescriptor.addForeignKeyField(foreignKeyField);
    }

    private static String convertToDBStyle(String string) {
	StringBuilder result = new StringBuilder(string.length() + 10);
	boolean first = true;
	for (char c : string.toCharArray()) {
	    if (first) {
		first = false;
	    } else if (Character.isUpperCase(c)) {
		result.append('_');
	    }
	    result.append(Character.toUpperCase(c));
	}

	return result.toString();
    }

}
