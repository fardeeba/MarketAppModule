package com.skylarksit.module.lib;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.skylarksit.module.BuildConfig;
import com.skylarksit.module.R;
import com.skylarksit.module.libs.Json;
import com.skylarksit.module.pojos.IJsonParam;
import com.skylarksit.module.pojos.ResponseBean;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.skylarksit.module.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Rest<T> {

    private boolean userApi;

    public interface GenericResponse {
        void onError(ErrorResource errorResource);
    }

    private int timeout = 120;
    private String requestTag;
    private Object params;
    private String baseUrl;
    private String uri;
    private Type resultType;
    private Response.Listener<Object> response;
    private boolean showLoader;
    private String loaderText = ServicesModel.instance().appContext.getString(R.string.loader_text);
    private GenericResponse errorListener;
    private final Gson gson = new Gson();


    public Rest() {

    }

    public Rest timeOut(int timeOutSecs) {
        this.timeout = timeOutSecs;
        return this;
    }

    public static Rest request() {
        return new Rest();
    }


    public Rest params(Json params) {
        this.params = params.toJson();
        return this;
    }


    public Rest uri(String uri) {
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        this.uri = uri;
        return this;
    }

    public Rest baseUrl(String baseUrl) {
        this.baseUrl = Utilities.currentUrl + baseUrl;
        return this;
    }

    public Rest userApi() {
        this.userApi = true;
        return this;
    }

    public Rest params(IJsonParam params) {
        this.params = params;
        return this;
    }

    public Rest params(JsonObject params) {
        this.params = params;
        return this;
    }

    public Rest tag(String requestTag) {
        this.requestTag = requestTag;
        return this;
    }

    public Rest response(Type classType, final Response.Listener<Object> response) {
        this.response = response;
        this.resultType = classType;
        return this;
    }

    public Rest error(final GenericResponse errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public Rest showLoader(String loaderText) {
        showLoader = true;
        this.loaderText = loaderText;
        return this;
    }

    public Rest showLoader() {
        showLoader = true;
        return this;
    }

    public Map<String, String> header;

    private Map<String, String> getHeaders() {

        if (header == null) {
            header = new HashMap<>();
        }

        if (Utilities.jwtToken == null || Utilities.jwtToken.isEmpty())
            Utilities.jwtToken = LocalStorage.instance().getString("jwtToken");
        header.put("Authorization", "Bearer " + Utilities.jwtToken);

        return header;
    }

    public void post() {
        execute(GenericRequest.Method.POST);
    }

    public void get() {
        execute(GenericRequest.Method.GET);
    }

    private void execute(final int method) {

        Context context = ServicesModel.instance().appContext;

        if (Utilities.isEmpty(baseUrl)) {
            if (!userApi) {
                baseUrl = Utilities.baseUrl;
            } else {
                baseUrl = Utilities.baseUserUrl;
            }
        }

        if (uri == null) {
            Utilities.Toast("Make sure to set the URI");
            return;
        }

        if (showLoader) {
            Utilities.showSpinner(loaderText);
        }
        GenericRequest request = new GenericRequest<String>(method, resultType, baseUrl + uri, params,
                json -> {
                    new GsonParserTask(gson, resultType, showLoader, errorListener, response).execute(json);
                },
                error -> {
                    if (showLoader)
                        Utilities.hideSpinner();
                    handleError(error);
                    Log.e("error", error.getMessage(), error);
                }, getHeaders()
        );

        request.configureRequest(timeout);

        if (requestTag != null) {
            request.setTag(requestTag);
        }

        RestClient.getInstance(context).addToRequestQueue(request);
    }

    private static class GsonParserTask extends AsyncTask<String, Void, Object> {

        Gson gson;
        Type resultType;
        Boolean showLoader;
        GenericResponse errorListener;
        Response.Listener<Object> response;

        GsonParserTask(Gson gson, Type resultType, Boolean showLoader, GenericResponse errorListener, Response.Listener<Object> response) {
            this.gson = gson;
            this.resultType = resultType;
            this.errorListener = errorListener;
            this.showLoader = showLoader;
            this.response = response;
        }

        protected Object doInBackground(String... json) {
            if (BuildConfig.DEBUG)
                Log.d("Response", json[0]);

            if (resultType == null) return null;
            return gson.fromJson(json[0], resultType);
        }

        protected void onPostExecute(Object res) {

            if (res instanceof ResponseBean) {
                if ("INVALID".equalsIgnoreCase(((ResponseBean) res).message)) {

                    if (showLoader)
                        Utilities.hideSpinner();

                    if (errorListener != null) {
                        ErrorResource resource = new ErrorResource();
                        resource.data = res;
                        resource.message = ((ResponseBean) res).message;
                        resource.description = ((ResponseBean) res).description;
                        errorListener.onError(resource);
                    } else {
                        Utilities.Error(((ResponseBean) res).description, null);
                    }

                    return;
                }
            }
            if (response != null) {
                response.onResponse(res);
            }

            if (showLoader)
                Utilities.hideSpinner();
        }
    }

    private void handleError(VolleyError error) {

        if (error == null) return;

        if (error instanceof NoConnectionError) {
            Utilities.noConnection();
            return;
        }

        final NetworkResponse response = error.networkResponse;

        int code = 0;
        if (error.networkResponse != null)
            code = error.networkResponse.statusCode;

        if (error instanceof ServerError) {

            if (response.data != null) {
                new AsyncExec(showLoader, errorListener).execute(response);
            }
        } else if (error instanceof AuthFailureError) {

            ErrorResource errorResource;
            try {
                errorResource = new Gson().fromJson(new String(response.data), ErrorResource.class);
                if (errorResource != null && "BAD_TOKEN".equalsIgnoreCase(errorResource.message)) {
                    if (showLoader)
                        Utilities.hideSpinner();
                    Utilities.logout();
                }
            } catch (Exception e) {
                return;
            }

        } else if (error instanceof ParseError) {
            error.printStackTrace();
        } else if (error instanceof TimeoutError) {
            error.printStackTrace();
        }

        ErrorResource resource = new ErrorResource();

        resource.status = String.valueOf(code);
        resource.message = error.getMessage();

    }

    public static void cancelAll(Activity context, String requestTag) {
        RestClient.getInstance(context).getRequestQueue().cancelAll(requestTag);
    }

    private static class AsyncExec extends AsyncTask<NetworkResponse, Void, ErrorResource> {

        private final Boolean showLoader;
        private final GenericResponse errorListener;

        AsyncExec(Boolean showLoader, GenericResponse errorListener) {
            this.showLoader = showLoader;
            this.errorListener = errorListener;
        }

        @Override
        protected ErrorResource doInBackground(NetworkResponse... params) {
            final NetworkResponse response = params[0];
            ErrorResource errorResource;
            try {
                errorResource = new Gson().fromJson(new String(response.data), ErrorResource.class);
            } catch (Exception e) {
                return null;
            }
            return errorResource;
        }

        @Override
        protected void onPostExecute(ErrorResource result) {

            if (showLoader)
                Utilities.hideSpinner();

            if (result == null) {
                Utilities.Toast("Please try again");
                return;
            }

            if (errorListener != null) {
                errorListener.onError(result);
            } else {
                if (!"INTERNAL_SERVER_ERROR".equals(result.status) && result.description != null) {
                    String title = ServicesModel.instance().appContext.getString(R.string.title_something_went_wrong);
                    if (result.title != null) {
                        title = result.title;
                    }
                    Utilities.Error(title, Utilities.isDebug() ? result.description : ServicesModel.instance().appContext.getString(R.string.pls_try_again));
                } else {
                    Utilities.Toast("Please try again");
                }
            }
        }
    }

}
