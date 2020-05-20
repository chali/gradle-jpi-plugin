package org.jenkinsci.gradle.plugins.jpi

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import groovy.transform.CompileStatic
import org.junit.Assert
import org.junit.Test

import javax.lang.model.element.Modifier
import java.nio.charset.StandardCharsets
import java.util.function.Function

@CompileStatic
class TestSupport {
    static final EMBEDDED_IVY_URL = "${System.getProperty('user.dir')}/src/test/repo"
            .replace('\\', '/')

    static final TypeSpec CALCULATOR_CLASS = TypeSpec.classBuilder('Calculator')
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder('add')
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, 'a')
                    .addParameter(int.class, 'b')
                    .addStatement('return a + b')
                    .returns(int.class)
                    .build())
            .build()

    static final JavaFile CALCULATOR = JavaFile.builder('org.example', CALCULATOR_CLASS).build()

    static final JavaFile PASSING_TEST = JavaFile.builder('org.example', TypeSpec.classBuilder('AdditionTest')
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder('shouldAdd')
                    .addAnnotation(Test)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void)
                    .addStatement('$T.assertEquals(3, 1 + 2)', Assert)
                    .build())
            .build())
            .build()

    static final Function<File, JavaFile> TEST_THAT_WRITES_SYSTEM_PROPERTIES_TO = new Function<File, JavaFile>() {
        @Override
        JavaFile apply(File file) {
            JavaFile.builder('org.example', TypeSpec.classBuilder('ExampleTest')
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(MethodSpec.methodBuilder('shouldHaveSystemProperties')
                            .addAnnotation(Test)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void)
                            .addException(Exception)
                            .addStatement('$1T writer = new $1T($2S, $3T.UTF_8)', FileWriter, file, StandardCharsets)
                            .addStatement('$T.getProperties().store(writer, null)', System)
                            .build())
                    .build()).build()
        }
    }
}