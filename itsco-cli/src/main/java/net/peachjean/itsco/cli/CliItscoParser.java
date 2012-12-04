package net.peachjean.itsco.cli;

import org.apache.commons.configuration.Configuration;

public interface CliItscoParser {
    <T> ParsedOptions parse(Class<T> itscoType, String[] args, Configuration baseConfiguration);

    <T> OptionsDescriptor buildDescriptor(Class<T> itscoType);
}
