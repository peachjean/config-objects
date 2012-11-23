package net.peachjean.itsco.support;

import com.google.common.base.Function;
import net.peachjean.itsco.support.example.CompoundItsco;
import net.peachjean.itsco.support.example.ExampleItsco;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ItscoFactorySupportTest {

    @Test
    public void simpleExample()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigurationItscoFactory factory = new ConfigurationItscoFactory();

        ExampleItsco exampleItsco = factory.create(config, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        Function<Configuration, ExampleItsco> generator = factory.createGenerator(ExampleItsco.class);

        ExampleItsco exampleItsco2 = generator.apply(config);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());
    }

    @Test
    public void dynamicBackerExample()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigurationItscoFactory factory = new ConfigurationItscoFactory();

        ExampleItsco exampleItsco = factory.create(config, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        config.setProperty("intValue", "42");

        assertEquals(42, exampleItsco.getIntValue().intValue());
    }

    @Test
    public void compoundItscoExample()
    {
        Configuration config = new BaseConfiguration();
        config.setProperty("subItsco.value1", "I am zee value!");
        config.setProperty("subItsco.intValue", "88");


        ConfigurationItscoFactory factory = new ConfigurationItscoFactory();

        CompoundItsco compoundItsco = factory.create(config, CompoundItsco.class);
        ExampleItsco exampleItsco = compoundItsco.getSubItsco();

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());
        assertEquals("secondValue", compoundItsco.getMyString());
        assertEquals(88 * 4.5f, compoundItsco.getMyFloat(), 0.0002);

        config.setProperty("subItsco.intValue", "42");

        assertEquals(42, exampleItsco.getIntValue().intValue());
        assertEquals(42 * 4.5f, compoundItsco.getMyFloat(), 0.0002);
    }
}
