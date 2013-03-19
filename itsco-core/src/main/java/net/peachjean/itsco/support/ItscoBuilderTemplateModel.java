package net.peachjean.itsco.support;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;

import java.util.List;

class ItscoBuilderTemplateModel {
    private final String builderName;
    private final String name;
    private final String defaultsName;
    private final String packageName;
    private final List<Prop> props;
    private final List<Prop> requiredProps;

    public ItscoBuilderTemplateModel(final String builderName, final String name, final String defaultsName, final String packageName, final List<Prop> props) {
        this.builderName = builderName;
        this.name = name;
        this.defaultsName = defaultsName;
        this.packageName = packageName;
        this.props = props;
        this.requiredProps = ListUtils.select(this.props, new Predicate<Prop>() {
            @Override
            public boolean evaluate(Prop input) {
                return input.isRequired();
            }
        });
    }

    public String getBuilderName() {
        return builderName;
    }

    public String getName() {
        return name;
    }

    public String getDefaultsName() {
        return defaultsName;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<Prop> getProps() {
        return props;
    }

    public List<Prop> getRequiredProps() {
        return requiredProps;
    }

    public static class Prop {
        private final String name;
        private final String type;
        private final String capName;
        private final boolean required;

        public Prop(final String name, final String type, final boolean required) {
            this.name = name;
            this.type = type;
            this.capName = name.substring(0, 1).toUpperCase() + name.substring(1);
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getCapName() {
            return capName;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
