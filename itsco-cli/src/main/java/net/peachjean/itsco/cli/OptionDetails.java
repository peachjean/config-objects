package net.peachjean.itsco.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.configuration.Configuration;

interface OptionDetails {

    Option getOption();

    String getValue(CommandLine commandLine, Configuration backingConfig);

    boolean hasValue(CommandLine commandLine, Configuration backingConfig);
}
