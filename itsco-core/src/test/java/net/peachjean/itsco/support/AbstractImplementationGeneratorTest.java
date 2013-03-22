package net.peachjean.itsco.support;

import net.peachjean.itsco.support.example.CompoundItsco;
import net.peachjean.itsco.support.example.ExampleItsco;
import net.peachjean.itsco.support.example.PrimitiveItsco;
import net.peachjean.itsco.support.example.shared.DependentItsco;
import net.peachjean.itsco.support.example.shared.MasterItsco;
import net.peachjean.itsco.support.example.shared.SharedItsco;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
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
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);

        mockBacker.setContaining(anyObject(ExampleItsco.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(mockBacker.lookup("intValue", Integer.class, 55)).andReturn(42).anyTimes();
        EasyMock.expect(mockBacker.lookup("value1", String.class)).andReturn("myFirstValue").anyTimes();
        EasyMock.expect(mockBacker.lookup("value2", String.class, "secondValue")).andReturn("mySecondValue").anyTimes();

        EasyMock.replay(mockBacker);

        ImplementationGenerator underTest = createUUT();

        final Class<? extends ExampleItsco> implClass = underTest.implement(ExampleItsco.class);
        Constructor<? extends ExampleItsco> constructor = implClass.getConstructor(ItscoBacker.class);

        ExampleItsco impl = constructor.newInstance(mockBacker);

        assertEquals(42, impl.getIntValue().intValue());
        assertEquals("myFirstValue", impl.getValue1());
        assertEquals("mySecondValue", impl.getValue2());

        ExampleItsco other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());
    }

    @Test
    public void testPrimitives() throws Exception {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);

        mockBacker.setContaining(anyObject(PrimitiveItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockBacker.lookup("booleanValue", Boolean.class)).andReturn(false);
        expect(mockBacker.lookup("byteValue", Byte.class)).andReturn((byte) 0xFE);
        expect(mockBacker.lookup("charValue", Character.class)).andReturn('x');
        expect(mockBacker.lookup("shortValue", Short.class)).andReturn((short) 3);
        expect(mockBacker.lookup("intValue", Integer.class)).andReturn(12);
        expect(mockBacker.lookup("longValue", Long.class)).andReturn(49l);
        expect(mockBacker.lookup("floatValue", Float.class)).andReturn(55.555f);
        expect(mockBacker.lookup("doubleValue", Double.class)).andReturn(23.39389);

        expect(mockBacker.lookup("booleanValue2", Boolean.class, PrimitiveItsco.DEFAULT_BOOLEAN)).andReturn(false);
        expect(mockBacker.lookup("byteValue2", Byte.class, (byte) PrimitiveItsco.DEFAULT_BYTE)).andReturn((byte) 0xFE);
        expect(mockBacker.lookup("charValue2", Character.class, PrimitiveItsco.DEFAULT_CHAR)).andReturn('x');
        expect(mockBacker.lookup("shortValue2", Short.class, (short) PrimitiveItsco.DEFAULT_SHORT)).andReturn((short) 3);
        expect(mockBacker.lookup("intValue2", Integer.class, PrimitiveItsco.DEFAULT_INT)).andReturn(12);
        expect(mockBacker.lookup("longValue2", Long.class, PrimitiveItsco.DEFAULT_LONG)).andReturn(49l);
        expect(mockBacker.lookup("floatValue2", Float.class, PrimitiveItsco.DEFAULT_FLOAT)).andReturn(55.555f);
        expect(mockBacker.lookup("doubleValue2", Double.class, PrimitiveItsco.DEFAULT_DOUBLE)).andReturn(23.39389);

        EasyMock.replay(mockBacker);

        ImplementationGenerator underTest = createUUT();

        final Class<? extends PrimitiveItsco> implClass = underTest.implement(PrimitiveItsco.class);
        Constructor<? extends PrimitiveItsco> constructor = implClass.getConstructor(ItscoBacker.class);

        PrimitiveItsco impl = constructor.newInstance(mockBacker);

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

        PrimitiveItsco other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());
    }

    @Test
    public void compoundItscoExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        ExampleItsco mockSub = EasyMock.createMock(ExampleItsco.class);

        mockBacker.setContaining(anyObject(CompoundItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockSub.getIntValue()).andReturn(88).anyTimes();
        expect(mockSub.getValue2()).andReturn("secondValue").anyTimes();

        expect(mockBacker.lookup("subItsco", ExampleItsco.class)).andReturn(mockSub).anyTimes();
//        expect(mockBacker.lookup("subItsco.value1", String.class)).andReturn("I am zee value!").anyTimes();
//        expect(mockBacker.lookup("subItsco.value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
//        expect(mockBacker.lookup("subItsco.intValue", Integer.class, 88)).andReturn(88).anyTimes();
        expect(mockBacker.lookup("myString", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(mockBacker.lookup("myFloat", Float.class, 88 * 4.5f)).andReturn(88 * 4.5f).anyTimes();

        EasyMock.replay(mockBacker, mockSub);

        ImplementationGenerator underTest = this.createUUT();

        Class<? extends CompoundItsco> implClass = underTest.implement(CompoundItsco.class);
        Constructor<? extends CompoundItsco> constructor = implClass.getConstructor(ItscoBacker.class);

        CompoundItsco impl =  constructor.newInstance(mockBacker);

//        assertEquals("I am zee value!", exampleItsco.getValue1());
//        assertEquals("secondValue", exampleItsco.getValue2());
//        assertEquals(88, exampleItsco.getIntValue().intValue());
        assertSame(mockSub, impl.getSubItsco());
        assertEquals("secondValue", impl.getMyString());
        assertEquals(88 * 4.5f, impl.getMyFloat(), 0.0002);

//        config.setProperty("subItsco.intValue", "42");
//
//        assertEquals(42, exampleItsco.getIntValue().intValue());
//        assertEquals(42 * 4.5f, impl.getMyFloat(), 0.0002);

        CompoundItsco other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());

        EasyMock.verify(mockBacker, mockSub);
    }

    @Test
    public void sharedDependencyExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);
        SharedItsco mockShared = EasyMock.createMock(SharedItsco.class);

        mockBacker.setContaining(anyObject(DependentItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(mockBacker.lookup("path", String.class, "myNamespace/myFile")).andReturn("franklin!");
        expect(mockShared.getNamespace()).andReturn("myNamespace").anyTimes();
        expect(mockShared.getMaxSize()).andReturn(67).anyTimes();

        EasyMock.replay(mockBacker, mockShared);

        ImplementationGenerator underTest = this.createUUT();

        Class<? extends DependentItsco> implClass = underTest.implement(DependentItsco.class);
        Constructor<? extends DependentItsco> constructor = implClass.getConstructor(ItscoBacker.class, SharedItsco.class );

        DependentItsco impl = constructor.newInstance(mockBacker, mockShared);

        assertEquals("franklin!", impl.getPath());

        DependentItsco other = constructor.newInstance(mockBacker);
        assertEquals(impl, other);
        assertEquals("hashCodes", impl.hashCode(), other.hashCode());
        assertEquals("toString", impl.toString(), other.toString());

        EasyMock.verify(mockBacker, mockShared);
    }



    protected abstract ImplementationGenerator createUUT();
}
