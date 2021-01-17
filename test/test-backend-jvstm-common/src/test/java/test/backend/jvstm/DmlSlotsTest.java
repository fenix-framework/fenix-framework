package test.backend.jvstm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;
import test.backend.jvstm.domain.BuiltInTypes;
import test.backend.jvstm.domain.CompositeValueType;
import test.backend.jvstm.domain.Counter;
import test.backend.jvstm.domain.ParametricValueType;
import test.backend.jvstm.domain.ValueTypes;

public class DmlSlotsTest {

    private static final Logger logger = LoggerFactory.getLogger(DmlSlotsTest.class);

    // built in types
    static boolean slotBooleanPrimitive;
    static byte slotBytePrimitive;
    static char slotCharPrimitive;
    static short slotShortPrimitive;
    static int slotIntPrimitive;
    static float slotFloatPrimitive;
    static long slotLongPrimitive;
    static double slotDoublePrimitive;

    static Boolean slotBoolean;
    static Byte slotByte;
    static Character slotCharacter;
    static Short slotShort;
    static Integer slotInteger;
    static Float slotFloat;
    static Long slotLong;
    static Double slotDouble;

    static String slotString;

    static byte[] slotBytearray;

    static DateTime slotDatetime;
    static LocalDate slotLocaldate;
    static LocalTime slotLocaltime;
    static Partial slotPartial;

    static Serializable slotSerializable;

    // value types
    static BigDecimal aBigDecimalAuto;
    static BigDecimal aBigDecimalManual;
    static CompositeValueType aCompositeValueType;
    static BigInteger aBigInteger;
    static CompositeValueType anotherCompositeValueType;
    static CompositeValueType yetAnotherCompositeValueType;
    static ParametricValueType<BigDecimal, String> aParametricValueType;
    static ParametricValueType<BigDecimal, String> anotherParametricValueType;

    // entity in relation to one
    static Counter counter;

    // entity in relation to many
    static private Set<Counter> counters;

    @BeforeAll
    public static void setup() {
        // set builtin types
        slotBooleanPrimitive = true;
        slotBytePrimitive = (byte) 1;
        slotCharPrimitive = 'c';
        slotShortPrimitive = (short) 4;
        slotIntPrimitive = 5;
        slotFloatPrimitive = 3.4f;
        slotLongPrimitive = 9L;
        slotDoublePrimitive = 3.5d;

        slotBoolean = new Boolean(true);
        slotByte = new Byte((byte) 1);
        slotCharacter = new Character('d');
        slotShort = new Short((short) 7);
        slotInteger = new Integer(13);
        slotFloat = new Float(3.7f);
        slotLong = new Long(10L);
        slotDouble = new Double(3.9d);

        slotString = "hello";

        slotBytearray = new byte[] { 0, 1, 0 };

        slotDatetime = new DateTime(2013, 3, 29, 22, 29, 0, 0);
        slotLocaldate = new LocalDate(2013, 3, 29);
        slotLocaltime = new LocalTime(22, 36, 0);
        slotPartial = new Partial(DateTimeFieldType.dayOfMonth(), 29);
        slotSerializable = "this is serializable";

        // set value types
        aBigDecimalAuto = new BigDecimal(123);
        aBigDecimalManual = new BigDecimal(123);
        aCompositeValueType = CompositeValueType.fromComponents(new BigDecimal(456), "euro");
        aBigInteger = new BigInteger("98");
        anotherCompositeValueType = CompositeValueType.fromComponents(new BigDecimal("007"), "libra");
        yetAnotherCompositeValueType = CompositeValueType.fromComponents(new BigDecimal("008"), "escudo");
        aParametricValueType = new ParametricValueType<BigDecimal, String>(new BigDecimal("9"), "peso");
        anotherParametricValueType = new ParametricValueType<BigDecimal, String>(new BigDecimal("10"), "xelim");
    }

    @Test
    public void testBuiltInTypes() throws InterruptedException {
        initBuiltInTypes();
        confirmBuiltInTypes();
    }

    @Atomic(mode = TxMode.WRITE)
    private void initBuiltInTypes() {
        BuiltInTypes bt = new BuiltInTypes();

        bt.setSlotBooleanPrimitive(slotBooleanPrimitive);
        bt.setSlotBytePrimitive(slotBytePrimitive);
        bt.setSlotCharPrimitive(slotCharPrimitive);
        bt.setSlotShortPrimitive(slotShortPrimitive);
        bt.setSlotIntPrimitive(slotIntPrimitive);
        bt.setSlotFloatPrimitive(slotFloatPrimitive);
        bt.setSlotLongPrimitive(slotLongPrimitive);
        bt.setSlotDoublePrimitive(slotDoublePrimitive);

        bt.setSlotBoolean(slotBoolean);
        bt.setSlotByte(slotByte);
        bt.setSlotCharacter(slotCharacter);
        bt.setSlotShort(slotShort);
        bt.setSlotInteger(slotInteger);
        bt.setSlotFloat(slotFloat);
        bt.setSlotLong(slotLong);
        bt.setSlotDouble(slotDouble);

        bt.setSlotString(slotString);

        bt.setSlotBytearray(slotBytearray);

        bt.setSlotDatetime(slotDatetime);
        bt.setSlotLocaldate(slotLocaldate);
        bt.setSlotLocaltime(slotLocaltime);
        bt.setSlotPartial(slotPartial);

        bt.setSlotSerializable(slotSerializable);

        FenixFramework.getDomainRoot().setBuiltInTypes(bt);
    }

