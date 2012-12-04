package net.peachjean.itsco.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.configuration.Configuration;

class SimpleOptionDetails implements OptionDetails {
    private final String name;
    private final boolean required;
    private final OptDesc optDesc;

    public SimpleOptionDetails(final String name, final boolean required, final OptDesc optDesc) {
        this.name = name;
        this.required = required;
        this.optDesc = optDesc;
    }

    @Override
    public Option getOption() {

        final Option option = optDesc == null
                ? new Option(name, true, name)
                : new Option(optDesc.shortOpt(), name, true, optDesc.description());
        option.setRequired(required);
        return option;
    }

    @Override
    public String getValue(final CommandLine commandLine, final Configuration backingConfig) {
        if(commandLine.hasOption(name))
        {
            return commandLine.getOptionValue(name);
        }
        else
        {
            return backingConfig.getString(name);
        }
    }

    @Override
    public boolean hasValue(final CommandLine commandLine, final Configuration backingConfig) {
        return commandLine.hasOption(name) || backingConfig.containsKey(name);
    }
}
