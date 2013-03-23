package net.peachjean.confobj.support;

import net.peachjean.confobj.support.example.CompoundConfigObject;
import net.peachjean.confobj.support.example.ExampleConfigObject;
import net.peachjean.confobj.support.example.PrimitiveConfigObject;
import net.peachjean.confobj.support.example.shared.DependentConfigObject;
import net.peachjean.confobj.support.example.shared.SharedConfigObject;
import org.easymock.EasyMock;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

        EasyMock.expect(mockBacker.lookup("intValue", Integer.class, 55)).andReturn(42).anyTimes();
        EasyMock.expect(mockBacker.lookup("value1", String.class)).andReturn("myFirstValue").anyTimes();
        EasyMock.expect(mockBacker.lookup("value2", String.class, "secondValue")).andReturn("mySecondValue").anyTimes();

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

        expect(mockBacker.lookup("booleanValue", Boolean.class)).andReturn(false).anyTimes();
        expect(mockBacker.lookup("byteValue", Byte.class)).andReturn((byte) 0xFE).anyTimes();
        expect(mockBacker.lookup("charValue", Character.class)).andReturn('x').anyTimes();
        expect(mockBacker.lookup("shortValue", Short.class)).andReturn((short) 3).anyTimes();
        expect(mockBacker.lookup("intValue", Integer.class)).andReturn(12).anyTimes();
        expect(mockBacker.lookup("longValue", Long.class)).andReturn(49l).anyTimes();
        expect(mockBacker.lookup("floatValue", Float.class)).andReturn(55.555f).anyTimes();
        expect(mockBacker.lookup("doubleValue", Double.class)).andReturn(23.39389).anyTimes();

        expect(mockBacker.lookup("booleanValue2", Boolean.class, PrimitiveConfigObject.DEFAULT_BOOLEAN)).andReturn(false).anyTimes();
        expect(mockBacker.lookup("byteValue2", Byte.class, (byte) PrimitiveConfigObject.DEFAULT_BYTE)).andReturn((byte) 0xFE).anyTimes();
        expect(mockBacker.lookup("charValue2", Character.class, PrimitiveConfigObject.DEFAULT_CHAR)).andReturn('x').anyTimes();
        expect(mockBacker.lookup("shortValue2", Short.class, (short) PrimitiveConfigObject.DEFAULT_SHORT)).andReturn((short) 3).anyTimes();
        expect(mockBacker.lookup("intValue2", Integer.class, PrimitiveConfigObject.DEFAULT_INT)).andReturn(12).anyTimes();
        expect(mockBacker.lookup("longValue2", Long.class, PrimitiveConfigObject.DEFAULT_LONG)).andReturn(49l).anyTimes();
        expect(mockBacker.lookup("floatValue2", Float.class, PrimitiveConfigObject.DEFAULT_FLOAT)).andReturn(55.555f).anyTimes();
        expect(mockBacker.lookup("doubleValue2", Double.class, PrimitiveConfigObject.DEFAULT_DOUBLE)).andReturn(23.39389).anyTimes();

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

        expect(mockBacker.lookup("subConfigObject", ExampleConfigObject.class)).andReturn(mockSub).anyTimes();
//        expect(mockBacker.lookup("subConfigObject.value1", String.class)).andReturn("I am zee value!").anyTimes();
//        expect(mockBacker.lookup("subConfigObject.value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
//        expect(mockBacker.lookup("subConfigObject.intValue", Integer.class, 88)).andReturn(88).anyTimes();
        expect(mockBacker.lookup("myString", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(mockBacker.lookup("myFloat", Float.class, 88 * 4.5f)).andReturn(88 * 4.5f).anyTimes();

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

        expect(mockBacker.lookup("path", String.class, "myNamespace/myFile")).andReturn("franklin!").anyTimes();
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



    protected abstract ImplementationGenerator createUUT();
}
