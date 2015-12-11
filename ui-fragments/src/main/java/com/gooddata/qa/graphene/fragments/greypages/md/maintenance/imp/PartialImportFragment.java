package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp;

import org.json.JSONException;

public class PartialImportFragment extends ImportFragment {

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("wTaskStatus").getString("status");
    }
}
