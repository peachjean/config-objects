package net.peachjean.itsco.cli;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.omg.CORBA.StringHolder;

/**
 * A map from a property name to an "OptionDetails" object.  OptionDetails provides the ability to query for a
 * particular option from a CommandLine
 */
public class OptionsDescriptor {
    private final ImmutableMap<String, OptionDetails> detailsMap;
    private final ImmutableMap<String, OptionsDescriptor> subDescriptorMap;

    public OptionsDescriptor(final ImmutableMap<String, OptionDetails> detailsMap, final ImmutableMap<String, OptionsDescriptor> subDescriptorMap) {
        this.detailsMap = detailsMap;
        this.subDescriptorMap = subDescriptorMap;
    }

    ImmutableMap<String, OptionDetails> getDetailsMap() {
        return detailsMap;
    }

    ImmutableMap<String, OptionsDescriptor> getSubDescriptorMap() {
        return subDescriptorMap;
    }

    public Options buildOptions() {
        Options options = new Options();

        addOptions(options);

        return options;
    }

    private void addOptions(final Options options) {
        for(OptionDetails optionDetails: detailsMap.values())
        {
            options.addOption(optionDetails.getOption());
        }
        for(OptionsDescriptor descriptor: subDescriptorMap.values())
        {
            descriptor.addOptions(options);
        }
    }
}
