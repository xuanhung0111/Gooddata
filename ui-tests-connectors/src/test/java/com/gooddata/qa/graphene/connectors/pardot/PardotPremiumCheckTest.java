package com.gooddata.qa.graphene.connectors.pardot;

import com.gooddata.qa.graphene.enums.Connectors;

public class PardotPremiumCheckTest extends AbstractPardotCheckTest {

    @Override
    protected void initProperties() {
        connectorType = Connectors.PARDOT_PREMIUM;
        super.initProperties();

        expectedDashboardsAndTabs.put("Premium", new String[]{
                "Prospect Funnel", "Prospect Waterfalls", "Content Targeting", "Content Performance", "Lead Cohorts",
                "Activities", "Duration / Velocity", "List Health", "Rep Analysis"
        });
    }
}
