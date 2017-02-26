package org.fluentj.core.processor;

import com.squareup.javapoet.*;
import org.fluentj.core.annotations.GenerateExtensibleFluentClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lucas on 13/02/17.
 */
@SupportedAnnotationTypes("org.fluentj.core.annotations.GenerateExtensibleFluentClass")
public class AnnotationsProcessor  extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        roundEnvironment.getElementsAnnotatedWith(GenerateExtensibleFluentClass.class).forEach(element -> {
            System.out.println(element);
            System.out.println(element.getKind());
            System.out.println(element.getEnclosedElements());
            System.out.println(element.getEnclosingElement());


            // You're doing it wrong. see http://stackoverflow.com/questions/18034626/annotation-processor-how-to-get-the-class-it-is-processing
            GenerateExtensibleFluentClass annotation = element.getAnnotation(GenerateExtensibleFluentClass.class);
            if (annotation == null) {
                return;
            }

            String source = createExtensibleVersion((TypeElement) element);
            try {
                System.out.println(source);
                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                        "Extensible" + element.getSimpleName(), element);
                Writer writer = sourceFile.openWriter();
                writer.write(source);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        return true;
    }


    public String createExtensibleVersion(TypeElement clazz) {

        Element enclosingElement = clazz.getEnclosingElement();
        if (enclosingElement.getKind() != ElementKind.PACKAGE) {
            return null;
        }

        PackageElement enclosingPackage = (PackageElement) enclosingElement;

        String packageName = enclosingPackage.getQualifiedName().toString();
        String clazzName = clazz.getSimpleName().toString();

        String targetClassName = "Extensible" + clazzName;
        TypeSpec.Builder builder = TypeSpec.classBuilder(targetClassName)
                .superclass(TypeName.get(clazz.asType()))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"all\"").build())
                .addTypeVariable(TypeVariableName.get("Self extends " + clazz.getSimpleName()));


        ;

        for(Element element: clazz.getEnclosedElements()) {

            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement method = (ExecutableElement) element;

            TypeMirror returnType = method.getReturnType();

            Set<Modifier> modifiers = method.getModifiers();

            if (!modifiers.contains(Modifier.PUBLIC)
                    || !returnType.equals(clazz.asType())) {
                continue;
            }

            List<? extends VariableElement> parameters = method.getParameters();

            builder.addMethod(MethodSpec.overriding(method)
                    .returns(TypeVariableName.get("Self"))
//                    .addAnnotation(AnnotationSpec.builder(Override.class).build())
                    .addModifiers(Modifier.PUBLIC)
//                    .addParameters(Arrays.asList(parameters).stream().map(
//                            parameter -> ParameterSpec
//                                    .builder(TypeName.get(parameter.getType()), parameter.getName(), Modifier.FINAL).build())
//                            .collect(Collectors.toList()))
                    .addStatement("super." + method.getSimpleName() + "(" + (parameters.stream()
                            .map(parameter -> parameter.getSimpleName()).collect(Collectors.joining(", "))) + ")")
                    .addStatement("return (Self) this")
                    .build());


        }

        return JavaFile.builder(packageName, builder.build()).build().toString();

    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.RELEASE_6;
    }
}
