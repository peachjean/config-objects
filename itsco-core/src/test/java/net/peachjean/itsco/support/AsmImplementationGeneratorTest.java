package net.peachjean.itsco.support;

import net.peachjean.itsco.support.example.CompoundItsco;
import net.peachjean.itsco.support.example.ExampleItscoImpl;
import net.peachjean.itsco.support.example.PrimitiveItscoImpl;
import net.peachjean.itsco.support.example.shared.DependentItscoImplExample;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class AsmImplementationGeneratorTest extends AbstractImplementationGeneratorTest {
    @Override
    protected ImplementationGenerator createUUT() {
        return new AsmImplementationGenerator();
    }

    @Test
    public void doAsmifier() throws Exception {
        byte[] bytes = new AsmImplementationGenerator().generateByteCode(CompoundItsco.class);
        System.out.println("=======================================================================================================");
        new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(
                System.out)), ClassReader.SKIP_DEBUG);
//        ASMifier.main(new String[] {createUUT().implement(CompoundItsco.class).getName()});
        System.out.println("=======================================================================================================");
    }
}
