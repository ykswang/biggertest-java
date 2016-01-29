package com.biggertest.core;

import com.biggertest.common.IO;
import com.biggertest.common.Reflect;
import com.biggertest.core.bean.BTAction;
import com.biggertest.core.bean.BTError;
import com.biggertest.core.bean.BTFeature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BiggerTest {

    List<BTAction> actions;
    List<BTFeature> features;

    public BiggerTest() {
        actions = new ArrayList<>();
        features = new ArrayList<>();
    }

    public void setActions(String pkgName) throws Exception {
        List<Class<?>> classes =  Reflect.getClassFromPackage(pkgName);
        for(Class<?> cls : classes) {
            if(cls.isAnnotation()) {
                continue;
            }
            Object obj = cls.newInstance();
            for(Method method : cls.getMethods()) {
                BTAction action = BTAction.getInstance(obj, method);
                if(action != null) actions.add(action);
            }
        }
    }

    public void setFeatures(String path, List<String> tags) throws BTError {
        List<String> filePaths = IO.glob(path);
        for(String filePath : filePaths) {
            BTFeature feature = BTFeature.getInstance(filePath, tags, actions);
            if(feature!=null) features.add(feature);
        }
    }


    public void run() {
        for(BTFeature feature:features) {
            feature.run();
        }
    }
}
