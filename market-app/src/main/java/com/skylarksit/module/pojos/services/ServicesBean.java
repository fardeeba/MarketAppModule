package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.MarketStoreGroup;
import com.skylarksit.module.pojos.DefaultLocation;
import com.skylarksit.module.pojos.MarketStoreTurf;

import java.util.List;
import java.util.Map;

public class ServicesBean {

    public List<MenuItemObject>  items;
    public List<ServiceObject> stores;

    public List<HomePageCategoryBean> homeSections;
    public Map<String, MarketStoreGroup> collectionGroups;

    public Map<String, List<String>> productRecommendations;

    public Map<String, MarketStoreTurf> turfs;

    public List<MenuCategoryObject> recentlyOrdered;
    public List<DefaultLocation> defaultLocations;

    public List<String> feedbackText;
    public String noServicesMessage;
    public List<String> favorites;
    public List<String> recentlyOrderedStores;
    public String homePageTitleMessage;

    public ServicesBean() {
	}


}
