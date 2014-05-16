package net.peachjean.confobj.support;

import net.peachjean.commons.test.junit.TmpDir;
import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.joda.time.Duration;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class JodaConvertResolutionStrategyTest {
    @Rule
    public TmpDir tmpDir = new TmpDir();

    @Test
    public void test() {
        Configuration config = new BaseConfiguration();
        config.setProperty("duration", "PT72.345S");

        JodaConvertResolutionStrategy jcs = new JodaConvertResolutionStrategy();

        assertTrue(jcs.supports(GenericType.forType(Duration.class)));
        assertFalse(jcs.supports(GenericType.forType(UnsupportedClass.class)));

        FieldResolution<Duration> durationResolution = jcs.resolve("duration", GenericType.forType(Duration.class), config, null);
        Duration duration = durationResolution.resolve();

        assertEquals(Duration.parse("PT72.345S"), duration);
    }

    private static class UnsupportedClass
    {
        public UnsupportedClass(int number, float decimal)
        {

        }
    }

}
