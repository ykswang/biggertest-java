package com.biggertest.core.bean;
import gherkin.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTScenario {

    private static final int S_WAIT = 0;
    private static final int S_PASS = 1;
    private static final int S_FAIL = 2;

    private String name;
    private String description;
    private List<BTStep> steps;
    private int status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BTStep> getSteps() {
        return steps;
    }

    public void setSteps(List<BTStep> steps) {
        this.steps = steps;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void pass() {
        setStatus(S_PASS);
    }

    public void fail() {
        setStatus(S_FAIL);
    }

    public static List<BTScenario> getInstances(ScenarioDefinition def, Background background,
                                                List<BTHook> hooks_before, List<BTHook> hooks_after, List<String> tags, List<BTAction> actions) {

        List<BTScenario> scenarios = new ArrayList<>();

        // Check Tags
        // If not allowed, return empty list
        if (tags.size() > 0) {
            boolean bValidTag = false;
            for(Tag gTag: def.getTags()) {
                if (!tags.contains(gTag.getName().trim())) continue;
                bValidTag = true;
                break;
            }
            if (!bValidTag) {
                return scenarios;
            }
        }

        // Get Step List
        List<Step> steps = def.getSteps();
        if (def instanceof Scenario) {
            List<BTStep> stepGroup = new ArrayList<>();
            for(Step step:steps) {
                stepGroup.add(BTStep.getInstance(step, actions, null));
            }
            BTScenario scenario = new BTScenario();
            if(def.getDescription()!=null)
                scenario.setDescription(def.getDescription().trim());
            scenario.setName(def.getName().trim());
            scenario.setSteps(stepGroup);
            scenarios.add(scenario);
        } else {
            List<Examples> examples = ((ScenarioOutline)def).getExamples();
            for(Examples example: examples) {
                TableRow title = example.getTableHeader();
                List<TableRow> lines = example.getTableBody();
                for(int idx=0; idx<lines.size(); idx++) {
                    List<BTStep> stepGroup = new ArrayList<>();
                    HashMap<String,String> dict = new HashMap<>();
                    for(int i=0; i<lines.get(idx).getCells().size(); i++) {
                        dict.put(title.getCells().get(i).getValue().trim(), lines.get(idx).getCells().get(i).getValue().trim());
                    }
                    for(Step step : steps) {
                        stepGroup.add(BTStep.getInstance(step, actions, dict));
                    }
                    BTScenario scenario = new BTScenario();
                    scenario.setDescription(def.getDescription().trim() + "\n" + example.getDescription().trim());
                    scenario.setName(def.getName().trim() + "|" + example.getName() + "|" + idx);
                    scenario.setSteps(stepGroup);
                    scenarios.add(scenario);
                }
            }
        }

        for(BTScenario scenario : scenarios) {
            List<BTStep> stepGroup = new ArrayList<>();

            // background
            if(background != null) {
                List<Step> bgSteps = background.getSteps();
                for(Step step : bgSteps) stepGroup.add(BTStep.getInstance(step, actions, null));
            }

            // Before
            if(hooks_before != null) {
                for(BTHook hook : hooks_before) {
                    List<Step> hookSteps = hook.getSteps();
                    for(Step step : hookSteps) stepGroup.add(BTStep.getInstance(step, actions, null));
                }
            }

            // normal steps
            stepGroup.addAll(scenario.getSteps());

            // After
            if(hooks_after != null) {
                for(BTHook hook : hooks_after) {
                    List<Step> hookSteps = hook.getSteps();
                    for(Step step : hookSteps) stepGroup.add(BTStep.getInstance(step, actions, null));
                }
            }
        }

        return scenarios;
    }

    public boolean run() {
        int i=0;

        try {
            for (; i < steps.size(); i++) {
                steps.get(i).run();
                steps.get(i).pass();
            }
        } catch (Exception e) {
            steps.get(i).fail();
            e.printStackTrace();
        }

        if (i < steps.size()) {
            fail();
            for (; i < steps.size(); i++) {
                steps.get(i).skip();
            }
            return false;
        } else {
            pass();
            return true;
        }
    }
}
