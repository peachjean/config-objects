package net.peachjean.confobj.support;

import net.peachjean.confobj.support.example.CompoundConfigObject;
import net.peachjean.confobj.support.example.ExampleConfigObject;
import net.peachjean.confobj.support.example.GenericCompound;
import net.peachjean.confobj.support.example.GenericConfigObject;
import net.peachjean.confobj.support.example.shared.MasterConfigObject;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

public class DefaultConfigObjectFactoryTest {

    @Test
    public void simpleExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleConfigObject exampleConfigObject = factory.create(config, ExampleConfigObject.class);

        assertEquals("I am zee value!", exampleConfigObject.getValue1());
        assertEquals("secondValue", exampleConfigObject.getValue2());
        assertEquals(88, exampleConfigObject.getIntValue().intValue());

        Instantiator<ExampleConfigObject> generator = factory.createGenerator(ExampleConfigObject.class);

        ExampleConfigObject exampleConfigObject2 = generator.instantiate(config);

        assertEquals("I am zee value!", exampleConfigObject.getValue1());
        assertEquals("secondValue", exampleConfigObject.getValue2());
        assertEquals(88, exampleConfigObject.getIntValue().intValue());
    }

    @Test
    public void simpleValidatorPOC() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleConfigObject exampleConfigObject = factory.create(config, ExampleConfigObject.class);

        assertEquals("I am zee value!", exampleConfigObject.getValue1());
        assertEquals("secondValue", exampleConfigObject.getValue2());
        assertEquals(88, exampleConfigObject.getIntValue().intValue());

        Validator validator = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
        final Set<ConstraintViolation<ExampleConfigObject>> violations = validator.validate(exampleConfigObject);
        assertTrue(violations.isEmpty());

        config.setProperty("intValue", "120");
        final Set<ConstraintViolation<ExampleConfigObject>> violations2 = validator.validate(exampleConfigObject);

        assertFalse(violations2.isEmpty());
    }

    @Test
    public void dynamicBackerExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("value1", "I am zee value!");
        config.setProperty("intValue", "88");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        ExampleConfigObject exampleConfigObject = factory.create(config, ExampleConfigObject.class);

        assertEquals("I am zee value!", exampleConfigObject.getValue1());
        assertEquals("secondValue", exampleConfigObject.getValue2());
        assertEquals(88, exampleConfigObject.getIntValue().intValue());

        config.setProperty("intValue", "42");

        assertEquals(42, exampleConfigObject.getIntValue().intValue());
    }

    @Test
    public void compoundConfigObjectExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("subConfigObject.value1", "I am zee value!");
        config.setProperty("subConfigObject.intValue", "88");


        ConfigObjectFactory factory = new DefaultConfigObjectFactory();

        CompoundConfigObject compoundConfigObject = factory.create(config, CompoundConfigObject.class);
        ExampleConfigObject exampleConfigObject = compoundConfigObject.getSubConfigObject();

        assertEquals("I am zee value!", exampleConfigObject.getValue1());
        assertEquals("secondValue", exampleConfigObject.getValue2());
        assertEquals(88, exampleConfigObject.getIntValue().intValue());
        assertEquals("secondValue", compoundConfigObject.getMyString());
        assertEquals(88 * 4.5f, compoundConfigObject.getMyFloat(), 0.0002);

        config.setProperty("subConfigObject.intValue", "42");

        assertEquals(42, exampleConfigObject.getIntValue().intValue());
        assertEquals(42 * 4.5f, compoundConfigObject.getMyFloat(), 0.0002);
    }

    @Test
    public void sharedDependencyExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("shared.namespace", "myNamespace");
        config.setProperty("shared.maxSize", "67");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        MasterConfigObject masterConfigObject = factory.create(config, MasterConfigObject.class);

        assertEquals("myNamespace", masterConfigObject.getShared().getNamespace());
        assertEquals(67, masterConfigObject.getShared().getMaxSize().intValue());
        assertEquals("myNamespace/myFile", masterConfigObject.getDependent().getPath());
    }

    @Test
    public void genericsListExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("string", "late late show");
        config.setProperty("allStrings", "first,second,third");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        GenericConfigObject obj = factory.create(config, GenericConfigObject.class);

        assertEquals("late late show", obj.getString());
        assertEquals(Arrays.asList("first", "second", "third"), obj.getAllStrings());
    }

    @Test
    public void genericsIndexedListExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("string", "late late show");
        config.setProperty("allStrings(0)", "first");
        config.setProperty("allStrings(1)", "second");
        config.setProperty("allStrings(2)", "third");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        GenericConfigObject obj = factory.create(config, GenericConfigObject.class);

        assertEquals("late late show", obj.getString());
        assertEquals(Arrays.asList("first", "second", "third"), obj.getAllStrings());
    }

    @Test
    public void genericInjectedCompoundsExample() {
        Configuration config = new BaseConfiguration();
        config.setProperty("roles", "first, second, third");
        config.setProperty("limits", "1, 2, 3, 4");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        GenericCompound obj = factory.create(config, GenericCompound.class);

        assertEquals("first,second,third", obj.getChild().getCombinedRoles());
        assertEquals(10, obj.getChild().getMaxLimit().intValue());
    }
}
