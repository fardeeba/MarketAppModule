package com.skylarksit.module.lib;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GenericRequest<T> extends JsonRequest<T> {


    private final Map<String, String> headers;
    private boolean muteRequest = false;
    public int timeoutSecs = 120;

    /**
     * Basically, this is the constructor which is called by the others.
     * It allows you to send an object of type A to the server and expect a JSON representing a object of type B.
     * The problem with the #JsonObjectRequest is that you expect a JSON at the end.
     * We can do better than that, we can directly receive our POJO.
     * That's what this class does.
     *
     * @param method:        HTTP Method
     * @param classtype:     Classtype to parse the JSON coming from the server
     * @param url:           url to be called
     * @param requestBody:   The body being sent
     * @param listener:      Listener of the request
     * @param errorListener: Error handler of the request
     * @param headers:       Added headers
     */
    public GenericRequest(int method, Type classtype, String url, String requestBody,
                          Response.Listener<T> listener, Response.ErrorListener errorListener, Map<String, String> headers) {
        super(method, url, requestBody, listener,
                errorListener);
        this.headers = headers;
        configureRequest(timeoutSecs);
    }

    public GenericRequest(int method, Type classtype, String url, Object requestBody,
                          Response.Listener<T> listener, Response.ErrorListener errorListener, Map<String, String> headers) {
        this(method,classtype,url,new Gson().toJson(requestBody),listener,errorListener,headers);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        // The magic of the mute request happens here
        if (muteRequest) {
            if (response.statusCode >= 200 && response.statusCode <= 299) {
                // If the status is correct, we return a success but with a null object, because the server didn't return anything
                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
            }
        } else {

            try {
                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success((T) json, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    public void configureRequest(int timeout) {
        final int finalTimeOut = timeout == -1 ? 1000 : timeout;
        setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(finalTimeOut),
                0,
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS));
    }
}
