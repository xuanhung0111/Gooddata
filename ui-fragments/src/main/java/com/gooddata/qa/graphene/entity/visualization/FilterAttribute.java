package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.sdk.model.md.Attribute;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static com.gooddata.qa.graphene.entity.visualization.FilterDate.initDateFilter;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static java.lang.String.format;

public class FilterAttribute {

    private Attribute attribute;
    private List<Integer> uriElementsNotIn;

    private FilterAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    private FilterAttribute(Attribute attribute, List<Integer> uriElementsNotIn) {
        this.attribute = attribute;
        this.uriElementsNotIn = uriElementsNotIn;
    }

    public static FilterAttribute createFilter(Attribute attribute, List<Integer> uriElementsNotIn) {
        return new FilterAttribute(attribute, uriElementsNotIn);
    }

    public static FilterAttribute createFilter(Attribute attribute) {
        return new FilterAttribute(attribute);
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public List<Integer> getUriElementsNotIn() {
        return this.uriElementsNotIn;
    }

    public static JSONArray initFilters(final List<FilterAttribute> filters) throws JSONException {
        return new JSONArray() {
            {
                if (!CollectionUtils.isEmpty(filters)) {
                    filters.forEach((filter) -> {
                        put(new JSONObject() {{
                            put("negativeAttributeFilter", new JSONObject() {{
                                put("displayForm", new JSONObject() {{
                                    put("uri", filter.getAttribute().getDefaultDisplayForm().getUri());
                                }});
                                put("notIn", new JSONArray() {{
                                    filter.getUriElementsNotIn().forEach((elementId) -> {
                                        put(filter.getAttribute().getUri() + "/elements?id=" + elementId);
                                    });
                                }});
                            }});
                        }});
                    });
                }
            }
        };
    }
}
