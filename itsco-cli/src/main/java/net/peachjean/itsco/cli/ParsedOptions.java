package net.peachjean.itsco.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.configuration.Configuration;

public class ParsedOptions {

    private final CommandLine commandLine;
    private final OptionsDescriptor optionsDescriptor;
    private final Configuration backingConfig;

    ParsedOptions(final CommandLine commandLine, final OptionsDescriptor optionsDescriptor, final Configuration backingConfig) {
        this.commandLine = commandLine;
        this.optionsDescriptor = optionsDescriptor;
        this.backingConfig = backingConfig;
    }

    boolean hasOption(String name)
    {
        return optionsDescriptor.getDetailsMap().containsKey(name) && optionsDescriptor.getDetailsMap().get(name).hasValue(commandLine, backingConfig);
    }

    String getValue(String name)
    {
        return optionsDescriptor.getDetailsMap().get(name).getValue(commandLine, backingConfig);
    }

    ParsedOptions subValue(String name)
    {
        return new ParsedOptions(commandLine, optionsDescriptor.getSubDescriptorMap().get(name), backingConfig.subset(name));
    }
}
