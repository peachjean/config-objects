package net.peachjean.itsco.cli;

import net.peachjean.itsco.support.ItscoFactorySupport;

public class CliItscoFactory extends ItscoFactorySupport<ParsedOptions> {
    @Override
    public boolean contains(final ParsedOptions context, final String key) {
        return context.hasOption(key);
    }

    @Override
    public String contextLookup(final ParsedOptions context, final String key) {
        return context.getValue(key);
    }

    @Override
    public ParsedOptions subContextLookup(final ParsedOptions context, final String key) {
        return context.subValue(key);
    }
}
