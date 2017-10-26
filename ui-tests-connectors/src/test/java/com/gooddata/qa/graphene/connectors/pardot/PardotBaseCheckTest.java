package com.gooddata.qa.graphene.connectors.pardot;

import com.gooddata.qa.graphene.enums.Connectors;

public class PardotBaseCheckTest extends AbstractPardotCheckTest {

    @Override
    protected void initProperties() {
        connectorType = Connectors.PARDOT_BASE;
        super.initProperties();
    }
}
