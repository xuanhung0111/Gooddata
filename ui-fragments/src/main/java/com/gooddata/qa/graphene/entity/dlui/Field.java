package com.gooddata.qa.graphene.entity.dlui;

public class Field {

    private String fieldName;
    private FieldTypes fieldType;

    public String getFieldName() {
        return fieldName;
    }

    public Field setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public FieldTypes getFieldType() {
        return fieldType;
    }

    public Field setFieldType(FieldTypes fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public enum FieldTypes {
        ALL("all data"),
        ATTRIBUTE("attributes"),
        FACT("facts"),
        DATE("dates"),
        LABLE_HYPERLINK("labels & hyperlinks");
        
        private String filterName;
        
        private FieldTypes(String filterName) {
            this.filterName = filterName;
        }

        public String getFilterName() {
            return this.filterName;
        }
    }
}
