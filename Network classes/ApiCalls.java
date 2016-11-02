package applabs.vabo.network;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import applabs.vabo.MainApplication;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;

public class ApiCalls {

    private final OkHttpClient mClient = new OkHttpClient();
    public static Context mContext;

    public static ApiCalls getInstance() {
        return new ApiCalls();
    }

    public ApiCalls() {
        mContext = MainApplication.mContext;
    }

    public OkHttpClient getHttpClient() {
        return mClient;
    }
    public static String CONTENT_TYPE = "application/x-www-form-urlencoded";

    public String getCurrentToken(){
        return SupportedClass.getPrefsString(mContext, Constants.LOGIN_TOKEN);
    }

    // code request code here
    public Response doGetRequest(String url) throws IOException {
        Log.d("token", SupportedClass.getPrefsString(mContext, Constants.LOGIN_TOKEN));
        Request request;
        setTimeOutSeconds();
        request = new Request.Builder()
                .url(url)
                .addHeader("Content-type", CONTENT_TYPE)
                .addHeader("TOKEN", getCurrentToken())
                .build();
        Response response = mClient.newCall(request).execute();
        return response;
    }

    public void setTimeOutSeconds(){
        mClient.setConnectTimeout(15, TimeUnit.SECONDS);
        mClient.setReadTimeout(15, TimeUnit.SECONDS);

    }

    // code request code here
    public Response doPostNoArgRequest(String url) throws IOException {
        Log.d("token", SupportedClass.getPrefsString(mContext, Constants.LOGIN_TOKEN));
        Request request;
        setTimeOutSeconds();
        request = new Request.Builder()
                .url(url)
                .addHeader("Content-type", CONTENT_TYPE)
                .addHeader("TOKEN", getCurrentToken())
                .post(new FormEncodingBuilder().build())
                .build();
        Response response = mClient.newCall(request).execute();
        return response;
    }


    // code request code here
    public Response doPostRequest(String url, RequestBody body, boolean withToken) throws IOException {
        final Request request;
        setTimeOutSeconds();
        if (withToken) {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .addHeader("TOKEN", getCurrentToken())
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .post(body)
                    .build();
        }
        Response response = mClient.newCall(request).execute();
        return response;
    }

    // code request code here
    public Response doPutRequest(String url, RequestBody body, boolean withToken) throws IOException {
        final Request request;
        if (withToken) {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .addHeader("TOKEN", getCurrentToken())
                    .put(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .put(body)
                    .build();
        }
        Response response = mClient.newCall(request).execute();
        return response;
    }

    // code request code here
    public Response doPatchRequest(String url, RequestBody body, boolean withToken) throws IOException {
        final Request request;
        if (withToken) {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .addHeader("TOKEN", getCurrentToken())
                    .patch(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-type", CONTENT_TYPE)
                    .patch(body)
                    .build();
        }
        Response response = mClient.newCall(request).execute();
        return response;
    }


    // code request code here
    public Response doDeleteRequest(String url) throws IOException {
        final Request request;
        request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        Response response = mClient.newCall(request).execute();
        return response;
    }


}
