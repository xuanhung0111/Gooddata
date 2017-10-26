package com.gooddata.qa.mdObjects.dashboard.filter;

import com.gooddata.qa.mdObjects.MdObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FilterItemContent implements MdObject {

    private String objUri;
    private boolean timeRangeDisabled;
    private boolean multiple;
    private FilterType type;
    private String label;
    private FilterConstraint filterConstraint;

    private String id = generateIdentifier();

    public void setObjUri(String uri) {
        this.objUri = uri;
    }

    public void setTimeRangeDisabled(boolean timeRangeDisabled) {
        this.timeRangeDisabled = timeRangeDisabled;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setFilterConstraint(FilterConstraint filterConstraint) {
        this.filterConstraint = filterConstraint;
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("filterItemContent", new JSONObject() {{
                put("obj", objUri);
                put("timeRangeDisabled", timeRangeDisabled ? 1 : 0);
                put("multiple", multiple ? 1 : 0);
                put("type", type.toString());
                put("label", label == null ? JSONObject.NULL : label);
                put("id", id);

                if (!Objects.isNull(filterConstraint))
                    put("default", filterConstraint.getMdObject());

            }});
        } catch (JSONException e) {
            handleJSONException(e);
        }

        return obj;
    }
}
