package net.peachjean.confobj.support;

import net.peachjean.commons.test.junit.TmpDir;
import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListResolutionStrategyTest {

    @Rule
    public TmpDir tmpDir = new TmpDir();

    @Test
    public void test() {
        Configuration config = new BaseConfiguration();
        config.setProperty("myList(0)", "val1");
        config.setProperty("myList(1)", "val2");
        config.setProperty("myList(2)", "val3");

        ListResolutionStrategy lrs = new ListResolutionStrategy();
        lrs.setDeterminer(new FieldResolutionStrategy.Determiner() {
            @Override
            public boolean isStrategyAvailable(GenericType<?> type) {
                return type.getRawType().equals(String.class);
            }

            @Override
            public FieldResolutionStrategy determineStrategy(GenericType<?> type) {
                return new StringResolutionStrategy();
            }
        });

        FieldResolution<List<String>> resolution = lrs.resolve("myList", GenericType.<List<String>, List>forTypeWithParams(List.class, GenericType.forTypeWithParams(String.class)), config, null);
        List<String> myList = resolution.resolve();

        assertEquals(Arrays.asList("val1", "val2", "val3"), myList);
    }

    @Test
    public void testXml() throws IOException, ConfigurationException {
        String xml =
                "<config>" +
                "  <myList>val1</myList>" +
                "  <myList>val2</myList>" +
                "  <myList>val3</myList>" +
                "</config>";
        File xmlFile = new File(tmpDir.getDir(), "config.xml");
        FileUtils.write(xmlFile, xml);

        Configuration config = new XMLConfiguration(xmlFile);

        ListResolutionStrategy lrs = new ListResolutionStrategy();
        lrs.setDeterminer(new FieldResolutionStrategy.Determiner() {
            @Override
            public boolean isStrategyAvailable(GenericType<?> type) {
                return type.getRawType().equals(String.class);
            }

            @Override
            public FieldResolutionStrategy determineStrategy(GenericType<?> type) {
                return new StringResolutionStrategy();
            }
        });

        FieldResolution<List<String>> resolution = lrs.resolve("myList", GenericType.<List<String>, List>forTypeWithParams(List.class, GenericType.forTypeWithParams(String.class)), config, null);
        List<String> myList = resolution.resolve();

        assertEquals(Arrays.asList("val1", "val2", "val3"), myList);
    }
}