    @Atomic
    private void confirmBuiltInTypes() {
        BuiltInTypes bt = FenixFramework.getDomainRoot().getBuiltInTypes();

        assertEquals(slotBooleanPrimitive, bt.getSlotBooleanPrimitive());
        assertEquals(slotBytePrimitive, bt.getSlotBytePrimitive());
        assertEquals(slotCharPrimitive, bt.getSlotCharPrimitive());
        assertEquals(slotShortPrimitive, bt.getSlotShortPrimitive());
        assertEquals(slotIntPrimitive, bt.getSlotIntPrimitive());
        assertEquals(slotFloatPrimitive, bt.getSlotFloatPrimitive(), 0);
        assertEquals(slotLongPrimitive, bt.getSlotLongPrimitive());
        assertEquals(slotDoublePrimitive, bt.getSlotDoublePrimitive(), 0);

        assertEquals(slotBoolean, bt.getSlotBoolean());
        assertEquals(slotByte, bt.getSlotByte());
        assertEquals(slotCharacter, bt.getSlotCharacter());
        assertEquals(slotShort, bt.getSlotShort());
        assertEquals(slotInteger, bt.getSlotInteger());
        assertEquals(slotFloat, bt.getSlotFloat());
        assertEquals(slotLong, bt.getSlotLong());
        assertEquals(slotDouble, bt.getSlotDouble());

        assertEquals(slotString, bt.getSlotString());

        assertArrayEquals(slotBytearray, bt.getSlotBytearray());

        assertEquals(slotDatetime, bt.getSlotDatetime());
        assertEquals(slotLocaldate, bt.getSlotLocaldate());
        assertEquals(slotLocaltime, bt.getSlotLocaltime());
        assertEquals(slotPartial, bt.getSlotPartial());
        assertEquals(slotSerializable, bt.getSlotSerializable());
    }

    @Test
    public void testValueTypes() throws InterruptedException {
        initValueTypes();
        confirmValueTypes();
    }

    @Atomic(mode = TxMode.WRITE)
    private void initValueTypes() {
        ValueTypes vt = new ValueTypes();

        vt.setABigDecimalAuto(aBigDecimalAuto);
        vt.setABigDecimalManual(aBigDecimalManual);
        vt.setACompositeValueType(aCompositeValueType);
        vt.setABigInteger(aBigInteger);
        vt.setAnotherCompositeValueType(anotherCompositeValueType);
        vt.setYetAnotherCompositeValueType(yetAnotherCompositeValueType);
        vt.setAParametricValueType(aParametricValueType);
        vt.setAnotherParametricValueType(anotherParametricValueType);

        FenixFramework.getDomainRoot().setValueTypes(vt);
    }

    @Atomic
    private void confirmValueTypes() {
        ValueTypes vt = FenixFramework.getDomainRoot().getValueTypes();

        assertEquals(aBigDecimalAuto, vt.getABigDecimalAuto());
        assertEquals(aBigDecimalManual, vt.getABigDecimalManual());
        assertEquals(aCompositeValueType, vt.getACompositeValueType());
        assertEquals(aBigInteger, vt.getABigInteger());
        assertEquals(anotherCompositeValueType, vt.getAnotherCompositeValueType());
        assertEquals(yetAnotherCompositeValueType, vt.getYetAnotherCompositeValueType());
        assertEquals(aParametricValueType, vt.getAParametricValueType());
        assertEquals(anotherParametricValueType, vt.getAnotherParametricValueType());

    }

    @Test
    public void testRelationToOne() {
        initRelationToOne();
        confirmRelationToOne();
    }

    @Atomic(mode = TxMode.WRITE)
    private void initRelationToOne() {
        counter = new Counter();
        counter.setValue(4);
        FenixFramework.getDomainRoot().setCounter(counter);
    }

    @Atomic
    private void confirmRelationToOne() {
        Counter c = FenixFramework.getDomainRoot().getCounter();

        assertEquals(counter, c);
        assertEquals(counter.getValue(), c.getValue());
    }

    @Test
    public void testRelationToMany() {
        initRelationToMany();
        confirmRelationToMany();
    }

    @Atomic(mode = TxMode.WRITE)
    private void initRelationToMany() {
        counters = new HashSet<Counter>();

        for (int i = 0; i < 10; i++) {
            Counter c = new Counter();
            c.setValue(i);
            FenixFramework.getDomainRoot().addMultipleCounter(c);
            counters.add(c);
        }
    }

    @Atomic
    private void confirmRelationToMany() {
        Set<Counter> actualCounters = FenixFramework.getDomainRoot().getMultipleCounterSet();

        assertEquals(counters, actualCounters);
    }

    public static void main(String[] args) {
        new DmlSlotsTest().testRelationToMany();
    }

}
