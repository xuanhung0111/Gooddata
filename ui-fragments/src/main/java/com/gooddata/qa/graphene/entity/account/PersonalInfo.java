package com.gooddata.qa.graphene.entity.account;

import java.util.Objects;

import com.google.common.base.Strings;

public class PersonalInfo {

    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private String phoneNumber;
    private String fullName;

    public PersonalInfo() {
        email = "";
        firstName = "";
        lastName = "";
        company = "";
        phoneNumber = "";
        fullName = "";
    }

    public PersonalInfo withEmail(String email) {
        this.email = email;
        return this;
    }

    public PersonalInfo withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public PersonalInfo withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public PersonalInfo withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public PersonalInfo withCompany(String company) {
        this.company = company;
        return this;
    }

    public PersonalInfo withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return Strings.isNullOrEmpty(fullName) ? firstName +  " " + lastName : fullName; 
    }

    public String getCompany() {
        return company;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof PersonalInfo)) {
            return false;
        }

        PersonalInfo personalInfo = (PersonalInfo) obj;

        if (!Objects.equals(getFullName(), personalInfo.getFullName())) {
            return false;
        }

        if (!Objects.equals(getEmail(), personalInfo.getEmail())) {
            return false;
        }

        if (!Objects.equals(getCompany(), personalInfo.getCompany())) {
            return false;
        }

        if (!Objects.equals(getPhoneNumber(), personalInfo.getPhoneNumber())) {
            return false;
        }

        return true;
    }

}
