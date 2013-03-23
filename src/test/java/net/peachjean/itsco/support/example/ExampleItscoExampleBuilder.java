package net.peachjean.itsco.support.example;

import com.google.common.collect.Sets;
import net.peachjean.itsco.ItscoBuilder;
import org.apache.bval.jsr303.ApacheValidationProvider;

import javax.inject.Named;
import javax.validation.*;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
public class ExampleItscoExampleBuilder implements ItscoBuilder<ExampleItsco> {

    private String value1 = null;
    private String value2 = null;
    private Integer intValue = null;

    private final Validator validator;
    private ExampleItsco current = new ExampleItsco.Defaults() {
        @Override
        public String getValue1() {
            return value1;
        }

        @Override
        public String getValue2() {
            return value2 != null ? value2 : super.getValue2();
        }

        @Override
        public Integer getIntValue() {
            return intValue != null ? intValue : super.getIntValue();
        }
    };

    private ExampleItscoExampleBuilder() {
        ValidatorFactory validationFactory = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory();
        validator = validationFactory.getValidator();
    }

    public ExampleItscoExampleBuilder(final ExampleItsco source) {
        this();
        this.value1 = source.getValue1();
        this.value2 = source.getValue2();
        this.intValue = source.getIntValue();
    }

    public ExampleItscoExampleBuilder(@Named("value1") final String value1) {
        this();
        this.value1 = value1;
    }

    public ExampleItscoExampleBuilder withValue1(final String value1) {
        this.value1 = value1;
        return this;
    }

    public ExampleItscoExampleBuilder withValue2(final String value2) {
        this.value2 = value2;
        return this;
    }

    public ExampleItscoExampleBuilder withIntValue(final Integer intValue) {
        this.intValue = intValue;
        return this;
    }

    @Override
    public void validate() {
        final Set<ConstraintViolation<?>> violations = Sets.newHashSet();
        violations.addAll(validator.validate(current));
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Invalid bean state.", violations);
        }
    }

    @Override
    public ExampleItsco build() {
        this.validate();
        return new Impl(value1, value2, intValue);
    }

    private static class Impl extends ExampleItsco.Defaults {
        private final String value1;
        private final String value2;
        private final Integer intValue;

        private Impl(final String value1, final String value2, final Integer intValue) {
            this.value1 = value1;
            this.value2 = value2;
            this.intValue = intValue;
        }

        @Override
        public String getValue1() {
            return value1;
        }

        @Override
        public String getValue2() {
            return value2 != null ? value2 : super.getValue2();
        }

        @Override
        public Integer getIntValue() {
            return intValue != null ? intValue : super.getIntValue();
        }
    }
}
