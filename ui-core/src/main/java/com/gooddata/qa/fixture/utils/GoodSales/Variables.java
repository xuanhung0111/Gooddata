package com.gooddata.qa.fixture.utils.GoodSales;

import com.gooddata.md.Attribute;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static java.lang.String.format;

public class Variables extends VariableRestRequest {

    public Variables(RestClient client, String projectId) {
        super(client, projectId);
    }

    public String createQuoteVariable() {
        return createNumericVarIfNotExit(VARIABLE_QUOTA, "3300000");
    }

    public String createStatusVariable() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        String defaultValuesExpression = format("[%s] IN ([%s])", attributeStatus.getUri(),
                getMdService().getAttributeElements(attributeStatus).stream()
                        .filter(element -> "Open".equals(element.getTitle()))
                        .findFirst()
                        .get()
                        .getUri());

        return createFilterVarIfNotExist(VARIABLE_STATUS, attributeStatus.getUri(), defaultValuesExpression);
    }

    private String createNumericVarIfNotExit(String title, String defaultValue) {
        try {
            return getVariableUri(title);
        } catch (ObjNotFoundException e) {
            return createNumericVariable(title, defaultValue);
        }
    }

    private String createFilterVarIfNotExist(String title, String attributeUri, String expression) {
        try {
            return getVariableUri(title);
        } catch (ObjNotFoundException e) {
            return createFilterVariable(title, attributeUri, expression);
        }
    }
}


