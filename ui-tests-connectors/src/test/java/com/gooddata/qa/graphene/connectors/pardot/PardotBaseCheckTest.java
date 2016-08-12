package com.gooddata.qa.graphene.connectors.pardot;

import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.enums.Connectors;

public class PardotBaseCheckTest extends AbstractPardotCheckTest {

    @BeforeClass
    @Override
    public void loadRequiredProperties() {
        super.loadRequiredProperties();
        connectorType = Connectors.PARDOT_BASE;
    }
}
