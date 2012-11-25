package net.peachjean.itsco.support;

import com.google.common.base.Function;
import net.peachjean.itsco.support.example.ExampleItsco;
import net.peachjean.itsco.support.example.ExampleItscoImpl;
import org.easymock.EasyMock;
import org.hamcrest.Matchers;
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
}
