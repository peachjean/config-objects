package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import net.peachjean.confobj.support.example.CompoundConfigObject;
import net.peachjean.confobj.support.example.ExampleConfigObject;
import net.peachjean.confobj.support.example.ExampleConfigObjectImpl;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.util.List;

public class AsmImplementationGeneratorTest extends AbstractImplementationGeneratorTest {
    @Override
    protected ImplementationGenerator createUUT() {
        return new AsmImplementationGenerator();
    }

    @Test
    public void doAsmifier() throws Exception {
//        byte[] bytes = new AsmImplementationGenerator().generateByteCode(CompoundConfigObject.class);
//        System.out.println("=======================================================================================================");
//        new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(
//                System.out)), ClassReader.SKIP_DEBUG);
        System.out.println("=======================================================================================================");
        ASMifier.main(new String[]{TestBed.class.getName()});
        System.out.println("=======================================================================================================");
    }

    public static class TestBed {
        public void doSomething() {
            String[] myArray = new String[26];
            myArray[25] = "twenty-fifth!";
            GenericType<String> gtString = GenericType.forTypeWithParams(String.class);

            GenericType<List> gtListString = GenericType.forTypeWithParams(List.class, GenericType.forTypeWithParams(String.class));

        }
    }
}
