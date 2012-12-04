package net.peachjean.itsco.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.configuration.Configuration;

class BooleanOptionDetails implements OptionDetails {
    private final String name;
    private final boolean required;
    private final boolean togglesFalse;
    private final OptDesc optDesc;

    public BooleanOptionDetails(final String name, final boolean required, final boolean togglesFalse, OptDesc optDesc) {
        this.name = name;
        this.required = required;
        this.togglesFalse = togglesFalse;
        this.optDesc = optDesc;
    }

    @Override
    public Option getOption() {
        final Option option = optDesc == null
                ? new Option(name, false, name)
                : new Option(optDesc.shortOpt(), name, false, optDesc.description());
        option.setRequired(required);
        return option;
    }

    @Override
    public String getValue(final CommandLine commandLine, final Configuration backingConfig) {
        if(commandLine.hasOption(name)) {
            if(togglesFalse) {
                return Boolean.toString(false);
            }
            else {
                return Boolean.toString(true);
            }
        }
        else {
            return backingConfig.getString(name);
        }
    }

    @Override
    public boolean hasValue(final CommandLine commandLine, final Configuration backingConfig) {
        return commandLine.hasOption(name) || backingConfig.containsKey(name);
    }
}
