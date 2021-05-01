package com.skylarksit.module.ui.model;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.skylarksit.module.R;
import com.skylarksit.module.analytics.Segment;
import com.skylarksit.module.lib.ErrorResource;
import com.skylarksit.module.libs.alertdialog.NokAlertDialog;
import com.skylarksit.module.libs.alertdialog.NokListAlertDialog;
import com.skylarksit.module.pojos.*;
import com.skylarksit.module.pojos.services.*;
import com.skylarksit.module.ui.activities.core.LoginActivity;
import com.skylarksit.module.ui.activities.hyperlocal.AddCreditCardActivity;
import com.skylarksit.module.ui.activities.hyperlocal.AddCreditCardMPGSActivity;
import com.skylarksit.module.ui.activities.hyperlocal.CheckoutActivity;
import com.skylarksit.module.ui.activities.hyperlocal.EddressPickerActivity;
import com.skylarksit.module.ui.lists.items.SearchResultItem;
import com.skylarksit.module.ui.utils.IClickListener;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.skylarksit.module.utils.Services;
import com.skylarksit.module.utils.Utilities;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.text.NumberFormat;
import java.util.*;

public class ServicesModel {

    private static ServicesModel mInstance = null;
    public int itemCount = 0;

    public List<AddressObject> myLocations = new ArrayList<>();
    public List<DefaultLocation> defaultLocations = new ArrayList<>();
    public List<String> feedbackText;
    public boolean closeMerchants = false;
    public boolean isClosed = false;

    public AddressObject pickupEddress;
    public AddressObject deliveryEddress;
    public String deliveryDate;
    public String notes;

    public PaymentOption paymentOption;

    public MenuItemObject activeProduct;

    public String paymentGateway;

    public List<String> storesFilters = new ArrayList<>();

    public Map<String, MarketStoreTurf> turfs;
    public Map<String, MenuItemObject> productsMap = new HashMap<>();
    public Map<String, ServiceObject> servicesMap = new HashMap<>();
    public Map<String, ServiceObject> servicesMapThirdPartyUid = new HashMap<>();

    public PromoResultBean promo;
    public PromoResultBean promoPending;
    public HomePageItemBean bannerItem;

    public Context appContext;
    public Activity currentActivity;
    public List<ServiceObject> favorites = new ArrayList<>();
    public List<String> favoriteIds = new ArrayList<>();

    public PurchaseOrderObject activePurchaseOrder;
    public List<ServiceObject> services = new ArrayList<>();

    public List<MenuItemObject> cart = new ArrayList<>();
    public List<CreditCardBean> creditCards = new ArrayList<>();

    public boolean loadRecentlyOrdered = true;

    public LatLng currentLocation;
    public LatLng lastKnownLocation;
    public AddressObject currentAddress;

    public DifferedLink differedLink;
    public MenuItemObject goToItem;
    public List<IListItem> homePageItems = new ArrayList<>();
    public List<IListItem> homePageItemsOriginal = new ArrayList<>();

    public Double amountDue = 0d;
    public Double totalPrice = 0d;
    public Double subtotalPrice = 0d;
    public Double discount;
    public String discountDecription;

    public double tip = 0d;

    public Double surcharge = 0D;
    //    public double vatAmount;
    public double deliveryVatAmount;

    public Double deductionFromWalletInServiceCurrency = 0d;

    public double deliveryPrice;
    public Double deliveryDistance;
    public String deliveryDistanceLabel;
    public boolean recalculateDeliveryPrice;

    public Double amountInWallet = 0D;
    public String tenantCurrency;
    public String tenantCurrencySymbol;
    public Boolean tenantCurrencyHideDecimals;
    public Integer exchangeRate;


    public String noServicesMessage = "No stores found";
    public String goToCategory;
    public MenuCategoryObject activeCategory;
    public String changeLocationText;
    public int changeLocationTooltip = -1;

    //Used to set current address in Main View
    public AddressObject setAsCurrentAddress;
    public List<OrderTrackingBean> activeOrders;

    public boolean favoritesHomeModified = false;
    public boolean favoritesViewModified = false;
    public boolean favoritesTabsModified = false;
    public boolean creditCardsEnabled;
    public String supportEmail;
    public String supportPhoneNumber;
    public String faqUrl;
    public String termsUrl;
    public boolean homePageAlignCenter;
    public boolean credibilityChecked;
    public boolean paymentGatewayInitialized = false;
    public MenuCategoryObject activeSubcategory;
    public String homePageTitleMessage = "Welcome!";
    public List<IListItem> favoritesProducts = new ArrayList<>();
    public String cartSessionId;
    public List<HomePageCategoryBean> homeSections;
    public boolean favoritesTooltip = false;
    public Map<String, Integer> inventory = new HashMap<>();
    public Map<String, MarketStoreGroup> collectionGroups;
    public boolean locationRequestIgnored = false;
    public boolean singleStore = false;
    public boolean show = false;
    public Map<String, List<String>> productRecommendations;

    public FeedReaderDbHelper dbHelper;

    public boolean calculatePromiseTime = true;
    public int promiseTime = 0;
    public Date lastSyncDate;

    public boolean hasUserDetails() {
        return !(Utilities.isEmpty(Utilities.getMyName()) || Utilities.isEmpty(LocalStorage.instance().getString("email")));
    }

