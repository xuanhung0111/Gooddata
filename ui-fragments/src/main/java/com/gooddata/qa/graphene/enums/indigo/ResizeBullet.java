package com.gooddata.qa.graphene.enums.indigo;

public enum ResizeBullet {

    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    ELEVEN("11"),
    TWELVE("12");

    private String number;

    ResizeBullet(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
