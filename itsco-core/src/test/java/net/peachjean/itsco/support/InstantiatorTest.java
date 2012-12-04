package net.peachjean.itsco.support;

import com.google.common.base.Function;
import net.peachjean.itsco.support.example.ExampleItsco;
import net.peachjean.itsco.support.example.ExampleItscoImpl;
import net.peachjean.itsco.support.example.PrimitiveItsco;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class InstantiatorTest {
    @Test
    public void testValues()
    {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88);
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue");
        expect(backer.lookup("value1", String.class)).andThrow(new IllegalStateException("No default value."));

        EasyMock.replay(backer);

        Instantiator underTest = new Instantiator();

        final Function<ItscoBacker,ExampleItsco> generator = underTest.lookupFunction(ExampleItsco.class);

        final ExampleItsco itsco1 = generator.apply(backer);

        assertNotNull(itsco1);
        assertEquals(88, itsco1.getIntValue().intValue());
        assertEquals("secondValue", itsco1.getValue2());

        try
        {
            itsco1.getValue1();
            fail("Should have thrown an exception.");
        }
        catch(IllegalStateException e)
        {
            // expected
        }

        EasyMock.verify(backer);
    }

    @Test
    public void testHashCodeAndEquals()
    {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);
        ItscoBacker otherBacker = EasyMock.createMock(ItscoBacker.class);

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88).anyTimes();
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(backer.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();

        expect(otherBacker.lookup("intvalue", Integer.class, 55)).andReturn(23).anyTimes();
        expect(otherBacker.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(otherBacker.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();


        EasyMock.replay(backer);

        Instantiator underTest = new Instantiator();

        final Function<ItscoBacker,ExampleItsco> generator = underTest.lookupFunction(ExampleItsco.class);

        final ExampleItsco itsco1 = generator.apply(backer);
        final ExampleItsco itsco2 = generator.apply(backer);

        assertNotNull(itsco1);
        assertNotNull(itsco2);

        assertEquals(itsco1.hashCode(), itsco2.hashCode());
        assertEquals(itsco1,  itsco2);

        final ExampleItsco itsco3 = generator.apply(otherBacker);

        assertNotNull(itsco3);
        assertThat(itsco1, Matchers.not(Matchers.equalTo(itsco3     )));
    }

    @Test
    public void testToString()
    {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        expect(backer.lookup("intValue", Integer.class, 55)).andReturn(88).anyTimes();
        expect(backer.lookup("value2", String.class, "secondValue")).andReturn("secondValue").anyTimes();
        expect(backer.lookup("value1", String.class)).andReturn("woohoo!").anyTimes();

        EasyMock.replay(backer);

        Instantiator underTest = new Instantiator();

        final ExampleItsco generated = underTest.lookupFunction(ExampleItsco.class).apply(backer);
        final ExampleItsco hardCoded = new ExampleItscoImpl(backer);

        assertNotNull(generated);
        assertEquals(hardCoded.toString(), generated.toString());
    }

    @Test
    public void testPrimitive()
    {
        ItscoBacker backer = EasyMock.createMock(ItscoBacker.class);

        expect(backer.lookup("booleanValue", Boolean.class)).andReturn(false);
        expect(backer.lookup("byteValue", Byte.class)).andReturn((byte)0xFE);
        expect(backer.lookup("charValue", Character.class)).andReturn('x');
        expect(backer.lookup("shortValue", Short.class)).andReturn((short)3);
        expect(backer.lookup("intValue", Integer.class)).andReturn(12);
        expect(backer.lookup("longValue", Long.class)).andReturn(49l);
        expect(backer.lookup("floatValue", Float.class)).andReturn(55.555f);
        expect(backer.lookup("doubleValue", Double.class)).andReturn(23.39389);

        EasyMock.replay(backer);

        Instantiator underTest = new Instantiator();

        final PrimitiveItsco generated = underTest.lookupFunction(PrimitiveItsco.class).apply(backer);

        assertEquals(false, generated.getBooleanValue());
        assertEquals((byte)0xFE, generated.getByteValue());
        assertEquals('x', generated.getCharValue());
        assertEquals(3, generated.getShortValue());
        assertEquals(12, generated.getIntValue());
        assertEquals(49l, generated.getLongValue());
        assertEquals(55.555f, generated.getFloatValue(), 0.00002);
        assertEquals(23.39389, generated.getDoubleValue(), 0.000002);
    }
}
