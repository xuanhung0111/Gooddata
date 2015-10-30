package com.gooddata.qa.graphene.entity.filter;

import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.metrics.FilterMetric;

public class RankingFilterItem extends FilterItem {

    private Ranking ranking;
    private int size;
    private String metric;
    private FilterMetric filter;
    private List<String> attributes;

    RankingFilterItem(Ranking ranking, int size, String metric, String...attributes) {
        this.ranking = ranking;
        this.size = size;
        this.metric = metric;
        this.attributes = Arrays.asList(attributes);
    }

    RankingFilterItem(String metric, String...attributes) {
        this(Ranking.TOP, 3, metric, attributes);
    }

    public RankingFilterItem withRanking(Ranking ranking) {
        this.ranking = ranking;
        return this;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public RankingFilterItem withSize(int size) {
        this.size = size;
        return this;
    }

    public int getSize() {
        return size;
    }

    public RankingFilterItem withMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public String getMetric() {
        return metric;
    }

    public RankingFilterItem withFilter(FilterMetric filter) {
        this.filter = filter;
        return this;
    }

    public FilterMetric getFilter() {
        return filter;
    }

    public RankingFilterItem withAttributes(String...attributes) {
        this.attributes = Arrays.asList(attributes);
        return this;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public enum Ranking {
        TOP,
        BOTTOM;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
