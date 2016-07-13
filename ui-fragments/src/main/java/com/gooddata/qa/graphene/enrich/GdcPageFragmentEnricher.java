package com.gooddata.qa.graphene.enrich;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.enricher.PageFragmentEnricher;
import org.jboss.arquillian.graphene.enricher.ReflectionHelper;
import org.jboss.arquillian.graphene.enricher.WebElementUtils;
import org.jboss.arquillian.graphene.enricher.exception.PageFragmentInitializationException;
import org.jboss.arquillian.graphene.findby.FindByUtilities;
import org.jboss.arquillian.graphene.proxy.GrapheneProxyInstance;
import org.jboss.arquillian.graphene.spi.configuration.GrapheneConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;

public class GdcPageFragmentEnricher extends PageFragmentEnricher {

    @Inject
    private Instance<GrapheneConfiguration> configuration;

    public GdcPageFragmentEnricher() {
    }

    @Override
    public void enrich(SearchContext searchContext, Object target) {
        List<Field> fields = FindByUtilities.getListOfFieldsAnnotatedWithFindBys(target);
        for (Field field : fields) {
            GrapheneContext grapheneContext = searchContext == null ? null : ((GrapheneProxyInstance) searchContext)
                .getGrapheneContext();
            final SearchContext localSearchContext;
            if (grapheneContext == null) {
                grapheneContext = GrapheneContext.getContextFor(ReflectionHelper.getQualifier(field.getAnnotations()));
                localSearchContext = grapheneContext.getWebDriver(SearchContext.class);
            } else {
                localSearchContext = searchContext;
            }
            // Page fragment
            if (isPageFragmentClass(field.getType(), target)) {
                setupGdcPageFragment(localSearchContext, target, field);
                // List<Page fragment>
            } else {
                try {
                    if (field.getType().isAssignableFrom(List.class) && isPageFragmentClass(getListType(field), target)) {
                        setupPageFragmentList(localSearchContext, target, field);
                    }
                } catch (ClassNotFoundException e) {
                    throw new PageFragmentInitializationException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Override method setupPageFragment() but it's marked final. So create a new one
     */
    private void setupGdcPageFragment(SearchContext searchContext, Object target, Field field) {
        //by should never by null, by default it is ByIdOrName using field name
        How defaultElementLocatingStrategy = configuration == null || configuration.get() == null ?
                How.ID_OR_NAME :
                configuration.get().getDefaultElementLocatingStrategy();
        // the by retrieved in this way is never null, by default it is ByIdOrName using field name
        By rootBy = FindByUtilities.getCorrectBy(field, defaultElementLocatingStrategy);
        WebElement root = WebElementUtils.findElementLazily(rootBy, searchContext);
        Object pageFragment = createPageFragment(field.getType(), root);
        setValue(field, target, pageFragment);
    }
}
