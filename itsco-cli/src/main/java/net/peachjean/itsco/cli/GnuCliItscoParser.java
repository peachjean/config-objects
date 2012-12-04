package net.peachjean.itsco.cli;

import com.google.common.collect.ImmutableMap;
import net.peachjean.itsco.introspection.ItscoIntrospector;
import net.peachjean.itsco.introspection.ItscoVisitor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;

public class GnuCliItscoParser implements CliItscoParser {

    private final Parser parser = new GnuParser();

    @Override
    public <T> ParsedOptions parse(final Class<T> itscoType, final String[] args, final Configuration backingConfig) {
        OptionsDescriptor optionsDescriptor = buildDescriptor(itscoType);

        try {
            final CommandLine commandLine = parser.parse(optionsDescriptor.buildOptions(), args);
            return new ParsedOptions(commandLine,  optionsDescriptor,  backingConfig);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> OptionsDescriptor buildDescriptor(final Class<T> itscoType) {
        return ItscoIntrospector.visitMembers(itscoType, new DescriptorBuilder(), (ItscoVisitor<T,DescriptorBuilder>) VISITOR).build();
    }

    private static class DescriptorBuilder {

        private final ImmutableMap.Builder<String, OptionDetails> detailsBuilder = ImmutableMap.builder();
        private final ImmutableMap.Builder<String, OptionsDescriptor> descriptorBuilder = ImmutableMap.builder();

        public OptionsDescriptor build() {
            return new OptionsDescriptor(detailsBuilder.build(), descriptorBuilder.build());
        }

        public void add(final String name, final OptionsDescriptor descriptor) {
            descriptorBuilder.put(name, descriptor);
        }

        public void add(final String name, final OptionDetails details) {
            detailsBuilder.put(name, details);
        }
    }

    private static final ItscoVisitor<?,DescriptorBuilder> VISITOR = new ItscoVisitor<Object,DescriptorBuilder>() {
        @Override
        public <T> void visitSimple(final String name, final Method method, final Class<T> propertyType, final boolean required, final DescriptorBuilder input) {
            if (Boolean.class.isAssignableFrom(propertyType)) {
                input.add(name, new BooleanOptionDetails(name, required, method.isAnnotationPresent(OptTogglesFalse.class), method.getAnnotation(OptDesc.class)));
            } else {
                input.add(name, new SimpleOptionDetails(name, required, method.getAnnotation(OptDesc.class)));
            }
        }

        @Override
        public <T> void visitItsco(final String name, final Method method, final Class<T> propertyType, final boolean required, final DescriptorBuilder input) {
            input.add(name, ItscoIntrospector.visitMembers(propertyType, new DescriptorBuilder(), (ItscoVisitor<T,DescriptorBuilder>) VISITOR).build());
        }

        @Override
        public <T> void visitPrimitive(final String name, final Method method, final Class<T> propertyType, final boolean required, final DescriptorBuilder input) {
            if (propertyType.isPrimitive() && Boolean.TYPE.isAssignableFrom(propertyType)) {
                input.add(name, new BooleanOptionDetails(name, required, method.isAnnotationPresent(OptTogglesFalse.class), method.getAnnotation(OptDesc.class)));
            } else {
                input.add(name, new SimpleOptionDetails(name, required, method.getAnnotation(OptDesc.class)));
            }
        }

        @Override
        public void visitDefaults(final Class<? extends Object> defaultsClass, final DescriptorBuilder input) {
            // we don't really care about defaults
        }
    };
}
