package com.skylarksit.module.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.skylarksit.module.R;
import com.skylarksit.module.lib.ErrorResource;
import com.skylarksit.module.lib.Rest;
import com.skylarksit.module.libs.Json;
import com.skylarksit.module.libs.alertdialog.NokAlertDialog;
import com.skylarksit.module.pojos.CalculateEtaResponse;
import com.skylarksit.module.pojos.CredibilityResponseBean;
import com.skylarksit.module.pojos.MarketStoreGroup;
import com.skylarksit.module.pojos.MarketStoreSection;
import com.skylarksit.module.pojos.MarketStoreTurf;
import com.skylarksit.module.pojos.MissingItem;
import com.skylarksit.module.pojos.ResponseBean;
import com.skylarksit.module.pojos.services.HomePageCategoryBean;
import com.skylarksit.module.pojos.services.MenuCategoryObject;
import com.skylarksit.module.pojos.services.MenuItemObject;
import com.skylarksit.module.pojos.services.OrderTrackingBean;
import com.skylarksit.module.pojos.services.PromoResultBean;
import com.skylarksit.module.pojos.services.ServiceObject;
import com.skylarksit.module.pojos.services.ServicesBean;
import com.skylarksit.module.pojos.InventoryResponse;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.paystack.android.PaystackSdk;

public class Services {

    private Activity activity;
    private ServiceResult result;

    public ServicesModel model;

    public interface ServiceResult<T> {
        void onResult(T result);

        void onError(ErrorResource errorResource);
    }

    private Services(Activity activity) {
        this.activity = activity;
        this.model = ServicesModel.instance(activity);
    }

    public static Services call(Activity activity) {
        return new Services(activity);
    }

    public Services result(ServiceResult result) {
        this.result = result;
        return this;
    }

    public void getInventory(Set<String> items) {

        if (!Utilities.isLoggedIn() || model.services==null || model.services.size() == 0) return;

        Set<String> storeIds = new HashSet<>();

        for (ServiceObject serviceObject : model.services) {
            if (serviceObject.hasInventory) {
                storeIds.add(serviceObject.id);
            }
        }

        if (storeIds.size() == 0) return;

        Json params = Json.param();
        params.p("storeIds", storeIds);

        Rest.request().baseUrl("api/market/app/").uri("inventory").params(params).response(InventoryResponse.class, (Response.Listener<InventoryResponse>) response -> {
            model.inventory = response.getInventory();
            if ( model.inventory!=null){

                List<MissingItem> missingItems = new ArrayList<>();

                for (MenuItemObject item:model.cart){
                    Integer inventoryItem = model.inventory.get(item.id);
                    if (inventoryItem == null || inventoryItem < 0) inventoryItem = 0;
                    if (inventoryItem < item.itemsOrdered){
                        MissingItem missingItem = new MissingItem(item.id,item.getLabel(),item.getImageUrl(),inventoryItem);
                        missingItems.add(missingItem);
                    }
                }

                model.displayMissingItems(activity, missingItems);
            }

            if (result!=null)
                result.onResult(response);

        }).post();

    }

    public void calculatePromiseTime(String storeId, Double lat, Double lon) {

        Json params = Json.param();
        params.p("storeId", storeId);
        params.p("appName", model.getAppName());
        Json coordinates = Json.param().p("lat", lat).p("lon", lon);
        params.o("coord", coordinates.toJson());

        Rest.request().baseUrl("api/market/app/").uri("calculatePromiseTime").params(params).response(CalculateEtaResponse.class, (Response.Listener<CalculateEtaResponse>) response -> {
            model.promiseTime = response.getPromiseTime();
            if (result!=null)
                result.onResult(model.promiseTime);
        }).error(errorResource -> {
            result.onError(null);
        }).post();

    }

