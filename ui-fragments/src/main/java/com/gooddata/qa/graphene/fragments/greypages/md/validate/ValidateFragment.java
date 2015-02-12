package com.gooddata.qa.graphene.fragments.greypages.md.validate;

import com.gooddata.qa.graphene.enums.Validation;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ValidateFragment extends AbstractGreyPagesFragment {

    protected static final String LOCATOR_SPAN_STATUS = "//*[local-name() = 'p'][3]//*[local-name() = 'span']";

    @FindBy(id = "invalid_objects")
    private WebElement invalid_objects;

    @FindBy(id = "ldm")
    private WebElement ldm;

    @FindBy(id = "metric_filter")
    private WebElement metric_filter;

    @FindBy(id = "pdm::elem_validation")
    private WebElement pdm__elem_validation;

    @FindBy(id = "pdm::pdm_vs_dwh")
    private WebElement pdm__pdm_vs_dwh;

    @FindBy(id = "pdm::pk_fk_consistency")
    private WebElement pdm__pk_fk_consistency;

    @FindBy(id = "pdm::transitivity")
    private WebElement pdm__transitivity;

    @FindBy
    private WebElement submit;

    public String validate() {
        waitForElementVisible(submit);
        Graphene.guardHttp(submit).click();
        waitForElementNotVisible(submit);
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        waitForElementNotPresent(BY_GP_PRE_JSON);
        return waitForElementVisible(By.xpath(LOCATOR_SPAN_STATUS), browser).getText();
    }

    public String validateOnly(Validation... data) {
        uncheckAll();
        for (Validation validation : data) {
            switch (validation) {
                case INVALID_OBJECTS:
                    if (!invalid_objects.isSelected()) invalid_objects.click();
                    break;
                case LDM:
                    if (!ldm.isSelected()) ldm.click();
                    break;
                case METRIC_FILTER:
                    if (!metric_filter.isSelected()) metric_filter.click();
                    break;
                case PMD__ELEM_VALIDATION:
                    if (!pdm__elem_validation.isSelected()) pdm__elem_validation.click();
                    break;
                case PMD__PDM_VS_DWH:
                    if (!pdm__pdm_vs_dwh.isSelected()) pdm__pdm_vs_dwh.click();
                    break;
                case PMD__PK_FK_CONSISTENCY:
                    if (!pdm__pk_fk_consistency.isSelected()) pdm__pk_fk_consistency.click();
                    break;
                case PMD__TRANSITIVITY:
                    if (!pdm__transitivity.isSelected()) pdm__transitivity.click();
                    break;
                default:
            }
        }
        return validate();
    }

    /**
     * Uncheck "by default selected" validation fields.
     */
    private void uncheckAll() {
        invalid_objects.click();
        ldm.click();
        metric_filter.click();
        pdm__elem_validation.click();
        pdm__pdm_vs_dwh.click();
        pdm__pk_fk_consistency.click();
        pdm__transitivity.click();
    }
}
