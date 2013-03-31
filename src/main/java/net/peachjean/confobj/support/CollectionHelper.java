package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;

import java.util.ArrayList;
import java.util.List;

class CollectionHelper<T> {
    private final String name;
    private final GenericType<T> memberType;
    private HierarchicalConfiguration backingConfiguration;
    private final FieldResolutionStrategy frs;
    private final Object resolutionContext;
    private final List<FieldResolution<T>> resolutionBacking = new ArrayList<FieldResolution<T>>();

    CollectionHelper(String name, GenericType<T> memberType, final Configuration backingConfiguration, FieldResolutionStrategy frs, Object resolutionContext) {
        this.name = name;
        this.memberType = memberType;
        this.backingConfiguration = ConfigurationUtils.convertToHierarchical(backingConfiguration);
        if(!(backingConfiguration instanceof HierarchicalConfiguration) && backingConfiguration instanceof EventSource) {
            ((EventSource)backingConfiguration).addConfigurationListener(new ConfigurationListener() {
                @Override
                public void configurationChanged(ConfigurationEvent event) {
                    if(!event.isBeforeUpdate()) {
                        HierarchicalConfiguration hc = ConfigurationUtils.convertToHierarchical(backingConfiguration);
                        CollectionHelper.this.backingConfiguration = hc;
                    }
                }
            });
        };
        this.frs = frs;
        this.resolutionContext = resolutionContext;
    }

    T resolve(int index) {
        if(resolutionBacking.size() <= index || resolutionBacking.get(index) == null) {
            resolutionBacking.add(index, frs.resolve(name + "(" + index + ")", memberType, backingConfiguration, resolutionContext));
        }
        return resolutionBacking.get(index).resolve();
    }

    int size() {
        return backingConfiguration.getMaxIndex(name) + 1;
    }
}
