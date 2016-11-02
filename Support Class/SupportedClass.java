package applabs.vabo.support;

import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import applabs.vabo.BaseActivity;
import applabs.vabo.MainApplication;
import applabs.vabo.R;
import applabs.vabo.custom.CustomEditText;
import applabs.vabo.custom.CustomTextView;
import applabs.vabo.db.DBModel;
import applabs.vabo.model.AppointmentModel;
import applabs.vabo.model.CardDetails;
import applabs.vabo.model.CountriesModel;
import applabs.vabo.model.ProfessionalData;
import applabs.vabo.model.ServiceData;
import applabs.vabo.model.StatesModel;
import applabs.vabo.model.TransactionHistoryModel;
import applabs.vabo.model.UserInfo;
import applabs.vabo.professional.ProfessionalMainView;
import applabs.vabo.utils.FileUtils;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class SupportedClass {

    public static final int REQUEST_CODE_WRITE_FILE_PERMISSION = 120;
    public static final int REQUEST_CODE_CAMERA = 121;
    public static final int REQUEST_PHONE_CALL = 122;
    public static final String APP_NAME_PREF = "vabo";
    public static final int APPLICATION_FEES = 10;

    public static String getProfessionalPaidFee(String value){
        value = value.substring(1).trim();
        return "$"+String.valueOf(Math.round(Float.parseFloat(value) - (Integer.parseInt(value) * 0.1)));
    }

    public static boolean emailAddressValidation(String matchemail) {
        String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (matchemail.matches(emailPattern))
            return true;
        else
            return false;
    }

    public static String getProvinceString(String value){
        return value.substring(value.indexOf("(")+1, value.length()-1);
    }

    public static String getStatesString(Context context){
            String json = null;
            try {
                InputStream is = context.getAssets().open("states.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        return json;
    }

    public static ArrayList<CountriesModel> getContriesModel(Context context){
        ArrayList<CountriesModel> arrayList = new ArrayList<>();
        try{
            String json = getStatesString(context);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray Countries = jsonObject.getJSONArray("Countries");
            for (int i=0; i<Countries.length(); i++){
                JSONObject subCountry = Countries.getJSONObject(i);
                CountriesModel countriesModel = new CountriesModel();

                //get all states
                JSONArray stateArray = subCountry.getJSONArray("s");
                ArrayList<StatesModel> allStates = new ArrayList<>();
                for (int j=0; j<stateArray.length(); j++){
                    JSONObject subState = stateArray.getJSONObject(j);
                    StatesModel statesModel = new StatesModel();
                    statesModel.setState(subState.getString("n"));
                    statesModel.setStateabbr(subState.getString("v"));
                    allStates.add(statesModel);
                }

                countriesModel.setCountry(subCountry.getString("n"));
                countriesModel.setCountryabbr(subCountry.getString("v"));
                countriesModel.setAllStates(allStates);
                arrayList.add(countriesModel);
            }

        }
        catch (Exception e){

        }
        return  arrayList;

    }

    public static boolean isAppointmentStartsSoon(Calendar calendar){
        Calendar nowCal = Calendar.getInstance();
        long times = calendar.getTimeInMillis() - nowCal.getTimeInMillis();
        long hour = times / (60*60*1000);
        Log.d("hour isApp starts soon", hour+"");
        if(hour >= 0) {
            if(hour > 2)
                return false;
            return true;
        }
        else
            return false;
    }

    public static boolean isAppointmentStartsSoonIn15Min(Calendar calendar){
        Calendar nowCal = Calendar.getInstance();
        long times = calendar.getTimeInMillis() - nowCal.getTimeInMillis();
        long min = times / (60*1000);
        Log.d("min isApp starts soon", min+"");
        if(min > 0) {
            if(min > 15)
                return false;
            return true;
        }
        else
            return true;
    }


    //accountId varchar(200), businessName varchar(100), accHolderName varchar(100), accHolderAddress varchar(200), accNo,professionalId
    public static ContentValues getAccountContentValues(String accid, String businessname, String accno, String name, String address){
        ContentValues values = new ContentValues();
        values.put("accountId", accid);
        values.put("businessName", businessname);
        values.put("accHolderName", name);
        values.put("accHolderAddress", address);
        values.put("accNo", accno);
        values.put("professionalId", SupportedClass.getIntPrefData(MainApplication.mContext, Constants.CURRENT_USER_ID_PREF));
        return values;
    }

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public static ContentValues getTransactionHistoryValue(TransactionHistoryModel transactionHistoryModel){
        ContentValues values = new ContentValues();
        values.put("id", transactionHistoryModel.getId());
        values.put("date", transactionHistoryModel.getDate());
        values.put("time", transactionHistoryModel.getTime());
        values.put("status", transactionHistoryModel.getStatus());
        values.put("serviceName", transactionHistoryModel.getmServiceName());
        values.put("personName", transactionHistoryModel.getmPersonName());
        values.put("servicePrice", transactionHistoryModel.getmServicePrice());
        return values;
    }

    //helpId INTEGER, helpQuestion varchar(200), helpAnswer varchar(100)
    public static ContentValues getHelpQuestions(JSONObject helpObje){
        ContentValues values = new ContentValues();
        try {
            values.put("helpId", helpObje.getInt("id"));
            values.put("helpQuestion", helpObje.getString("help_question"));
            values.put("helpAnswer", helpObje.getString("help_answer"));

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return values;
    }


    static String uploadpth;
    static Bitmap theBitmap = null;
    public static void refreshImage(final boolean freshload, final Context context, CircleImageView mAddedProfilePhoto, DBModel dbModel, final CustomTextView mAddNewPhotoText){

        uploadpth = context.getString(R.string.upload_path);
        int userId = SupportedClass.getIntPrefData(context, Constants.CURRENT_USER_ID_PREF);
        UserInfo userInfo = dbModel.getUserInfo(userId);
        Log.d("profieImage", userInfo.getProfileImage());
        if(!userInfo.getProfileImage().isEmpty()) {
            uploadpth = context.getString(R.string.txt_amazon_path)+uploadpth.replace("USERID", String.valueOf(userId));
            Log.d("profieImage", uploadpth);

            if(freshload) {
                Picasso.with(context).load(uploadpth)
                        .resize(200, 200)
                        .centerCrop()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(mAddedProfilePhoto, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (mAddNewPhotoText != null)
                                    mAddNewPhotoText.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
            else{
                Picasso.with(context).load(uploadpth)
                        .resize(200, 200)
                        .centerCrop()
                        .into(mAddedProfilePhoto, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (mAddNewPhotoText != null)
                                    mAddNewPhotoText.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }

/*
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        theBitmap = Glide.
                                with(context).
                                load(uploadpth)
                                .asBitmap()
                                .into(-1, -1).
                                        get();
                    } catch (final ExecutionException e) {
                        Log.e("CustPro", e.getMessage());
                    } catch (final InterruptedException e) {
                        Log.e("CustPro", e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void dummy) {
                    if (null != theBitmap) {
                        // The full bitmap should be available here
                        listner.returnBitmp(theBitmap);
                    }

                }
            }.execute();
*/
        }


    }

    public static void updateUserData(String value, DBModel dbModel, Dialog mNoConnectionErrorDialog){
        try {
            JSONObject jsonObject = new JSONObject(value);
            if (TextUtils.equals(jsonObject.getString("status"), "Success")) {
                UserInfo oldInfo = new UserInfo();
                JSONObject userObj =  jsonObject.getJSONArray("userData").getJSONObject(0);
                oldInfo.setUserId(userObj.getInt("id"));
                oldInfo.setFirstname(userObj.getString("firstname"));
                oldInfo.setLastname(userObj.getString("lastname"));
                oldInfo.setDob(userObj.getString("dob"));
                oldInfo.setGender(userObj.getString("gender"));
                oldInfo.setAddress(userObj.getString("address"));
                oldInfo.setCity(userObj.getString("city"));
                oldInfo.setProvince(userObj.getString("province"));
                oldInfo.setCountry(userObj.getString("country"));
                oldInfo.setZipCode(userObj.getString("zip"));
                oldInfo.setCellNumber(userObj.getString("phone"));
                oldInfo.setProfileImage(userObj.getString("img_path"));
                oldInfo.setIsProfessional(userObj.getInt("is_professional"));
                oldInfo.setEmail(SupportedClass.getPrefsString(MainApplication.mContext, Constants.EMAIL_ID_PREF));
                dbModel.addUsers(SupportedClass.setUpdateUserContentValues(oldInfo));
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            mNoConnectionErrorDialog.show();
        }

    }

    public static int openOnBoardingScreen(DBModel dbModel){
        String flag = dbModel.getBoardingFlag(getIntPrefData(MainApplication.mContext, Constants.CURRENT_USER_ID_PREF));
        Log.d("openOnBoardingScreen", flag+"");
        if(flag.isEmpty())
            return 0;
        else if(TextUtils.equals(flag, "1"))
            return 1;
        else if(TextUtils.equals(flag, "2"))
            return 2;
        else
            return 3;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, Bitmap originalBitmap,
                                                         int reqWidth, int reqHeight) {
        return Bitmap.createScaledBitmap(originalBitmap, reqWidth, reqHeight, false);
    }

    /*
    CREATE TABLE CardDetails (custId INTEGER, cardHolderName varchar(200), cardNumber varchar(200),
    cardMonth varchar(200), cardYear varchar(200), cardCVC INTEGER, cardId varchar(200), userId INTEGER)
     */

    public static ContentValues getCardDetailsContents(CardDetails cardDetails) {
        ContentValues values = new ContentValues();
        values.put("custId", cardDetails.getCustId());
        values.put("cardHolderName", cardDetails.getCardHolderName());
        values.put("cardNumber", cardDetails.getCardNo());
        values.put("cardMonth", cardDetails.getCardMonth());
        values.put("cardYear", cardDetails.getCardYear());
        values.put("cardCVC", cardDetails.getCardCVC());
        values.put("cardId", cardDetails.getCardId());
        values.put("userId", cardDetails.getUserId());
        return values;
    }

    public static interface OnSpinnerEventsListener {

        void onSpinnerOpened();

        void onSpinnerClosed();

    }

    public static String getTwoDiditNumber(int number) {
        return String.format("%02d", number);
    }

    public static boolean compareTwoCal(Calendar calendar1, Calendar calendar2){
        long millis1 = calendar1.getTimeInMillis();
        long millis2 = calendar2.getTimeInMillis();
        long millis24hour = 24 * 60 * 60 * 1000;
        if(millis1 < millis2)
            return false;
        if((millis1 - millis2) > millis24hour )
            return true;
        else
            return false;
    }

    public static boolean checkForFileWritePermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationAccessPermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasLocationAccessPermission != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_FILE_PERMISSION);
                return false;
            } else
                return true;
        }
        return true;
    }

    public static boolean checkForCameraPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationAccessPermission = context.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasLocationAccessPermission != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_CAMERA);
                return false;
            } else
                return true;
        }
        return true;
    }

    public static boolean checkForCameraPermissionOnly(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationAccessPermission = context.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasLocationAccessPermission != PackageManager.PERMISSION_GRANTED) {

                return false;
            } else
                return true;
        }
        return true;
    }

    public static boolean checkForPhoneCall(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationAccessPermission = context.checkSelfPermission(Manifest.permission.CALL_PHONE);
            if (hasLocationAccessPermission != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_PHONE_CALL);
                Toast.makeText(context, R.string.txt_phone_call_permision_require, Toast.LENGTH_SHORT).show();
                return false;
            } else
                return true;
        }
        return true;
    }

    public static Dialog getDialog(int layoutId, Activity activity) {
        Dialog mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(layoutId);
        return mDialog;
    }

    public static String getDay(int dayCount, Context context) {
        Log.d("getDay", dayCount + "");
        return context.getResources().getStringArray(R.array.array_day)[dayCount];
    }

    public static String getMonth(int monthCount, Context context) {
        Log.d("getMonth", monthCount + "");
        return context.getResources().getStringArray(R.array.array_month)[monthCount];
    }

    public static String getDateString(Calendar calendar, Context context) {
        String day = getDay(calendar.get(Calendar.DAY_OF_WEEK) - 1, context);
        String month = getMonth(calendar.get(Calendar.MONTH), context);
        //String time = getTwoDiditNumber(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTwoDiditNumber(calendar.get(Calendar.MINUTE));
        return day + ", " + month + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR) ;//+ " @ " + time;
    }

    public static String getTimeString(Calendar calendar, int addminutes){
        String time = convert24to12(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        String[] parts = time.split(":");
        String[] parts1 = parts[1].split(" ");

        String completetime = parts[0] + ":" + parts1[0] + " - ";
        calendar.add(Calendar.MINUTE, addminutes);

        time = convert24to12(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        parts = time.split(":");
        parts1 = parts[1].split(" ");
        completetime = completetime + parts[0] + ":" + parts1[0] + " " + parts1[1];
        return completetime;

    }

    public static Calendar stringToCalender(String string) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = simpleDateFormat.parse(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(date);
        return calendar;
    }

    public static String calendarToString(Calendar calendar) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String timeZoneToDefaultTime(String timeString) {

        SimpleDateFormat sourceFormat = new SimpleDateFormat("HH:mm");
        sourceFormat.setTimeZone(TimeZone.getDefault());

        Date parsed;
        String dateAsString = "";
        try {
            parsed = sourceFormat.parse(timeString);
            sourceFormat.setTimeZone(TimeZone.getDefault());
            dateAsString = sourceFormat.format(parsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("value", dateAsString);
        return dateAsString;
    }



    public static String timeToTimeZoneTime(String timeString, String timezone) {

        Log.d("timezone", timezone);
        SimpleDateFormat sourceFormat = new SimpleDateFormat("HH:mm");
        sourceFormat.setTimeZone(TimeZone.getDefault());

        Date parsed;
        String dateAsString = "";
        try {
            parsed = sourceFormat.parse(timeString);
            sourceFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            dateAsString = sourceFormat.format(parsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("value", dateAsString);
        return dateAsString;
    }

    public static String convert24to12(String time) {
        String convertedTime = "";
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
            Date date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
            System.out.println("convertedTime : " + convertedTime);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return convertedTime.toUpperCase();
        //Output will be 10:23 PM
    }

    public static String convert12to24(String time) {
        time = time.toLowerCase();
        String convertedTime = "";
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
            Date date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
            System.out.println("convertedTime : " + convertedTime);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return convertedTime.toUpperCase();
        //Output will be 10:23 PM
    }

    public static ContentValues getBookedAppoContentValues(AppointmentModel appointmentModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put("appoReqId", appointmentModel.getAppId());
        contentValues.put("messageCount", appointmentModel.getmessageCount());

        contentValues.put("rateByProf", appointmentModel.getRateByProf());
        contentValues.put("rateByCust", appointmentModel.getRateByCust());
        contentValues.put("status", appointmentModel.getStatus());
        contentValues.put("userId", appointmentModel.getUserId());
        contentValues.put("appoTime", appointmentModel.getAppoTime());
        contentValues.put("appoExtraNotes", appointmentModel.getAppExtraNotes());
        contentValues.put("appoPaymentMethod", appointmentModel.getAppoPaymentMethod());
        contentValues.put("appoRecureStatus", appointmentModel.getAppoRecureStatus());
        contentValues.put("serviceId", appointmentModel.getServiceData().getServiceId());
        contentValues.put("serviceCategory", appointmentModel.getServiceData().getServiceGroup());
        contentValues.put("serviceCategoryId", appointmentModel.getServiceData().getServiceGroupId());
        contentValues.put("serviceName", appointmentModel.getServiceData().getServiceName());
        contentValues.put("professionalName", appointmentModel.getProfessionalName());
        contentValues.put("professionalNumber", appointmentModel.getProfessionalNumber());
        contentValues.put("serviceDesc", appointmentModel.getServiceData().getServiceDesc());
        contentValues.put("servicePrice", appointmentModel.getServiceData().getServicePrice());
        contentValues.put("serviceDuration", appointmentModel.getServiceData().getServiceDuration());
        return contentValues;
    }

    public static ContentValues getAppoRequestsContentValues(AppointmentModel appointmentModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put("appoReqId", appointmentModel.getAppId());
        contentValues.put("messageCount", appointmentModel.getmessageCount());
        contentValues.put("rateByProf", appointmentModel.getRateByProf());
        contentValues.put("rateByCust", appointmentModel.getRateByCust());
        contentValues.put("userId", appointmentModel.getUserId());
        contentValues.put("appoTime", appointmentModel.getAppoTime());
        contentValues.put("appoExtraNotes", appointmentModel.getAppExtraNotes());
        contentValues.put("appoPaymentMethod", appointmentModel.getAppoPaymentMethod());
        contentValues.put("appoRecureStatus", appointmentModel.getAppoRecureStatus());
        contentValues.put("serviceId", appointmentModel.getServiceData().getServiceId());
        contentValues.put("serviceCategory", appointmentModel.getServiceData().getServiceGroup());
        contentValues.put("serviceCategoryId", appointmentModel.getServiceData().getServiceGroupId());
        contentValues.put("serviceName", appointmentModel.getServiceData().getServiceName());
        contentValues.put("servicePrice", appointmentModel.getServiceData().getServicePrice());
        contentValues.put("serviceDuration", appointmentModel.getServiceData().getServiceDuration());
        contentValues.put("serviceDesc", appointmentModel.getServiceData().getServiceDesc());
        contentValues.put("status", appointmentModel.getStatus());
        contentValues.put("professionalId", appointmentModel.getProfessionalId());
        return contentValues;
    }

    public static ContentValues setSignupContentValues(UserInfo userInfo){
        ContentValues contentValues = new ContentValues();
        if(userInfo.getUserId() != -1)
            contentValues.put("userId", userInfo.getUserId());
        contentValues.put("email", userInfo.getEmail());
        contentValues.put("token", userInfo.getToken());
        contentValues.put("password", userInfo.getPassword());
        contentValues.put("firstname", userInfo.getFirstname());
        contentValues.put("lastname", userInfo.getLastname());
        contentValues.put("dob", userInfo.getDob());
        contentValues.put("gender", userInfo.getGender());
        contentValues.put("profileImage", userInfo.getProfileImage());
        contentValues.put("address", userInfo.getAddress());
        contentValues.put("city", userInfo.getCity());
        contentValues.put("province", userInfo.getProvince());
        contentValues.put("country", userInfo.getCountry());
        contentValues.put("zipCode", userInfo.getZipCode());
        contentValues.put("cellNumber", userInfo.getCellNumber());
        contentValues.put("isProfessional", userInfo.getIsProfessional());
        return contentValues;
    }

    public static ContentValues setUpdateUserContentValues(UserInfo userInfo){
        ContentValues contentValues = new ContentValues();
        contentValues.put("userId", userInfo.getUserId());
        contentValues.put("firstname", userInfo.getFirstname());
        contentValues.put("lastname", userInfo.getLastname());
        contentValues.put("dob", userInfo.getDob());
        contentValues.put("gender", userInfo.getGender());
        contentValues.put("profileImage", userInfo.getProfileImage());
        if(userInfo.getEmail() != null || !userInfo.getEmail().isEmpty())
            contentValues.put("email", userInfo.getEmail());
        contentValues.put("address", userInfo.getAddress());
        contentValues.put("city", userInfo.getCity());
        contentValues.put("province", userInfo.getProvince());
        contentValues.put("country", userInfo.getCountry());
        contentValues.put("zipCode", userInfo.getZipCode());
        contentValues.put("cellNumber", userInfo.getCellNumber());
        contentValues.put("isProfessional", userInfo.getIsProfessional());
        return contentValues;
    }

    public static ContentValues setUpdateUserCell(UserInfo userInfo){
        ContentValues contentValues = new ContentValues();
        contentValues.put("userId", userInfo.getUserId());
        contentValues.put("cellNumber", userInfo.getCellNumber());
        return contentValues;
    }


    public static ContentValues setProfessionalContentValues(ProfessionalData profeInfo){
        ContentValues contentValues = new ContentValues();
        contentValues.put("userId", profeInfo.getUserId());
        contentValues.put("que1", profeInfo.getQue1());
        contentValues.put("que2", profeInfo.getQue2());
        contentValues.put("que3", profeInfo.getQue3());
        contentValues.put("que4", profeInfo.getQue4());
        contentValues.put("que5", profeInfo.getQue5());
        contentValues.put("que6", profeInfo.getQue6());
        return contentValues;
    }

    public static ContentValues setMessageData(JSONObject messageObj){
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("messageid", messageObj.getInt("id"));
            contentValues.put("userId", messageObj.getInt("user_id"));
            contentValues.put("appointmentRequestId", messageObj.getInt("appointment_request_id"));
            contentValues.put("senderName", messageObj.getString("firstname"));
            contentValues.put("senderImage", messageObj.getString("img_path"));
            contentValues.put("message", messageObj.getString("message"));
            contentValues.put("messageTime", messageObj.getString("time"));
            contentValues.put("messageIdentity", messageObj.getString("type"));
            contentValues.put("status", messageObj.getInt("status"));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return contentValues;
    }

    public static void clearPrefs() {
        FileUtils fileUtils = new FileUtils(MainApplication.mContext, "Images");
        File file = fileUtils.getSimpleFile(Constants.PROFILE_IMAGE);
        if(file.exists()){
            file.delete();
        }


        savePrefsString(MainApplication.mContext, "", Constants.CAMERA_IMAGE_PATH);
        savePrefsString(MainApplication.mContext, "", Constants.START_TIME_MILLIS);
        saveIntPrefData(MainApplication.mContext, -1, Constants.CURRENT_USER_ID_PREF);
        savePrefsString(MainApplication.mContext, "", Constants.EMAIL_ID_PREF);
        saveIntPrefData(MainApplication.mContext, -1, Constants.RUNNING_APPO_ID);
        savePrefsString(MainApplication.mContext, "", Constants.LOGIN_TOKEN);
        SupportedClass.savePrefsBoolean(MainApplication.mContext, true, Constants.NOTIFICATION_ENABLE);
        SupportedClass.savePrefsBoolean(MainApplication.mContext, false, Constants.IS_REGISTERED);
    }

    public static String getPrefsString(Context con, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        return prefs.getString(key, "");
    }


    public static boolean getPrefsBoolean(Context con, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static void savePrefsString(Context con, String keyvalue, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, keyvalue);
        editor.commit();
    }

    public static void saveIntPrefData(Context con, int keyvalue, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, keyvalue);
        editor.commit();
    }

    public static int getIntPrefData(Context con, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public static void savePrefsBoolean(Context con, boolean keyvalue, String key) {
        SharedPreferences prefs = con.getSharedPreferences(APP_NAME_PREF,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, keyvalue);
        editor.commit();
    }

    public static void decodeAppointmentResponse(JSONObject jsonObject, Activity activity, DBModel dbModel, String name){
        try{

            ArrayList<AppointmentModel> allRequestApp = new ArrayList<>();
            if(TextUtils.equals("Success", jsonObject.getString("status"))) {


                dbModel.deleteTable(name);

                JSONArray appoData = jsonObject.getJSONArray("appointment_requestData");
                for (int i = 0; i < appoData.length(); i++) {
                    AppointmentModel appointmentModel = new AppointmentModel();
                    JSONObject appbj = appoData.getJSONObject(i);
                    appointmentModel.setUserId(appbj.getJSONArray("userData").getJSONObject(0).getInt("id"));
                    appointmentModel.setAppId(appbj.getInt("appointment_request_id"));
                    appointmentModel.setMessageCount(appbj.getInt("message_count"));
                    appointmentModel.setAppoPaymentMethod("PAYPAL");//appbj.getString("PAYPAL"));
                    appointmentModel.setAppoTime(appbj.getString("schedule_date"));
                    appointmentModel.setAppExtraNotes(appbj.getString("notes"));
                    appointmentModel.setAppoRecureStatus(appbj.getString("recure_id"));
                    appointmentModel.setStatus(appbj.getString("status"));
                    appointmentModel.setProfessionalId(appbj.getInt("professional_id"));
                    appointmentModel.setRateByCust(appbj.getString("rate_by_cust"));
                    appointmentModel.setRateByProf(appbj.getString("rate_by_prof"));

                    ServiceData serviceData = new ServiceData(appbj.getInt("service_id"),
                            appbj.getInt("category_id"),
                            appbj.getString("catagory_name"),
                            appbj.getString("name"),
                            appbj.getString("duration"),
                            appbj.getString("price"),
                            appbj.getString("gender"),
                            appbj.getString("description"));
                    appointmentModel.setServiceData(serviceData);

                    if(TextUtils.equals(name, Constants.ACCEPTED_APPOINTMENT_REQUESTS))
                        dbModel.addAcceptedAppointment(SupportedClass.getAppoRequestsContentValues(appointmentModel));
                    else
                        dbModel.addRequestedAppointment(SupportedClass.getAppoRequestsContentValues(appointmentModel));
                    allRequestApp.add(appointmentModel);

                    JSONObject userObj =  appbj.getJSONArray("userData").getJSONObject(0);
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(userObj.getInt("id"));
                    userInfo.setEmail(userObj.getString("email"));
                    userInfo.setFirstname(userObj.getString("firstname"));
                    userInfo.setLastname(userObj.getString("lastname"));
                    userInfo.setDob(userObj.getString("dob"));
                    userInfo.setGender(userObj.getString("gender"));
                    userInfo.setAddress(userObj.getString("address"));
                    userInfo.setCity(userObj.getString("city"));
                    userInfo.setProvince(userObj.getString("province"));
                    userInfo.setCountry(userObj.getString("country"));
                    userInfo.setZipCode(userObj.getString("zip"));
                    userInfo.setCellNumber(userObj.getString("phone"));
                    userInfo.setIsProfessional(0);

                    dbModel.addUsers(SupportedClass.setUpdateUserContentValues(userInfo));


                }

            }
            else
                Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
            ((ProfessionalMainView)activity).showConnectionErrorDialog();
        }
    }

    public static Bundle getCustomerNotificationBundle(String notificationType){
        Bundle bundle = new Bundle();
        switch (notificationType){
            case Constants.ACCEPT_APPOINTMENT:
            case Constants.START_APPOINTMENT:
            case Constants.NEW_MESSAGE:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, true);
                return bundle;
            case Constants.COMPLETE_APPOINTMENT:
            case Constants.PROFESSIONAL_REVIEW:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, true);
                bundle.putBoolean(Constants.OPEN_COMPLETE_TAB, true);
                return bundle;
            default:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, true);
                return bundle;
        }
    }

    public static Bundle getProfessionalNotificationBundle(String notificationType){
        Bundle bundle = new Bundle();
        switch (notificationType){
            case Constants.CREATE_APPOINTMENT:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, false);
                return bundle;
            case Constants.NEW_MESSAGE:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, true);
                return bundle;
            case Constants.CUSTOMER_REVIEW:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, true);
                bundle.putBoolean(Constants.OPEN_COMPLETE_TAB, true);
                return bundle;

            default:
                bundle.putBoolean(Constants.OPEN_APPO_TAB, false);
                return bundle;
        }
    }

    public static class UsPhoneNumberFormatter implements TextWatcher {
        // This TextWatcher sub-class formats entered numbers as 1 (123)
        // 456-7890
        private boolean mFormatting; // this is a flag which prevents the
        // stack(onTextChanged)
        private boolean clearFlag;
        private int mLastStartLocation;
        private String mLastBeforeText;
        private WeakReference<CustomEditText> mWeakEditText;

        public UsPhoneNumberFormatter(WeakReference<CustomEditText> weakEditText) {
            this.mWeakEditText = weakEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (after == 0 && s.toString().equals("1 ")) {
                clearFlag = true;
            }
            mLastStartLocation = start;
            mLastBeforeText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO: Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Make sure to ignore calls to afterTextChanged caused by the work
            // done below
            if (!mFormatting) {
                mFormatting = true;
                int curPos = mLastStartLocation;
                Log.d("curPos", curPos + "");

                String beforeValue = mLastBeforeText;
                Log.d("beforeValue", beforeValue);

                String currentValue = s.toString();
                Log.d("currentValue", currentValue);

                String formattedValue = formatUsNumber(s);
                Log.d("formattedValue", formattedValue);

				/*
				 * if (currentValue.length() > beforeValue.length()) {
				 * Log.d("true","called" ); int setCusorPos =
				 * formattedValue.length() - (beforeValue.length() - curPos);
				 * mWeakEditText.get().setSelection( setCusorPos < 0 ? 0 :
				 * setCusorPos); } else { Log.d("false","called" ); int
				 * setCusorPos = formattedValue.length() -
				 * (currentValue.length() - curPos); if (setCusorPos > 0 &&
				 * !Character.isDigit(formattedValue .charAt(setCusorPos - 1)))
				 * { setCusorPos--; } mWeakEditText.get().setSelection(
				 * setCusorPos < 0 ? 0 : setCusorPos); }
				 */
                int setCusorPos = formattedValue.length();
                // - (beforeValue.length() - curPos);
                mWeakEditText.get().setSelection(
                        setCusorPos < 0 ? 0 : setCusorPos);

                mFormatting = false;
            }
        }

        private String formatUsNumber(Editable text) {
            StringBuilder formattedString = new StringBuilder();
            // Remove everything except digits
            int p = 0;
            while (p < text.length()) {
                char ch = text.charAt(p);
                if (!Character.isDigit(ch)) {
                    text.delete(p, p + 1);
                } else {
                    p++;
                }
            }
            // Now only digits are remaining
            String allDigitString = text.toString();

            int totalDigitCount = allDigitString.length();

            if (totalDigitCount == 0 || (totalDigitCount > 10)// &&
                    // !allDigitString.startsWith("1"))
                    || totalDigitCount > 11) {
                // May be the total length of input length is greater than the
                // expected value so we'll remove all formatting
                text.clear();
                text.append(allDigitString);
                return allDigitString;
            }
            int alreadyPlacedDigitCount = 0;
            // Only '1' is remaining and user pressed backspace and so we clear
            // the edit text.
			/*
			 * if (allDigitString.equals("1") && clearFlag) { text.clear();
			 * clearFlag = false; return ""; } if
			 * (allDigitString.startsWith("1")) { formattedString.append("1 ");
			 * alreadyPlacedDigitCount++; }
			 */
            // The first 3 numbers beyond '1' must be enclosed in brackets "()"
            if (totalDigitCount - alreadyPlacedDigitCount > 3) {
                formattedString.append("("
                        + allDigitString.substring(alreadyPlacedDigitCount,
                        alreadyPlacedDigitCount + 3) + ") ");
                alreadyPlacedDigitCount += 3;
            }
            // There must be a '-' inserted after the next 3 numbers
            if (totalDigitCount - alreadyPlacedDigitCount > 3) {
                formattedString.append(allDigitString.substring(
                        alreadyPlacedDigitCount, alreadyPlacedDigitCount + 3)
                        + "-");
                alreadyPlacedDigitCount += 3;
            }
            // All the required formatting is done so we'll just copy the
            // remaining digits.
            if (totalDigitCount > alreadyPlacedDigitCount) {
                formattedString.append(allDigitString
                        .substring(alreadyPlacedDigitCount));
            }

            text.clear();
            text.append(formattedString.toString());
            return formattedString.toString();
        }

    }

    public static String simpleCellNumber(String number){
        number = number.replace("(","");
        number = number.replace(")","");
        number = number.replace(" ","");
        number = number.replace("-","");
        return number;

    }

}
