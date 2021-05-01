package com.skylarksit.module.pojos.services;

import android.content.Context;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.AddressObject;
import com.skylarksit.module.pojos.CategoryViewType;
import com.skylarksit.module.pojos.MarketStoreSection;
import com.skylarksit.module.pojos.MarketStoreTurf;
import com.skylarksit.module.pojos.PaymentOption;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.ui.utils.HFRecyclerView;
import com.skylarksit.module.utils.Utilities;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.maps.android.PolyUtil;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceObject implements IListItem {

    public String id;
    public String serviceProviderUid;
    public String thirdPartyUid;
    public String serviceName;
    public String imageUrl;
    public String thumbnailUrl;
    public String name;
    public String slug;
    public Boolean hideImages = false;

    public List<MarketStoreTurf> turfs;

    public List<String> recentlyOrdered;
    public boolean hasInventory = false;

    public Long version;

    public String countryIso;
    public String defaultEtaText;
    public String deliveryFeeLabel;
    public String tipOptions;
    public Integer position;
    public String eddressCode;
    public String specialNote;
    public Double minimumCharge;
    public Double minimumChargeFee;
    public Double deliveryCharge;
    public Double deliveryVat;
    public boolean isClosed = false;
    public boolean showEta = true;

    public PriceTag priceTag;

    public List<String> storeTags;

    public Double vat;
    public List<PaymentOption> paymentOptions = new ArrayList<>();
    public String favoritesImageUrl;

    public String effectiveDeliveryPricingType;
    public List<String> areaIds;
    public Integer defaultEtaMins;
    public boolean showItemSuggestion = true;

    public Integer getPos() {
        if (position == null) return 0;
        return position;
    }

    public Currency currency;

    public Double lat;
    public Double lon;

    public String backgroundImageUrl;
    public String serviceDescription;
    public String notesPromptText;
    public String recipientTextPrompt;
    public String openingHours;

    public String discountType;
    public Double discountValue;

    public String viewType = "list";
    public boolean isTimeRequired = true;
    public Boolean showRecipientText = false;
    public Boolean hasPickup = false;
    public Boolean hasDelivery = false;
    public boolean hasTimeSchedule = false;
    public Boolean hasNotes = false;
    public Boolean allowMultiSelect = false;
    public Boolean hasPricing = false;
    public CategoryViewType categoryViewType = CategoryViewType.SWIPE;
    public List<MenuCategoryObject> categories;

    public Context context;

    public MenuCategoryObject findCategoryByName(String name) {
        for (MenuCategoryObject c : categories) {
            if (name.equalsIgnoreCase(c.getLabel())) {
                return c;
            }
        }
        return null;
    }

    public List<MarketStoreSection> marketStoreSections;

    public List<ServiceScheduleBean> schedule;
    public boolean mustUpdate;

    public String getCurrencySymbol() {
        if (currency.symbol == null) currency.symbol = "$";

        return currency.symbol;
    }

    public Integer getExchangeRate() {
        if (currency.exchangeRate == null) {
            return 1;
        }
        return currency.exchangeRate;
    }

    public void setExchangeRate(Integer exchangeRate) {
        this.currency.exchangeRate = exchangeRate;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getDeliveryCharge() {
        if (deliveryCharge == null) {
            return 0.0;
        }
        return deliveryCharge;
    }

    public Double getDeliveryVat() {
        return deliveryVat;
    }

    public void setDeliveryVat(Double deliveryVat) {
        this.deliveryVat = deliveryVat;
    }

    public String getEddressCode() {
        return eddressCode;
    }

    public void setEddressCode(String eddressCode) {
        this.eddressCode = eddressCode;
    }

    public boolean deliversTo(Double lat, Double lon) {

        if (turfs == null || turfs.size() == 0 || lat == null || lat == 0 || lon == null || lon == 0) return true;

        for (MarketStoreTurf turf:turfs){
            boolean result = PolyUtil.containsLocation(new LatLng(lat, lon), Objects.requireNonNull(turf.getTurfPoly()), true);
            if (result) return true;
        }

        return false;

    }

    @Expose(serialize = false)
    private AddressObject addressObject;

    public String getPriceTagLabel() {
        if (priceTag != null) {
            StringBuilder dollar = new StringBuilder();
            for (int i = 0; i <= priceTag.getValue(); i++) {
                dollar.append("$");
            }
            return dollar.toString();
        }
        return "";
    }

    public String getEtaText() {

        if (isClosed()) {
            String opening = getNextOpening();
            if (opening != null) {
                return opening;
            } else {
                return "";
            }
        }

        if (defaultEtaText != null) {
            return defaultEtaText;
        }
        return "ASAP";
    }

    public boolean isClosed() {
        if (ServicesModel.instance().closeMerchants || ServicesModel.instance().isClosed)
            return true;

        return isClosed(LocalDateTime.now());
    }

    private static LocalTime morning = new LocalTime(5, 0, 0);

    private boolean isClosed(LocalDateTime nowDateTime) {

        if (isClosed) return true;

        Integer day = nowDateTime.getDayOfWeek();
        Integer yesterday = nowDateTime.minusDays(1).getDayOfWeek();

        if (schedule == null) return true;

        ServiceScheduleBean bean = getSchedule(day);
        if (bean == null) return true;

        LocalTime startTime = Utilities.timeFormatJoda.parseLocalTime(bean.timeOpenString);
        LocalTime endTime = Utilities.timeFormatJoda.parseLocalTime(bean.timeClosedString);
        boolean pastMidnight = endTime.isBefore(morning);

        LocalDateTime startDate = startTime.toDateTimeToday().toLocalDateTime();
        LocalDateTime endDate = pastMidnight ? endTime.toDateTimeToday().plusDays(1).toLocalDateTime() : endTime.toDateTimeToday().toLocalDateTime();

        ServiceScheduleBean yesterdayBean = getSchedule(yesterday);
        if (yesterdayBean != null) {
            LocalTime endTimeYesterday = Utilities.timeFormatJoda.parseLocalTime(yesterdayBean.timeClosedString);
            boolean yesterdayPastMidnight = endTimeYesterday.isBefore(morning);
            if (yesterdayPastMidnight) {
                LocalDateTime endDateYesterday = endTimeYesterday.toDateTimeToday().toLocalDateTime();
                if (nowDateTime.isBefore(endDateYesterday)) {
                    return false;
                }
            }
        }

        return !(nowDateTime.isAfter(startDate) && nowDateTime.isBefore(endDate));

    }

    private ServiceScheduleBean getSchedule(Integer day) {
        for (ServiceScheduleBean bean : schedule) {
            if (bean.dayOfWeek.equals(day)) {
                return bean;
            }
        }
        return null;
    }

    private String getNextOpening() {

        if (schedule == null) return null;

        LocalDateTime nowDateTime = LocalDateTime.now();

        LocalDateTime tomorrow = nowDateTime.plusDays(1);
        LocalDateTime end = nowDateTime.plusDays(7);

        for (LocalDateTime date = nowDateTime; date.isBefore(end); date = date.plusDays(1)) {
            Integer dayOfWeek = date.getDayOfWeek();
            for (ServiceScheduleBean s : schedule) {
                if (s.isActive && s.dayOfWeek.equals(dayOfWeek)) {

                    boolean isToday = date.getDayOfWeek() == nowDateTime.getDayOfWeek();
                    boolean isTomorrow = date.getDayOfWeek() == tomorrow.getDayOfWeek();

                    LocalTime startTime = Utilities.timeFormatJoda.parseLocalTime(s.timeOpenString);
                    LocalDateTime startDate = startTime.toDateTimeToday().toLocalDateTime();

                    if (isToday) {
                        if (nowDateTime.isBefore(startDate)) {
                            String text = "Closed.";
                            if (hasTimeSchedule) {
                                text += " Pre-order for " + s.timeOpenString;
                            } else {
                                text += " Opens at " + s.timeOpenString;
                            }
                            return text;
                        }

                    } else if (isTomorrow) {
                        String text = "Closed.";
                        if (hasTimeSchedule) {
                            text += " Pre-order for " + s.timeOpenString + " tomorrow";
                        } else {
                            text += " Opens at " + s.timeOpenString + " tomorrow";
                        }
                        return text;
                    } else {
                        return "Closed till " + date.toString(Utilities.dateFormatJoda) + " " + s.timeOpenString;
                    }
                }
            }
        }

        return null;
    }

    public String getOpeningTime() {

        if (schedule == null) return null;

        LocalDateTime nowDateTime = LocalDateTime.now();

        LocalDateTime tomorrow = nowDateTime.plusDays(1);
        LocalDateTime end = nowDateTime.plusDays(7);

        for (LocalDateTime date = nowDateTime; date.isBefore(end); date = date.plusDays(1)) {
            Integer dayOfWeek = date.getDayOfWeek();
            for (ServiceScheduleBean s : schedule) {
                if (s.isActive && s.dayOfWeek.equals(dayOfWeek)) {

                    boolean isToday = date.getDayOfWeek() == nowDateTime.getDayOfWeek();
                    boolean isTomorrow = date.getDayOfWeek() == tomorrow.getDayOfWeek();

                    LocalTime startTime = Utilities.timeFormatJoda.parseLocalTime(s.timeOpenString);
                    LocalDateTime startDate = startTime.toDateTimeToday().toLocalDateTime();

                    if (isToday) {
                        if (nowDateTime.isBefore(startDate)) {
                            String text = "";
                            if (hasTimeSchedule) {
                                text += "Pre-order for " + s.timeOpenString;
                            } else {
                                text += "Opens at " + s.timeOpenString;
                            }
                            return text;
                        }

                    } else if (isTomorrow) {
                        String text = "";
                        if (hasTimeSchedule) {
                            text += "Pre-order for " + s.timeOpenString;
                        } else {
                            text += "Opens " + s.timeOpenString;
                        }
                        return text;
                    } else {
                        return "Closed till " + date.toString(Utilities.dateFormatJoda) + " " + s.timeOpenString;
                    }
                }
            }
        }

        return null;
    }


    @Override
    public String getLabel() {
        return getServiceTitle();
    }

    @Override
    public String getDescription() {
        return openingHours;
    }

    @Override
    public String getPrice() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return getBackgroundImageUrl();
    }

    @Override
    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : getImageUrl();
    }

    @Override
    public String getTag() {
        return getEtaText();
    }

    public String getDeliveryFeeLabel() {
        if (Utilities.isEmpty(deliveryFeeLabel)) {
            return context.getString(R.string.delivery_fee);
        }

        return deliveryFeeLabel;
    }

    public void setDeliveryFeeLabel(String deliveryFeeLabel) {
        this.deliveryFeeLabel = deliveryFeeLabel;
    }

    @Override
    public int getSortOrder() {
        return position;
    }

    @Override
    public Integer getViewType() {
        return HFRecyclerView.TYPE_SERVICE;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public boolean getColorizeIcon() {
        return false;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public boolean isCourier() {
        return serviceName != null && serviceName.equalsIgnoreCase("runner");
    }


    //menu object
    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public Boolean getHasPickup() {
        return hasPickup;
    }

    public void setHasPickup(Boolean hasPickup) {
        this.hasPickup = hasPickup;
    }

    public Boolean getHasNotes() {
        return hasNotes;
    }

    public void setHasNotes(Boolean hasNotes) {
        this.hasNotes = hasNotes;
    }

    public String getNotesPromptText() {
        return notesPromptText;
    }

    public void setNotesPromptText(String notesPromptText) {
        this.notesPromptText = notesPromptText;
    }

    public List<MenuCategoryObject> getCategories() {
        return categories;
    }

    public void setCategories(List<MenuCategoryObject> categories) {
        this.categories = categories;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getServiceTitle() {
        return name;
    }

//    public void setServiceTitle(String serviceTitle) {
//        this.serviceTitle = serviceTitle;
//    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public Boolean getHasPricing() {
        return hasPricing;
    }

    public void setHasPricing(Boolean hasPricing) {
        this.hasPricing = hasPricing;
    }

    public Boolean getHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(Boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    public boolean tagsContain(String searchString) {
        return storeTags != null && storeTags.contains(searchString);
    }

    public boolean hasMinimumCharge(Double subtotalPrice) {
        return minimumCharge != null && minimumCharge > 0 && subtotalPrice < minimumCharge;
    }

    public boolean hasSurcharge(Double subtotalPrice) {
        return minimumCharge != null && minimumCharge > 0 && minimumChargeFee != null && minimumChargeFee > 0 && subtotalPrice < minimumCharge;
    }

    public boolean hasBookNow() {
        return !Utilities.isEmpty(defaultEtaText) && defaultEtaText.equalsIgnoreCase("BOOK NOW");
    }

    public boolean hasCategories() {
        return categories != null && categories.size() > 1;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean showRecommendations() {
        return !"list".equalsIgnoreCase(viewType);
    }
}
