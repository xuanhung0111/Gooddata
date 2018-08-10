package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp;

import org.json.JSONException;

import java.util.stream.Stream;

public class PartialImportFragment extends ImportFragment {

    @Override
    protected State getPollState() throws JSONException {
        return Stream.of(State.values())
                .filter(state -> state.toString().equals(loadJSON().getJSONObject("wTaskStatus").getString("status")))
                .findFirst()
                .get();
    }
}
