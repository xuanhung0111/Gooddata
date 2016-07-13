package com.gooddata.qa.graphene.enrich;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.graphene.enricher.PageFragmentEnricher;
import org.jboss.arquillian.graphene.enricher.WebElementEnricher;
import org.jboss.arquillian.graphene.spi.enricher.SearchContextTestEnricher;

public class GdcGrapheneExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.override(SearchContextTestEnricher.class, WebElementEnricher.class, GdcWebElementEnricher.class);
        builder.override(SearchContextTestEnricher.class, PageFragmentEnricher.class, GdcPageFragmentEnricher.class);
    }

}
