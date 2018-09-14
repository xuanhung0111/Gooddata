package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;

public class PartialImportFragment extends ImportFragment {

    @Override
    protected State getPollState() throws JSONException {
        Graphene.waitGui().until(browser -> !loadJSON().getJSONObject("wTaskStatus").getString("status").isEmpty());
        return State.valueOf(loadJSON().getJSONObject("wTaskStatus").getString("status"));
    }
}
