package com.skylarksit.module.pojos;

public class DifferedLink {

    public enum DifferedLinkType { EDDRESS, ORDER, PROMO, NOTIFICATION}

    public transient DifferedLinkType type;
    public String data;
    public String uri;
    public String productSku;
    public String promoCode;
    public String description;
    public String imageUrl;
    public String serviceName;
    public String serviceType;
    public String actionLabel;

    public String branchIdentifier;
    public String referringUserUid;
    public Double referringBonus;
    public String bannerUid;

//    public String feature;
//    public String linkTitle;
//    public String campaign;
//    public String channel;
//    public String tags;

    public DifferedLink() {
    }

}
