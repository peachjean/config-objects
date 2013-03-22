package net.peachjean.itsco.support;

import org.junit.Ignore;

import java.lang.reflect.InvocationTargetException;

public class InlineImplementationGeneratorTest extends AbstractImplementationGeneratorTest {

    @Override
    protected InlineImplementationGenerator createUUT() {
        return new InlineImplementationGenerator();
    }

    @Override
    @Ignore("This is not implemented for the inline generator")
    public void sharedDependencyExample() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super.sharedDependencyExample();
    }
}
