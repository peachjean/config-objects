package net.peachjean.confobj.support;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigurationUtilsTest {
    @Test
    public void testDetermineFullPath() throws Exception {
        Configuration config = new BaseConfiguration();
        String key = "net.peachjean.confobj.support.thing.myConfig";
        config.setProperty(key, "Something");

        Configuration lowest = config.subset("net").subset("peachjean").subset("confobj").subset("support").subset("thing");

        String fullName = ConfigurationUtils.determineFullPath(lowest, "myConfig");

        assertEquals(key, fullName);
    }
}