    public String getItemCount() {
        if (itemCount == 1) return itemCount + " Item";
        return itemCount + " Items";
    }

    public boolean addToCart(Context context, MenuItemObject obj, Integer items, CollectionData collectionData, IClickListener<?> callBackListener) {

        if (!Utilities.isLoggedIn()) {
            Utilities.alertSignInFirst(context);
            return false;
        }

        if (!isCartUsed()) clearCart(false);

        if (!obj.service.isCourier() && (currentAddress == null || currentAddress.getUid() == null)) {
            final NokAlertDialog alert = new NokAlertDialog(context).setTitle(context.getString(R.string.title_select_address))
                    .setMessage(context.getString(R.string.select_address_before_processing))
                    .setCancelText(context.getString(R.string.not_now))
                    .setConfirmText(context.getString(R.string.select_an_address));

            alert.setConfirmClickListener(sweetAlertDialog -> {

                Intent intent = new Intent(context, EddressPickerActivity.class);
                intent.putExtra("returnToMainView", true);
                context.startActivity(intent);

            }).show();

            return false;
        }

        if (!isSameProvider(obj.serviceSlug)) {
            displayClearCart(context, callBackListener, false);
            return false;
        }

        if (cartSessionId == null) cartSessionId = UUID.randomUUID().toString();

        boolean isAdd = (items > obj.itemsOrdered);

        if (obj.isSingleSelection()) {
            clearCart(false);
            items = 1;
        }

        if (items > obj.getMaxQty()) {
            Utilities.Toast(context.getString(R.string.max_items_reached));
            return false;
        }
        if (items > obj.getStock()) {
            Utilities.Toast(context.getString(R.string.max_stock_reached));
            return false;
        }

        if (items < obj.minQty) {
            items = isAdd ? obj.minQty : 0;
        }

        if (items <= 0) items = 0;

        obj.setItems(items);

        if (items.equals(0)) {
            cart.remove(obj);
            Segment.INSTANCE.productRemoved(obj, collectionData);
        } else if (!cartContains(obj)) {
            cart.add(obj);
            Segment.INSTANCE.productAdded(obj, collectionData);
        }

        calcuateTotal();

        saveCartToStorage(context);

        return true;
    }

    private boolean cartContains(MenuItemObject obj) {

        return cart.contains(obj);

    }

    public Integer stockItem(ServiceObject serviceObject, MenuItemObject item) {
        if (item.outOfStock) return 0;
        if (!serviceObject.hasInventory || inventory == null || inventory.isEmpty()) return Integer.MAX_VALUE;
        Integer stock = inventory.get(item.id);
        if (stock == null) return 0;
        return stock;
    }

    public boolean hasStock(ServiceObject serviceObject, MenuItemObject item) {
        return stockItem(serviceObject, item) > 0;
    }

    public boolean isSameProvider(String serviceSlug) {
        return !isCartUsed() || Objects.equals(cart.get(0).serviceSlug, serviceSlug);
    }


    private NokAlertDialog displayCart;

    public void displayClearCart(Context context, final IClickListener<?> clickListener, boolean clearCart) {

        if (displayCart != null) displayCart.hide();

        displayCart = new NokAlertDialog(context, NokAlertDialog.WARNING)
                .setTitle(clearCart ? context.getString(R.string.title_clear_cart) : context.getString(R.string.title_start_new_cart))
                .setMessage(clearCart ? context.getString(R.string.clear_cart_description) : context.getString(R.string.clear_cart_if_different_merchant))
                .setConfirmText(context.getString(R.string.yes_clear_cart))
                .showCancelButton(true)
                .setCancelText(context.getString(R.string.dont_clear_cart))
                .setConfirmClickListener(sweetAlertDialog -> {
                    clearCart(true);
                    if (clickListener != null)
                        clickListener.click(null);
                });

        displayCart.show();

    }

    private ServicesModel() {
    }

    public void loadCartFromStorage() {

        cart.clear();

        String cartBean = LocalStorage.instance().getString("savedCart");
        if (Utilities.notEmpty(cartBean)) {
            try {
                CartBean bean = new Gson().fromJson(cartBean, CartBean.class);
                ServiceObject serviceObject = servicesMap.get(bean.getService());
                if (serviceObject == null && !singleStore) {
                    LocalStorage.instance().clear(appContext, "savedCart");
                    return;
                }

                for (CartBean.CartItemBean item : bean.getItems()) {
                    MenuItemObject product = productsMap.get(item.getId());
                    if (product != null) {
                        product.itemsOrdered = item.getQty();
                        cart.add(product);
                    }
                }
            } catch (Exception e) {
                LocalStorage.instance().clear(appContext, "savedCart");
            }
        }

    }

    public void saveCartToStorage(Context context) {

        if (cart.isEmpty()) {
            LocalStorage.instance().clear(context, "savedCart");
            return;
        }

        List<CartBean.CartItemBean> basketItems = new ArrayList<>();

        for (MenuItemObject item : cart) {
            if (!item.service.isCourier())
                basketItems.add(new CartBean.CartItemBean(item.id, item.itemsOrdered));
        }

        ServiceObject activeService = getCartService();

        if (activeService != null) {
            CartBean bean = new CartBean(activeService.slug, basketItems);
            String cartString = new Gson().toJson(bean);
            LocalStorage.instance().save(context, "savedCart", cartString);
        } else {
            LocalStorage.instance().clear(context, "savedCart");
        }


    }

