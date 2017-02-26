package org.fluentj.core;

import com.squareup.javapoet.*;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by lucas on 29/01/17.
 */
public class JavapoetTest {


    @Test
    public void test() {
        createExtensibleVersion(MyFluent.class);

    }


    public static class MyFluent {

        public MyFluent doStuff() {
            return this;
        }

        public MyFluent doStuff(String x, Integer y) {
            return this;
        }
    }


    public void createExtensibleVersion(Class<?> clazz) {

        String packageName = clazz.getPackage().getName();
        String clazzName = clazz.getName();



        String targetClassName = "Extensible" + clazz.getSimpleName();
        TypeSpec.Builder builder = TypeSpec.classBuilder(targetClassName)
                .superclass(clazz)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"all\"").build())
                .addTypeVariable(TypeVariableName.get("Self extends " + clazz.getSimpleName()));


        Method[] declaredMethods = clazz.getDeclaredMethods();

        for(Method method: declaredMethods) {

            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            int modifiers = method.getModifiers();

            if ((modifiers & java.lang.reflect.Modifier.PUBLIC) == 0
                    || !returnType.equals(clazz)) {
                continue;
            }

            Parameter[] parameters = method.getParameters();

            builder.addMethod(MethodSpec.methodBuilder(methodName)
                    .returns(TypeVariableName.get("Self"))
                    .addAnnotation(AnnotationSpec.builder(Override.class).build())
                    .addModifiers(Modifier.PUBLIC)
            .addParameters(Arrays.asList(parameters).stream().map(
                    parameter -> ParameterSpec
                    .builder(TypeName.get(parameter.getType()), parameter.getName(), Modifier.FINAL).build())
                    .collect(Collectors.toList()))
            .addStatement("super." + methodName + "(" + (Arrays.asList(parameters).stream().map(Parameter::getName).collect(Collectors.joining(", "))) + ")")
            .addStatement("return (Self) this")
            .build());


        }

        System.out.println(JavaFile.builder(packageName, builder.build()).build().toString());

    }

    @SuppressWarnings("all")
    class ExtensibleMyFluent<Self extends MyFluent> extends JavapoetTest.MyFluent {
        @Override
        public Self doStuff() {
            super.doStuff();
            return (Self) this;
        }

        @Override
        public Self doStuff(final String x, final Integer y) {
            super.doStuff(x, y);
            return (Self) this;
        }
    }


}
