package com.gooddata.qa.graphene.entity.dashboard.drilling;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DrillSetting {
    List<String> leftValues;
    String rightValue;
    String group;
    List<DrillSetting> innerDrillSetting;

    public DrillSetting(List<String> leftValues, String rightValue, String group) {
        this.leftValues = leftValues;
        this.rightValue = rightValue;
        this.group = group;

        innerDrillSetting = new ArrayList<>();
    }

    public DrillSetting addInnerDrillSetting(DrillSetting innerDrillSetting) {
        this.innerDrillSetting.add(innerDrillSetting);

        return this;
    }

    public List<String> getLeftValues() {
        return leftValues;
    }

    public String getRightValue() {
        return rightValue;
    }

    public String getGroup() {
        return group;
    }

    public List<DrillSetting> getInnerDrillSetting() {
        return innerDrillSetting;
    }

    public Pair<List<String>, String> getValuesAsPair() {
        return Pair.of(leftValues, rightValue);
    }
}