    public void getActiveOrders() {

        Rest.request().uri("getActiveOrders/").response(OrderTrackingBean[].class, (Response.Listener<OrderTrackingBean[]>) response -> {

            if (model.activeOrders == null)
                model.activeOrders = new ArrayList<>();

            model.activeOrders.clear();

            if (response != null)
                Collections.addAll(model.activeOrders, response);

            processActiveOrders();

            result.onResult(response);
        }).post();


    }

    private void processActiveOrders() {

        for (Iterator it = model.homePageItems.iterator(); it.hasNext(); ) {
            if (it.next() instanceof OrderTrackingBean) {
                it.remove();
            }
        }
        model.homePageItems.addAll(0, model.activeOrders);

    }

    public void saveProfilePic(String image) {

        Rest.request().params(Json.param().p("profilePic", image).toJson()).showLoader().uri("updateProfilePic").response(ResponseBean.class, new Response.Listener<ResponseBean>() {
            @Override
            public void onResponse(ResponseBean response) {
                LocalStorage.instance().save(activity, "profilePic", response.description);
                result.onResult(response);
            }
        }).post();
    }


//    private boolean loadRecent;

    public void applyPromo(final String promo, final String serviceUid, final boolean isValidation) {

        if (promo.length() > 0) {
            Json params = Json.param();

            params.p("promo", promo);
            params.p("serviceUid", serviceUid);
            Rest.request().uri("validatePromoCode").showLoader("Verifying code...").params(params).response(PromoResultBean.class, new Response.Listener<PromoResultBean>() {
                @Override
                public void onResponse(PromoResultBean response) {
                    response.serviceUid = serviceUid;
                    response.promoCode = promo;
                    if (isValidation) {
                        model.promoPending = response;
                    } else {
                        model.promo = response;
                    }

                    result.onResult(response);
                }
            }).error(errorResource -> {
                result.onError(errorResource);
            }).post();
        }

    }

    public void loadMarketPlace() {

        Rest request = Rest.request();

        double lat = 0;
        double lon = 0;
        if (model.getValidLocation() != null) {
            lat = model.getValidLocation().latitude;
            lon = model.getValidLocation().longitude;
        }

        if (lat == 0 || lon == 0) {
            lat = 34; lon = 34;
        }

        JsonObject marketAppParam = new JsonObject();

        JsonObject localityParam = new JsonObject();

        JsonObject coordParam = new JsonObject();
        coordParam.addProperty("lat", lat);
        coordParam.addProperty("lon", lon);
        localityParam.add("coordinates", coordParam);
        marketAppParam.add("locality", localityParam);

        String methodName = "services";
        if (!Utilities.isLoggedIn()) {
            methodName = "public";
            marketAppParam.addProperty("appName", model.appContext.getResources().getString(R.string.application));
        }

        model.lastSyncDate = Calendar.getInstance().getTime();

        Log.d("SERVICES", "START");

        request.uri(methodName).baseUrl("api/market/app/").params(marketAppParam).response(ServicesBean.class, (Response.Listener<ServicesBean>) response -> new ServicesBuilder(result).execute(response)).error(errorResource -> {
            if (result != null)
                result.onError(errorResource);
        }).post();

    }

    static class ServicesBuilder extends AsyncTask<ServicesBean, Void, ServicesBean> {

        private final ServiceResult result;

        ServicesBuilder(ServiceResult result) {
            this.result = result;
        }

