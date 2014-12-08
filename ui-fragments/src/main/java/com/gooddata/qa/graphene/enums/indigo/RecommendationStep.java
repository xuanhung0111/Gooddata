package com.gooddata.qa.graphene.enums.indigo;

import com.gooddata.qa.graphene.entity.indigo.Recommendation;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.SeeingPercentsRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.TrendingRecommendation;

public enum RecommendationStep {

    SEE_TREND("See trend", TrendingRecommendation.class),
    COMPARE("Compare", ComparisonRecommendation.class),
    SEE_PERCENTS("See percents", SeeingPercentsRecommendation.class);

    private String text;
    private Class<? extends Recommendation> clazz;

    private RecommendationStep(String text, Class<? extends Recommendation> clazz) {
        this.text = text;
        this.clazz = clazz;
    }

    public Class<? extends Recommendation> getSupportedClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return text;
    }
}
