package applabs.vabo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import applabs.vabo.adapter.SpinnerListItemAdapter;
import applabs.vabo.custom.CustomEditText;
import applabs.vabo.custom.CustomTextView;
import applabs.vabo.customer.BookAppointmentActivity;
import applabs.vabo.customer.CustomerMainView;
import applabs.vabo.customer.RateAppointmentActivity;
import applabs.vabo.db.DBModel;
import applabs.vabo.model.AppointmentModel;
import applabs.vabo.model.CardDetails;
import applabs.vabo.model.StatesModel;
import applabs.vabo.model.UserInfo;
import applabs.vabo.network.ApiRequests;
import applabs.vabo.professional.ProfessionalMainView;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;
import applabs.vabo.utils.FileUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity {


    public Uri imageUri;
    public Uri selectedImageUri;
    FileUtils fileUtils;

    public Intent getNexIntent(int isProf){
        Intent intent = null;
        Bundle bundle= getIntent().getExtras();

        if(SupportedClass.openOnBoardingScreen(dbModel) <3) {
            intent = new Intent(getAppCon(), OnBoardingActivity.class);
            intent.putExtra(Constants.IS_PROFE, isProf);
        }
        else {
            if(isProf == 0)
                intent = new Intent(getAppCon(), CustomerMainView.class);
            else
                intent = new Intent(getAppCon(), ProfessionalMainView.class);
            if(bundle != null){
                intent.putExtras(bundle);
            }
        }
        return intent;
    }

    public void getUnRatedAppointment(Activity mActivity) {

        AppointmentModel appointmentModel = dbModel.getUnRatedAppointment();
        if (appointmentModel.getAppId() != -1) {

            Intent intent = new Intent(mActivity, RateAppointmentActivity.class);
            intent.putExtra("isProf", 0);
            intent.putExtra("isCancelable", false);
            intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
            ((CustomerMainView) mActivity).startActivityForResult(intent, 2);


//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
//            dialogBuilder.setMessage(getString(R.string.txt_unrate_appointment_dialog));
//            dialogBuilder.setCancelable(false);
//            dialogBuilder
//                    .setNegativeButton(getString(R.string.txt_later), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    })
//
//                    .setPositiveButton(R.string.txt_rate_now, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//
//                }
//            });
//            AlertDialog alertDialog = dialogBuilder.create();
//            alertDialog.show();
        }


    }

    public Context getAppCon() {
        return MainApplication.mContext;
    }

    //define dialog
    public Dialog mConfirmationDialog, mRejectAppoDialog;
    public Dialog mChooseServiceDialog, mErrorDialog, mNoConnectionErrorDialog;

    //api request
    public ApiRequests apiRequests = new ApiRequests();
    public ProgressDialog mProgressDialog;

    //binding dialog class
    public BindConfirmationCell bindConfirmationCell;
    public BindErrorClass bindErrorClass;
    public BindRejectAppointment bindRejectAppo;
    public BindChoosedService bindChoosedService;
    public PopupWindow popupWindow;
    public ListView mPopUpListView;
    public CustomTextView mSelectedValue;
    public SpinnerListItemAdapter spinnerListItemAdapter;
    public DBModel dbModel = DBModel.getInstance(getAppCon());

    public int getSelectedIndexSpinnerStates(ArrayList<StatesModel> data, String value) {
        for (int i = 0; i < data.size(); i++) {
            if (TextUtils.equals(data.get(i).getStateabbr(), value)) {
                Log.d("province done", i + "");
                return i;
            }
        }
        return 0;
    }

    public int getCurrnrUserId() {
        return SupportedClass.getIntPrefData(getAppCon(), Constants.CURRENT_USER_ID_PREF);
    }

    public int initDropDownPopUp(final Activity activity, final View shadowRelative) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.dialog_spinner_popup, null, false);
        mPopUpListView = (ListView) popUpView.findViewById(R.id.popupList);
        mSelectedValue = (CustomTextView) popUpView.findViewById(R.id.txt_selectedValue);
        RelativeLayout mSelectedLayout = (RelativeLayout) popUpView.findViewById(R.id.selected_value);
        popupWindow = new PopupWindow(activity);
        popupWindow.setContentView(popUpView);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                shadowRelative.setVisibility(View.GONE);
            }
        });

        mSelectedValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        return mSelectedLayout.getHeight();
    }

    public String getUserAddress(int userId, DBModel dbModel) {
        UserInfo oldInfo = dbModel.getUserInfo(userId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(oldInfo.getAddress() + ",\n");
        stringBuilder.append(oldInfo.getCity() + ", " + oldInfo.getProvince() + ",\n" + oldInfo.getCountry() + ", " + oldInfo.getZipCode());
        return stringBuilder.toString();
    }

    public String getUserAddressPostalCode(int userId, DBModel dbModel) {
        UserInfo oldInfo = dbModel.getUserInfo(userId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(oldInfo.getZipCode());
        return stringBuilder.toString();
    }


    public void showErrorDialog() {
        try {
            if (apiRequests.getResponseString() != null) {
                Log.d("response error", apiRequests.getResponseString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void performCameraAction(FileUtils mFileUtils, String filename) {
        selectedImageUri = null;
        File file = mFileUtils.getSimpleFile(filename);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        Log.d("file", file.getAbsolutePath());

        // imageUri is the current activity attribute, define and save it
        // for later usage (also in onSaveInstanceState)
        imageUri = Uri.fromFile(file);
        SupportedClass.savePrefsString(MainApplication.mContext, file.getAbsolutePath(), Constants.CAMERA_IMAGE_PATH);
        // create new Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, 3);
    }

    public void performGalleryAction() {
        selectedImageUri = null;
        Intent gintent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        gintent.setType("image/*");
        // gintent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gintent, 2);
    }

    public void setToolbar(Toolbar mToolbar, CustomTextView headerText, String title, boolean isBackEnable) {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/avenir_medium.ttf");
        mToolbar.setTitle("");
        headerText.setText(title);
        mToolbar.setTitleTextColor(ContextCompat.getColor(getAppCon(), R.color.color_white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(isBackEnable);
    }

    public void setToolbar(Toolbar mToolbar, CustomTextView headerText, int title, boolean isBackEnable) {
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/avenir_medium.ttf");
        mToolbar.setTitle("");
        headerText.setText(title);
        mToolbar.setTitleTextColor(ContextCompat.getColor(getAppCon(), R.color.color_white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(isBackEnable);
    }

    public class BindConfirmationCell {

        int isProfessional;
        Activity activity;

        public BindConfirmationCell(int isProfe, Activity activity) {
            this.isProfessional = isProfe;
            this.activity = activity;
        }

        @OnClick(R.id.tv_dialog_submit)
        public void submitButtonClicked() {
            mConfirmationDialog.cancel();
            Intent intent = new Intent(getAppCon(), OnBoardingActivity.class);
            intent.putExtra(Constants.IS_PROFE, isProfessional);
            startActivity(intent);
            activity.finish();
        }

        @OnClick(R.id.ic_dialog_close)
        public void closeButtonClicked() {
            mConfirmationDialog.cancel();
        }


    }

    public class BindErrorClass {

        Activity activity;
        boolean isFinish = false;

        @Bind(R.id.tv_dialog_submit)
        public CustomTextView mSubmitButton;

        @Bind(R.id.tv_dialog_signin)
        public CustomTextView mSignInButton;

        @Bind(R.id.tv_dialog_text)
        public CustomTextView mDialogText;

        public void setIsFinish(boolean isFinish) {
            this.isFinish = isFinish;
        }

        public void changeAlignment(boolean isLeft) {
            mDialogText.setGravity(isLeft ? Gravity.LEFT : Gravity.CENTER);
        }

        public void setText(String showError) {
            mDialogText.setText(showError);
            changeAlignment(false);
        }

        public BindErrorClass(Activity activity) {
            this.activity = activity;
        }

        @OnClick(R.id.tv_dialog_submit)
        public void submitButtonClicked() {
            mErrorDialog.cancel();
            if (isFinish) {
                Intent intent = new Intent();
                intent.putExtra("close", true);
                setResult(RESULT_OK, intent);
                activity.finish();
            }
        }


    }

    public class BindConnectionErrorDialogClass {

        Activity activity;

        @Bind(R.id.tv_dialog_submit)
        public CustomTextView mSubmitButton;

        @Bind(R.id.tv_dialog_text)
        public CustomTextView mDialogText;

        public void setText() {
            mDialogText.setText(R.string.txt_timeout_message);
        }

        public BindConnectionErrorDialogClass(Activity activity) {
            this.activity = activity;
        }

        @OnClick(R.id.tv_dialog_submit)
        public void submitButtonClicked() {
            mNoConnectionErrorDialog.cancel();

        }
    }

    public BindConnectionErrorDialogClass bindConnectionErrorDialogClass;

    public void bindConnectionErrorClass() {
        mNoConnectionErrorDialog = SupportedClass.getDialog(R.layout.dialog_text_display, this);
        bindConnectionErrorDialogClass = new BindConnectionErrorDialogClass(this);
        ButterKnife.bind(bindConnectionErrorDialogClass, mNoConnectionErrorDialog);
        bindConnectionErrorDialogClass.setText();
        bindConnectionErrorDialogClass.mSubmitButton.setText(R.string.txt_ok);
    }

    public class BindRejectAppointment {

        int isCancel;
        AppointmentModel bookedAppointment;
        ReturnAppointmentListner returnAppointmentListner;

        public void setBookedAppointment(AppointmentModel bookedAppointment) {
            this.bookedAppointment = bookedAppointment;
        }

        public void setReturnListner(ReturnAppointmentListner returnAppointmentListner) {
            this.returnAppointmentListner = returnAppointmentListner;
        }

        public void setIdentity(int isCancel) { // 1: cancel appointment 2: reject appointment
            this.isCancel = isCancel;
        }


        @Bind(R.id.tv_dialog_text)
        public CustomTextView mDialogText;

        @Bind(R.id.tv_dialog_submit)
        public CustomTextView mDialogButtonText;

        @OnClick(R.id.tv_dialog_submit)
        public void submitButtonClicked() {

            mRejectAppoDialog.cancel();
            if (apiRequests.isNetworkAvailable(getAppCon())) {
                if (isCancel == 1) {
                    new AppointmentAcceptRejectCalls(returnAppointmentListner, 3, bookedAppointment).execute(bookedAppointment);
                } else
                    new AppointmentAcceptRejectCalls(returnAppointmentListner, 2, bookedAppointment).execute(bookedAppointment);
            } else
                Snackbar.make(mainActivityLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();

        }

        @OnClick(R.id.ic_dialog_close)
        public void closeButtonClicked() {
            mRejectAppoDialog.cancel();
        }


    }

    public class BindChoosedService {

        AppointmentModel bookedAppointment;
        ReturnAppointmentListner returnAppointmentListner;

        public void setBookedAppointment(AppointmentModel bookedAppointment) {
            this.bookedAppointment = bookedAppointment;
        }

        public void setReturnListner(ReturnAppointmentListner returnAppointmentListner) {
            this.returnAppointmentListner = returnAppointmentListner;
        }

        @Bind(R.id.layout_prof_side)
        public LinearLayout mLayoutProfSide;

        @Bind(R.id.tv_dialog_service_time)
        public CustomTextView mServiceDialogTime;

        @Bind(R.id.layout_number)
        public RelativeLayout mNumberLayout;

        @Bind(R.id.layout_address)
        public RelativeLayout mAddressLayout;

        @Bind(R.id.tv_dialog_service_number)
        public CustomTextView mServiceDialogNumber;

        @Bind(R.id.tv_dialog_service_address)
        public CustomTextView mServiceDialogAddress;

        @Bind(R.id.tv_dialog_book)
        public CustomTextView mServiceDialogConfirm;

        @Bind(R.id.txt_professional_notes)
        public CustomTextView mExtraNotes;

        @OnClick({R.id.ic_map_icon, R.id.layout_address})
        public void openMap() {
            Intent searchAddress = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + mServiceDialogAddress.getText().toString()));
            //Intent searchAddress = new  Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q="+getUserAddress(bookedAppointment.getUserId(), dbModel)));
            startActivity(searchAddress);
        }

        @OnClick({R.id.ic_number, R.id.tv_dialog_service_number})
        public void callNow() {
            //if (SupportedClass.checkForPhoneCall(BaseActivity.this)) {
                String number = mServiceDialogNumber.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                startActivity(intent);
            //}

        }

        @OnClick(R.id.tv_dialog_book)
        public void bookDialogClicked() {
            mChooseServiceDialog.cancel();
//            if(mLayoutProfSide.getVisibility() == View.GONE) {
//                Intent in = new Intent(getAppCon(), BookAppointmentActivity.class);
//                in.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, bookedAppointment);
//                startActivity(in);
//            }
//            else{
            if (TextUtils.equals(getString(R.string.txt_edit), mServiceDialogConfirm.getText().toString())) {

            }
            if (TextUtils.equals(getString(R.string.txt_accept), mServiceDialogConfirm.getText().toString())) {
                if (apiRequests.isNetworkAvailable(getAppCon()))
                    new AppointmentAcceptRejectCalls(returnAppointmentListner, 1, bookedAppointment).execute(bookedAppointment);
                else
                    Snackbar.make(mainActivityLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
            }
            //}
        }

        @OnClick(R.id.ic_dialog_close)
        public void closeButtonClicked() {
            mChooseServiceDialog.cancel();
        }

        @Bind(R.id.tv_dialog_service_name)
        public CustomTextView mServiceDialogName;

        @Bind(R.id.tv_dialog_service_group)
        public CustomTextView mServiceDialogGroup;

        @Bind(R.id.tv_dialog_service_price)
        public CustomTextView mServiceDialogPrice;


        @Bind(R.id.tv_dialog_service_duration)
        public CustomTextView mServiceDialogDuration;
    }

    public void hideKeyboard(CustomEditText customEditText) {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (customEditText != null) {
            imm.hideSoftInputFromWindow(customEditText.getWindowToken(), 0);
        } else if (view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


    }

    public void toastMessage(int toast) {
        Snackbar.make(mainActivityLayout, toast, Snackbar.LENGTH_SHORT).show();
    }


    public interface ReturnListner {
        public void returnResult(String value);
    }

    public interface ReturnBitmapProfile {
        public void returnBitmp(android.graphics.Bitmap bitmap);
    }


    public interface ReturnAppointmentListner {
        public void acceptAppointment(String value, AppointmentModel appointmentModel);

        public void rejectAppointment(String value, AppointmentModel appointmentModel);

    }

    public class CreateUser extends AsyncTask<RequestBody, Void, JSONObject> {

        public ReturnListner listener;
        public boolean isNew;

        public CreateUser(ReturnListner listner, boolean isNew) {
            this.listener = listner;
            this.isNew = isNew;
        }

        @Override
        protected void onPreExecute() {
            if (isNew)
                mProgressDialog.setMessage(getString(R.string.txt_creating_user));
            else
                mProgressDialog.setMessage(getString(R.string.txt_updating_user));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject aVoid) {
            mProgressDialog.cancel();
            if (aVoid != null) {
                Log.d("response", aVoid.toString());
                if (listener != null) {
                    listener.returnResult(aVoid.toString());
                }
            } else {
                mNoConnectionErrorDialog.show();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected JSONObject doInBackground(RequestBody... params) {
            if (isNew)
                return apiRequests.createUser(params[0], "");
            else
                return apiRequests.updateUser(params[0]);

        }
    }

    public class AppointmentCreateCalls extends AsyncTask<RequestBody, Void, JSONObject> {

        public ReturnListner listener;
        int identity;

        public AppointmentCreateCalls(ReturnListner listner, int val) {
            this.listener = listner;
            this.identity = val;
        }

        @Override
        protected void onPreExecute() {
            if (identity != 2) {
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject aVoid) {
            if (identity != 2) {
                mProgressDialog.cancel();
            }
            if (aVoid != null) {
                Log.d("response", aVoid.toString());
                if (listener != null)
                    listener.returnResult(aVoid.toString());
            } else {
                listener.returnResult(new JSONObject().toString());
                mNoConnectionErrorDialog.show();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected JSONObject doInBackground(RequestBody... params) {
            return apiRequests.appointmentCalls(params[0], identity);

        }
    }

    public class AppointmentAcceptRejectCalls extends AsyncTask<AppointmentModel, Void, JSONObject> {

        public ReturnAppointmentListner listener;
        int identity;
        AppointmentModel appointmentModel;

        public AppointmentAcceptRejectCalls(ReturnAppointmentListner listner, int val, AppointmentModel appointmentModel) {
            this.listener = listner;
            this.identity = val;
            this.appointmentModel = appointmentModel;
        }

        @Override
        protected void onPreExecute() {
            if (identity == 1)  //1: accept 2:cancel 3:Reject
                mProgressDialog.setMessage(getString(R.string.txt_accepting_appo));
            else if (identity == 2)
                mProgressDialog.setMessage(getString(R.string.txt_rejecting_appo));
            else
                mProgressDialog.setMessage(getString(R.string.txt_cancelling_appo));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject aVoid) {
            mProgressDialog.cancel();
            if (aVoid != null) {
                Log.d("response", aVoid.toString());
                if (listener != null) {
                    if (identity == 1)
                        listener.acceptAppointment(aVoid.toString(), appointmentModel);
                    else
                        listener.rejectAppointment(aVoid.toString(), appointmentModel);
                }
            } else
                mNoConnectionErrorDialog.show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected JSONObject doInBackground(AppointmentModel... params) {
            if (identity == 1)
                return apiRequests.acceptApponintment(params[0].getAppId());
            else if (identity == 2)
                return apiRequests.rejectAppointment(params[0].getAppId(), SupportedClass.getIntPrefData(getAppCon(), Constants.CURRENT_USER_ID_PREF));
            else
                return apiRequests.cancelAppointment(params[0].getAppId());

        }
    }

    public ReturnListner listnerrrrr = new ReturnListner() {
        @Override
        public void returnResult(String value) {
            if (!TextUtils.isEmpty(value)) {
                Toast.makeText(getAppCon(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                //new UpdateUser(null).execute(apiRequests.getJSONUpdateUserImage(value));
            }
        }
    };


    String mBucketName;
    TransferManager transferManager;
    CognitoCachingCredentialsProvider credentialsProvider;


    public class UploadProfilePicture extends AsyncTask<Void, Void, String> {

        ReturnListner listner;
        File file;
        String uploadname = "";

        public UploadProfilePicture(ReturnListner listner, File file) {
            this.listner = listner;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage(getString(R.string.txt_uploading_image));
            mProgressDialog.setCancelable(false);
            if (listner != null)
                mProgressDialog.show();
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            setupAmzon();
            uploadname = getString(R.string.upload_path);
            uploadname = uploadname.replace("USERID", String.valueOf(SupportedClass.getIntPrefData(getAppCon(), Constants.CURRENT_USER_ID_PREF)));//"staging/"+SupportedClass.getUserPrefData(getAppCon(), Constants.CURRENT_USER_ID_PREF)+"avatar.jpg";//

            //set ACL right
            PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, uploadname, file);
            AccessControlList acl = new AccessControlList();
            acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
            putObjectRequest.setAccessControlList(acl);

            Upload upload = transferManager.upload(putObjectRequest);
            while (!upload.isDone()) {
            }
            if (upload.getProgress().getPercentTransferred() == 100) {
                Log.d("upload photo done", upload.toString());
                Log.d("upload photo done", upload.getDescription());

                return "Success";

            } else {
                Log.d("Return null", "Restart again");
                return null;
            }

        }

        public void setupAmzon() {
            mBucketName = getString(R.string.bucket_name);
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(), // Context
                    "us-east-1:e685830a-9b2b-413e-8761-2e7134af719c", // Identity Pool ID
                    Regions.US_EAST_1
            );
            String identityId = credentialsProvider.getIdentityId();
            String token = credentialsProvider.getToken();
            Log.d("CognitoCaching", identityId + " " + token);
            transferManager = new TransferManager(credentialsProvider);

            /*
            provider = new VaboAuthenticationProvider("login.cct.cctserver", getResources().getString(R.string.poolid));
            provider.setToken(getAmazonData().getToken());

            provider.setIdentityId(getAmazonData().getAmazonId());
            HashMap<String, String> loginsMap = new HashMap<String, String>();
            loginsMap.put("login.cct.cctserver", mBucketName);
            provider.setLogins(loginsMap);
            provider.refresh();

            credentialsProvider = new CognitoCachingCredentialsProvider(
                    getAppCon(), provider, getResources().getString(
                    R.string.unauthRoleArn), getResources().getString(
                    R.string.authRoleArn));
            try {
                credentialsProvider.refresh();
                transferManager = new TransferManager(credentialsProvider);
            }
            catch (Exception e){
                provider.refresh();
                setupAmzon();
            }
            */


        }

        @Override
        protected void onPostExecute(String aVoid) {
            if (listner != null)
                mProgressDialog.dismiss();
            if (TextUtils.equals("Success", aVoid)) {
                if (listner != null) {
                    if (apiRequests.isNetworkAvailable(getAppCon()))
                        new CreateUser(listner, false).execute(new FormEncodingBuilder().add("img_path", uploadname).build());
                    else
                        Snackbar.make(mainActivityLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getBaseContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }
    }

    public class GetStripeDetails extends AsyncTask<Void, Void, JSONObject> {
        RelativeLayout mLoadingLayout;
        ImageView mLoadingImage;
        ReturnListner listner;

        public GetStripeDetails(RelativeLayout mLoadingLayout, ImageView mLoadingImage, ReturnListner listner) {
            this.mLoadingLayout = mLoadingLayout;
            this.mLoadingImage = mLoadingImage;
            this.listner = listner;
        }

        @Override
        protected void onPreExecute() {
            if(mLoadingLayout != null) {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(mLoadingLayout != null) {
                mLoadingLayout.setVisibility(View.GONE);
            }
            if (jsonObject != null)
                listner.returnResult(jsonObject.toString());
            else
                listner.returnResult("");
            super.onPostExecute(jsonObject);
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            return apiRequests.getStripeCards();
        }
    }


    public void addCardsToDB(JSONObject jsonObject) throws JSONException {
        JSONArray data = jsonObject.getJSONArray("cardsData");
        for (int i = 0; i < data.length(); i++) {
            JSONObject cardobj = data.getJSONObject(i);
            String last4 = cardobj.getString("last4");
            String exp_month = cardobj.getString("exp_month");
            String exp_year = cardobj.getString("exp_year");
            String brand = cardobj.getString("brand");
            int userid = SupportedClass.getIntPrefData(getAppCon(), Constants.CURRENT_USER_ID_PREF);


            CardDetails cardDetails = new CardDetails();
            cardDetails.setCardId(cardobj.getString("id"));
            cardDetails.setCardHolderName(cardobj.getString("name"));
            cardDetails.setUserId(userid);
            cardDetails.setCustId(jsonObject.getString("customer_id"));
            cardDetails.setCardNo(brand + " XXXX " + last4);
            cardDetails.setCardMonth(exp_month);
            cardDetails.setCardYear(exp_year);
            cardDetails.setCardCVC("");
            dbModel.addCardDetails(SupportedClass.getCardDetailsContents(cardDetails));
        }
    }

    // hide keyboard on outside screen touch
    public void setupUI(View view, final Activity activity) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof CustomEditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public class SimpleTask extends AsyncTask<String, String, JSONObject> {

        int identity;
        ImageView mLoadingImage;
        RelativeLayout mLoadingLayout;
        ReturnListner returnListner;

        public SimpleTask(int identity, ImageView mLoadingImage, RelativeLayout mLoadingLayout, ReturnListner returnListner) {
            this.identity = identity;
            this.mLoadingImage = mLoadingImage;
            this.mLoadingLayout = mLoadingLayout;
            this.returnListner = returnListner;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            if (identity == 0)
                return apiRequests.helpAppointment(Integer.parseInt(strings[0]), strings[1]);
            else
                return apiRequests.contactUs(strings[0]);
        }

        @Override
        protected void onPreExecute() {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            super.onPostExecute(result);
            mLoadingLayout.setVisibility(View.GONE);

            if (result != null) {
                returnListner.returnResult(result.toString());
                showErrorDialog();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkregister = new NetworkChangeReceiver();
    }

    //network connection identifier process
    public ArrayList<Handler> mhaHandlers = new ArrayList<>();
    public static NetworkChangeReceiver networkregister;
    public boolean isConnected = false;
    public static String LOG_TAG = "NETWORK";
    public RelativeLayout noConnectionLayout, mainActivityLayout;
    public Animation bottomUp = AnimationUtils.loadAnimation(getAppCon(),
            R.anim.bottom_up);
    public Animation bottomdown = AnimationUtils.loadAnimation(getAppCon(),
            R.anim.bottom_down);

    public void addMainLayout(RelativeLayout mainlayout) {
        mainActivityLayout = mainlayout;
        LayoutInflater layoutInflater = (LayoutInflater) getAppCon().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        noConnectionLayout = (RelativeLayout) layoutInflater.inflate(R.layout.layout_no_connection_layout, null);
        //addNoConnectionLayout();
    }

    @Override
    protected void onResume() {
        startReceiver();
        super.onResume();
    }

    public void startReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        if (networkregister == null)
            networkregister = new NetworkChangeReceiver();
        registerReceiver(networkregister, filter);
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG, "onDestory");
        super.onPause();
        if (networkregister != null) {
            unregisterReceiver(networkregister);
        }
    }

    public void addNoConnectionLayout() {
        RelativeLayout.LayoutParams rLParams =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mainActivityLayout.addView(noConnectionLayout, rLParams);
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {


            isConnected = apiRequests.isNetworkAvailable(getAppCon());
            Log.v(LOG_TAG, "Receieved notification about network status " + isConnected + " " + noConnectionLayout + "  " + mainActivityLayout);
            /*
            for (int i = 0; i < mhaHandlers.size(); i++) {
                Message message = mhaHandlers.get(i).obtainMessage();
                message.arg1 = isConnected ? 1 : 0;
                mhaHandlers.get(i).sendMessage(message);
            }
            */

            if (noConnectionLayout != null && mainActivityLayout != null) {
                Log.d(LOG_TAG, isConnected + "");
                if (isConnected) {
                    noConnectionLayout.startAnimation(bottomdown);
                    noConnectionLayout.setVisibility(View.GONE);
                } else {

                    InputMethodManager inputManager = (InputMethodManager) getAppCon()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(noConnectionLayout.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    noConnectionLayout.startAnimation(bottomUp);
                    noConnectionLayout.setVisibility(View.VISIBLE);
                }
            }

        }

        void register(Handler handler) {
            mhaHandlers.add(handler);
        }

        void unregister(Handler handler) {
            mhaHandlers.remove(handler);
        }
    }


    public class getProfessionalAccountDetails extends AsyncTask<RequestBody, Void, JSONObject> {

        ImageView mLoadingImage;
        RelativeLayout mLoadingLayout;
        ReturnListner returnListner;

        public getProfessionalAccountDetails(ImageView mLoadingImage, RelativeLayout mLoadingLayout, ReturnListner returnListner) {
            this.mLoadingLayout = mLoadingLayout;
            this.mLoadingImage = mLoadingImage;
            this.returnListner = returnListner;
        }

        @Override
        protected void onPreExecute() {

            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));

            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(RequestBody... params) {
            return apiRequests.getStripeAccount();
        }

        @Override
        protected void onPostExecute(JSONObject aVoid) {
            mLoadingLayout.setVisibility(View.GONE);
            if (aVoid != null) {
                Log.d("response", aVoid.toString());
                try {
                    if (TextUtils.equals(aVoid.getString("status"), "Success")) {
                        returnListner.returnResult(aVoid.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mNoConnectionErrorDialog.show();
            }
            super.onPostExecute(aVoid);
        }

    }

    public class GetHelpQuestions extends AsyncTask<Void, Void, JSONObject> {
        ReturnListner returnListner;

        public GetHelpQuestions(ReturnListner returnListner) {
            this.returnListner = returnListner;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return apiRequests.getHelpQuestions();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null)
                returnListner.returnResult(jsonObject.toString());
            super.onPostExecute(jsonObject);
        }
    }

    public class GetTransactionHistory extends AsyncTask<Void, Void, JSONObject> {
        ImageView mLoadingImage;
        RelativeLayout mLoadingLayout;
        ReturnListner returnListner;

        public GetTransactionHistory(ImageView mLoadingImage, RelativeLayout mLoadingLayout, ReturnListner returnListner) {
            this.mLoadingLayout = mLoadingLayout;
            this.mLoadingImage = mLoadingImage;
            this.returnListner = returnListner;
        }

        @Override
        protected void onPreExecute() {

            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));

            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return apiRequests.getTransactionHistroy();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mLoadingLayout.setVisibility(View.GONE);

            if (jsonObject != null)
                returnListner.returnResult(jsonObject.toString());
            super.onPostExecute(jsonObject);
        }
    }

    public class GetCityCountryFromZip extends AsyncTask<Void, Void, JSONObject> {
        ImageView mLoadingImage;
        RelativeLayout mLoadingLayout;
        ReturnListner returnListner;
        String zipCode;

        public GetCityCountryFromZip(ImageView mLoadingImage, RelativeLayout mLoadingLayout, ReturnListner returnListner, String zipcode) {
            this.mLoadingLayout = mLoadingLayout;
            this.mLoadingImage = mLoadingImage;
            this.returnListner = returnListner;
            this.zipCode = zipcode;
        }

        @Override
        protected void onPreExecute() {

            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));

            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return apiRequests.getCityCountry(zipCode);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            mLoadingLayout.setVisibility(View.GONE);

            if (jsonObject != null)
                returnListner.returnResult(jsonObject.toString());
            super.onPostExecute(jsonObject);
        }
    }

    public ReturnListner helpQuestionReturnListner = new ReturnListner() {
        @Override
        public void returnResult(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                if (TextUtils.equals(jsonObject.getString("status"), "Success")) {
                    JSONArray helpData = jsonObject.getJSONArray("helpData");
                    for (int i = 0; i < helpData.length(); i++) {
                        dbModel.addHelpQuestions(SupportedClass.getHelpQuestions(helpData.getJSONObject(i)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mNoConnectionErrorDialog != null)
                    mNoConnectionErrorDialog.show();
            }
        }
    };

    public String getCityCountry(String responseString) {
        String mCountryName = "";
        try {

            JSONObject googleMapResponse = new JSONObject(responseString);
            if (!TextUtils.equals(googleMapResponse.getString("status"), "ZERO_RESULTS")) {
                Log.d("mapResponse", googleMapResponse.toString());
                JSONArray addCompo = ((JSONArray) googleMapResponse.get("results")).getJSONObject(0)
                        .getJSONArray("address_components");
                for (int i = 0; i < addCompo.length(); i++) {
                    JSONArray types = addCompo.getJSONObject(i).getJSONArray("types");
                    for (int j = 0; j < types.length(); j++) {

                        if (TextUtils.equals(types.getString(j), "locality")) {
                            mCountryName = addCompo.getJSONObject(i).getString("long_name") + "/";
                            break;
                        }

                        if (TextUtils.equals(types.getString(j), "administrative_area_level_1")) {
                            mCountryName += addCompo.getJSONObject(i).getString("short_name");
                            break;
                        }

                        if (TextUtils.equals(types.getString(j), "country")) {
                            mCountryName += ", " + addCompo.getJSONObject(i).getString("long_name");
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Log.d("mCountryName", mCountryName);
        return mCountryName;
    }

    public class StripeProfessionalCharge extends AsyncTask<RequestBody, Void, JSONObject> {

        ReturnListner returnListner;
        RelativeLayout mLoadingLayout;
        ImageView mLoadingImage;

        public StripeProfessionalCharge(ReturnListner returnListner, RelativeLayout mLoadingLayout, ImageView mLoadingImage) {
            this.returnListner = returnListner;
            this.mLoadingLayout = mLoadingLayout;
            this.mLoadingImage = mLoadingImage;
        }

        @Override
        protected void onPreExecute() {

            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(getAppCon(), R.anim.anim_rotate));

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject aVoid) {
            mLoadingLayout.setVisibility(View.GONE);
            if (aVoid != null) {
                Log.d("response", aVoid.toString());
                returnListner.returnResult(aVoid.toString());
            } else {
                mNoConnectionErrorDialog.show();
            }
            super.onPostExecute(aVoid);
        }


        @Override
        protected JSONObject doInBackground(RequestBody... params) {
            return apiRequests.chargeProfessional(params[0]);
        }
    }



}
