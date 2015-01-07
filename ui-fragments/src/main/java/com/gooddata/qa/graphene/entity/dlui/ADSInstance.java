package com.gooddata.qa.graphene.entity.dlui;

public class ADSInstance {

    private String adsName;
    private String adsDescription;
    private String adsAuthorizationToken;
    private String adsId;

    public String getAdsAuthorizationToken() {
        return adsAuthorizationToken;
    }

    public ADSInstance setAdsAuthorizationToken(String adsAuthorizationToken) {
        this.adsAuthorizationToken = adsAuthorizationToken;
        return this;
    }

    public String getAdsDescription() {
        return adsDescription;
    }

    public ADSInstance setAdsDescription(String adsDescription) {
        this.adsDescription = adsDescription;
        return this;
    }

    public String getAdsName() {
        return adsName;
    }

    public ADSInstance setAdsName(String adsName) {
        this.adsName = adsName;
        return this;
    }

    public String getAdsId() {
        return adsId;
    }

    public ADSInstance setAdsId(String adsId) {
        this.adsId = adsId;
        return this;
    }
}
