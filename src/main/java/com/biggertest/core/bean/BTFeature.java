package com.biggertest.core.bean;

import com.biggertest.common.IO;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BTFeature {

    public static final int STATUS_WAIT = 0;
    public static final int STATUS_PASS = 1;
    public static final int STATUS_FAIL = 2;


    private String name;
    private String description;
    private List<BTScenario> scenarios;
    private int status;

    private BTFeature() {
        scenarios = new ArrayList<>();
    }

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

    public List<BTScenario> getScenarios() {
        return scenarios;
    }

    public void addScenarios(List<BTScenario> scenarios) {
        this.scenarios.addAll(scenarios);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static BTFeature getInstance(String path, List<String> tags, List<BTAction> actions) throws BTError {
        Parser<Feature> parser = new Parser<>(new AstBuilder());

        // Read feature file
        Feature gFeature = null;
        try {
            System.out.println(path);
            TokenMatcher matcher = new TokenMatcher();
            gFeature = parser.parse(IO.readAll(new File(path)), matcher);
        } catch (Exception e) {
            throw new BTError(e.getMessage());
        }

        return getInstance(gFeature, tags, actions);
    }

    public static BTFeature getInstance(Feature gFeature, List<String> tags, List<BTAction> actions) throws BTError {

        BTFeature feature = new BTFeature();

        // Check Tags
        // Check only the option tags is not empty
        // If features is empty, skip check
        if (tags.size() > 0 && gFeature.getTags().size() > 0) {
            boolean bValidTag = false;
            List<Tag> gTags = gFeature.getTags();
            for (Tag gTag: gTags) {
                if (tags.contains(gTag.getName())) {
                    bValidTag = true;
                    break;
                }
            }
            if(!bValidTag) {
                return null;
            }
        }

        // transform the information
        feature.setName(gFeature.getName().trim());
        feature.setDescription(gFeature.getDescription());

        // Get Hooks
        List<ScenarioDefinition> gScenarioDefinitions_all = gFeature.getScenarioDefinitions();
        List<ScenarioDefinition> gScenarioDefinitions = new ArrayList<>();
        List<BTHook> hooklib_b = new ArrayList<>();
        List<BTHook> hooklib_a = new ArrayList<>();

        for(ScenarioDefinition def: gScenarioDefinitions_all) {
            BTHook hook = BTHook.getInstance(def);
            if(hook == null) {
                gScenarioDefinitions.add(def);
                continue;
            }
            switch (hook.getKey()) {
                case "before":
                    hooklib_b.add(hook);
                    break;
                case "after":
                    hooklib_a.add(hook);
                    break;
                default:
                    throw new BTError("Invalid hook key: @" + hook.getKey());
            }
        }

        // Get Scenario
        for (ScenarioDefinition def: gScenarioDefinitions) {
            List<BTHook> hooks_b = BTHook.getHooks(hooklib_b, def);
            List<BTHook> hooks_a = BTHook.getHooks(hooklib_a, def);
            feature.addScenarios(BTScenario.getInstances(def, gFeature.getBackground(), hooks_b, hooks_a, tags, actions));
        }

        feature.setStatus(BTFeature.STATUS_WAIT);
        return feature;
    }

    public void run() {
        for(BTScenario scenario: scenarios) {
            scenario.run();
        }
    }
}
