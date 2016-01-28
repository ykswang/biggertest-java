package com.biggertest.common;

import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Reflect {

    /**
     * List all classes under the package ( include sub package )
     * @param packageName: any package name in classpath
     * @return The list of classes
     */
    public static List<Class<?>> getClassFromPackage(String packageName) {

        List<Class<?>> ret = new ArrayList<>();

        final FilterBuilder TestModelFilter = new FilterBuilder().include(packageName + "(.*)");
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .filterInputsBy(TestModelFilter)
                .setScanners(
                        new SubTypesScanner(false),
                        new TypeAnnotationsScanner(),
                        new FieldAnnotationsScanner(),
                        new MethodAnnotationsScanner(),
                        new MethodParameterScanner(),
                        new MethodParameterNamesScanner(),
                        new MemberUsageScanner()));

        for(Class<?> obj:reflections.getSubTypesOf(Object.class)) {
            ret.add(obj);
        }

        return ret;
    }

}
