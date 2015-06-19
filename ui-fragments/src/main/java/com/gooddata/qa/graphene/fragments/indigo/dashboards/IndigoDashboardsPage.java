package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class IndigoDashboardsPage extends AbstractFragment {
    @FindBy(className = Kpi.MAIN_CLASS)
    protected List<Kpi> kpis;
}
