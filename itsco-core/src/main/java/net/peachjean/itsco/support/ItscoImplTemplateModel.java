package net.peachjean.itsco.support;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;

import java.util.List;

class ItscoImplTemplateModel {
    private final String implName;
    private final String visibility;
    private final String name;
    private final String defaultsName;
    private final String packageName;
    private final List<Prop> props;
    private final List<Prop> requiredProps;

    public ItscoImplTemplateModel(final String implName, String visibility, final String name, final String defaultsName, final String packageName, final List<Prop> props) {
        this.implName = implName;
        this.visibility = visibility;
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

    public String getImplName() {
        return implName;
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

    String getVisibility() {
        return visibility;
    }

    public static class Prop {
        private final String name;
        private final String type;
        private final String capName;
        private final boolean required;
        private final boolean primitive;

        public Prop(final String name, final String type, final boolean required, boolean primitive) {
            this.name = name;
            this.type = type;
            this.primitive = primitive;
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

        public boolean isPrimitive() {
            return primitive;
        }
    }
}
