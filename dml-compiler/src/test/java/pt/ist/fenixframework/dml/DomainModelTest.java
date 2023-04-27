package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.dml.runtime.TestUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DomainModelTest {
    private DomainClass clazz;
    private DomainRelation relation;
    private DomainModel model;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        clazz = new DomainClass(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.Clazz", null, null);

        relation = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.TestRelation", null,
                new ArrayList<>());

        RoleTest.MyDomainEntity type = new RoleTest.MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(),
                "pt.ist.fenixframework.MyDomainEntity1");
        relation.addRole(new Role("Role1", type));
        relation.addRole(new Role("Role2", type));

        model = new DomainModel();
        model.addClass(clazz);
        model.addRelation(relation);
    }

    @Test
    public void isNullableType() {
        String[] types = { "boolean", "byte", "char", "short", "int", "float", "long", "double" };

        for (String type : types) {
            ValueType vt = new PlainValueType(type);
            assertFalse(DomainModel.isNullableType(vt));
        }

        assertTrue(DomainModel.isNullableType(new PlainValueType("TestVT")));
    }

    @Test
    public void isNullableTypeFullName() {
        String[] types = { "boolean", "byte", "char", "short", "int", "float", "long", "double" };

        for (String type : types) {
            assertFalse(DomainModel.isNullableTypeFullName(type));
        }

        assertTrue(DomainModel.isNullableTypeFullName("java.io.Serializable"));
    }

    @Test
    public void isBuiltinValueTypeFullName() {
        String[] types = { "boolean", "byte", "char", "short", "int", "float", "long", "double", "java.lang.Boolean",
                "java.lang.Byte", "java.lang.Character", "java.lang.Short", "java.lang.Integer", "java.lang.Float",
                "java.lang.Long", "java.lang.Double", "java.lang.String", "byte[]", "org.joda.time.DateTime",
                "org.joda.time.LocalDate", "org.joda.time.LocalTime", "org.joda.time.Partial", "java.io.Serializable",
                "com.google.gson.JsonElement", };

        for (String type : types) {
            assertTrue(DomainModel.isBuiltinValueTypeFullName(type));
        }

        assertFalse(DomainModel.isBuiltinValueTypeFullName("pt.ist.dml.test.TestVT"));
    }

    @Test
    public void findClassOrExternal() throws MalformedURLException {
        assertNotNull(model.findClassOrExternal("pt.ist.dml.test.Clazz"));
        assertNull(model.findClassOrExternal("pt.ist.dml.test.Clazz2"));
        model.addExternalEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.Clazz2");
        assertNotNull(model.findClassOrExternal("pt.ist.dml.test.Clazz2"));
    }

    @Test
    public void findClass() {
        assertNotNull(model.findClass("pt.ist.dml.test.Clazz"));
        assertNull(model.findClass("pt.ist.dml.test.Clazz2"));
    }

    @Test
    public void findRelation() {
        assertNotNull(model.findRelation("pt.ist.dml.test.TestRelation"));
        assertNull(model.findRelation("pt.ist.dml.test.NonExistentRelation"));
    }

    @Test
    public void addRepeatedClass() {
        assertThrows(RuntimeException.class, () -> model.addClass(clazz));
    }

    @Test
    public void addRepeatedRelation() {
        assertThrows(RuntimeException.class, () -> model.addRelation(relation));
    }

    @Test
    public void addExternalEntity() throws MalformedURLException {
        model.addExternalEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.Clazz2", "Clazz2");
        assertNotNull(model.findClassOrExternal("pt.ist.dml.test.Clazz2"));
        assertNotNull(model.findClassOrExternal("Clazz2"));
    }

    @Test
    public void addClassToFinalizedDomain() throws MalformedURLException {
        DomainClass clazz2 =
                new DomainClass(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.Clazz", null, null);

        model.finalizeDomain();
        assertThrows(RuntimeException.class, () -> model.addClass(clazz2));
    }

    @Test
    public void addRelationToFinalizedDomain() throws MalformedURLException {
        DomainRelation relation2 = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(),
                "pt.ist.dml.test.Relation2", null, new ArrayList<>());

        model.finalizeDomain();
        assertThrows(RuntimeException.class, () -> model.addRelation(relation2));
    }

    @Test
    public void finalizeDomain() {
        assertDoesNotThrow(() -> model.finalizeDomain());
    }

    @Test
    public void finalizeDomainWithExternalEntitiy() throws MalformedURLException {
        model.addExternalEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "External1");
        assertThrows(RuntimeException.class, () -> model.finalizeDomain(true));
    }

    @Test
    public void finalizeDomainWith3Roles() throws MalformedURLException {
        Role role1 = new Role("Role1",
                new DomainEntityTest.MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "Entity1"));
        Role role2 = new Role("Role2",
                new DomainEntityTest.MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "Entity2"));
        Role role3 = new Role("Role3",
                new DomainEntityTest.MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "Entity3"));
        DomainRelation relation2 = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(),
                "pt.ist.dml.test.TestRelation2", null, new ArrayList<>());
        relation2.addRole(role1);
        relation2.addRole(role2);
        relation2.addRole(role3);
        model.addRelation(relation2);
        assertThrows(RuntimeException.class, model::finalizeDomain);
    }

    @Test
    public void checkForRepeatedSlots() {
        // TODO this only prints to stdout...
    }

    @Test
    public void findValueType() {
        model.newValueType("vt1", "pt.ist.dml.test.vt1");
        model.newValueType("vt2", new PlainValueType("pt.ist.dml.test.vt2"));
        model.newEnumType("vt3", "pt.ist.dml.test.vt3");
        //              custom + builtins
        assertEquals(3 + 24, model.getAllValueTypes().size());
        assertNotNull(model.findValueType("vt1"));
        assertNotNull(model.findValueType("vt2"));
        assertNotNull(model.findValueType("vt3"));
        assertNull(model.findValueType("vt4"));
    }

    @Test
    public void isEnumType() {
        model.newValueType("vt1", new PlainValueType("pt.ist.dml.test.vt1"));
        model.newEnumType("vt2", "pt.ist.dml.test.vt2");
        assertFalse(model.isEnumType("vt1"));
        assertTrue(model.isEnumType("vt2"));
        assertFalse(model.isEnumType("vt3"));
    }

    @Test
    public void getters() {
        assertTrue(model.getClasses().hasNext());
        assertEquals(1, model.getDomainClasses().size());
        assertTrue(model.getRelations().hasNext());
        assertEquals(1, model.getDomainRelations().size());
    }

    public static class TestDeletionBlockerListener<T extends DomainObject> implements DeletionBlockerListener<T> {

        public final String blocker;

        public TestDeletionBlockerListener(String blocker) {
            this.blocker = blocker;
        }

        @Override
        public void getDeletionBlockers(T object, Collection<String> blockers) {
            if (blocker != null) {
                blockers.add(blocker);
            }
        }

    }

    public static class TestDeletionListener<T extends DomainObject> implements DeletionListener<T> {
        @Override
        public void deleting(DomainObject object) {
        }
    }

    @Test
    public void addDeletionListener() {
        TestDeletionListener<TestUtils.MyDomainObject> dl = new TestDeletionListener<>();
        TestDeletionBlockerListener<TestUtils.MyDomainObject> dbl = new TestDeletionBlockerListener<>("");

        model.finalizeDomain();

        assertDoesNotThrow(() -> model.registerDeletionBlockerListener(TestUtils.MyDomainObject.class, dbl));

        assertDoesNotThrow(() -> model.registerDeletionListener(TestUtils.MyDomainObject.class, dl));

        List<DeletionBlockerListener<DomainObject>> results = new ArrayList<>();
        model.getDeletionBlockerListenersForType(TestUtils.MyDomainObject.class).forEach(results::add);
        assertTrue(results.contains(dbl));

        List<DeletionListener<DomainObject>> results2 = new ArrayList<>();
        model.getDeletionListenersForType(TestUtils.MyDomainObject.class).forEach(results2::add);
        assertTrue(results2.contains(dl));
    }

    @Test
    public void addDeletionListenerNotFinalized() {
        assertThrows(IllegalStateException.class, () -> model.registerDeletionBlockerListener(TestUtils.MyDomainObject.class,
                new TestDeletionBlockerListener<>("")));

        assertThrows(IllegalStateException.class,
                () -> model.registerDeletionListener(TestUtils.MyDomainObject.class, new TestDeletionListener<>()));

        assertFalse(model.getDeletionBlockerListenersForType(TestUtils.MyDomainObject.class).iterator().hasNext());
        assertFalse(model.getDeletionListenersForType(TestUtils.MyDomainObject.class).iterator().hasNext());
    }

}
