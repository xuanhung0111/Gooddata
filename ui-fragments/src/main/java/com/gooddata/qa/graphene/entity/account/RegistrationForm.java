package com.gooddata.qa.graphene.entity.account;

public class RegistrationForm {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String company;
    private String jobTitle;
    private String industry;

    public RegistrationForm() {
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.phone = "";
        this.company = "";
        this.jobTitle = "";
        this.industry = "Please choose...";
    }

    public RegistrationForm withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public RegistrationForm withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public RegistrationForm withEmail(String email){
        this.email = email;
        return this;
    }

    public RegistrationForm withPassword(String password) {
        this.password = password;
        return this;
    }

    public RegistrationForm withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public RegistrationForm withCompany(String company) {
        this.company = company;
        return this;
    }

    public RegistrationForm withJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public RegistrationForm withIndustry(String industry) {
        this.industry = industry;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getIndustry() {
        return industry;
    }
}
