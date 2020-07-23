package com.gooddata.qa.graphene.redshift;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.sdk.common.GoodDataException;
import com.gooddata.sdk.model.warehouse.Warehouse;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CleanUpADSInstanceTest extends AbstractTest {

    @Test
    public void cleanUpOldADSInstance() {
        RestClient restClient = new RestClient(getProfile(ADMIN));
        List<String> warehouses = new ArrayList<>();
        restClient.getWarehouseService().listWarehouses().allItemsStream()
                .filter(warehouse -> isOldAdsLCMInstance(warehouse))
                .forEach(warehouse -> {
                    try {
                        restClient.getWarehouseService().removeWarehouse(warehouse);
                        warehouses.add(warehouse.getTitle());
                    } catch (GoodDataException ignored){
                        //ADS instance cannot be deleted because projects reference it
                    }
                });
        log.info("List of warehouses was removed: " + warehouses);
    }

    private boolean isOldAdsLCMInstance(Warehouse warehouse) {
        return warehouse.getUpdated().toLocalDate().isBefore(LocalDate.now().minusDays(testParams.getDatabaseRetentionDays())) &&
                warehouse.getTitle().contains("att-ads-");
    }
}
