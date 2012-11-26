package net.peachjean.itsco.support;

import net.peachjean.commons.test.junit.CumulativeAssertionRule;
import net.peachjean.commons.test.junit.TmpDir;
import net.peachjean.tater.test.CompilerHarness;
import net.peachjean.tater.test.CompilerResults;
import net.peachjean.tater.test.JavaSourceFromText;
import org.junit.Rule;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;

public class ItscoAnnotationProcessorTest {
    @Rule
    public TmpDir tmpDir = new TmpDir();

    @Rule
    public CumulativeAssertionRule accumulator = new CumulativeAssertionRule();

    @Test
    public void testSimpleExample() throws Exception {
        JavaFileObject[] sourceFiles = {
                JavaSourceFromText.builder("com.example.ExampleItsco")
                        .line("package com.example;")
                        .line("import net.peachjean.itsco.Itsco;")
                        .line("@Itsco")
                        .line("public interface ExampleItsco {")
                        .line("  public String getValue1();")
                        .line("  public String getValue2();")
                        .line("  public Integer getIntValue();")
                        .line("  public static abstract class Defaults implements ExampleItsco {")
                        .line("    public String getValue2() {")
                        .line("      return \"secondValue\";")
                        .line("    }")
                        .line("    public Integer getIntValue() {")
                        .line("      return 55;")
                        .line("    }")
                        .line("  }")
                        .line("}")
                .build(),
                JavaSourceFromText.builder("com.example.ExampleItscoAsserter")
                        .line("package com.example;")
                        .line("import net.peachjean.tater.test.*;")
                        .line("import net.peachjean.commons.test.junit.AssertionHandler;")
                        .line("public class ExampleItscoAsserter implements CompilerAsserter {")
                        .line("  public void doAssertions(AssertionHandler assertionHandler) {")
                        .line("    ExampleItsco ex = new ExampleItscoBuilder(\"myFirstValue\")")
                        .line("                      .withIntValue(88)")
                        .line("                      .build();")
                        .line("    assertionHandler.assertEquals(\"value1\", \"myFirstValue\", ex.getValue1());")
                        .line("    assertionHandler.assertEquals(\"value2\", \"secondValue\", ex.getValue2());")
                        .line("    assertionHandler.assertEquals(\"intValue\", 88, ex.getIntValue().intValue());")
                        .line("  }")
                        .line("}")
                .build()

        };

        CompilerResults results = new CompilerHarness(tmpDir.getDir(), accumulator, sourceFiles)
                .addProcessor(new ItscoAnnotationProcessor()).invoke();

        for (Diagnostic<? extends JavaFileObject> diagnostic : results.getDiagnostics()) {
            System.out.println(diagnostic);
        }
        results.assertNoOutput();
        results.assertNumberOfDiagnostics(Diagnostic.Kind.ERROR, 0);
        results.assertNumberOfDiagnostics(Diagnostic.Kind.WARNING, 0);

        results.runAssertion("com.example.ExampleItscoAsserter");

    }
}
