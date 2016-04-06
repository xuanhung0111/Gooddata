package com.gooddata.qa.graphene.entity.dlui;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.dlui.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Dataset {

    private static final By BY_DATASET_TITLE = By.cssSelector("label");

    private String name;
    private List<Field> fields = Lists.newArrayList();

    public Dataset() {}

    public String getName() {
        return name;
    }

    public Dataset withName(String name) {
        this.name = name;
        return this;
    }

    public List<Field> getAllFields() {
        return Lists.newArrayList(fields);
    }

    public Collection<Field> getAvailableFields() {
        return Collections2.filter(fields, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                return field.getStatus() != FieldStatus.ADDED;
            }
        });
    }

    public Collection<Field> getSelectedFields() {
        return getFieldsWithStatus(FieldStatus.SELECTED);
    }

    public List<Field> getFieldsInSpecificFilter(FieldTypes fieldType) {
        if (fieldType == FieldTypes.ALL)
            return Lists.newArrayList(getAvailableFields());

        List<Field> filteredFields = Lists.newArrayList();
        for (Field field : getAvailableFields()) {
            if (field.getType() == fieldType)
                filteredFields.add(field);
        }
        return filteredFields;
    }

    public Dataset withFields(Field... fields) {
        return withFields(Lists.newArrayList(fields));
    }

    public Dataset withFields(List<Field> fields) {
        this.fields.addAll(fields);
        return this;
    }

    public Dataset updateFieldStatus(List<Field> customStatusFields) {
        checkValidFields(customStatusFields);

        for (final Field customStatusField : customStatusFields) {
            Iterables.find(this.fields, new Predicate<Field>() {

                @Override
                public boolean apply(Field field) {
                    return customStatusField.getName().equals(field.getName());
                }
            }).setStatus(customStatusField.getStatus());
        }

        return this;
    }

    public void addSelectedFields(boolean confirmed) {
        for (Field field : fields) {
            if (field.getStatus() != FieldStatus.SELECTED)
                continue;
            if (confirmed)
                field.setStatus(FieldStatus.ADDED);
            else
                field.setStatus(FieldStatus.AVAILABLE);
        }
    }

    public WebElement getCorrespondingWebElement(Collection<WebElement> elements) {
        return Iterables.find(elements, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement datasetElement) {
                return name.equals(datasetElement.findElement(BY_DATASET_TITLE).getText());
            }
        });
    }

    private void checkValidFields(List<Field> validatedFields) {
        for (final Field validatedField : validatedFields) {
            try {
                Iterables.find(getAllFields(), new Predicate<Field>() {

                    @Override
                    public boolean apply(Field field) {
                        return field.getName().equals(validatedField.getName());
                    }
                });
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Dataset '" + this.name
                        + "' doesn't contain field '" + validatedField.getName() + "'", e);
            }
        }
    }

    private Collection<Field> getFieldsWithStatus(final FieldStatus status) {
        return Collections2.filter(getAllFields(), new Predicate<Field>() {

            @Override
            public boolean apply(Field field) {
                return field.getStatus() == status;
            }
        });
    }
}
