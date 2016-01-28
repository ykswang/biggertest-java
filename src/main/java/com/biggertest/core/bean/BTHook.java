package com.biggertest.core.bean;

import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BTHook implements Comparable<BTHook> {

    private int priority;
    private String key;
    private List<Step> Steps;
    private Pattern pattern;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Step> getSteps() {
        return Steps;
    }

    public void setSteps(List<Step> steps) {
        Steps = steps;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public static BTHook getInstance(ScenarioDefinition def) throws BTError {

        if (def instanceof ScenarioOutline) {
            return null;
        }

        Scenario scenario = (Scenario) def;

        String text = scenario.getName().trim();

        Pattern pattern = Pattern.compile("@(.+)/(.+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            String head = matcher.group(1).trim();
            String body = matcher.group(2).trim();
            String key = "";
            int priority = 0;
            if (head.contains("(") & head.contains(")")) {
                Pattern headPattern = Pattern.compile("(.+)\\((.+)\\)");
                Matcher headMatcher = headPattern.matcher(head);
                if (headMatcher.matches()) {
                    key = headMatcher.group(1).trim().toLowerCase();
                    priority = Integer.valueOf(headMatcher.group(2).trim());
                } else {
                    throw new BTError("Invalid Hook Head: " + head);
                }
            } else {
                key = head.toLowerCase();
                priority = 0;
            }

            BTHook hook = new BTHook();
            hook.setKey(key);
            hook.setPriority(priority);
            hook.setSteps(scenario.getSteps());
            hook.setPattern(Pattern.compile(body));
            return hook;
        } else {
            return null;
        }
    }

    public static List<BTHook> getHooks(List<BTHook> hooklib, ScenarioDefinition def) {
        List<BTHook> ret = new ArrayList<>();
        for(BTHook hook:hooklib) {
            if (hook.getPattern().matcher(def.getName().trim()).matches() ) ret.add(hook);
        }
        Collections.sort(ret);
        return ret;
    }

    @Override
    public int compareTo(BTHook o) {
        return Integer.valueOf(this.priority).compareTo(o.getPriority());
    }
}
