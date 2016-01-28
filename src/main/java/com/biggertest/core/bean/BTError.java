package com.biggertest.core.bean;

public class BTError extends Exception {
    private final String message;
    private final Throwable stack;
    private final BTFeature feature;
    private final BTScenario scenario;
    private final BTStep step;

    public BTError(String message) {
        this.message = message;
        this.stack = new Throwable();
        this.feature = null;
        this.scenario = null;
        this.step = null;
    }

    public BTError(String message, Throwable stack, BTFeature feature, BTScenario scenario, BTStep step) {
        this.message = message;
        this.stack = new Throwable();
        this.feature = feature;
        this.scenario = scenario;
        this.step = step;
    }
}


