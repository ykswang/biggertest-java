package com.biggertest.core.bean;

import gherkin.ast.DataTable;
import gherkin.ast.Step;
import gherkin.ast.TableRow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class BTStep {

    private static final int S_WAIT = 0;
    private static final int S_PASS = 1;
    private static final int S_FAIL = 2;
    private static final int S_SKIP = 3;

    private String text = null;
    private BTAction action = null;
    private int status = S_WAIT;
    private Object[] params = null;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BTAction getAction() {
        return action;
    }

    public void setAction(BTAction action) {
        this.action = action;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void pass() {
        setStatus(S_PASS);
    }

    public void fail() {
        setStatus(S_FAIL);
    }

    public void skip() {
        setStatus(S_SKIP);
    }

    public static BTStep getInstance(Step gStep, List<BTAction> actions, HashMap<String,String> example) {
        BTStep step = new BTStep();
        step.setStatus(S_WAIT);

        String text = gStep.getText();
        if(example!=null) {
            for(String key:example.keySet()) {
                text = text.replace("<" + key + ">", example.get(key));
            }
        }

        // Text
        step.setText(text);

        // Find action
        List<Object> params = new ArrayList<>();
        for(BTAction action : actions) {
            Matcher matcher = action.getPattern().matcher(step.getText());
            if(!matcher.matches()) continue;
            step.setAction(action);
            for(int i=1; i<matcher.groupCount(); i++) {
                params.add(matcher.group(i));
            }
            break;
        }

        if(step.getAction() == null) return null;

        // Read Arg Params
        if(gStep.getArgument() instanceof DataTable) {
            DataTable table = (DataTable) gStep.getArgument();
            if(table.getRows().get(0).getCells().size() == 1) {
                // It's a string list
                List<String> param = new ArrayList<>();
                for(TableRow row : table.getRows()) {
                    param.add(row.getCells().get(0).getValue().trim());
                }
                params.add(param);
            } else {
                // It's a map list
                List<Map<String,String>> param = new ArrayList<>();
                for(int i=1; i<table.getRows().size(); i++) {
                    TableRow row = table.getRows().get(i);
                    Map<String,String> line = new HashMap<>();
                    for(int j=0; j<row.getCells().size(); j++) {
                        line.put(table.getRows().get(0).getCells().get(j).getValue().trim(), row.getCells().get(j).getValue().trim());
                    }
                    param.add(line);
                }
                params.add(param);
            }
        }
        return step;
    }

    public void run() throws Exception {
        Method method = action.getMethod();
        method.invoke(action.getInstance(), params);
    }
}