        protected ServicesBean doInBackground(ServicesBean... resp) {

            Log.d("SERVICES", "RECEIVCED");

            ServicesBean response = resp[0];

            ServicesModel model = ServicesModel.instance();

            model.loadRecentlyOrdered = false;

            model.defaultLocations.clear();
            model.noServicesMessage = response.noServicesMessage;
            model.homePageTitleMessage = response.homePageTitleMessage;
            model.feedbackText = response.feedbackText;
            model.productRecommendations = response.productRecommendations;
            model.turfs = response.turfs;

            deleteNonFoundServices(response.stores);

            model.services.clear();

            if (response.stores.size() > 0) {

                for (ServiceObject s : response.stores) {
                    if (s.mustUpdate) {
                        model.services.add(s);
                    } else {
                        model.services.add(s);
                    }
                }
            }

            model.initializeProductsMap(response.items);

            model.collectionGroups = response.collectionGroups;

            for (ServiceObject s : model.services) {

                s.categories = new ArrayList<>();


                if (model.turfs!=null){
                    if (s.areaIds != null){
                        if (s.turfs == null)
                            s.turfs = new ArrayList<>();
                        for (String id:s.areaIds){
                            MarketStoreTurf area = model.turfs.get(id);
                            if (area!=null){
                                s.turfs.add(area);
                            }
                        }
                    }
                }

                for (MarketStoreSection section : s.marketStoreSections) {
                    MenuCategoryObject category = section.collection;

                    if (category == null)
                        break;

                    category.sortOrder = section.sortOrder;

                    category.uidServiceProvider = s.id;
                    Map<String, MenuCategoryObject> subCategories = new HashMap<>();

                    if (Utilities.notNullAndEmpty(category.items)) {

                        Collections.sort(category.items);

                        for (MenuItemObject item : category.items) {
                            MarketStoreGroup group = model.collectionGroups.get(item.groupId);
                            MenuItemObject productItem = model.findMenuItemById(item.idProductString);

                            if (productItem != null) {
                                if (group != null) {
                                    productItem.service = s;
                                    productItem.serviceSlug = s.slug;
                                    productItem.sortOrder = item.sortOrder;

                                    MenuCategoryObject subCategory = subCategories.get(group.id);
                                    if (subCategory == null) {
                                        subCategory = new MenuCategoryObject();
                                        subCategory.items = new ArrayList<>();
                                    }

                                    subCategory.id = group.id;
                                    subCategory.label = group.label;
                                    subCategory.serviceSlug = s.slug;
                                    subCategory.sortOrder = group.getSortOrder();
                                    productItem.subcategory = subCategory.label;
/*
                                    item.category = category.label;
*/
                                    model.productsMap.put(productItem.id, productItem);
                                    subCategory.items.add(productItem);
                                    subCategories.put(subCategory.id, subCategory);
                                } else {
                                    model.productsMap.remove(item.id);
                                }
                            }
                        }

                    }
                    category.serviceSlug = s.slug;

                    category.subcategories = new ArrayList<>(subCategories.values());
                    Collections.sort(category.subcategories);
                    s.categories.add(category);
                    Collections.sort(s.categories);
                }
            }


            // remove all items where service is not received from server
            List<String> itemIds = new ArrayList<>();
            for (MenuItemObject item : model.productsMap.values()) {
                if (item.service == null) {
                    itemIds.add(item.id);
                }
            }
            for (String id : itemIds) {
                model.productsMap.remove(id);
            }

            if (Utilities.notNullAndEmpty(response.defaultLocations)) {
                model.defaultLocations.addAll(response.defaultLocations);
            }

            if (response.favorites != null) {
                model.favoriteIds.clear();
                model.favoriteIds.addAll(response.favorites);
            }

            model.initializeServices();

            model.homeSections = response.homeSections;

            model.buildHomePageSections(response.homeSections);


            HomePageCategoryBean bean = null;

            for (IListItem cat : model.homePageItems) {
                if (cat instanceof HomePageCategoryBean && HomePageCategoryBean.Type.NO_SERVICES == ((HomePageCategoryBean) cat).type) {
                    bean = (HomePageCategoryBean) cat;
                    break;
                }
            }

            if (model.services.size() == 0) {

                model.homePageItems.clear();

                if (bean == null) {
                    bean = new HomePageCategoryBean();
                    bean.type = HomePageCategoryBean.Type.NO_SERVICES;
                    bean.label = model.noServicesMessage;
                    bean.showActionText = false;
                    bean.items = new ArrayList<>();
                    model.homePageItems.add(0, bean);

                }
            } else {
                model.homePageItems.remove(bean);
            }

            model.favoritesTabsModified = true;
            model.loadCartFromStorage();


            Log.d("SERVICES", "COMPLETE");

            return resp[0];
        }

