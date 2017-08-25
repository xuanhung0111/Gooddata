package com.gooddata.qa.fixture.utils.Invoice.entities;

import java.time.LocalDate;

public class Invoice {
    private String name;
    private int total;
    private Person person;
    private InvoiceItem item;
    private LocalDate date;

    public String getName() {
        return name;
    }

    public int getTotal() {
        return total;
    }

    public Person getPerson() {
        return person;
    }

    public InvoiceItem getItem() {
        return item;
    }

    public LocalDate getDate() {
        return date;
    }

    public Invoice(String name, int total, Person person, InvoiceItem item, LocalDate date) {
        this.name = name;
        this.total = total;
        this.person = person;
        this.item = item;
        this.date = date;
    }
}