    public static ServicesModel instance(Context context) {
        if (mInstance == null) {
            mInstance = new ServicesModel();
            mInstance.appContext = context;
        }
        return mInstance;
    }

    public static ServicesModel instance() {
        if (mInstance == null) {
            mInstance = new ServicesModel();
        }
        return mInstance;
    }

    public void clearCart(boolean reset) {

        if (reset) {
            pickupEddress = null;
            deliveryEddress = null;
            deliveryDate = null;
            notes = null;
            tip = 0D;
            paymentOption = null;
            surcharge = 0D;
            if (promo != null && promo.serviceUid != null) {
                promo = null;
            }
        }

        for (MenuItemObject menuItem : cart) {
            menuItem.itemsOrdered = 0;
            if (menuItem.customizationItems != null) {
                for (IProductCustomizationItem pg : menuItem.customizationItems) {
                    for (ProductCustomizationItem pi : ((ProductCustomizationGroup) pg).items) {
                        pi.isSelected = false;
                    }
                }
            }
        }

        LocalStorage.instance().clear(appContext, "savedCart");

        cart.clear();

        cartSessionId = null;
        deliveryDistance = null;
        deliveryDistanceLabel = null;
        deliveryPrice = 0;
        subtotalPrice = 0d;
        deductionFromWalletInServiceCurrency = 0d;
        totalPrice = 0d;
        itemCount = 0;

    }

    public List<MenuItemObject> findItemsWithTag(@NonNull ServiceObject serviceObject, @NonNull String tag, boolean includeOutOfStock) {
        List<MenuItemObject> result = new LinkedList<>();

        for (MenuCategoryObject category : serviceObject.categories) {
            for (MenuCategoryObject sub : category.subcategories) {
                for (MenuItemObject item : sub.items) {
                    if (item.recommendationTags != null && item.recommendationTags.contains(tag)) {
                        if (includeOutOfStock || hasStock(serviceObject, item))
                            result.add(item);
                    }
                }
            }
        }
        return result;
    }

    private final Set<String> brands = new HashSet<>();

