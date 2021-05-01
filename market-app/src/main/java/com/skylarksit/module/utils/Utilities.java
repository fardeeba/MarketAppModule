package com.skylarksit.module.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.crashlytics.android.Crashlytics;
import com.skylarksit.module.BuildConfig;
import com.skylarksit.module.MainActivity;
import com.skylarksit.module.R;
import com.skylarksit.module.libs.alertdialog.NokAlertDialog;
import com.skylarksit.module.libs.alertdialog.NokAlertDialogBuilder;
import com.skylarksit.module.libs.alertdialog.NokProgressDialog;
import com.skylarksit.module.pojos.AddressObject;
import com.skylarksit.module.pojos.CountryBean;
import com.skylarksit.module.pojos.services.Currency;
import com.skylarksit.module.ui.activities.core.LoginActivity;
import com.skylarksit.module.ui.activities.main.CreateEddressActivity;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.ui.utils.IClickListener;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.Context.WIFI_SERVICE;
import static com.facebook.common.internal.Preconditions.checkArgument;

public class Utilities {

    public final static String apiAppPath = "v1/app/";
    public final static String apiUserPath = "v1/user/";
    public final static int FINE_LOCATION_PERMISSION = 2;
    public final static int CAMERA_PERMISSION = 3;
    public final static int CAMERA_READ_EXTERNAL_STORAGE_PERMISSION = 7;
    public static boolean production = true;

    public static final DateTimeFormatter timeFormatJoda = DateTimeFormat.forPattern("h:mm a").withLocale(Locale.ENGLISH);
    public static final DateTimeFormatter dateFormatJoda = DateTimeFormat.forPattern("EEEE").withLocale(Locale.ENGLISH);

    public static int screenHeight;
    public static int screenWidth;
    public static float zoom = 13.0f;
    public static float zoomFar = 11.0f;
    public static float closeZoom = 17.0f;
    public static LocationRequest REQUEST = LocationRequest.create().setInterval(10000).setFastestInterval(10).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    public static int simCountryIndex = -1;
    public static int pickedCountryIndex = -1;

    public static String currentCountry;
    public static CountryBean pickedCountry;

    public static AddressObject activeAddressObject;
    public static AddressObject newAddressObject;
    public static List<AddressObject> searchResults = new ArrayList<>();
    public static String jwtToken = "";
    public static Uri imageUri;

    public static String checkoutProdKey = "";
    public static String appType;

    @SuppressLint("SimpleDateFormat")

