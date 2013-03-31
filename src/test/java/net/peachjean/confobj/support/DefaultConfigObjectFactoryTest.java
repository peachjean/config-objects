package net.peachjean.confobj.support;

import net.peachjean.confobj.support.example.*;
import net.peachjean.confobj.support.example.shared.MasterConfigObject;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

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
        config.addProperty("stringSet", "alpha");
        config.addProperty("stringSet", "bravo");
        config.addProperty("stringSet", "charlie");
        config.addProperty("numberMap.everything", "42");
        config.addProperty("numberMap.timesTwo", "84");
        config.addProperty("numberMap.reallyThatsPi", "3");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        GenericConfigObject obj = factory.create(config, GenericConfigObject.class);

        assertEquals("late late show", obj.getString());
        assertEquals(Arrays.asList("first", "second", "third"), obj.getAllStrings());
        assertEquals(new HashSet<String>(Arrays.asList("alpha", "bravo", "charlie")), obj.getStringSet());
        Map<String, Integer> expectedMap = new HashMap<String, Integer>();
        expectedMap.put("everything", 42);
        expectedMap.put("timesTwo", 84);
        expectedMap.put("reallyThatsPi", 3);
        assertEquals(expectedMap, obj.getNumberMap());

        config.clearProperty("allStrings(2)");
        config.setProperty("stringSet", "omega");
        config.clearProperty("numberMap.reallyThatsPi");
        assertEquals(Arrays.asList("first", "second"), obj.getAllStrings());
        assertEquals(Collections.singleton("omega"), obj.getStringSet());
        Map<String, Integer> changedExpectedMap = new HashMap<String, Integer>();
        changedExpectedMap.put("everything", 42);
        changedExpectedMap.put("timesTwo", 84);
        assertEquals(changedExpectedMap, obj.getNumberMap());
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

    @Test
    public void pluginExample() {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty("count", "5");
        configuration.setProperty("late", "true");
        configuration.setProperty("early", "false");
        configuration.setProperty("maxInstances", "10");
        configuration.setProperty("minInstances", "2");

        ConfigObjectFactory factory = new DefaultConfigObjectFactory();
        PluginParentCO obj = factory.createNamedImpl(configuration, PluginParentCO.class, "one");

        assertTrue("is implementation type", obj instanceof PluginOneCO);

        PluginOneCO one = (PluginOneCO) obj;
        assertEquals(5, one.getCount().intValue());
        assertEquals(10, one.getMaxInstances().intValue());
        assertEquals(true, one.isLate());

        PluginParentCO obj2 = factory.createNamedImpl(configuration, PluginParentCO.class, "two");

        assertTrue("is implementation type", obj2 instanceof PluginTwoCO);

        PluginTwoCO two = (PluginTwoCO) obj2;
        assertEquals(5, two.getCount().intValue());
        assertEquals(2, two.getMinInstances().intValue());
        assertEquals(false, two.isEarly());
    }
}
