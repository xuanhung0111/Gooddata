package com.gooddata.qa.graphene.enums.indigo;

public enum OptionalStacking {
    //Because Input's css (".s-stack-to-percent") has opacity = 0 so it isn't visible and unable to click.
    MEASURES(".s-stack-measures", ".s-stack-measures + span.input-label-text"),
    PERCENT(".s-stack-to-percent", ".s-stack-to-percent + span.input-label-text");

    private String option;
    private String optionLabel;

    OptionalStacking(String option, String optionLabel) {
        this.option = option;
        this.optionLabel = optionLabel;
    }

    @Override
    public String toString() {
        return option;
    }

    public String getOptionLabel() { return optionLabel; }
}
