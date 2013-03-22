package net.peachjean.itsco.support;

public class TemplatedImplementationGeneratorTest extends AbstractImplementationGeneratorTest {
    @Override
    protected ImplementationGenerator createUUT() {
        return new TemplatedImplementationGenerator();
    }
}