    public void prepareSearchDatabase() {

        Log.d("SERVICE", "PREPARING_DB");

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("delete from " + FeedReaderContract.FeedEntry.TABLE_NAME);

        db.beginTransaction();
        try {
            for (ServiceObject serviceObject : services) {
                for (MenuCategoryObject category : serviceObject.categories) {
                    for (MenuCategoryObject sub : category.subcategories) {
                        for (MenuItemObject item : sub.items) {
                            String brandLabel = Utilities.replaceString((item.brandName != null) ? item.brandName.toLowerCase() : "");
                            String itemLabel = item.getSearchLabel();
                            ContentValues values = new ContentValues();
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ID, item.id);
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SKU, item.sku);
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SERVICE, item.serviceSlug);
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_LABEL, Utilities.replaceString(itemLabel));
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_BRAND, Utilities.replaceString(brandLabel));
                            if (Utilities.notEmpty(item.searchKeywords))
                                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KEYWORDS, Utilities.replaceString(item.searchKeywords));
                            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBCATEGORY, Utilities.replaceString(sub.getLabel().toLowerCase()));
                            brands.add(brandLabel);
                            db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();

            Log.d("SERVICE", "PREPARING_DB_COMPLETE");
        }

    }

    public List<MenuItemObject> searchDatabase(String searchString, String service) {

        List<MenuItemObject> items = new ArrayList<>();

        if (searchString == null) return items;

        if (service != null) service = DatabaseUtils.sqlEscapeString(service).replace("'", "");

        searchString = Utilities.replaceString(searchString);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selectionArgs = new StringBuilder(searchString.contains(" ") ? searchString : "'" + searchString + "*'");

        String sql = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_ID + " FROM " + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.TABLE_NAME + " MATCH ?";

        String[] criteria = new String[]{selectionArgs.toString()};

        if (Utilities.notEmpty(service)) {
            sql += " AND service = ?";
            criteria = new String[]{selectionArgs.toString(), service};
        }

        Cursor cursor = db.rawQuery(sql, criteria);

        while (cursor.moveToNext()) {
            String itemId = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_ID));
            items.add(productsMap.get(itemId));
        }
        cursor.close();

        // if there are no results look in the brand name of product
        if (items.size() == 0) {

            int threshold = 0;
            Set<String> selectors = new HashSet<>();

            while (selectors.size() == 0 && threshold <= 2) {
                LevenshteinDistance d = new LevenshteinDistance(threshold);
                for (String brand : brands) {
                    brand = Utilities.replaceString(brand);
                    String brandLabel = brand.length() <= searchString.length() ? brand : brand.substring(0, searchString.length());
                    int distance = d.apply(brandLabel, searchString);
                    if (distance >= 0) {
                        selectors.add(brand);
                    }
                }
                threshold++;
            }

            if (selectors.size() > 0) {
                Iterator<String> iterator = selectors.iterator();
                selectionArgs = new StringBuilder(iterator.next());
                while (iterator.hasNext()) {
                    selectionArgs.append(" OR ").append(iterator.next());
                }

                sql = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_ID + " FROM " + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_BRAND + " MATCH ?";
                criteria = new String[]{selectionArgs.toString()};

                if (Utilities.notEmpty(service)) {
                    sql += " AND service = ?";
                    criteria = new String[]{DatabaseUtils.sqlEscapeString(selectionArgs.toString()).replace("'", ""), DatabaseUtils.sqlEscapeString(service).replace("'", "")};
                }

                cursor = db.rawQuery(sql, criteria);

                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            String itemId = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_ID));
                            items.add(productsMap.get(itemId));
                        }
                    } catch (Exception ignored) {

                    }

                    cursor.close();
                }

            }

        }

        Collections.sort(items, (t0, t1) -> t1.recommendationLevel.compareTo(t0.recommendationLevel));

        return items;
    }

    private final List<ServiceObject> searchResult = new LinkedList<>();

    public List<ServiceObject> findServiceProviders(String searchString) {

        searchResult.clear();

        if (searchString == null || searchString.length() < 3) return searchResult;

        for (ServiceObject serviceObject : services) {
            if (serviceObject.getLabel().toLowerCase().contains(searchString) || serviceObject.tagsContain(searchString)) {
                searchResult.add(serviceObject);
            }
        }

        Collections.sort(searchResult, (s1, s2) -> s1.getLabel().compareTo(s2.getLabel()));

        return searchResult;
    }

    public void initializeServices() {

        servicesMap.clear();
        servicesMapThirdPartyUid.clear();

        for (ServiceObject s : services) {

            servicesMap.put(s.slug, s);
            servicesMapThirdPartyUid.put(s.thirdPartyUid, s);

            if (s.recentlyOrdered == null || s.recentlyOrdered.isEmpty()) continue;

            if (s.categories.size() > 0 && s.categories.get(0) != null && s.categories.get(0).getLabel().equalsIgnoreCase("Trending")) {
                s.categories.remove(0);
            }

            MenuCategoryObject recCat = new MenuCategoryObject();
            recCat.label = "Trending";
            recCat.subcategories = new ArrayList<>();
            recCat.uidServiceProvider = s.serviceProviderUid;
            recCat.imageUrl = s.favoritesImageUrl;
            s.categories.add(0, recCat);

            for (String productUid : s.recentlyOrdered) {

                MenuItemObject product = findMenuItemById(productUid);
                if (product != null && !product.isOutOfStock()) {

                    MenuCategoryObject subCategory = null;
                    for (MenuCategoryObject sub : recCat.subcategories) {
                        if (sub.label.equals(product.subcategory)) {
                            subCategory = sub;
                            break;
                        }
                    }
                    if (subCategory == null) {
                        subCategory = new MenuCategoryObject();
                        subCategory.label = product.subcategory;
//                        subCategory.description = product.subCategory.description;
                        subCategory.items = new ArrayList<>();
                        recCat.subcategories.add(subCategory);
                    }

                    subCategory.items.add(product);
                }

            }

            Collections.sort(s.categories);

        }

    }

    public void initializeProductsMap(List<MenuItemObject> items) {

        productsMap.clear();

        if (Utilities.notNullAndEmpty(items))

            for (MenuItemObject item : items) {
                if (Utilities.notEmpty(item.thirdPartyUid)) {
                    item.service = findProviderByThirdPartyUid(item.thirdPartyUid);
                    if (item.service != null) {
                        item.serviceSlug = item.service.slug;
                        productsMap.put(item.id, item);
                    }
                }
            }


//        if(Utilities.notNullAndEmpty(items)) {
//            for (MenuItemObject item : items) {
////            if (Utilities.notEmpty(item.thirdPartyUid)) {
////                item.service = findProviderByThirdPartyUid(item.thirdPartyUid);
////            if (item.service != null) {
////                item.serviceSlug = item.service.slug;
//                productsMap.put(item.id, item);
////                }
////            }
//            }
//        }
    }

    public void buildFavorites() {
        favorites.clear();
        for (String fav : favoriteIds) {
            MenuItemObject item = productsMap.get(fav);
            if (item != null) {
                addToFavorite(item);
            }
        }
    }

    private void addToFavorite(MenuItemObject item) {

        ServiceObject serviceBean = null;

        item.isFavorite = true;

        for (ServiceObject favoriteServiceBean : favorites) {
            if (favoriteServiceBean.slug.equals(item.serviceSlug)) {
                serviceBean = favoriteServiceBean;
                break;
            }
        }

        MenuCategoryObject favoriteCat = null;

        if (serviceBean == null) {
            serviceBean = new ServiceObject();
            serviceBean.categories = new ArrayList<>();
            serviceBean.slug = item.serviceSlug;
            serviceBean.name = item.service.getLabel();
            favorites.add(serviceBean);
        }

        if (serviceBean.categories.size() > 0) {
            favoriteCat = serviceBean.categories.get(0);
        }


        for (MenuCategoryObject fav : serviceBean.categories) {
            if (fav.label.equalsIgnoreCase(item.subcategory)) {
                favoriteCat = fav;
                break;
            }
        }

        if (favoriteCat == null) {
            favoriteCat = new MenuCategoryObject();
            favoriteCat.label = "";
            favoriteCat.items = new ArrayList<>();
            serviceBean.categories.add(favoriteCat);
        }

        favoriteCat.items.add(item);

    }


    public void updateService(Context context, ServiceObject serviceObject) {
        for (Iterator<ServiceObject> iterator = services.iterator(); iterator.hasNext(); ) {
            ServiceObject string = iterator.next();
            if (string.serviceProviderUid.equals(serviceObject.serviceProviderUid)) {
                iterator.remove();
                break;
            }
        }
        String json = new Gson().toJson(serviceObject);
        LocalStorage.instance().save(context, "cachedService-" + serviceObject.serviceProviderUid, json);
        services.add(serviceObject);
    }

    public void clearItems(MenuItemObject menuItemObject) {
        menuItemObject.itemsOrdered = 0;
        cart.remove(menuItemObject);
        calcuateTotal();
    }

    public void applyPromo(final Activity activity, String promoCode, String serviceUid, boolean isValidation) {

        Services.call(activity).result(new Services.ServiceResult<PromoResultBean>() {
            @Override
            public void onResult(PromoResultBean result) {
                NokAlertDialog sDialog = new NokAlertDialog(activity, NokAlertDialog.SUCCESS)
                        .setTitle("Promo code redeemed!")
                        .setMessage(result.description)
                        .setConfirmText("Thanks!")
                        .showCancelButton(false)
                        .setConfirmClickListener(new NokAlertDialog.NokAlertClickListener() {
                            @Override
                            public void onClick(NokAlertDialog sDialog) {
                                sDialog.dismiss();
                            }
                        });
                sDialog.show();
            }

            @Override
            public void onError(ErrorResource errorResource) {
                NokAlertDialog sDialog = new NokAlertDialog(activity, NokAlertDialog.ERROR)
                        .setTitle("Invalid Promo Code!")
                        .setMessage(errorResource.description)
                        .setConfirmText("Close")
                        .showCancelButton(false)
                        .setConfirmClickListener(new NokAlertDialog.NokAlertClickListener() {
                            @Override
                            public void onClick(NokAlertDialog sDialog) {
                                sDialog.dismiss();
                            }
                        });
                sDialog.show();
            }
        }).applyPromo(promoCode, serviceUid, isValidation);

    }

    public ServiceObject findProviderByName(String slug) {

        if (slug == null) return null;

        ServiceObject so = servicesMap.get(slug);
        if (so != null) return so;

        for (ServiceObject s : services) {
            if ((s.slug != null && s.slug.equals(slug)) || (s.name != null && s.name.equalsIgnoreCase(slug))) {
                return s;
            }
        }
        return null;
    }

    public ServiceObject findProviderById(String id) {
        for (ServiceObject s : services) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        return null;
    }

    public MenuCategoryObject findCategoryById(String id) {
        for (ServiceObject s : services) {
            for (MenuCategoryObject categoryObject : s.getCategories()) {
                if (categoryObject.id.equals(id)) {
                    return categoryObject;
                }
            }
        }
        return null;
    }

    public ServiceObject findProviderByThirdPartyUid(String thirdPartyUid) {
        ServiceObject so = servicesMapThirdPartyUid.get(thirdPartyUid);
        if (so != null) return so;

        for (ServiceObject s : services) {
            if (s.thirdPartyUid.equals(thirdPartyUid)) {
                return s;
            }
        }
        return null;
    }


    public MenuItemObject findMenuItemById(String id) {
        return productsMap.get(id);
    }

    public MenuCategoryObject findMenuCategoryById(String id) {

        for (ServiceObject serviceObject : services) {

            for (MenuCategoryObject category : serviceObject.categories) {
                if (category.id.equalsIgnoreCase(id)) {
                    return category;
                }
            }
        }
        return null;
    }

    public String getCategoryImageFromHomePageItems(String category) {

        if (category == null) return null;

        for (IListItem item : homePageItems) {
            if (item instanceof HomePageCategoryBean) {
                HomePageCategoryBean homePageCategoryBean = (HomePageCategoryBean) item;
                if (homePageCategoryBean.type == HomePageCategoryBean.Type.BLOCKS) {
                    for (HomePageItemBean itemBean : homePageCategoryBean.items) {
                        if (category.equals(itemBean.serviceCategory)) {
                            return itemBean.imageUrl;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean hasVariablePrice() {
        for (MenuItemObject item : cart) {
            if (item.unit != null && !item.unit.equalsIgnoreCase("unit")) {
                return true;
            }
        }
        return false;
    }

    public boolean isCartUsed() {
        return cart.size() > 0;
    }

    public boolean servicesLoaded() {
        return services != null && services.size() > 0;
    }

    public String getSubTotal() {
        if (isCartUsed()) {
            return Utilities.getCurrencyFormatter(cart.get(0).service.currency).format(subtotalPrice);
        }
        return "";
    }

    public String getTotal() {
        if (isCartUsed()) {
            return Utilities.getCurrencyFormatter(cart.get(0).service.currency).format(totalPrice);
        }
        return "";
    }

    public ServiceObject getCartService() {
        if (!isCartUsed()) return null;
        return servicesMap.get(cart.get(0).serviceSlug);
    }

    public AddressObject findAddress(String addressCode) {
        for (AddressObject loc : myLocations) {
            if (loc.code.equals(addressCode))
                return loc;
        }
        return null;
    }

    public String getEtaText() {

        if (Utilities.isMarketplace() || services == null || services.size() == 0) {
            return null;
        }

        ServiceObject serviceObject = services.get(0);
        return serviceObject.getEtaText();

    }

    public void calculateBasket(ServiceObject activeService) {

        this.subtotalPrice = calculateSubtotal();

        if (!hasDynamicPricing(activeService)) {
            this.deliveryPrice = activeService.getDeliveryCharge();
        }

        this.surcharge = calculateSurcharge(activeService);

        this.deliveryVatAmount = calculateDeliveryVat(activeService);

        // delivery discount is applied on this value
        Double totalDeliveryFee = this.deliveryPrice + this.surcharge + this.deliveryVatAmount;

        this.totalPrice = subtotalPrice + totalDeliveryFee + tip;

        this.discount = this.calculateDiscount(activeService, totalPrice, subtotalPrice, totalDeliveryFee);

        if (this.hasDiscountOnSubtotal(activeService) || this.hasDiscountOnDelivery(activeService)) {
            this.totalPrice -= discount;
            this.amountDue = this.totalPrice;
        } else {
            this.amountDue = this.totalPrice - discount;
        }

        this.deductionFromWalletInServiceCurrency = calculateDeductionFromWallet(activeService);

        this.amountDue -= this.deductionFromWalletInServiceCurrency;

        if (amountDue < 0) amountDue = 0d;

    }

    private Double calculateSubtotal() {
        subtotalPrice = 0d;
        itemCount = 0;

        for (MenuItemObject menuItem : cart) {
            itemCount += menuItem.itemsOrdered;
            subtotalPrice += menuItem.getTotalPrice() * menuItem.itemsOrdered;
        }
        return subtotalPrice;
    }

    private Double calculateDiscount(ServiceObject activeService, Double total, Double subtotal, Double totalDeliveryFee) {

        Double discountValue = 0d;
        if (promo != null) {

            if (promo.appliedOn == null) {
                promo.appliedOn = PromocodeApplication.TOTAL;
            }

            if ("percentage".equalsIgnoreCase(activeService.discountType) || "percentage".equalsIgnoreCase(promo.discountType)) {
                if (promo.discountValue > 0) {
                    switch (promo.appliedOn) {
                        case TOTAL:
                            discountValue = discountWithValue(promo.discountValue, total);
                            discountDecription = "-" + promo.discountValue + "% from total";
                            break;
                        case DELIVERY:
                            discountValue = discountWithValue(promo.discountValue, totalDeliveryFee);
                            discountDecription = "-" + promo.discountValue + "% from delivery";
                            break;
                        case SUBTOTAL:
                            discountValue = discountWithValue(promo.discountValue, subtotal);
                            discountDecription = "-" + promo.discountValue + "% from subtotal";
                            break;
                        default:
                            break;
                    }
                } else {
                    discountValue = activeService.discountValue * subtotal / 100.0;
                    discountDecription = "from subtotal";
                }

            } else if ("value".equalsIgnoreCase(activeService.discountType) || "amount".equalsIgnoreCase(promo.discountType)) {
                NumberFormat formatter = Utilities.getCurrencyFormatter(activeService.currency);

                if (promo.discountValue > 0) {
                    discountValue = promo.discountValue;

                    switch (promo.appliedOn) {
                        case TOTAL:
                            discountDecription = "from total";
                            break;
                        case DELIVERY:
                            discountDecription = "from delivery";
                            break;
                        case SUBTOTAL:
                            discountDecription = "from subtotal";
                            break;
                        default:
                            break;
                    }

                } else {
                    discountValue = activeService.discountValue;
                    discountDecription = "from subtotal";
                }
            }
        }

        return discountValue;
    }

    private Double discountWithValue(Double discountValue, Double onAmount) {
        Double discount = discountValue * onAmount / 100.0;
        return discount < onAmount ? discount : 0;
    }


    private Double calculateSurcharge(ServiceObject activeService) {
        Double minCharge = activeService.minimumCharge;
        Double minChargeFee = activeService.minimumChargeFee;

        if (minCharge != null && minCharge >= 0) {

            if (subtotalPrice < minCharge) {
                if (minChargeFee != null && minChargeFee > 0) {
                    return activeService.minimumChargeFee;
                }
            }
        }
        return 0d;
    }


    private Double calculateDeliveryVat(ServiceObject activeService) {
        if (activeService.getDeliveryVat() != null && activeService.getDeliveryVat() > 0) {
            return (deliveryPrice + surcharge) * activeService.getDeliveryVat() / 100;
        }
        return 0d;
    }

    private Double calculateDeductionFromWallet(ServiceObject activeService) {
        Double amountInServiceRate = 0d;
        if (Utilities.notEmpty(tenantCurrency)) {
            if (tenantCurrency.equalsIgnoreCase(activeService.currency.iso)) {
                amountInServiceRate = amountInWallet;
            } else {

                if (exchangeRate == null || exchangeRate == 0)
                    exchangeRate = 1;

                Double amountInDollars = amountInWallet / exchangeRate;
                amountInServiceRate = amountInDollars * activeService.getExchangeRate();
            }
        }

        return amountInServiceRate <= this.amountDue ? amountInServiceRate : this.amountDue;
    }


    public void calcuateTotal() {

        totalPrice = 0d;
        itemCount = 0;
        calculateSubtotal();
    }

    public boolean hasDiscountOnSubtotal(ServiceObject activeService) {
        return activeService.discountType != null && !activeService.discountType.isEmpty()
                || (promo != null && promo.appliedOn == PromocodeApplication.SUBTOTAL);
    }

    public boolean hasDiscountOnDelivery(ServiceObject activeService) {
        return promo != null && promo.appliedOn == PromocodeApplication.DELIVERY;
    }

    public boolean hasDiscountOnTotal(ServiceObject activeService) {
        return promo != null && promo.appliedOn == PromocodeApplication.TOTAL;
    }


    public boolean hasDynamicPricing(ServiceObject serviceObject) {

        if (serviceObject == null) return false;

        if (serviceObject.getDeliveryCharge() != null && serviceObject.getDeliveryCharge() > 0) {
            return false;
        }

        return !Utilities.isEmpty(serviceObject.effectiveDeliveryPricingType) && serviceObject.effectiveDeliveryPricingType.equalsIgnoreCase("Distance");
    }

    public LatLng getValidLocation() {

        if (currentAddress != null && currentAddress.lat != null && currentAddress.lon != null) {
            return currentAddress.getLatLng();
        }
        return currentLocation;
    }

    public void buildFilters() {

        Set<String> set = new HashSet<>();

        storesFilters.clear();

        for (ServiceObject serviceObject : services) {
            set.add(serviceObject.serviceName);
        }

        storesFilters.addAll(set);

    }


    public boolean homePageOpenNowFilter = false;

    public void refreshHomePageItems() {

        homePageItems.clear();

        if (homePageOpenNowFilter) {
            List<IListItem> filtered = new ArrayList<>();
            for (IListItem i : homePageItemsOriginal) {

                HomePageCategoryBean b = (HomePageCategoryBean) i;

                HomePageCategoryBean cat = new HomePageCategoryBean();
                cat.type = b.type;
                cat.label = b.label;
                cat.imageUrl = b.imageUrl;
                cat.items = new ArrayList<>();

                for (HomePageItemBean bi : b.items) {
                    if (bi.serviceSlug != null) {
                        ServiceObject so = findProviderByName(bi.serviceSlug);
                        if (so != null && so.isClosed()) {
                            cat.items.add(bi);
                        }
                    }
                }

                if (cat.items.size() > 0) {
                    filtered.add(cat);
                }
            }

            homePageItems.addAll(filtered);

        } else {

            homePageItems.addAll(homePageItemsOriginal);
        }

    }

    public void addCreditCardActivity(final Activity activity) {

        if (!Utilities.notEmpty(LocalStorage.instance().getString("email"))) {
            activity.startActivity(new Intent(activity, LoginActivity.class));
            return;
        }

        boolean showCharge = true;

        switch (paymentGateway.toUpperCase()) {
            case "PAYSTACK":
                showCharge = false;
        }

        if (showCharge) {
            final NokAlertDialog sDialog = new NokAlertDialog(activity, NokAlertDialog.WARNING);
            sDialog.setMessage("You will be charged $0.01 for verification purposes.");
            sDialog.setConfirmText("Continue");
            sDialog.setCancelText("Cancel");
            sDialog.setTitle("Add Credit Card").setConfirmClickListener(sweetAlertDialog -> startCreditCardActivity(activity)).show();
        } else startCreditCardActivity(activity);
    }

    private void startCreditCardActivity(Activity activity) {
        Intent intent = new Intent(activity, AddCreditCardActivity.class);
        if (!Utilities.isEmpty(paymentGateway)) {
            switch (paymentGateway.toUpperCase()) {
                case "MPGS":
                case "BOB":
                case "AREEBA":
                    intent = new Intent(activity, AddCreditCardMPGSActivity.class);
            }
        }
        activity.startActivity(intent);
    }

    public void buildHomePageSections(List<HomePageCategoryBean> homeSections) {

        homePageItems.clear();
        this.homeSections = homeSections;
        if (homeSections != null) {
            for (HomePageCategoryBean h : homeSections) {
                for (IListItem hpi : h.getItems()) {
                    HomePageItemBean homePageItemBean = (HomePageItemBean) hpi;
                    if (homePageItemBean.type.equalsIgnoreCase("PRODUCT")) {
                        homePageItemBean.menuItemObject = findMenuItemById(homePageItemBean.itemId);

                        if (homePageItemBean.menuItemObject == null || homePageItemBean.menuItemObject.service == null) {
                            continue;
                        }

                        homePageItemBean.service = findProviderByName(homePageItemBean.menuItemObject.service.slug);
                        if (homePageItemBean.service != null)
                            homePageItemBean.serviceSlug = homePageItemBean.service.slug;
                    } else if (homePageItemBean.type.equalsIgnoreCase("STORE")) {
                        homePageItemBean.service = findProviderById(homePageItemBean.itemId);
                        if (homePageItemBean.service == null) {
                            continue;
                        }
                        homePageItemBean.serviceSlug = homePageItemBean.service.slug;

                        if (h.type == HomePageCategoryBean.Type.STORES) {
                            homePageItemBean.imageUrl = homePageItemBean.service.backgroundImageUrl;
                            homePageItemBean.label = homePageItemBean.service.name;
                        }

                    } else if (homePageItemBean.type.equalsIgnoreCase("COLLECTION")) {
                        homePageItemBean.categoryObject = findMenuCategoryById(homePageItemBean.itemId);
                        if (homePageItemBean.categoryObject == null) {
                            continue;
                        }
                        homePageItemBean.serviceCategory = homePageItemBean.categoryObject.label;
                        homePageItemBean.service = findProviderById(homePageItemBean.categoryObject.uidServiceProvider);
                        homePageItemBean.serviceSlug = homePageItemBean.service.slug;

                    }

                }

                if (h.type == HomePageCategoryBean.Type.PRODUCTS) {
                    List<MenuItemObject> is = new ArrayList<>();
                    for (IListItem b : h.getItems()) {
                        HomePageItemBean item = (HomePageItemBean) b;
                        MenuItemObject itemObject = findMenuItemById(item.itemId);
                        if (itemObject != null) {
                            is.add(itemObject);
                        }
                    }
                    h.listItems.clear();
                    h.listItems.addAll(is);
                    if (h.label.contains("Favorites")) {
                        favoritesProducts.clear();
                        favoritesProducts.addAll(h.listItems);
                    }
                } else if (h.type == HomePageCategoryBean.Type.COLLECTIONS) {
                    List<IListItem> newIListItems = new ArrayList<>();
                    for (IListItem iListItem : h.getItems()) {
                        HomePageItemBean bean = (HomePageItemBean) iListItem;
                        if (bean.service != null) {
                            newIListItems.add(bean);
                        }
                    }
                    h.getItems().clear();
                    h.getItems().addAll(newIListItems);

                }
                if (h.getItems().size() > 0)
                    homePageItems.add(h);

                removeNullServiceSlugItems(h);
            }
        }

    }

    private void removeNullServiceSlugItems(HomePageCategoryBean categoryBean) {
        /*int size = categoryBean.getItems().size();
        for (int i = 0; i < size; i++) {
            IListItem item = categoryBean.getItems().get(i);
            if (item instanceof HomePageItemBean) {
                HomePageItemBean homePageItemBean = (HomePageItemBean) item;
                if ("COLLECTION".equals(homePageItemBean.type) && homePageItemBean.serviceSlug == null) {
                    homeSections.get(homeSections.indexOf(categoryBean)).getItems().remove(item);
                    size--;
                }
            }
        }*/
        List<IListItem> newList = new ArrayList<>();
        for (IListItem item : categoryBean.getItems()) {
            if (item instanceof HomePageItemBean) {
                HomePageItemBean homePageItemBean = (HomePageItemBean) item;
                if ("COLLECTION".equals(homePageItemBean.type) && homePageItemBean.serviceSlug != null) {
                    newList.add(item);
                }
            }
        }
        categoryBean.listItems.clear();
        categoryBean.listItems.addAll(newList);

    }

    public void displayMissingItems(Activity activity, List<MissingItem> missingItems) {

        if (missingItems == null || missingItems.isEmpty()) return;

        List<IListItem> listItems = new ArrayList<>();
        for (MissingItem item : missingItems) {

            SearchResultItem searchResultItem = new SearchResultItem();
            searchResultItem.label = item.getLabel();
            searchResultItem.description = "Available Quantity: " + item.getQuantityOnHand();
            searchResultItem.imageUrl = item.getImageUrl();

            MenuItemObject menuItem = findMenuItemById(item.getId());
            if (menuItem != null) {
                inventory.put(menuItem.id, item.getQuantityOnHand());
                if (item.getQuantityOnHand() < menuItem.itemsOrdered)
                    addToCart(activity, menuItem, item.getQuantityOnHand(), new CollectionData("missing_items", "Missing Items", CollectionData.Type.BASKET), null);
            }
            listItems.add(searchResultItem);
        }

        NokListAlertDialog<SearchResultItem> alertDialog = new NokListAlertDialog<>(activity);
        alertDialog.setCancelable(false);

        if (activity instanceof CheckoutActivity)
            alertDialog.onHideListener = object -> activity.onBackPressed();

        alertDialog.setItems(listItems);
        alertDialog.setTitle("The following items are not available in this store! We've adjusted your basket accordingly.");
        alertDialog.show();

    }


    public AddressObject findClosestLocation(Location location) {

        float closestDistance = Float.MAX_VALUE;
        AddressObject closestAddress = null;

        for (AddressObject addressObject : myLocations) {
            float dist = addressObject.getLocation().distanceTo(location);
            if (dist <= 100 && dist < closestDistance) {
                closestAddress = addressObject;
                closestDistance = dist;
            }
        }

        return closestAddress;

    }

    public boolean hasInventory() {
        return singleStore && !services.isEmpty() && services.get(0).hasInventory;
    }

    public ServiceObject getSingleStoreService() {
        if (singleStore && !services.isEmpty()) {
            return services.get(0);
        }
        return null;
    }

    public boolean hasSavedCart() {
        String saveCart = LocalStorage.instance().getString("savedCart");
        return Utilities.notEmpty(saveCart);
    }

    public String getAppName() {
        return appContext.getString(R.string.application);
    }
}
