package com.gooddata.qa.graphene.connectors.pardot;

import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.enums.Connectors;

public class PardotPremiumCheckTest extends AbstractPardotCheckTest {

    @Override
    @BeforeClass
    public void loadRequiredProperties() {
        super.loadRequiredProperties();
        connectorType = Connectors.PARDOT_PREMIUM;

        expectedDashboardsAndTabs.put("Premium", new String[]{
                "Prospect Funnel", "Prospect Waterfalls", "Content Targeting", "Content Performance", "Lead Cohorts",
                "Activities", "Duration / Velocity", "List Health", "Rep Analysis"
        });
    }
}
