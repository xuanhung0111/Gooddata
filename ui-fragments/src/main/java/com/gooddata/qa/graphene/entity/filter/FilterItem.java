package com.gooddata.qa.graphene.entity.filter;

import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem.Range;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.ResultSize;

public abstract class FilterItem {

    public static final class Factory {
        private Factory() {
        }

        public static FilterItem createListValuesFilter(String attribute, String... values) {
            return new SelectFromListValuesFilterItem(attribute, values);
        }

        public static FilterItem createRangeFilter(String attribute, String metric, Range range) {
            return new NumericRangeFilterItem(attribute, metric, range);
        }

        public static FilterItem createRangeFilter(String attribute, String metric) {
            return new NumericRangeFilterItem(attribute, metric);
        }

        public static FilterItem createRankingFilter(ResultSize size, String attribute, String metric) {
            return new RankingFilterItem(size, attribute, metric);
        }

        public static FilterItem createRankingFilter(String attribute, String metric) {
            return new RankingFilterItem(attribute, metric);
        }

        public static FilterItem createVariableFilter(String variable, String... prompts) {
            return new VariableFilterItem(variable, prompts);
        }
    }
}
