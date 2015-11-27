package com.gooddata.qa.graphene.entity.variable;

import com.gooddata.qa.graphene.enums.user.UserRoles;

public class NumericVariable extends AbstractVariable {

    private int defaultNumber;
    private int userNumber;
    private UserRoles userRole;

    public NumericVariable(String name) {
        super(name);
        userNumber = Integer.MAX_VALUE;
    }

    public NumericVariable withDefaultNumber(int number) {
        defaultNumber = number;
        return this;
    }

    public NumericVariable withUserNumber(UserRoles userRole, int number) {
        userNumber = number;
        this.userRole = userRole;
        return this;
    }

    public int getDefaultNumber() {
        return defaultNumber;
    }

    public int getUserNumber() {
        return userNumber;
    }
    
    public UserRoles getUserRole() {
        return userRole;
    }
}
