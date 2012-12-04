package net.peachjean.itsco.cli;

import net.peachjean.itsco.cli.example.ExampleItsco;
import net.peachjean.itsco.support.ItscoFactory;
import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CliItscoFactoryTest {
    @Test
    public void testCreate() throws Exception {
        ItscoFactory<ParsedOptions> underTest = new CliItscoFactory();
        CliItscoParser parser = new GnuCliItscoParser();

        final ExampleItsco exampleItsco = underTest.create(parser.parse(ExampleItsco.class, new String[]{"-i", "88", "-v", "-q"}, new BaseConfiguration()), ExampleItsco.class);

        assertEquals(88, exampleItsco.getMyInt().intValue());
        assertEquals("stringness!", exampleItsco.getStringness());
        assertEquals(true, exampleItsco.getValid());
        assertFalse(exampleItsco.getVerbose());
    }
}
