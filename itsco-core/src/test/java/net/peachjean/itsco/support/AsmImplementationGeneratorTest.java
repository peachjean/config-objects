package net.peachjean.itsco.support;

import net.peachjean.itsco.support.example.ExampleItscoImpl;
import net.peachjean.itsco.support.example.PrimitiveItscoImpl;
import net.peachjean.itsco.support.example.shared.DependentItscoImplExample;
import org.junit.Test;
import org.objectweb.asm.util.ASMifier;

public class AsmImplementationGeneratorTest extends AbstractImplementationGeneratorTest {
    @Override
    protected ImplementationGenerator createUUT() {
        return new AsmImplementationGenerator();
    }

    @Test
    public void doAsmifier() throws Exception {
        System.out.println("=======================================================================================================");
        ASMifier.main(new String[] {PrimitiveItscoImpl.class.getName()});
        System.out.println("=======================================================================================================");
        ASMifier.main(new String[] {ExampleItscoImpl.class.getName()});
    }
}
