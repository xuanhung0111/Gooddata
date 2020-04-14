package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import static org.openqa.selenium.By.cssSelector;

public class JointLayers extends AbstractFragment {
    private static final String JOINT_LAYERS = "#v-2 .joint-layers";

    @FindBy(className = "joint-cells-layer")
    private JointCellsLayer jointCellsLayer;

    public static JointLayers getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                JointLayers.class, waitForElementVisible(cssSelector(JOINT_LAYERS), searchContext));
    }

    public JointCellsLayer getJointCellsLayer() {
        return jointCellsLayer;
    }
}
