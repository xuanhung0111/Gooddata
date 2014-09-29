package com.gooddata.qa.graphene.entity;

public class HowItem {

    private Attribute attribute;
    private Position position;

    public HowItem(Attribute attribute, Position position) {
        this.attribute = attribute;
        this.position = position;
    }

    public HowItem(Attribute attribute) {
        this.attribute = attribute;
        this.position = Position.LEFT;
    }

    //TODO: move to definition
    public HowItem(String attributeName) {
        this(new Attribute(attributeName));
    }

    //TODO: move to definition
    public HowItem(String attributeName, Position position) {
        this(new Attribute(attributeName), position);
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

    public static enum Position {
        LEFT,
        TOP
    }
}
