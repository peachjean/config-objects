package net.peachjean.itsco.cli;

import org.apache.commons.cli.Options;

import java.util.Map;

/**
 * A map from a property name to an "OptionDetails" object.  OptionDetails provides the ability to query for a
 * particular option from a CommandLine
 */
public class OptionsDescriptor {
    private final Map<String, OptionDetails> detailsMap;
    private final Map<String, OptionsDescriptor> subDescriptorMap;

    public OptionsDescriptor(final Map<String, OptionDetails> detailsMap, final Map<String, OptionsDescriptor> subDescriptorMap) {
        this.detailsMap = detailsMap;
        this.subDescriptorMap = subDescriptorMap;
    }

    Map<String, OptionDetails> getDetailsMap() {
        return detailsMap;
    }

    Map<String, OptionsDescriptor> getSubDescriptorMap() {
        return subDescriptorMap;
    }

    public Options buildOptions() {
        Options options = new Options();

        addOptions(options);

        return options;
    }

    private void addOptions(final Options options) {
        for (OptionDetails optionDetails : detailsMap.values()) {
            options.addOption(optionDetails.getOption());
        }
        for (OptionsDescriptor descriptor : subDescriptorMap.values()) {
            descriptor.addOptions(options);
        }
    }
}
