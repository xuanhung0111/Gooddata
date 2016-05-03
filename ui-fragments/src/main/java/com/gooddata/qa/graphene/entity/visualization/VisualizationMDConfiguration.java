package com.gooddata.qa.graphene.entity.visualization;

public class VisualizationMDConfiguration {

    private final String title;
    private final String type;

    private VisualizationMDConfiguration(final String newTitle, final String newType) {
        this.title = newTitle;
        this.type = newType;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public static class Builder {

        private String nestedTitle;
        private String nestedType = "bar";

        public Builder title(String newTitle) {
            this.nestedTitle = newTitle;
            return this;
        }

        public Builder type(String newType) {
            this.nestedType = newType;
            return this;
        }

        public VisualizationMDConfiguration build() {
            return new VisualizationMDConfiguration(nestedTitle, nestedType);
        }
    }
}
