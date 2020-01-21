package com.gooddata.qa.graphene.freemium;

import com.gooddata.qa.graphene.AbstractFreemiumGrowthTest;
import org.testng.annotations.Test;

public class BasicFreemiumTest extends AbstractFreemiumGrowthTest {

    @Override
    public void initProperties() {
        maxProjects = 1;
        editionName = "FREE";
    }

    @Test(groups = {"createProject"})
    public void creatingNewProjects() {
        for (int i = 0; i< maxProjects; i++) {
            createNewEmptyProject(String.format("ATT_%s_%d", editionName + generateHashString(), i));
        }
        openUrl(PAGE_PROJECTS);
    }
}
