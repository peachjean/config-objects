package net.peachjean.itsco.support;

import org.apache.commons.io.Charsets;
import net.peachjean.itsco.Itsco;
import net.peachjean.tater.utils.TypeSourceFormatter;
import net.peachjean.tater.utils.Utils;
import org.apache.bval.jsr303.util.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItscoAnnotationProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Itsco.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing @Implemented...");
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                TypeElement serviceElement = (TypeElement) element;
                PackageElement packageElement = getPackage(serviceElement);
                final String packageName =
                        packageElement == null ? "" : packageElement.getQualifiedName().toString();
                Itsco itsco = serviceElement.getAnnotation(Itsco.class);
                if (!itsco.generateBuilder()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Not generating builder due to generateBuilder=false.", serviceElement);
                    continue;
                }
                final String implName = "".equals(itsco.builderName()) ? serviceElement.getSimpleName().toString() + "Builder" :
                        itsco.builderName();

                try {
                    final ItscoBuilderTemplateModel templateModel = new ItscoBuilderTemplateModel(
                            implName,
                            serviceElement.getSimpleName().toString(),
                            serviceElement.getSimpleName().toString() + "." + "Defaults",
                            packageName,
                            buildPropList(serviceElement));

                    try {
                        final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageName + "." + implName, element);

                        this.writeBuilderSource(templateModel, sourceFile.openWriter());
                    } catch (IOException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ExceptionUtils.getRootCauseMessage(e), element);
                    }
                } catch (IllegalStateException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
                }

//                final List<FieldDescriptor> fields = createFieldList(serviceElement);
//                final String localName = determineLocalName(serviceElement);
//                final boolean isPublic = serviceElement.getModifiers().contains(Modifier.PUBLIC);
//                ImplementedDescriptor annotationDescriptor = new ImplementedDescriptor(isPublic, packageName, implName, localName, fields);
//                createImplSourceFile(annotationDescriptor, serviceElement, packageName + "." + implName);
            }
        }

        return true;
    }

    private List<ItscoBuilderTemplateModel.Prop> buildPropList(final TypeElement serviceElement) {
        List<ItscoBuilderTemplateModel.Prop> propList = new ArrayList<ItscoBuilderTemplateModel.Prop>();

        TypeElement defaultsClass = getInnerClass(serviceElement, "Defaults");
        if(defaultsClass == null)
        {
            throw new IllegalStateException("No defaults class found.");
        }
        for (Element inner : serviceElement.getEnclosedElements()) {
            if (ElementKind.METHOD.equals(inner.getKind())) {
                ExecutableElement method = (ExecutableElement) inner;
                String methodName = method.getSimpleName().toString();
                ExecutableElement defaultMethod = getMethod(defaultsClass, methodName);
                if (defaultMethod != null && !processingEnv.getElementUtils().overrides(defaultMethod, method, defaultsClass)) {
                    throw new IllegalStateException("Method " + methodName + " in Defaults does not override the method of the same methodName in the Itsco interface.");
                }

                final String name = methodName.substring(3,4).toLowerCase() + methodName.substring(4);
                final String type = method.getReturnType().accept(TypeSourceFormatter.INSTANCE, Utils.from(processingEnv));

                propList.add(new ItscoBuilderTemplateModel.Prop(name, type, defaultMethod == null));
            }
        }

        return propList;
    }

    private void writeBuilderSource(ItscoBuilderTemplateModel templateModel, Writer writer) throws IOException {
        STGroup stGroup = new STGroupFile(locateTemplate(), Charsets.UTF_8.name(), '<', '>');
        ST typeTemplate = stGroup.getInstanceOf("builder");

        typeTemplate.add("itsco", templateModel);
        try {
            typeTemplate.write(new AutoIndentWriter(writer, "\n"));
        } finally {
            IOUtils.closeQuietly(writer);
        }

    }

    private URL locateTemplate() {
        return this.getClass().getResource("builder.stg");
    }

    private PackageElement getPackage(final Element serviceElement) {
        if (serviceElement == null) {
            return null;
        }
        Element enclosing = serviceElement.getEnclosingElement();
        if (enclosing instanceof PackageElement) {
            return (PackageElement) enclosing;
        } else {
            return getPackage(enclosing);
        }
    }

    private TypeElement getInnerClass(final TypeElement outerClass, final String name) {
        for (Element element : outerClass.getEnclosedElements()) {
            if (ElementKind.CLASS.equals(element.getKind()) && element.getSimpleName().toString().equals(name)) {
                TypeElement typeElement = (TypeElement) element;
                return typeElement;
            }
        }
        return null;
    }

    ;

    private ExecutableElement getMethod(final TypeElement type, final String name) {
        for (Element element : type.getEnclosedElements()) {
            if (ElementKind.METHOD.equals(element.getKind()) && element.getSimpleName().toString().equals(name)) {
                ExecutableElement executableElement = (ExecutableElement) element;
                return executableElement;
            }
        }
        return null;
    }
}
