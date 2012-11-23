package net.peachjean.itsco.support;

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
    }
}
