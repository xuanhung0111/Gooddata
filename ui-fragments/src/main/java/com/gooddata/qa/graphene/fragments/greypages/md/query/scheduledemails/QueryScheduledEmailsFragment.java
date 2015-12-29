package com.gooddata.qa.graphene.fragments.greypages.md.query.scheduledemails;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class QueryScheduledEmailsFragment extends AbstractGreyPagesFragment {
    public final static int WRONG_ID = -1;

    @FindBy(css = "ul li strong a")
    protected List<WebElement> scheduleLinks;

    public boolean existsScheduleWithTitle(String title) {
        for (WebElement scheduleLink : scheduleLinks) {
            if (scheduleLink.getText().equals(title)) {
                return true;
            }
        }

        return false;
    }

    public int getScheduleId(String title) {
        int objectID = QueryScheduledEmailsFragment.WRONG_ID;

        for (WebElement scheduleLink : scheduleLinks) {
            if (scheduleLink.getText().equals(title)) {
                String[] uriParts = scheduleLink.getAttribute("href").split("/");
                objectID = Integer.parseInt(uriParts[uriParts.length - 1]);
                break;
            }
        }

        return objectID;
    }
}
