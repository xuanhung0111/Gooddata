package com.gooddata.qa.graphene.entity.dlui;

import java.util.Collection;

import org.apache.commons.lang.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Field {

    private String name;
    private FieldTypes type;
    private FieldStatus status;

    public Field() {}

    public Field(String name, FieldTypes type) {
        this(name, type, FieldStatus.AVAILABLE);
    }

    public Field(String name, FieldTypes type, FieldStatus status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public FieldTypes getType() {
        return type;
    }

    public FieldStatus getStatus() {
        return status;
    }

    public void setStatus(FieldStatus status) {
        this.status = status;
    }

    @Override
    public Field clone() {
        return new Field(name, type, status);
    }

    public boolean hasCorrespondingWebElement(Collection<WebElement> elements) {
        return Iterables.any(elements, findWebElementPredicate());
    }

    public WebElement getCorrespondingWebElement(Collection<WebElement> elements) {
        return Iterables.find(elements, findWebElementPredicate());
    }

    private Predicate<WebElement> findWebElementPredicate() {
        return new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement field) {
                return name.equals(field.getText());
            }
        };
    }

    public enum FieldTypes {
        ALL("all data"),
        ATTRIBUTE("attributes"),
        FACT("facts"),
        DATE("dates"),
        LABEL_HYPERLINK("labels & hyperlinks");

        private String filterName;

        private FieldTypes(String filterName) {
            this.filterName = filterName;
        }

        public By getFilterBy() {
            return By.xpath(String.format("//a[text()='%s']", this.filterName));
        }

        public String getEmptyStateMessage() {
            return String.format(
                    "No sources with %s",
                    this == LABEL_HYPERLINK ? "Label or Hyperlink" : WordUtils
                            .capitalizeFully(filterName));
        }
    }

    public enum FieldStatus {
        AVAILABLE,
        SELECTED,
        ADDED;
    }
}
