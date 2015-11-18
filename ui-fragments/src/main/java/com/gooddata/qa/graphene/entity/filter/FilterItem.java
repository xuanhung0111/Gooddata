package com.gooddata.qa.graphene.entity.filter;

import com.gooddata.qa.graphene.entity.filter.RangeFilterItem.RangeType;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.Ranking;

public abstract class FilterItem {

    public static final class Factory {
        private Factory() {
        }

        public static AttributeFilterItem createAttributeFilter(String attribute, String... values) {
            return new AttributeFilterItem(attribute, values);
        }

        public static RangeFilterItem createRangeFilter(RangeType rangeType, int rangeNumber, String metric,
                String...attributes) {
            return new RangeFilterItem(rangeType, rangeNumber, metric, attributes);
        }

        public static RangeFilterItem createRangeFilter(String metric, String...attributes) {
            return new RangeFilterItem(metric, attributes);
        }

        public static RankingFilterItem createRankingFilter(Ranking ranking, int size, String metric,
                String...attributes) {
            return new RankingFilterItem(ranking, size, metric, attributes);
        }

        public static RankingFilterItem createRankingFilter(String metric, String...attributes) {
            return new RankingFilterItem(metric, attributes);
        }

        public static PromptFilterItem createPromptFilter(String variable, String... prompts) {
            return new PromptFilterItem(variable, prompts);
        }
    }
}
