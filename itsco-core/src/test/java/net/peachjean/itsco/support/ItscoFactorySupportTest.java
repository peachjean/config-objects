package net.peachjean.itsco.support;

import com.google.common.base.Function;
import net.peachjean.itsco.support.example.ExampleItsco;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ItscoFactorySupportTest {

    @Test
    public void simpleExample()
    {
        Properties props = new Properties();
        props.setProperty("value1", "I am zee value!");
        props.setProperty("intValue", "88");

        PropertiesItscoFactory factory = new PropertiesItscoFactory();

        ExampleItsco exampleItsco = factory.create(props, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        Function<Properties, ExampleItsco> generator = factory.createGenerator(ExampleItsco.class);

        ExampleItsco exampleItsco2 = generator.apply(props);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());
    }

    @Test
    public void dynamicBackerExample()
    {
        Properties props = new Properties();
        props.setProperty("value1", "I am zee value!");
        props.setProperty("intValue", "88");

        PropertiesItscoFactory factory = new PropertiesItscoFactory();

        ExampleItsco exampleItsco = factory.create(props, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        props.setProperty("intValue", "42");

        assertEquals(42, exampleItsco.getIntValue().intValue());
    }
}
