package applabs.vabo.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimeZone;

import applabs.vabo.BuildConfig;
import applabs.vabo.MainApplication;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;

public class ApiRequests {

    public static final String TAG = "ApiRequests";
    private Context context;
    private String url = null;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public ApiRequests() {
        this.context = MainApplication.mContext;
    }
    public String responseString;

    public void setResponseString(String value){
        this.responseString = value;
    }

    public String getResponseString(){
        return this.responseString;
    }

    public static final String BASE_URL = BuildConfig.BASEURL;

    public boolean isNetworkAvailable(Context con) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) con
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (activeNetworkInfo != null
                    && activeNetworkInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }



    public enum Endpoint {
        CREATE_USER("/user/signup"),
        SIGN_IN("/user/signin"),
        EDIT_USER("/api/user/edit"),
        GET_SERVICES("/api/service/services"),
        APPOINTMENT_CREATE("/api/customer/appointment/create"),
        APPOINTMENT_EDIT("/api/customer/appointment/edit"),
        APPOINTMENT_CUST("/api/customer/appointments"),
        APPOINTMENT_CANCEL("/api/customer/appointment/cancel"),
        ALL_NEW_APPONTMENTS_PROF("/api/appointment/new"),
        APPOINTMENT_ACCEPT_PROF("/api/professional/appointment/accept"),
        APPOINTMENT_REJECT_PROF("/api/professional/appointment/reject"),
        APPOINTMENT_CANCEL_PROF("/api/professional/appointment/cancel"),
        APPOINTMENT_COMPLETE_PROF("/api/professional/appointment/complete"),
        ALL_ACCEPTED_APPOINTMENT("/api/professional/appointment"),
        CREATE_STRIPE_USER("/api/card/create"),
        EDIT_STRIPE_USER("/api/card/edit"),
        DELETE_STRIPE_USER("/api/card/delete"),
        CHANGE_DEFAULT("/api/card/defaultChange"),
        RATE_APPOINTMENT_BY_CUST("/api/customer/appointment/rate"),
        RATE_APPOINTMENT_BY_PROF("/api/professional/appointment/rate"),
        GET_STIPE_CARD_DETAILS("/api/customer/payment/details"),
        CHANGE_PASSWORD("/api/user/changePassword"),
        RESET_PASSWORD("/user/resetPassword"),
        FORGOT_PASSWORD("/user/forgotPassword"),
        GET_ALL_MESSAGE("/api/appointment/viewMessage"),
        MAKE_ALL_READ("/api/appointment/readMessage"),
        SEND_MESSAGE("/api/appointment/sendMessage"),
        START_APPOINTMENT("/api/appointment/start"),
        REGISTER_DEVICE("/api/RegisterDevice"),
        HELP_APPOINTMENT("/api/appointment/help"),
        CONTACT_US("/api/appointment/contact"),
        LOGOUT("/api/user/logout"),
        IS_AVAILABLE("/api/user/available"),
        CREATE_STRIPE_ACCOUNT("/api/professionals/stripeAccountCreate"),
        STRIPE_ACCOUNT_DETAILS("/api/professionals/accountDetail"),
        GET_TRANSACTION_HISTORY("/api/professionals/retrieveTransactionHistory"),
        PROFESSIONAL_PENLATY_CHARGE("/api/professionals/Charge"),
        GET_HELP_QUESTIONS("/api/appointment/questions")
        ;

        public final String path;

        Endpoint(String path) {
            this.path = path;
        }
    }

    public JSONObject getCityCountry(String zipcode){
        url = Constants.GOOGLE_GET_CITY_COUNTRY.replace("ZIP", zipcode);

        ApiCalls apiCalls = ApiCalls.getInstance();
        try {
            Response response = apiCalls.doGetRequest(url);
            Log.d("url", response.toString());
            String responseString = "";
            if (response != null)
                responseString = readStream(response.body().byteStream());

            setResponseString(response.code() + "," + response.message()+","+responseString);

            if (response != null && response.isSuccessful()) {
                return new JSONObject(responseString);
            } else
                return null;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    public void logout() {
        url = BASE_URL + Endpoint.LOGOUT.path;
        JSONObject jsonObject = getPostEmptyBodyResponse();
        Log.d("logout", jsonObject != null ? jsonObject.toString() : "Error");
    }

    public JSONObject changeAvailability(boolean isAvailable) {
        url = BASE_URL + Endpoint.IS_AVAILABLE.path;
        JSONObject jsonObject = getPostJsonResponse(new FormEncodingBuilder().add("is_available", "" + (isAvailable ? 1 : 0)).add("user_id", "" + SupportedClass.getIntPrefData(MainApplication.mContext, Constants.CURRENT_USER_ID_PREF)).build());
        if(jsonObject != null){
            SupportedClass.savePrefsBoolean(MainApplication.mContext, isAvailable, Constants.IS_AVAILABLE);
        }
        return jsonObject;
    }

    public JSONObject signInUser(String email, String password) {
        url = BASE_URL + Endpoint.SIGN_IN.path;
        ApiCalls apiCalls = ApiCalls.getInstance();
        Response response;
        try {

            FormEncodingBuilder formEncodingBuilder =  new FormEncodingBuilder();
            formEncodingBuilder.add("email", email);
            formEncodingBuilder.add("password", password);

            RequestBody requestBody = formEncodingBuilder.build();
            Log.d(TAG + "url", url);
            Log.d(TAG + "Request", formEncodingBuilder.toString());

            response = apiCalls.doPostRequest(url, requestBody, false);

            Log.d(TAG + "response ", response.toString());
            String responseString = "";
            if (response != null)
                responseString = readStream(response.body().byteStream());

            setResponseString(response.code() + "," + response.message()+","+responseString);
            Log.d("responseString", responseString);
            if (response != null && response.isSuccessful()) {
                return new JSONObject(responseString);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getStripeCards() {
        url = BASE_URL + Endpoint.GET_STIPE_CARD_DETAILS.path;
        return getPostEmptyBodyResponse();
    }

    public JSONObject getTransactionHistroy() {
        url = BASE_URL + Endpoint.GET_TRANSACTION_HISTORY.path;
        return getPostEmptyBodyResponse();
    }

    public JSONObject getHelpQuestions() {
        url = BASE_URL + Endpoint.GET_HELP_QUESTIONS.path;
        return getPostEmptyBodyResponse();
    }


    public JSONObject createUser(RequestBody requestBody, String imagePath) {
        url = BASE_URL + Endpoint.CREATE_USER.path;
        ApiCalls apiCalls = ApiCalls.getInstance();
        Response response;
        try {

            if(!imagePath.isEmpty()){

            }
            Log.d(TAG + "url", url);
            Log.d(TAG + "Request", requestBody.toString());

            response = apiCalls.doPostRequest(url, requestBody, false);

            String responseString = "";
            if (response != null)
                responseString = readStream(response.body().byteStream());

            setResponseString(response.code() + "," + response.message()+","+responseString);
            Log.d(TAG + "response", responseString+" "+response.toString());

            if (response != null && response.isSuccessful()) {
                return new JSONObject(responseString);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject appointmentCalls(RequestBody requestBody, int identity){
        switch (identity){
            case 0 : url = BASE_URL + Endpoint.APPOINTMENT_CREATE.path;
                break;

            case 1 : url = BASE_URL + Endpoint.APPOINTMENT_EDIT.path;
                break;

            case 2 : url = BASE_URL + Endpoint.APPOINTMENT_CANCEL_PROF.path;
                break;
        }

        return getPostJsonResponse(requestBody);
    }

    public JSONObject updateUser(RequestBody  requestBody) {
        url = BASE_URL + Endpoint.EDIT_USER.path;
        return getPostJsonResponse(requestBody);
    }

    public JSONObject changePassword(RequestBody requestBody, int isOTP) {
        if(isOTP == 0)
            url = BASE_URL + Endpoint.CHANGE_PASSWORD.path;
        else
            url = BASE_URL + Endpoint.RESET_PASSWORD.path;
        return getPostJsonResponse(requestBody);
    }

    public JSONObject forgotPassword(String email) {
        url = BASE_URL + Endpoint.FORGOT_PASSWORD.path;
        return getPostJsonResponse(new FormEncodingBuilder().add("email", email).build());
    }


    public JSONObject chargeProfessional(RequestBody requestBody) {
        url = BASE_URL + Endpoint.PROFESSIONAL_PENLATY_CHARGE.path;
        return getPostJsonResponse(requestBody);


    }

    public JSONObject createUpdateStripeUser(RequestBody requestBody, int isCreate) {
        if(isCreate == 1)
            url = BASE_URL + Endpoint.CREATE_STRIPE_USER.path;
        else if(isCreate == 2)
            url = BASE_URL + Endpoint.EDIT_STRIPE_USER.path;
        else if(isCreate == 3)
            url = BASE_URL + Endpoint.DELETE_STRIPE_USER.path;
        else
            url = BASE_URL + Endpoint.CHANGE_DEFAULT.path;
        return getPostJsonResponse(requestBody);


    }

    public JSONObject getPostJsonResponse(RequestBody requestBody){
        Response response;
        ApiCalls apiCalls = ApiCalls.getInstance();
        try {
            Log.d(TAG + " url", url);

            Log.d(TAG+" request", requestBody.toString());
            response = apiCalls.doPostRequest(url, requestBody, true);
            Log.d(TAG + " response", response + "");

            String responseString = "";
            if (response != null)
                responseString = readStream(response.body().byteStream());

            setResponseString(response.code() + "," + response.message() + "," + responseString);

            if (response != null && response.isSuccessful()) {

                return new JSONObject(responseString);
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getAppointmentCust() {
        url = BASE_URL + Endpoint.APPOINTMENT_CUST.path;
        Log.d("url", url);
        return getPostEmptyBodyResponse();
    }

    public JSONObject rateByProfessional(int appointmentReqID, float ratings) {
        url = BASE_URL + Endpoint.RATE_APPOINTMENT_BY_PROF.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).add("rate", "" + ratings).build());
    }

    public JSONObject helpAppointment(int appointmentReqID, String message) {
        url = BASE_URL + Endpoint.HELP_APPOINTMENT.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).add("message", "" + message).build());
    }

    public JSONObject createStripeAccount(RequestBody requestBody) {
        url = BASE_URL + Endpoint.CREATE_STRIPE_ACCOUNT.path;
        Log.d("url", url);
        return getPostJsonResponse(requestBody);
    }

    public JSONObject getStripeAccount() {
        url = BASE_URL + Endpoint.STRIPE_ACCOUNT_DETAILS.path;
        Log.d("url", url);
        return getPostEmptyBodyResponse();
    }

    public JSONObject contactUs(String message) {
        url = BASE_URL + Endpoint.CONTACT_US.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("message", "" + message).build());
    }

    public JSONObject startAppointment(int appointmentReqID) {
        url = BASE_URL + Endpoint.START_APPOINTMENT.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).build());
    }

    public JSONObject registerDevice(String token, int userid, int type) {
        url = BASE_URL + Endpoint.REGISTER_DEVICE.path;
        Log.d("register url", url);
        Log.d("register token", token);
        Log.d("register userid", userid+"");
        Log.d("register type", type+"");
        JSONObject  jsonObject = getPostJsonResponse(new FormEncodingBuilder().add("api_token", token).add("user_id", "" + userid).add("type", "" + type).build());
        if(jsonObject != null)
            SupportedClass.savePrefsBoolean(MainApplication.mContext, true, Constants.IS_REGISTERED);
        return jsonObject;
    }

    public JSONObject messageOperation(RequestBody requestBody, int task) {
        switch (task) {
            case 0:
                url = BASE_URL + Endpoint.GET_ALL_MESSAGE.path;
                break;

            case 1:
                url = BASE_URL + Endpoint.SEND_MESSAGE.path;
                break;

            case 2:
                url = BASE_URL + Endpoint.MAKE_ALL_READ.path;
                break;
        }
        Log.d("url", url);
        return getPostJsonResponse(requestBody);
    }



    public JSONObject rateByCustomer(int appointmentReqID, float ratings) {
        url = BASE_URL + Endpoint.RATE_APPOINTMENT_BY_CUST.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).add("rate", "" + ratings).build());
    }

    public JSONObject getNewAppoProf(int professional_id) {
        url = BASE_URL + Endpoint.ALL_NEW_APPONTMENTS_PROF.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("professional_id", "" + professional_id).build());
    }

    public JSONObject acceptApponintment(int appointmentReqID) {
        url = BASE_URL + Endpoint.APPOINTMENT_ACCEPT_PROF.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).build());
    }

    public JSONObject cancelAppointment(int appointmentReqID) {
        url = BASE_URL + Endpoint.APPOINTMENT_CANCEL_PROF.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).build());
    }

    public JSONObject completeAppointment(int appointmentReqID, String amount) {
        url = BASE_URL + Endpoint.APPOINTMENT_COMPLETE_PROF.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).add("amount", amount).build());
    }

    public JSONObject rejectAppointment(int appointmentReqID, int professional_id) {
        url = BASE_URL + Endpoint.APPOINTMENT_REJECT_PROF.path;
        Log.d("url", url + appointmentReqID+ professional_id);
        return getPostJsonResponse(new FormEncodingBuilder().add("appointment_request_id", "" + appointmentReqID).add("professional_id", ""+professional_id).build());
    }



    public JSONObject getAcceptedAppoProf(int professionalId) {
        url = BASE_URL + Endpoint.ALL_ACCEPTED_APPOINTMENT.path;
        Log.d("url", url);
        return getPostJsonResponse(new FormEncodingBuilder().add("professional_id", ""+professionalId).build());
    }

    public JSONObject getAppointmentCancel(RequestBody requestBody) {
        url = BASE_URL + Endpoint.APPOINTMENT_CANCEL.path;
        Log.d("url", url);
        return getPostJsonResponse(requestBody);
    }

    public JSONObject getServicesListing() {
        url = BASE_URL + Endpoint.GET_SERVICES.path;
        Log.d("url", url);
        return getPostEmptyBodyResponse();
    }

    public JSONObject getPostEmptyBodyResponse(){
        ApiCalls apiCalls = ApiCalls.getInstance();
        try {
            Response response = apiCalls.doPostNoArgRequest(url);
            Log.d("url", response.toString());
            String responseString = "";
            if (response != null)
                responseString = readStream(response.body().byteStream());

            setResponseString(response.code() + "," + response.message()+","+responseString);

            if (response != null && response.isSuccessful()) {
                return new JSONObject(responseString);
            } else
                return null;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {

            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
