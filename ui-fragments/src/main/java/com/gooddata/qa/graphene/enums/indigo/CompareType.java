package com.gooddata.qa.graphene.enums.indigo;

/**
 * compare types of {@link com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown}
 */
public enum CompareType {

    NOTHING("nothing"),
    SAME_PERIOD_LAST_YEAR("same_period_last_year");

    private String compareTypeName;

    CompareType(String type) {
        this.compareTypeName = type;
    }

    public String getCompareTypeName() {
        return compareTypeName;
    }
}
