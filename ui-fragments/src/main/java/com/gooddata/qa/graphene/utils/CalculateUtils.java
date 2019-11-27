package com.gooddata.qa.graphene.utils;

public final class CalculateUtils {
    private CalculateUtils(){
    }

    public static Integer addWidth(Integer firstValue, Integer secondValue){
        return firstValue + secondValue;
    }

    public static Integer halfOfWidth(String value){
        return convertToInteger(value) / 2;
    }

    public static Integer convertToInteger(String value){
        return Integer.parseInt(value);
    }
}
