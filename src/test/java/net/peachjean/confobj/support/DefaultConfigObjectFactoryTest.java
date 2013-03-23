package net.peachjean.confobj.support;

import net.peachjean.confobj.support.example.CompoundItsco;
import net.peachjean.confobj.support.example.ExampleItsco;
import net.peachjean.confobj.support.example.shared.MasterItsco;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.*;

public class DefaultConfigObjectFactoryTest {

    @Test
    public void simpleExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleItsco exampleItsco = factory.create(config, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        Instantiator<ExampleItsco> generator = factory.createGenerator(ExampleItsco.class);

        ExampleItsco exampleItsco2 = generator.instantiate(config);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());
    }

    @Test
    public void simpleValidatorPOC() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleItsco exampleItsco = factory.create(config, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        Validator validator = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
        final Set<ConstraintViolation<ExampleItsco>> violations = validator.validate(exampleItsco);
        assertTrue(violations.isEmpty());

        config.setProperty("intValue", "120");
        final Set<ConstraintViolation<ExampleItsco>> violations2 = validator.validate(exampleItsco);

        assertFalse(violations2.isEmpty());
    }

    @Test
    public void dynamicBackerExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleItsco exampleItsco = factory.create(config, ExampleItsco.class);

        assertEquals("I am zee value!", exampleItsco.getValue1());
        assertEquals("secondValue", exampleItsco.getValue2());
        assertEquals(88, exampleItsco.getIntValue().intValue());

        config.setProperty("intValue", "42");

        assertEquals(42, exampleItsco.getIntValue().intValue());
    }

    @Test
    public void compoundItscoExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("subItsco.value1", "I am zee value!");
        config.setProperty("subItsco.intValue", "88");


        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

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

    @Test
    public void sharedDependencyExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("shared.namespace", "myNamespace");
        config.setProperty("shared.maxSize", "67");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        MasterItsco masterItsco = factory.create(config, MasterItsco.class);

        assertEquals("myNamespace", masterItsco.getShared().getNamespace());
        assertEquals(67, masterItsco.getShared().getMaxSize().intValue());
        assertEquals("myNamespace/myFile", masterItsco.getDependent().getPath());
    }
}
