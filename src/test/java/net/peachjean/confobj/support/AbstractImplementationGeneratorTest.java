package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import net.peachjean.confobj.support.example.CompoundConfigObject;
import net.peachjean.confobj.support.example.ExampleConfigObject;
import net.peachjean.confobj.support.example.GenericConfigObject;
import net.peachjean.confobj.support.example.PrimitiveConfigObject;
import net.peachjean.confobj.support.example.shared.DependentConfigObject;
import net.peachjean.confobj.support.example.shared.SharedConfigObject;
import org.easymock.EasyMock;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public abstract class AbstractImplementationGeneratorTest {
    @Test
    public void testImplement() throws Exception {
        ConfigObjectBacker mockBacker = EasyMock.createMock(ConfigObjectBacker.class);

        mockBacker.setContaining(anyObject(ExampleConfigObject.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(mockBacker.lookup("intValue", GenericType.forType(Integer.class), 55)).andReturn(42).anyTimes();
        EasyMock.expect(mockBacker.lookup("value1", GenericType.forType(String.class))).andReturn("myFirstValue").anyTimes();
        EasyMock.expect(mockBacker.lookup("value2", GenericType.forType(String.class), "secondValue")).andReturn("mySecondValue").anyTimes();

        EasyMock.replay(mockBacker);

        ImplementationGenerator underTest = createUUT();

        final Class<? extends ExampleConfigObject> implClass = underTest.implement(ExampleConfigObject.class);
        Constructor<? extends ExampleConfigObject> constructor = implClass.getConstructor(ConfigObjectBacker.class);

        ExampleConfigObject impl = constructor.newInstance(mockBacker);

        assertEquals(42, impl.getIntValue().intValue());
        assertEquals("myFirstValue", impl.getValue1());
        assertEquals("mySecondValue", impl.getValue2());

        ExampleConfigObject other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());
    }

    @Test
    public void testPrimitives() throws Exception {
        ConfigObjectBacker mockBacker = EasyMock.createMock(ConfigObjectBacker.class);

        mockBacker.setContaining(anyObject(PrimitiveConfigObject.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockBacker.lookup("booleanValue", GenericType.forType(Boolean.class))).andReturn(false).anyTimes();
        expect(mockBacker.lookup("byteValue", GenericType.forType(Byte.class))).andReturn((byte) 0xFE).anyTimes();
        expect(mockBacker.lookup("charValue", GenericType.forType(Character.class))).andReturn('x').anyTimes();
        expect(mockBacker.lookup("shortValue", GenericType.forType(Short.class))).andReturn((short) 3).anyTimes();
        expect(mockBacker.lookup("intValue", GenericType.forType(Integer.class))).andReturn(12).anyTimes();
        expect(mockBacker.lookup("longValue", GenericType.forType(Long.class))).andReturn(49l).anyTimes();
        expect(mockBacker.lookup("floatValue", GenericType.forType(Float.class))).andReturn(55.555f).anyTimes();
        expect(mockBacker.lookup("doubleValue", GenericType.forType(Double.class))).andReturn(23.39389).anyTimes();

        expect(mockBacker.lookup("booleanValue2", GenericType.forType(Boolean.class), PrimitiveConfigObject.DEFAULT_BOOLEAN)).andReturn(false).anyTimes();
        expect(mockBacker.lookup("byteValue2", GenericType.forType(Byte.class), (byte) PrimitiveConfigObject.DEFAULT_BYTE)).andReturn((byte) 0xFE).anyTimes();
        expect(mockBacker.lookup("charValue2", GenericType.forType(Character.class), PrimitiveConfigObject.DEFAULT_CHAR)).andReturn('x').anyTimes();
        expect(mockBacker.lookup("shortValue2", GenericType.forType(Short.class), (short) PrimitiveConfigObject.DEFAULT_SHORT)).andReturn((short) 3).anyTimes();
        expect(mockBacker.lookup("intValue2", GenericType.forType(Integer.class), PrimitiveConfigObject.DEFAULT_INT)).andReturn(12).anyTimes();
        expect(mockBacker.lookup("longValue2", GenericType.forType(Long.class), PrimitiveConfigObject.DEFAULT_LONG)).andReturn(49l).anyTimes();
        expect(mockBacker.lookup("floatValue2", GenericType.forType(Float.class), PrimitiveConfigObject.DEFAULT_FLOAT)).andReturn(55.555f).anyTimes();
        expect(mockBacker.lookup("doubleValue2", GenericType.forType(Double.class), PrimitiveConfigObject.DEFAULT_DOUBLE)).andReturn(23.39389).anyTimes();

        EasyMock.replay(mockBacker);

        ImplementationGenerator underTest = createUUT();

        final Class<? extends PrimitiveConfigObject> implClass = underTest.implement(PrimitiveConfigObject.class);
        Constructor<? extends PrimitiveConfigObject> constructor = implClass.getConstructor(ConfigObjectBacker.class);

        PrimitiveConfigObject impl = constructor.newInstance(mockBacker);

        assertNotNull(impl);

        assertEquals(false, impl.getBooleanValue());
        assertEquals((byte) 0xFE, impl.getByteValue());
        assertEquals('x', impl.getCharValue());
        assertEquals(3, impl.getShortValue());
        assertEquals(12, impl.getIntValue());
        assertEquals(49l, impl.getLongValue());
        assertEquals(55.555f, impl.getFloatValue(), 0.00002);
        assertEquals(23.39389, impl.getDoubleValue(), 0.000002);

        assertEquals(false, impl.getBooleanValue2());
        assertEquals((byte) 0xFE, impl.getByteValue2());
        assertEquals('x', impl.getCharValue2());
        assertEquals(3, impl.getShortValue2());
        assertEquals(12, impl.getIntValue2());
        assertEquals(49l, impl.getLongValue2());
        assertEquals(55.555f, impl.getFloatValue2(), 0.00002);
        assertEquals(23.39389, impl.getDoubleValue2(), 0.000002);
//
        PrimitiveConfigObject other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());
    }

    @Test
    public void compoundConfigObjectExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ConfigObjectBacker mockBacker = EasyMock.createMock(ConfigObjectBacker.class);
        ExampleConfigObject mockSub = EasyMock.createMock(ExampleConfigObject.class);

        mockBacker.setContaining(anyObject(CompoundConfigObject.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockSub.getIntValue()).andReturn(88).anyTimes();
        expect(mockSub.getValue2()).andReturn("secondValue").anyTimes();

        expect(mockBacker.lookup("subConfigObject", GenericType.forType(ExampleConfigObject.class))).andReturn(mockSub).anyTimes();
//        expect(mockBacker.lookup("subConfigObject.value1", GenericType.forType(String.class))).andReturn("I am zee value!").anyTimes();
//        expect(mockBacker.lookup("subConfigObject.value2", GenericType.forType(String.class), "secondValue")).andReturn("secondValue").anyTimes();
//        expect(mockBacker.lookup("subConfigObject.intValue", GenericType.forType(Integer.class), 88)).andReturn(88).anyTimes();
        expect(mockBacker.lookup("myString", GenericType.forType(String.class), "secondValue")).andReturn("secondValue").anyTimes();
        expect(mockBacker.lookup("myFloat", GenericType.forType(Float.class), 88 * 4.5f)).andReturn(88 * 4.5f).anyTimes();

        EasyMock.replay(mockBacker, mockSub);

        ImplementationGenerator underTest = this.createUUT();

        Class<? extends CompoundConfigObject> implClass = underTest.implement(CompoundConfigObject.class);
        Constructor<? extends CompoundConfigObject> constructor = implClass.getConstructor(ConfigObjectBacker.class);

        CompoundConfigObject impl =  constructor.newInstance(mockBacker);

//        assertEquals("I am zee value!", exampleConfigObject.getValue1());
//        assertEquals("secondValue", exampleConfigObject.getValue2());
//        assertEquals(88, exampleConfigObject.getIntValue().intValue());
        assertSame(mockSub, impl.getSubConfigObject());
        assertEquals("secondValue", impl.getMyString());
        assertEquals(88 * 4.5f, impl.getMyFloat(), 0.0002);

//        config.setProperty("subConfigObject.intValue", "42");
//
//        assertEquals(42, exampleConfigObject.getIntValue().intValue());
//        assertEquals(42 * 4.5f, impl.getMyFloat(), 0.0002);

        CompoundConfigObject other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());

        EasyMock.verify(mockBacker, mockSub);
    }

    @Test
    public void sharedDependencyExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ConfigObjectBacker mockBacker = EasyMock.createMock(ConfigObjectBacker.class);
        SharedConfigObject mockShared = EasyMock.createMock(SharedConfigObject.class);

        mockBacker.setContaining(anyObject(DependentConfigObject.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockBacker.lookup("path", GenericType.forType(String.class), "myNamespace/myFile")).andReturn("franklin!").anyTimes();
        expect(mockShared.getNamespace()).andReturn("myNamespace").anyTimes();
        expect(mockShared.getMaxSize()).andReturn(67).anyTimes();

        EasyMock.replay(mockBacker, mockShared);

        ImplementationGenerator underTest = this.createUUT();

        Class<? extends DependentConfigObject> implClass = underTest.implement(DependentConfigObject.class);
        Constructor<? extends DependentConfigObject> constructor = implClass.getConstructor(ConfigObjectBacker.class, SharedConfigObject.class );

        DependentConfigObject impl = constructor.newInstance(mockBacker, mockShared);

        assertEquals("franklin!", impl.getPath());

        DependentConfigObject other = constructor.newInstance(mockBacker, mockShared);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());

        EasyMock.verify(mockBacker, mockShared);
    }

    @Test
    public void genericExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ConfigObjectBacker mockBacker = EasyMock.createMock(ConfigObjectBacker.class);

        mockBacker.setContaining(anyObject(DependentConfigObject.class));
        EasyMock.expectLastCall().anyTimes();
        expect(mockBacker.lookup("string", GenericType.forType(String.class))).andReturn("late late show").anyTimes();
        expect(mockBacker.lookup("allStrings", GenericType.forTypeWithParams(List.class, GenericType.forTypeWithParams(String.class)))).andReturn(Arrays.asList("first", "second", "third")).anyTimes();
        expect(mockBacker.lookup("numberMap", GenericType.forTypeWithParams(Map.class, GenericType.forTypeWithParams(String.class), GenericType.forTypeWithParams(Integer.class)))).andReturn(Collections.emptyMap()).anyTimes();
        expect(mockBacker.lookup("stringSet", GenericType.forTypeWithParams(Set.class, GenericType.forTypeWithParams(String.class)))).andReturn(Collections.emptySet()).anyTimes();

        EasyMock.replay(mockBacker);

        ImplementationGenerator underTest = this.createUUT();

        Class<? extends GenericConfigObject> implClass = underTest.implement(GenericConfigObject.class);
        Constructor<? extends GenericConfigObject> constructor = implClass.getConstructor(ConfigObjectBacker.class);

        GenericConfigObject impl = constructor.newInstance(mockBacker);

        assertEquals("late late show", impl.getString());
        assertEquals(Arrays.asList("first", "second", "third"), impl.getAllStrings());

        GenericConfigObject other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());

        EasyMock.verify(mockBacker);
    }



    protected abstract ImplementationGenerator createUUT();
}
