package com.gooddata.qa.graphene.entity.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariableFilterItem extends FilterItem {

    private String variable;
    private List<String> promptElements;

    VariableFilterItem(String variable, String... prompts) {
        this.variable = variable;
        this.promptElements = new ArrayList<String>(Arrays.asList(prompts));
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public List<String> getPromptElements() {
        return promptElements;
    }

    public void addPromptElements(String prompt) {
        this.promptElements.add(prompt);
    }
}
