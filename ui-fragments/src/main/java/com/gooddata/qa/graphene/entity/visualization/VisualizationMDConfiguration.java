package com.gooddata.qa.graphene.entity.visualization;

public class VisualizationMDConfiguration {

    private final String title;

    private VisualizationMDConfiguration(final String newTitle) {
        this.title = newTitle;
    }

    public String getTitle() {
        return title;
    }

    public static class Builder {

        private String nestedTitle;

        public Builder title(String newTitle) {
            this.nestedTitle = newTitle;
            return this;
        }

        public VisualizationMDConfiguration build() {
            return new VisualizationMDConfiguration(nestedTitle);
        }
    }
}