        protected void onPostExecute(ServicesBean response) {
            if (result != null){
                result.onResult(response.stores.size() > 0 || (response.recentlyOrdered != null && response.recentlyOrdered.size() > 0));
                new PrepareSearchDatabase().execute();
            }
            Log.d("SERVICES", "POST_COMPLETE");
        }

        void deleteNonFoundServices(List<ServiceObject> serviceObjects) {

            for (Iterator<ServiceObject> iterator = ServicesModel.instance().services.iterator(); iterator.hasNext(); ) {
                ServiceObject service = iterator.next();
                boolean foundService = false;
                for (ServiceObject so : serviceObjects) {
                    if (so.serviceProviderUid.equalsIgnoreCase(service.serviceProviderUid)) {
                        foundService = true;
                        break;
                    }
                }

                if (!foundService)
                    iterator.remove();
            }

        }
    }


    private static class PrepareSearchDatabase extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            ServicesModel.instance().prepareSearchDatabase();
            return null;
        }
    }


    public void checkCredibility(Context context) {

        if (!Utilities.isLoggedIn()) {
            return;
        }

        if (!model.credibilityChecked) {

            JsonObject params = new JsonObject();

            params.addProperty("pushToken", LocalStorage.instance().getString("token"));
            params.addProperty("appVersionCode", Utilities.getVersionCode());
            params.addProperty("os", "android");

            Rest.request().uri("checkCredibility/").params(params).response(CredibilityResponseBean.class, (Response.Listener<CredibilityResponseBean>) response -> {

                if (response.forceUpdate) {
                    String title = "Update required!";
                    String updateDescription = "This app requires an update from the store.";
                    if (response.forceUpdateDesc != null && !response.forceUpdateDesc.isEmpty()) {
                        updateDescription = response.forceUpdateDesc;
                    }

                    NokAlertDialog dialog = new NokAlertDialog(context).showCancelButton(false).setCancelable(false).setMessage(updateDescription).setTitle(title).setConfirmText("Update App").setConfirmClickListener(sweetAlertDialog -> {
                        final String appPackageName = context.getPackageName();
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, U.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, U.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    });

                    Utilities.clearData(context);

                    dialog.show();
                    return;
                }


                model.supportEmail = response.supportEmail;

                model.credibilityChecked = true;

                if (response.userStapCodeReserveBean != null) {
                    model.myLocations.clear();
                    model.myLocations.addAll(response.userStapCodeReserveBean);
                }

                model.creditCards = response.creditCards;
                model.creditCardsEnabled = response.creditCardsEnabled;
                model.faqUrl = response.faqUrl;
                model.termsUrl = response.termsUrl;
                model.supportPhoneNumber = response.supportPhoneNumber;

                if (response.amountInWallet != null) {
                    model.amountInWallet = response.amountInWallet;
                }

                model.tenantCurrency = response.tenantCurrency;
                model.tenantCurrencySymbol = response.tenantCurrencySymbol;
                model.exchangeRate = response.exchangeRate;

                model.paymentGateway = response.paymentGateway;

                Utilities.checkoutProdKey = response.checkoutProdKey;

                initPaymentSdk(context);

                if (result != null)
                    result.onResult(null);


            }).post();
        } else {
            if (result != null)
                result.onResult(null);
        }

    }


    public void initPaymentSdk(Context context) {
        if ("PAYSTACK".equalsIgnoreCase(model.paymentGateway) && !model.paymentGatewayInitialized) {
            model.paymentGatewayInitialized = true;
            PaystackSdk.initialize(context);
        }
    }


}
