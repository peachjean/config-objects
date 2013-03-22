package net.peachjean.itsco.support;

import net.peachjean.itsco.support.example.ExampleItsco;
import net.peachjean.itsco.support.example.PrimitiveItsco;
import org.easymock.EasyMock;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractImplementationGeneratorTest {
    @Test
    public void testImplement() throws Exception {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);

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
    }

    @Test
    public void testPrimitives() throws Exception {
        ItscoBacker mockBacker = EasyMock.createMock(ItscoBacker.class);

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

    }

    protected abstract ImplementationGenerator createUUT();
}
