package com.gooddata.qa.graphene.entity.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HowItem {

    private Attribute attribute;
    private Position position;
    private List<String> filterValues;

    public HowItem(Attribute attribute, Position position, String... filterValues) {
        this.attribute = attribute;
        this.position = position;
        this.filterValues = new ArrayList<String>(Arrays.asList(filterValues));
    }

    public HowItem(Attribute attribute, String... filterValues) {
        this(attribute, Position.LEFT, filterValues);
    }

    public HowItem(String attributeName, String... filterValues) {
        this(new Attribute(attributeName), filterValues);
    }

    public HowItem(String attributeName, Position position, String... filterValues) {
        this(new Attribute(attributeName), position, filterValues);
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }

    public HowItem addFilterValue(String... values) {
        filterValues.addAll(Arrays.asList(values));
        return this;
    }

    public enum Position {
        LEFT("sndAttributePosition_rows"),
        TOP("sndAttributePosition_columns");

        private String direction;

        Position(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return direction;
        }
    }
}
