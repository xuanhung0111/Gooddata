package com.gooddata.qa.fixture.utils.Invoice.entities;

public class InvoiceItem {
    private int total;
    private int quantity;

    public InvoiceItem(int total, int quantity) {
        this.total = total;
        this.quantity = quantity;
    }

    public int getTotal() {
        return total;
    }

    public int getQuantity() {
        return quantity;
    }
}
