package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class JointCellsLayer extends AbstractFragment {
    private static final String JOINT_CELLS_LAYER = "joint-cells-layer";

    public static JointCellsLayer getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                JointCellsLayer.class, waitForElementVisible(className(JOINT_CELLS_LAYER), searchContext));
    }

    public Model getModel(String datasetName) {
        return Model.getInstance(this.getRoot(), datasetName);
    }

    public DateModel getDateModel(String date) {
        return DateModel.getInstance(this.getRoot(), date);
    }
}
