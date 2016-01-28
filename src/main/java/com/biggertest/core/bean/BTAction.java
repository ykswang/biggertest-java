package com.biggertest.core.bean;


import com.biggertest.common.Reflect;
import com.biggertest.core.ann.BT;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BTAction {

    public static List<BTAction> libs = new ArrayList<>();

    private Pattern pattern;

    private Object instance;

    private Method method;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public static BTAction getInstance(Object obj, Method method) {
        Annotation[] annotations = method.getAnnotations();

        BT BT = null;
        for(Annotation annotation : annotations) {
            String anName = annotation.annotationType().getName();
            if (anName.equals(BT.class.getName())) {
                BT = (BT)annotation;
                break;
            }
        }

        if (BT == null) return null;

        BTAction action = new BTAction();
        action.setInstance(obj);
        action.setMethod(method);
        action.setPattern(Pattern.compile(BT.value()));
        return action;
    }

    public static void buildStepLibs(String packageName) throws Exception {
        libs = new ArrayList<>();
        List<Class<?>> classes =  Reflect.getClassFromPackage(packageName);
        for(Class<?> cls : classes) {
            if(cls.isAnnotation()) {
                continue;
            }
            Object obj = cls.newInstance();
            for(Method method : cls.getMethods()) {
                BTAction action = getInstance(obj, method);
                if(action != null) libs.add(action);
            }
        }
    }
}
