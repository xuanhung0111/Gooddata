package com.gooddata.qa.graphene.fragments.disc.process;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;

public class DataloadProcessDetail extends AbstractProcessDetail {

    public DataloadScheduleDetail openSchedule(String scheduleName) {
        findSchedule(scheduleName).get().findElement(By.cssSelector(".schedule-title-cell a")).click();
        return DataloadScheduleDetail.getInstance(browser);
    }
}
