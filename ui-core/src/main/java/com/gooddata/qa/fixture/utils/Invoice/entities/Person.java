package com.gooddata.qa.fixture.utils.Invoice.entities;

public class Person {
    private String fullname;
    private int personId;
    private String firstname;
    private String surname;

    public Person(String fullname, int personId, String firstname, String surname) {
        this.fullname = fullname;
        this.personId = personId;
        this.firstname = firstname;
        this.surname = surname;
    }

    public String getFullname() {
        return fullname;
    }

    public int getPersonId() {
        return personId;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }
}
