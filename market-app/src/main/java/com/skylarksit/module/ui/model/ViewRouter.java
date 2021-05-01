package com.skylarksit.module.ui.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.StringRes;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.skylarksit.module.R;
import com.skylarksit.module.analytics.Segment;
import com.skylarksit.module.lib.Rest;
import com.skylarksit.module.libs.Json;
import com.skylarksit.module.libs.alertdialog.NokAlertDialog;
import com.skylarksit.module.libs.alertdialog.NokAlertDialogBuilder;
import com.skylarksit.module.libs.alertdialog.NokListAlertDialog;
import com.skylarksit.module.pojos.AddressObject;
import com.skylarksit.module.pojos.CollectionData;
import com.skylarksit.module.pojos.services.*;
import com.skylarksit.module.ui.activities.hyperlocal.*;
import com.skylarksit.module.ui.utils.IClickListener;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.skylarksit.module.utils.MyAppCompatActivity;
import com.skylarksit.module.utils.Utilities;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewRouter {

    private static ViewRouter mInstance = null;

    public static final int IC_PRODUCT = 91;
    public static final int IC_ADDRESS_PICKED = 92;

    private ViewRouter(){
    }

    private ServicesModel model = ServicesModel.instance();

    public static ViewRouter instance(){
        if(mInstance == null)
        {
            mInstance = new ViewRouter();
        }
        return mInstance;
    }

    public void openOrderDetails(final Context activity, final String orderUid){
        if (orderUid!=null && !orderUid.isEmpty()) {
            Rest.request().uri("getOrderDetails/" + orderUid).showLoader(activity.getString(R.string.loader_text)).response(PurchaseOrderObject.class, new Response.Listener<PurchaseOrderObject>() {
                @Override
                public void onResponse(PurchaseOrderObject response) {
                    ServicesModel.instance(activity).activePurchaseOrder = response;
                    Intent orderCompletedActivity = new Intent(activity, OrderCompletedActivity.class);
                    activity.startActivity(orderCompletedActivity);
                }
            }).post();
        }
    }

    public void goToServiceProvider(Activity activity, MenuItemObject menuItemObject, CollectionData collectionData) {
        model.goToItem = menuItemObject;
//        model.activeCategory = menuItemObject.category;
//        model.activeSubcategory = menuItemObject.subCategory;
        goToServiceProvider(activity,menuItemObject.serviceSlug,null,null,collectionData);
    }


    public void goToServiceProvider(Activity activity, String serviceName, String category, String promo, CollectionData collectionData) {
        goToServiceProviderFunc(activity,serviceName,category,promo,collectionData);
    }

    private void goToServiceProviderFunc(Activity activity, String serviceName,String category, String promo, CollectionData collectionData) {
        if (serviceName!=null){
            ServiceObject service = model.findProviderByName(serviceName);

            if (service!=null){

                if (service.getCategories()==null || service.getCategories().size()==0){
                    return;
                }

                if (category!=null&&!category.isEmpty()) {
                    model.goToCategory = category;
                    model.activeCategory = service.findCategoryByName(category);
                }

                if (service.isCourier()){

                    if (!model.isSameProvider(serviceName)){
                        model.displayClearCart(activity,null,false);
                        return;
                   }

                    goToBasket(activity,service,promo);
                }
                else{
                    Intent intent = new Intent(activity, FinalGridActivity.class);
                    intent.putExtra("activeService",service.slug);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (promo != null){
                        intent.putExtra("promoCode",promo);
                    }
                    activity.startActivity(intent);

                    activity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }

                if (collectionData != null)
                    Segment.INSTANCE.storeViewed(service,collectionData);

            }
        }
    }

    public void goToServiceGroup(Activity activity, String title, List<String> serviceGroup, CollectionData collectionData){
        Intent serviceActivity = new Intent(activity, ProvidersActivity.class);
        serviceActivity.putExtra("label",title);
        serviceActivity.putExtra("collectionData",new Gson().toJson(collectionData));
        serviceActivity.putExtra("services",serviceGroup.toArray());
        activity.startActivity(serviceActivity);

        Set<ServiceObject> providers = new HashSet<>();
        for (String serviceSlug:serviceGroup){
            ServiceObject serviceObject = model.findProviderByName(serviceSlug);
            if (serviceObject!=null){
                providers.add(serviceObject);
            }
        }
        Segment.INSTANCE.storesListViewed(providers, collectionData);
    }


    public void displayProduct(Activity activity, final HomePageItemBean homePageItemBean){
        MenuItemObject item = model.findMenuItemById(homePageItemBean.itemId);
        displayProduct(activity,item, homePageItemBean.getCollectionData(), true, true, false,false);
    }

    public void displayProduct(Activity activity, final MenuItemObject item, CollectionData collectionData, boolean isNew, boolean editable, boolean goToProvider, boolean fromProvider){

        if (item == null) {
            Utilities.Toast("Item not found...");
            return;
        }

        LocalStorage.instance().save(activity,"finishAllProductsPopup",false);

        if (isNew && item.hasCustomizations()){
            model.activeProduct = new MenuItemObject(item);
        }else
            model.activeProduct = item;

        Intent intent = new Intent(activity, ProductActivity.class);
        intent.putExtra("isEditable",editable);
        intent.putExtra("fromProvider",fromProvider);
        intent.putExtra("goToProvider",goToProvider);
        intent.putExtra("activeService",item.serviceSlug);
        intent.putExtra("collectionData",new Gson().toJson(collectionData));

        activity.startActivityForResult(intent,IC_PRODUCT);

        Segment.INSTANCE.productViewed(item,collectionData);

        activity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

    }

    public void displayBanner(Activity activity, HomePageItemBean item) {
        model.bannerItem = item;

        String confirmText = "Okay";
        if (item.actionLabel != null && !item.actionLabel.isEmpty()) {
            confirmText = item.actionLabel;
        }

        NokAlertDialog dialog = new NokAlertDialog(activity).setTitle(item.label).showCancelButton(false).setMessage(item.getDescription()).setConfirmText(confirmText);
        dialog.setConfirmClickListener(new NokAlertDialogBuilder.NokAlertClickListener() {
            @Override
            public void onClick(NokAlertDialog sweetAlertDialog) {
                dialog.dismiss();
                if (item.bannerType.equalsIgnoreCase("INVITE")) {
                    Rest.request().uri("getAppInviteLink/").params(Json.param().p("bannerUid",item.bannerUid).toJson()).showLoader().response(AppInviteResultBean.class, new Response.Listener<AppInviteResultBean>() {
                        @Override
                        public void onResponse(AppInviteResultBean response) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, response.inviteLink);
                            sendIntent.setType("text/plain");
                            activity.startActivityForResult(Intent.createChooser(sendIntent, "Share " + activity.getString(R.string.app_name)), 324);
                        }
                    }).post();

                }
            }
        });

        dialog.show();
    }

    public void changeYourLocation(Activity activity, boolean setAsCurrent) {

        if(Utilities.isLoggedIn()) {
            Intent intent = new Intent(activity, EddressPickerActivity.class);
            if (setAsCurrent){
                intent.putExtra("returnToMainView", true);
            }

            if (model.isCartUsed()) {
                model.displayClearCart(activity, object -> activity.startActivityForResult(intent, ViewRouter.IC_ADDRESS_PICKED), true);
            } else {
                activity.startActivityForResult(intent, ViewRouter.IC_ADDRESS_PICKED);
            }
        } else {
            Utilities.alertSignInFirst(activity);
        }

    }

    public void goToBasket(Context context, ServiceObject activeService, String promo) {

        if (context == null || activeService == null) return;

        if (activeService.isCourier()){

            if (!Utilities.isLoggedIn()){
                Utilities.alertSignInFirst(context);
                return;
            }

            Intent intent = new Intent(context, PickDropActivity.class);

            if (promo != null){
                intent.putExtra("promoCode",promo);
            }

            intent.putExtra("activeService",activeService.slug);
            context.startActivity(intent);
        }
        else{
            Intent intent = new Intent(context, BasketActivity.class);

            if (promo != null){
                intent.putExtra("promoCode",promo);
            }

            intent.putExtra("activeService",activeService.slug);
            context.startActivity(intent);
        }

        if (context instanceof Activity)
            ((Activity) context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

    }

    public void startCheckout(Activity context, ServiceObject service) {

        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra("orderUid", RandomStringUtils.randomAlphanumeric(30));
        intent.putExtra("activeService",service.slug);
        context.startActivity(intent);

    }

    public void showAddressPopup(Context context, @StringRes int text, String serviceSlug, boolean isPickup, boolean setAsCurrent, IClickListener clickListener){
        NokListAlertDialog<AddressObject> alertDialog = new NokListAlertDialog<>(context);
        alertDialog.setCancelable(false);
        alertDialog.setActionButton(R.string.add_location, object -> {
            Utilities.addNewEddress((MyAppCompatActivity) context, serviceSlug != null, isPickup, setAsCurrent, serviceSlug, false);
        });

        List<AddressObject> addressObjects = new ArrayList<>();
        ServiceObject service = serviceSlug != null ? model.servicesMap.get(serviceSlug) : null;
        if (service!=null && service.isCourier()){
            for (AddressObject addressObject:model.myLocations){
                if (service.deliversTo(addressObject.lat, addressObject.lon)){
                    addressObjects.add(addressObject);
                }
            }
        }
        else{
            addressObjects = new ArrayList<>(model.myLocations);
        }

        if (addressObjects.isEmpty()){
            Utilities.addNewEddress((MyAppCompatActivity) context, serviceSlug != null, isPickup, setAsCurrent, serviceSlug, false);
        }

        alertDialog.setItems(new ArrayList<>(addressObjects));
        alertDialog.setTitle(text);
        alertDialog.setItemClickListener(item -> {
            ServiceObject cartService = model.getCartService();
            if (cartService == null || cartService.deliversTo(item.lat, item.lon)){

                if (setAsCurrent){
                    model.setAsCurrentAddress = item;
                }

                if (clickListener!=null){
                    clickListener.click(item);
                }
            }
            else{
                NokAlertDialog displayCart = new NokAlertDialog(context, NokAlertDialog.WARNING)
                        .setTitle(context.getString(R.string.confirm_change_address))
                        .setConfirmText(context.getString(R.string.confirm))
                        .showCancelButton(true)
                        .setCancelText(context.getString(R.string.dismiss))
                        .setConfirmClickListener(sweetAlertDialog -> {

                            if (setAsCurrent){
                                model.setAsCurrentAddress = item;
                            }
                            if (clickListener!=null){
                                item.returnToMainView = true;
                                clickListener.click(item);
                            }
                        });
                displayCart.show();
            }

        });
        alertDialog.show();
    }
}