    public static String baseUrl;
    public static String baseUserUrl;

    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.US);
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    public static boolean isTokenModified = false;
    private static DisplayMetrics displayMetric;
    public static String currentUrl;
    private static boolean marketplace;

    private static Map<String, NumberFormat> currencyFormaters = new HashMap<>();
    private static Map<String, NumberFormat> shortCurrencyFormaters = new HashMap<>();

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetric);
    }

    public static Boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    public static Boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean hasJWTToken() {
        return !(jwtToken == null || jwtToken.isEmpty());
    }

    public static boolean isLoggedIn() {
        String userUid = LocalStorage.instance().getString("uid");
        return !Utilities.isEmpty(userUid);
    }

    private static Intent getMainActivityIntent(Activity activity) {
        Intent intent;
        intent = new Intent(activity, MainActivity.class);
        Utilities.loginCrashlytics(activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }


    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static void startMainActivity(MyAppCompatActivity activity) {
        Intent intent = getMainActivityIntent(activity);
        activity.saveValue("tutorialComplete", true);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        activity.finish();
    }

    public static void setFontStyle(TextView textView, int fontName) {
        if (Build.VERSION.SDK_INT >= 23) {
            textView.setTextAppearance(fontName);
        } else {
            textView.setTextAppearance(textView.getContext(), fontName);
        }
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static void showSpinner(final String text) {

        Activity context = ServicesModel.instance().currentActivity;

        if (context == null || context.isFinishing()) return;

        if (progress == null) {
            progress = new NokProgressDialog(context);
        }

        if (!context.equals(progress.getContext())) {
            progress.hide();
            progress = new NokProgressDialog(context);
        }

        progress.setMessage(text);

        if (!progress.isShowing()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> progress.show());
        }
    }

    public static void showSpinner() {
        showSpinner(ServicesModel.instance().appContext.getString(R.string.loader_text));
    }

    public static String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }


    public static boolean checkGps(Activity activity, IClickListener<Object> hideClickListener) {

        if (!isGpsEnabled(activity)) {
            alertGPSon(activity, hideClickListener);
            return false;
        }
        return true;

    }

    public static final Uri getUriToDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
        return imageUri;
    }

    private static Boolean isGpsEnabled(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static Boolean isWifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifi != null && wifi.isWifiEnabled();
    }

    private static NokAlertDialog gpsAlertDialog;

    private static void alertGPSon(final Activity act, IClickListener<?> hideCallBack) {

        if (gpsAlertDialog != null) {
            if (!gpsAlertDialog.isShowing()) {
                gpsAlertDialog.show();
            }
        } else {
            String message = act.getString(R.string.turn_on_gps);

            gpsAlertDialog = new NokAlertDialog(act).setTitle("Location Services")
                    .setCancelClickListener(sweetAlertDialog -> hideCallBack.click(null)).setCancelText("not now")
                    .setMessage(message).setCancelable(false).setConfirmText("Go to settings").setConfirmClickListener(sweetAlertDialog -> {
                        if (act != null && !act.isFinishing())
                            act.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    });
            gpsAlertDialog.show();
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private static NokAlertDialog permissionsDialog;

    public static void alertGpsPermission(final Activity act, IClickListener<?> hideCallBack) {

        if (permissionsDialog != null)
            permissionsDialog.dismiss();
        else {
            permissionsDialog = new NokAlertDialog(act).setCancelClickListener(sweetAlertDialog -> {
                if (hideCallBack != null) hideCallBack.click(null);
            }).setTitle("GPS Permission Required").setMessage("Location permission is recommended to use this app!").setCancelable(false).showCancelButton(true).setCancelText("not now").setConfirmText("Edit Permissions").setConfirmClickListener(sweetAlertDialog -> {
                if (act != null && !act.isFinishing()) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", act.getPackageName(), null);
                    intent.setData(uri);
                    act.startActivity(intent);
                }
            });
        }

        permissionsDialog.show();

    }

    public static void alertSignInFirst(final Context act) {
        alertSignInFirst(act, false);
    }

    public static void alertSignInFirst(final Context act, boolean skipPopup) {

        if (skipPopup) {
            act.startActivity(new Intent(act, LoginActivity.class));
            return;
        }

        NokAlertDialog alert = new NokAlertDialog(act, NokAlertDialog.SUCCESS);

        alert.setTitle(act.getString(R.string.title_sign_in));
        alert.setMessage(act.getString(R.string.sign_in_description));
        alert.setConfirmText(act.getString(R.string.sign_in_yes));
        alert.setCancelText(act.getString(R.string.sign_in_cancel));
        alert.setCancelable(true);
        alert.setConfirmClickListener(sweetAlertDialog -> {
            act.startActivity(new Intent(act, LoginActivity.class));
            alert.dismiss();
        });
        alert.show();
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void Error(final String title, final String text) {
        hideSpinner();


        final String tempTitle = (title == null || title.isEmpty()) ? ServicesModel.instance().currentActivity.getString(R.string.oops) : title;
        final String tempText = (text == null || text.isEmpty()) ? ServicesModel.instance().currentActivity.getString(R.string.pls_try_again) : text;

        new NokAlertDialog(ServicesModel.instance().currentActivity, NokAlertDialog.ERROR)
                .setTitle(tempTitle)
                .setMessage(tempText)
                .show();
    }


    public static void Toast(final String text) {
        Toast.makeText(ServicesModel.instance().appContext, text, Toast.LENGTH_LONG).show();
    }

    private static NokAlertDialogBuilder progress;

    public static void hideSpinner() {
        if (progress != null && progress.isShowing()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> progress.hide());
        }
    }

    public static String getTimeAgo(Long updatedOn) {

        if (updatedOn == null) {
            return "";
        }

        double diff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - updatedOn);
        if (diff < 60) {
            return "now";
        } else if (diff < 3600) {
            return ((int) (diff / 60.0)) + "m ago";
        } else if (diff < 86400) {
            return ((int) (diff / 3600.0)) + "h ago";
        } else if (diff < 172800) {
            return "Yesterday";
        } else {
            return ((int) (diff / 86400.0)) + "d ago";
        }
    }

    public static String getResponseString(JSONObject json, String name) {
        if (json.has(name) && !json.isNull(name)) {
            try {
                String val = json.getString(name);
                if (val.compareToIgnoreCase("null") == 0) {
                    return "";
                }
                return val;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getMyId() {
        return LocalStorage.instance().getString("id");
    }

    public static String getMyName() {
        return LocalStorage.instance().getString("fullName");
    }

    public static String getMyPhoneNumber() {
        return LocalStorage.instance().getString("phoneNumber");
    }

    private static DecimalFormat usdFormatter;
    private static DecimalFormat currencyFormatter;


    public static NumberFormat getCurrencyFormatter(Currency currency) {
        return getCurrencyFormatter(currency.iso, currency.hideDecimals);
    }

    public static String formatCurrency(Double value, String currency, Boolean hideDecimals) {

        return getCurrencyFormatter(currency, hideDecimals).format(value);
    }


    public static double formatCurrencyToDouble(Double amount, Currency currency) {
        String currencyStr = Utilities.getCurrencyFormatter(currency, true).format(amount);
        try {
            return Double.parseDouble(currencyStr.replace(",", ""));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return amount;
        }
    }


    public static NumberFormat getCurrencyFormatter(Currency currency, boolean hideCurrency) {
        return getCurrencyFormatter(currency.iso, currency.hideDecimals, hideCurrency);
    }


    public static NumberFormat getCurrencyFormatter(String currency, Boolean hideDecimals) {
        return getCurrencyFormatter(currency, hideDecimals, false);
    }

    public static NumberFormat getCurrencyFormatter(String currency, Boolean hideDecimals, boolean hideCurrency) {

        if (currency == null) currency = "USD";
        if (hideDecimals == null) hideDecimals = false;


        Map<String, NumberFormat> map = hideCurrency ? shortCurrencyFormaters : currencyFormaters;

        NumberFormat formatter = map.get(currency);
        if (formatter == null) {
            formatter = createCurrencyFormatter(currency, hideDecimals, hideCurrency);
            map.put(currency, formatter);
        }
        return formatter;
    }

    private static NumberFormat createCurrencyFormatter(String currency, boolean hideDecimals, boolean hideCurrency) {

        if (hideDecimals) {
            String pattern = "#,##0";
            if (!hideCurrency) pattern += " " + currency;
            currencyFormatter = (DecimalFormat)
                    NumberFormat.getNumberInstance(Locale.ENGLISH);
            currencyFormatter.applyPattern(pattern);
            currencyFormatter.setMaximumFractionDigits(0);
            currencyFormatter.setMinimumFractionDigits(0);
        } else {
            String pattern = "#,##0.00";
            if (!hideCurrency) pattern += " " + currency;
            currencyFormatter = (DecimalFormat)
                    NumberFormat.getNumberInstance(Locale.ENGLISH);
            currencyFormatter.applyPattern(pattern);
            currencyFormatter.setMaximumFractionDigits(2);
            currencyFormatter.setMinimumFractionDigits(2);
        }
        return currencyFormatter;

    }

    public static JSONObject getBasicParams() {
        return new JSONObject();
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static void loginCrashlytics(Context context) {

        if (!isDebug()) {
            String uid = LocalStorage.instance().getString("uid");
            String fullName = LocalStorage.instance().getString("fullName");
            String phone = LocalStorage.instance().getString("phoneNumber");

//            Crashlytics.setUserIdentifier(uid);
//            Crashlytics.setString("phoneNumber", phone);
//            Crashlytics.setUserName(fullName);
        }

    }


    public static String replaceString(String string) {
        return string.replaceAll("[;/:*?\"<>|&']","replaceString");
    }

    public static Bitmap getMarkerPin(Context context, @ColorRes int color, int newWidth) {
        return getMarkerPin(context, color, R.drawable.pin_dark, newWidth);
    }

    public static Bitmap getMarkerPin(Context context, @ColorRes int color, int drawable, int newWidth) {


        if (newWidth == 0) newWidth = dpToPx(30);

        Bitmap bitmap;
        bitmap = getBitmap(context, drawable);
        bitmap = ImageHelper.changeBitmapColor(bitmap, ContextCompat.getColor(context, color));
        return scaleBitmap(bitmap, newWidth);
    }


    private static Bitmap getBitmap(Context context, int drawable) {

        return BitmapFactory.decodeResource(context.getResources(),
                drawable);
    }

    public static Bitmap getBitmap(Context context, int drawable, int width) {
        return scaleBitmap(getBitmap(context, drawable), width);
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int width) {

        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();

        float ratio = originalHeight / originalWidth;

        int newY = (int) (width * ratio);

        return Bitmap.createScaledBitmap(bitmap, width, newY, true);
    }

    public static void addNewEddress(MyAppCompatActivity activity, boolean setAsCurrent, String serviceSlug, Boolean returnToMainView) {
        addNewEddress(activity, false, false, setAsCurrent, serviceSlug, returnToMainView);
    }

    public static void addNewEddress(MyAppCompatActivity activity, boolean isHLS, boolean isPickup, boolean setAsCurrent, String serviceSlug, Boolean returnToMainView) {
        Intent createEddressIntent = new Intent(activity, CreateEddressActivity.class);
        createEddressIntent.putExtra("isHLS", isHLS);
        createEddressIntent.putExtra("isPickup", isPickup);
        createEddressIntent.putExtra("setAsCurrent", setAsCurrent);
        createEddressIntent.putExtra("activeService", serviceSlug);
        createEddressIntent.putExtra("returnToMainView", returnToMainView);
        activity.startActivity(createEddressIntent);
    }

    public static Intent getIntentForCam(Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        return intent;
    }

    public static void clearData(Context context) {
        ServicesModel.instance().clearCart(true);
        ServicesModel.instance().myLocations.clear();
        LocalStorage.instance().clear(context);
        ImageHelper.deleteCache(context);
    }


    public static String getColor(Context context, int color) {
        return context.getResources().getString(color);
    }

    public static void warn(String title, String message) {
        NokAlertDialog alert = new NokAlertDialog(ServicesModel.instance().currentActivity, NokAlertDialog.WARNING);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setConfirmText("Close");
        alert.setConfirmClickListener(sweetAlertDialog -> alert.dismiss());
        alert.showCancelButton(false);
        alert.show();
    }

    private static boolean noConnectionOpen = false;

    public static void noConnection() {

        Utilities.hideSpinner();

        if (noConnectionOpen) return;

        noConnectionOpen = true;

        NokAlertDialog alert = new NokAlertDialog(ServicesModel.instance().currentActivity, NokAlertDialog.WARNING);
        alert.setTitle("Could not connect to server.");
        alert.setMessage("Please check your internet connection.");
        alert.setConfirmText("Close");
        alert.setConfirmClickListener(sweetAlertDialog -> {
            noConnectionOpen = false;
            alert.dismiss();
        });
        alert.setCancelable(false);
        alert.showCancelButton(false);
        alert.show();
    }

    public static void init(Context applicationContext, Display display) {

        if (displayMetric != null) return;

        displayMetric = applicationContext.getResources().getDisplayMetrics();

        String idCountry = LocalStorage.instance().getString("idCountry");
        if (idCountry != null && !idCountry.isEmpty()) {
            pickedCountryIndex = Integer.parseInt(idCountry);
        }

        Point size = new Point();
        display.getSize(size);
        Utilities.screenHeight = size.y;
        Utilities.screenWidth = size.x;
    }

    public static void logout() {

        clearData(ServicesModel.instance().appContext);

        if (!LoginActivity.active) {
            Intent intent = new Intent(ServicesModel.instance().appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ServicesModel.instance().appContext.startActivity(intent);
        }

    }

    public static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    static Integer getVersionCode() {

        int versionName = 0;

        try {
            versionName = ServicesModel.instance().appContext.getPackageManager()
                    .getPackageInfo(ServicesModel.instance().appContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;

    }

    public static boolean isMarketplace() {
        return "marketplace".equalsIgnoreCase(appType);
    }

    public static boolean isNullOrEmpty(Collection<?> list) {
        return !notNullAndEmpty(list);
    }


    public static boolean notNullAndEmpty(Collection<?> list) {
        return list != null && !list.isEmpty();
    }


    public static String getUniqueIdentifier(Context context) {

        String uniqueId = LocalStorage.instance().getString("uniqueIdentifier");

        if (Utilities.isEmpty(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            LocalStorage.instance().save(context, "uniqueIdentifier", uniqueId);
        }

        return uniqueId;
    }

    public static String getDayAt(int dayOfWeek) {
        switch (dayOfWeek) {

            case 1: {
                return "MON";
            }
            case 2: {
                return "TUE";
            }
            case 3: {
                return "WED";
            }
            case 4: {
                return "THU";
            }
            case 5: {
                return "FRI";
            }
            case 6: {
                return "SAT";
            }
            case 7: {
                return "SUN";
            }
        }
        return "";
    }


//    public static void changeLanguage(Context context, String lang) {
//        Locale locale = new Locale(lang);
//        Locale.setDefault(locale);
//        Configuration config = context.getResources().getConfiguration();
//        config.locale = locale;
//        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
//    }

    public static List<LatLng> bspline(List<LatLng> poly) {

        if (poly.get(0).latitude != poly.get(poly.size() - 1).latitude || poly.get(0).longitude != poly.get(poly.size() - 1).longitude) {
            poly.add(new LatLng(poly.get(0).latitude, poly.get(0).longitude));
        } else {
            poly.remove(poly.size() - 1);
        }
        poly.add(0, new LatLng(poly.get(poly.size() - 1).latitude, poly.get(poly.size() - 1).longitude));
        poly.add(new LatLng(poly.get(1).latitude, poly.get(1).longitude));

        Double[] lats = new Double[poly.size()];
        Double[] lons = new Double[poly.size()];

        for (int i = 0; i < poly.size(); i++) {
            lats[i] = poly.get(i).latitude;
            lons[i] = poly.get(i).longitude;
        }

        double ax, ay, bx, by, cx, cy, dx, dy, lat, lon;
        float t;
        int i;
        List<LatLng> points = new ArrayList<>();
        // For every point
        for (i = 2; i < lats.length - 2; i++) {
            for (t = 0; t < 1; t += 0.2) {
                ax = (-lats[i - 2] + 3 * lats[i - 1] - 3 * lats[i] + lats[i + 1]) / 6;
                ay = (-lons[i - 2] + 3 * lons[i - 1] - 3 * lons[i] + lons[i + 1]) / 6;
                bx = (lats[i - 2] - 2 * lats[i - 1] + lats[i]) / 2;
                by = (lons[i - 2] - 2 * lons[i - 1] + lons[i]) / 2;
                cx = (-lats[i - 2] + lats[i]) / 2;
                cy = (-lons[i - 2] + lons[i]) / 2;
                dx = (lats[i - 2] + 4 * lats[i - 1] + lats[i]) / 6;
                dy = (lons[i - 2] + 4 * lons[i - 1] + lons[i]) / 6;
                lat = ax * Math.pow(t + 0.1, 3) + bx * Math.pow(t + 0.1, 2) + cx * (t + 0.1) + dx;
                lon = ay * Math.pow(t + 0.1, 3) + by * Math.pow(t + 0.1, 2) + cy * (t + 0.1) + dy;
                points.add(new LatLng(lat, lon));
            }
        }
        return points;

    }

    public static List<LatLng> createOuterBounds() {
        return new ArrayList<LatLng>() {{
            add(new LatLng(-180, -90));
            add(new LatLng(-180, 90));
            add(new LatLng(180, 90));
            add(new LatLng(180, -90));
            add(new LatLng(-180, -90));
        }};
    }



}
