package com.gooddata.qa.graphene.enrich;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.enricher.AbstractSearchContextEnricher;
import org.jboss.arquillian.graphene.enricher.ReflectionHelper;
import org.jboss.arquillian.graphene.enricher.WebElementUtils;
import org.jboss.arquillian.graphene.findby.FindByUtilities;
import org.jboss.arquillian.graphene.proxy.GrapheneProxyInstance;
import org.jboss.arquillian.graphene.spi.configuration.GrapheneConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;

public class GdcWebElementEnricher extends AbstractSearchContextEnricher {

    @Inject
    private Instance<GrapheneConfiguration> configuration;

    @Override
    public void enrich(SearchContext searchContext, Object target) {
        try {
            List<Field> fields = FindByUtilities.getListOfFieldsAnnotatedWithFindBys(target);
            for (Field field : fields) {
                GrapheneContext grapheneContext = searchContext == null ?
                        null : ((GrapheneProxyInstance) searchContext).getGrapheneContext();

                final SearchContext localSearchContext;
                if (grapheneContext == null) {
                    grapheneContext = GrapheneContext.getContextFor(ReflectionHelper.getQualifier(field.getAnnotations()));
                    localSearchContext = grapheneContext.getWebDriver(SearchContext.class);
                } else {
                    localSearchContext = searchContext;
                }

                //by should never by null, by default it is ByIdOrName using field name
                How defaultElementLocatingStrategy = configuration == null || configuration.get() == null ?
                        How.ID_OR_NAME :
                        configuration.get().getDefaultElementLocatingStrategy();
                By by = FindByUtilities.getCorrectBy(field, defaultElementLocatingStrategy);

                // WebElement
                if (field.getType().isAssignableFrom(WebElement.class)) {
                    WebElement element = WebElementUtils.findElementLazily(by, localSearchContext);
                    setValue(field, target, element);
                    // List<WebElement>
                } else if (field.getType().isAssignableFrom(List.class)
                    && getListType(field).isAssignableFrom(WebElement.class)) {
                    List<WebElement> elements = WebElementUtils.findElementsLazily(by, localSearchContext);
                    setValue(field, target, elements);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public int getPrecedence() {
        return 1;
    }

}
