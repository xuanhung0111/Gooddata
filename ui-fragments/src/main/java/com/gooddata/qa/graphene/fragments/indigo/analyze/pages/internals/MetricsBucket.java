package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricsBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-item")
    protected List<MetricConfiguration> metrics;

    public MetricConfiguration getMetricConfiguration(final String metric) {
        return waitForCollectionIsNotEmpty(metrics).stream()
            .filter(input -> metric.equals(input.getHeader()))
            .findFirst()
            .get();
    }

    public WebElement get(final String name) {
        return metrics.stream()
            .filter(input -> name.equals(input.getHeader()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find metric: " + name))
            .getRoot();
    }

    public List<String> getItemNames() {
        return metrics.stream()
            .map(MetricConfiguration::getHeader)
            .collect(toList());
    }
}
