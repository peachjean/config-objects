package net.peachjean.confobj.support;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubsetConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of file content.
 *
 * @author jbunting
 *         3/31/13
 */
public class ConfigurationUtils {
    public static String determineFullPath(Configuration config, String name) {
        StringBuilder sb = new StringBuilder();
        addParentPath(config, sb);
        sb.append(name);
        return sb.toString();
    }

    private static void addParentPath(Configuration config, StringBuilder sb) {
        if(config instanceof SubsetConfiguration) {
            SubsetConfiguration subset = (SubsetConfiguration) config;
            addParentPath(subset.getParent(), sb);
            sb.append(subset.getPrefix()).append(".");
        }
    }
}
