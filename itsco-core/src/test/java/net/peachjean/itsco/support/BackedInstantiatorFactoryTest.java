package net.peachjean.itsco.support;

import net.peachjean.itsco.support.example.ExampleItsco;
import net.peachjean.itsco.support.example.ExampleItscoImpl;
import net.peachjean.itsco.support.example.PrimitiveItsco;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BackedInstantiatorFactoryTest {
    @Test
    public void testValues() {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        backer.setContaining(anyObject(ExampleItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88);
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue");
        expect(backer.lookup("value1", String.class)).andThrow(new IllegalStateException("No default value."));

        EasyMock.replay(backer);

        BackedInstantiatorFactory underTest = new BackedInstantiatorFactory();

        final BackedInstantiator<ExampleItsco> generator = underTest.lookupFunction(ExampleItsco.class);

        final ExampleItsco itsco1 = generator.instantiate(backer);

        assertNotNull(itsco1);
        assertEquals(88, itsco1.getIntValue().intValue());
        assertEquals("secondValue", itsco1.getValue2());

        try {
            itsco1.getValue1();
            fail("Should have thrown an exception.");
        } catch (IllegalStateException e) {
            // expected
        }

        EasyMock.verify(backer);
    }

    @Test
    public void testHashCodeAndEquals() {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);
        ItscoBacker otherBacker = EasyMock.createMock(ItscoBacker.class);

        backer.setContaining(anyObject(ExampleItsco.class));
        EasyMock.expectLastCall().anyTimes();
        otherBacker.setContaining(anyObject(ExampleItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88).anyTimes();
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(backer.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();

        expect(otherBacker.lookup("intvalue", Integer.class, 55)).andReturn(23).anyTimes();
        expect(otherBacker.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(otherBacker.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();


        EasyMock.replay(backer);

        BackedInstantiatorFactory underTest = new BackedInstantiatorFactory();

        final BackedInstantiator<ExampleItsco> generator = underTest.lookupFunction(ExampleItsco.class);

        final ExampleItsco itsco1 = generator.instantiate(backer);
        final ExampleItsco itsco2 = generator.instantiate(backer);

        assertNotNull(itsco1);
        assertNotNull(itsco2);

        assertEquals(itsco1.hashCode(), itsco2.hashCode());
        assertEquals(itsco1, itsco2);

        final ExampleItsco itsco3 = generator.instantiate(otherBacker);

        assertNotNull(itsco3);
        assertThat(itsco1, Matchers.not(Matchers.equalTo(itsco3)));
    }

    @Test
    public void testToString() {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        backer.setContaining(anyObject(ExampleItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88).anyTimes();
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(backer.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();

        EasyMock.replay(backer);

        BackedInstantiatorFactory underTest = new BackedInstantiatorFactory();

        final ExampleItsco generated = underTest.lookupFunction(ExampleItsco.class).instantiate(backer);
        final ExampleItsco hardCoded = new ExampleItscoImpl(backer);

        assertNotNull(generated);
        assertEquals(hardCoded.toString(), generated.toString());
    }

    @Test
    public void testPrimitive() {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        backer.setContaining(anyObject(PrimitiveItsco.class));
        EasyMock.expectLastCall().anyTimes();

        expect(backer.lookup("booleanValue", Boolean.class)).andReturn(false);
        expect(backer.lookup("byteValue", Byte.class)).andReturn((byte) 0xFE);
        expect(backer.lookup("charValue", Character.class)).andReturn('x');
        expect(backer.lookup("shortValue", Short.class)).andReturn((short) 3);
        expect(backer.lookup("intValue", Integer.class)).andReturn(12);
        expect(backer.lookup("longValue", Long.class)).andReturn(49l);
        expect(backer.lookup("floatValue", Float.class)).andReturn(55.555f);
        expect(backer.lookup("doubleValue", Double.class)).andReturn(23.39389);

        expect(backer.lookup("booleanValue2", Boolean.class, PrimitiveItsco.DEFAULT_BOOLEAN)).andReturn(false);
        expect(backer.lookup("byteValue2", Byte.class, (byte) PrimitiveItsco.DEFAULT_BYTE)).andReturn((byte) 0xFE);
        expect(backer.lookup("charValue2", Character.class, PrimitiveItsco.DEFAULT_CHAR)).andReturn('x');
        expect(backer.lookup("shortValue2", Short.class, (short) PrimitiveItsco.DEFAULT_SHORT)).andReturn((short) 3);
        expect(backer.lookup("intValue2", Integer.class, PrimitiveItsco.DEFAULT_INT)).andReturn(12);
        expect(backer.lookup("longValue2", Long.class, PrimitiveItsco.DEFAULT_LONG)).andReturn(49l);
        expect(backer.lookup("floatValue2", Float.class, PrimitiveItsco.DEFAULT_FLOAT)).andReturn(55.555f);
        expect(backer.lookup("doubleValue2", Double.class, PrimitiveItsco.DEFAULT_DOUBLE)).andReturn(23.39389);

        EasyMock.replay(backer);

        BackedInstantiatorFactory underTest = new BackedInstantiatorFactory();

        final PrimitiveItsco generated = underTest.lookupFunction(PrimitiveItsco.class).instantiate(backer);

        assertNotNull(generated);

        assertEquals(false, generated.getBooleanValue());
//        assertEquals((byte) 0xFE, generated.getByteValue());
//        assertEquals('x', generated.getCharValue());
//        assertEquals(3, generated.getShortValue());
//        assertEquals(12, generated.getIntValue());
//        assertEquals(49l, generated.getLongValue());
//        assertEquals(55.555f, generated.getFloatValue(), 0.00002);
//        assertEquals(23.39389, generated.getDoubleValue(), 0.000002);
//
//        assertEquals(false, generated.getBooleanValue2());
//        assertEquals((byte) 0xFE, generated.getByteValue2());
//        assertEquals('x', generated.getCharValue2());
//        assertEquals(3, generated.getShortValue2());
//        assertEquals(12, generated.getIntValue2());
//        assertEquals(49l, generated.getLongValue2());
//        assertEquals(55.555f, generated.getFloatValue2(), 0.00002);
//        assertEquals(23.39389, generated.getDoubleValue2(), 0.000002);
    }

    @Test
    public void testContextualInstantiation() {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        expect(backer.lookup("shared.namespace", String.class)).andReturn("myNamespace");
        expect(backer.lookup("shared.maxSize", Integer.class)).andReturn(67);

        EasyMock.replay(backer);

        BackedInstantiatorFactory underTest = new BackedInstantiatorFactory();

//        final DependentItsco generated = underTest.lookupFunction(DependentItsco.class, MasterItsco.class, "dependent").transform(backer);
//
//        assertEquals("myNamespace/myFile", generated.getPath());
    }


}
