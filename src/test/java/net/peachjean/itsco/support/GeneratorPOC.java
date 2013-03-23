package net.peachjean.itsco.support;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import net.peachjean.itsco.support.example.ExampleItsco;
import org.junit.Test;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.List;

public class GeneratorPOC {
    @Test
    public void runGenerator() throws IOException {

        File output = new File("target/tests/Generated.java");
        output.getParentFile().mkdirs();
        STGroup stGroup = new STGroupFile(Resources.getResource(GeneratorPOC.class, "builder.stg"), Charsets.UTF_8.name(), '<', '>');
        ST typeTemplate = stGroup.getInstanceOf("builder");

        typeTemplate.add("itsco", buildItscoModel());
        Writer writer = new FileWriter(output);
        try {
            typeTemplate.write(new AutoIndentWriter(writer, "\n"));
        } finally {
            Closeables.close(writer, false);
        }

    }

    private ItscoBuilderTemplateModel buildItscoModel() {
        return new ItscoBuilderTemplateModel(
                ExampleItsco.class.getSimpleName() + "Builder",
                ExampleItsco.class.getSimpleName(),
                ExampleItsco.class.getSimpleName() + "." + ExampleItsco.Defaults.class.getSimpleName(),
                ExampleItsco.class.getPackage().getName(),
                buildPropsList()
        );
    }

    private List<ItscoBuilderTemplateModel.Prop> buildPropsList() {
        ImmutableList.Builder<ItscoBuilderTemplateModel.Prop> list = ImmutableList.builder();

        for (Method m : ExampleItsco.class.getMethods()) {
            String methodName = m.getName();
            String name = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
            list.add(new ItscoBuilderTemplateModel.Prop(name, m.getReturnType().getName(), isRequired(m)));
        }
        return list.build();
    }

    private boolean isRequired(final Method m) {
        try {
            ExampleItsco.Defaults.class.getDeclaredMethod(m.getName());
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }
}
