package applabs.vabo.customer;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Calendar;

import applabs.vabo.BaseActivity;
import applabs.vabo.R;
import applabs.vabo.adapter.SearchListItemAdapter;
import applabs.vabo.adapter.SearchServiceAdapter;
import applabs.vabo.adapter.ViewPagerAdapterCustomer;
import applabs.vabo.custom.CustomTextView;
import applabs.vabo.db.DBModel;
import applabs.vabo.model.AppointmentModel;
import applabs.vabo.model.ServiceData;
import applabs.vabo.network.ApiRequests;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leolin.shortcutbadger.ShortcutBadger;

public class CustomerMainView extends BaseActivity {

    public final int EDIT_APPOINTMENT_ACTIVITY = 1;
    public final int RATE_APPOINTMENT_ACTIVITY = 2;
    public final int CHAT_SCREEN_ACTIVITY = 4;
    public final int HELP_APPOINTMENT_ACTIVITY = 3;


    @Bind(R.id.tab_layout)
    public TabLayout mTabLayout;

    @Bind(R.id.view_pager)
    ViewPager mPager;

    public void showConnectionErrorDialog(){
        mNoConnectionErrorDialog.show();
    }

    @Bind(R.id.layout_search_bar)
    public RelativeLayout mLayoutSearchBar;

    @Bind(R.id.main_layout)
    public RelativeLayout mMainLayout;

    @Bind(R.id.ed_searchbar)
    AutoCompleteTextView mSearchBar;

    @Bind(R.id.ic_clear)
    ImageView mSearchTextClearIcon;


    @Bind(R.id.layout_loading)
    public RelativeLayout mLoadingLayout;

    @Bind(R.id.loading_imageview)
    public ImageView mLoadingImage;

    ArrayList<ServiceData> menServices = new ArrayList<>();
    ArrayList<ServiceData> womenServices = new ArrayList<>();

    @OnClick(R.id.ic_clear)
    public void clearSearchBar() {
        mSearchBar.setText("");

        mSearchTextClearIcon.setVisibility(View.GONE);
        hideKeyboard(null);
    }

    public void showConnectionSnackaBar(){
        Snackbar.make(mMainLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
    }

    public boolean openPreviousAppointmentTab = false;
    ViewPagerAdapterCustomer mViewPagerAdapter;

    private static final String TAB_1_TAG = "HOME";
    private static final String TAB_2_TAG = "APPOINTMENT";
    private static final String TAB_3_TAG = "PROFILE";
    AppointmentModel bookedAppointment;
    int currentTab = 0;
    PopupWindow popupWindow;
    ListView mPopUpListView;
    SearchListItemAdapter searchListItemAdapter;

    @Override
    public void onBackPressed() {
        switch (mPager.getCurrentItem()){
            case 0:
                super.onBackPressed();
                break;

            case 1:
                mPager.setCurrentItem(0);
                break;

            case 2:
                mPager.setCurrentItem(1);
                break;
        }

    }

    public void initPopUp() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.dialog_search_window_popup, null);


        //popUpView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        mPopUpListView = (ListView) popUpView.findViewById(R.id.list_services);
        final ArrayList<String> searchService = new ArrayList<>();
        searchService.add("Fast and Furious facial");
        searchService.add("Cosmetic Surgery");
        searchService.add("Beauty Salon Reservation");

        searchListItemAdapter = new SearchListItemAdapter(getApplicationContext(), searchService);

        mPopUpListView.setAdapter(searchListItemAdapter);

        View listItem = searchListItemAdapter.getView(0, null, mPopUpListView);
        listItem.measure(0, 0);
        int totalHeight = listItem.getMeasuredHeight() * 4;

        popupWindow = new PopupWindow(popUpView, 0, totalHeight, true);
        //popupWindow.setContentView(popUpView);


        popupWindow.setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);


    }

    public void showAppointmentData(final AppointmentModel appointmentModel) {
        bindChoosedService.mServiceDialogGroup.setText(appointmentModel.getServiceData().getServiceGroup());
        bindChoosedService.mServiceDialogName.setText(appointmentModel.getServiceData().getServiceName());
        bindChoosedService.mServiceDialogDuration.setText("Duration " + appointmentModel.getServiceData().getServiceDuration() + " min");
        bindChoosedService.mServiceDialogPrice.setText(appointmentModel.getServiceData().getServicePrice());

        bindChoosedService.mLayoutProfSide.setVisibility(View.VISIBLE);
        bindChoosedService.mExtraNotes.setVisibility(appointmentModel.getAppExtraNotes().isEmpty() ? View.GONE : View.VISIBLE);
        bindChoosedService.mExtraNotes.setText(appointmentModel.getAppExtraNotes());

        // set app time
        Calendar calendar = SupportedClass.stringToCalender(appointmentModel.getAppoTime());
        String completetime = SupportedClass.getTimeString(calendar, Integer.parseInt(appointmentModel.getServiceData().getServiceDuration()));
        bindChoosedService.mServiceDialogTime.setText(SupportedClass.getDateString(calendar, getAppCon()) + "\n" + completetime);

        String serviceCategory = "";
        if(appointmentModel.getServiceData().getServiceName().isEmpty() || appointmentModel.getServiceData().getServiceName().equals(" "))
            serviceCategory = appointmentModel.getServiceData().getServiceGroup();
        else
            serviceCategory = appointmentModel.getServiceData().getServiceGroup()+"-"+appointmentModel.getServiceData().getServiceName();
        bindChoosedService.mServiceDialogGroup.setText(serviceCategory);
        bindChoosedService.mServiceDialogName.setText(appointmentModel.getServiceData().getServiceDesc());


        bindChoosedService.mAddressLayout.setVisibility(View.GONE);
        if(!appointmentModel.getProfessionalNumber().equals("null")) {
            bindChoosedService.mNumberLayout.setVisibility(View.VISIBLE);
            bindChoosedService.mServiceDialogNumber.setText(appointmentModel.getProfessionalNumber());
        }
        else
            bindChoosedService.mNumberLayout.setVisibility(View.GONE);
        bindChoosedService.mServiceDialogConfirm.setText(R.string.txt_edit);


        bindChoosedService.setBookedAppointment(appointmentModel);
        bindChoosedService.mServiceDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseServiceDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), EditAppointmentActivity.class);
                intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
                startActivityForResult(intent, EDIT_APPOINTMENT_ACTIVITY);

            }
        });
        mChooseServiceDialog.show();
    }

    public void startBookAppointment(ServiceData serviceData) {
        bookedAppointment = new AppointmentModel();
        bookedAppointment.setServiceData(serviceData);
        Intent in = new Intent(getAppCon(), BookAppointmentActivity.class);
        in.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, bookedAppointment);
        startActivity(in);
    }


    public void showServiceChoosedDialog(ServiceData serviceData) {

        String serviceCategory = "";
        if(serviceData.getServiceName().isEmpty() || serviceData.getServiceName().equals(" "))
            serviceCategory = serviceData.getServiceGroup();
        else
            serviceCategory = serviceData.getServiceGroup()+"-"+serviceData.getServiceName();
        bindChoosedService.mServiceDialogGroup.setText(serviceCategory);
        bindChoosedService.mServiceDialogName.setText(serviceData.getServiceDesc());

        bindChoosedService.mServiceDialogDuration.setText("Duration " + serviceData.getServiceDuration() + " min");
        bindChoosedService.mServiceDialogPrice.setText(serviceData.getServicePrice());
        bindChoosedService.mLayoutProfSide.setVisibility(View.GONE);
        bookedAppointment = new AppointmentModel();
        bookedAppointment.setServiceData(serviceData);
        bindChoosedService.mServiceDialogConfirm.setText(R.string.txt_book);
        bindChoosedService.setBookedAppointment(bookedAppointment);
        bindChoosedService.mServiceDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mChooseServiceDialog.cancel();
                Intent in = new Intent(getAppCon(), BookAppointmentActivity.class);
                in.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, bookedAppointment);
                startActivity(in);
            }
        });

        mChooseServiceDialog.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "called");
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case EDIT_APPOINTMENT_ACTIVITY:
                    if (data.hasExtra(Constants.DELETE_APPO) && data.getBooleanExtra(Constants.BOOK_APPO, true)) {
                        Snackbar.make(mainActivityLayout, R.string.txt_appo_canceled, Snackbar.LENGTH_SHORT).show();
                        mViewPagerAdapter.notifyDataSetChanged();
                        mPager.setCurrentItem(0);

                    }

                    if (data.hasExtra(Constants.UPDATE_APPO) && data.getBooleanExtra(Constants.UPDATE_APPO, true)) {
                        mViewPagerAdapter.notifyDataSetChanged();
                    }
                    break;

                case RATE_APPOINTMENT_ACTIVITY:
                    if(data.getBooleanExtra(Constants.UPDATE_APPO, true)) {
                        mViewPagerAdapter.notifyDataSetChanged();
                        getUnRatedAppointment(this);
                    }
                    else
                        finish();
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ArrayList<ServiceData> currentFilterServiceData;

    public void initSearcBarAdapter(int item) {

        if (item == 0) {
            currentFilterServiceData = dbModel.getAllServices("women");

        } else {
            currentFilterServiceData = dbModel.getAllServices("men");

        }

        SearchServiceAdapter searchServiceAdapter = new SearchServiceAdapter(getAppCon(), R.layout.row_search_list, currentFilterServiceData);
        mSearchBar.setAdapter(searchServiceAdapter);
        mSearchBar.setDropDownHeight(400);
        mSearchBar.setDropDownVerticalOffset(10);
        mSearchBar.setThreshold(1);
        mSearchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSearchTextClearIcon.performClick();
                showServiceChoosedDialog(currentFilterServiceData.get(position));
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SupportedClass.REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), R.string.txt_phone_call_permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainview_layout);
        ButterKnife.bind(this);

        ShortcutBadger.applyCount(this,  0 );
       // SupportedClass.checkForPhoneCall(this);

       // getUnRatedAppointment(this);
        if(dbModel.getAllHelpQuestions().size() == 0)
            new GetHelpQuestions(helpQuestionReturnListner).execute();
        addMainLayout(mMainLayout);
        bindConnectionErrorClass();

        if (!SupportedClass.getPrefsBoolean(getAppCon(), Constants.IS_REGISTERED)) {
            final String token = FirebaseInstanceId.getInstance().getToken();
            if (token != null) {
                new android.os.AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        new ApiRequests().registerDevice(token, SupportedClass.getIntPrefData(getApplicationContext(), Constants.CURRENT_USER_ID_PREF),
                                DBModel.getInstance(getAppCon()).getIsProfessional(SupportedClass.getPrefsString(getAppCon(), Constants.EMAIL_ID_PREF)));
                        return null;
                    }
                }.execute();
            }
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        Log.d("service check", dbModel.getAllServices("women").size() + " " + dbModel.getAllServices("men").size());

        // dialog binding
        mChooseServiceDialog = SupportedClass.getDialog(R.layout.dialog_choosed_service, this);
        bindChoosedService = new BindChoosedService();
        ButterKnife.bind(bindChoosedService, mChooseServiceDialog);


        initViews();
        //initPopUp();
        mSearchTextClearIcon.setVisibility(View.GONE);
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {

                    mSearchTextClearIcon.setVisibility(View.VISIBLE);


                } else {
                    mSearchTextClearIcon.performClick();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (getIntent().getBooleanExtra(Constants.OPEN_APPO_TAB, false)) {
                mPager.setCurrentItem(1);
            }
            openPreviousAppointmentTab = getIntent().getBooleanExtra(Constants.OPEN_COMPLETE_TAB, false);
            if (getIntent().hasExtra(Constants.BOOKD_APPOINTMENT_EXTRA)) {
                Intent intent = new Intent(getAppCon(), BookAppointmentSuccessActivity.class);
                intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, getIntent().getSerializableExtra(Constants.BOOKD_APPOINTMENT_EXTRA));
                startActivity(intent);

            }
        }

        // show error dialog
        mErrorDialog = SupportedClass.getDialog(R.layout.dialog_text_display, this);
        bindErrorClass = new BindErrorClass(CustomerMainView.this);
        ButterKnife.bind(bindErrorClass, mErrorDialog);
        bindErrorClass.mSubmitButton.setText(R.string.txt_ok);
    }


    public void initViews() {
        initialisePaging();
        mTabLayout.getTabAt(0).setCustomView(setIndicator(CustomerMainView.this, getString(R.string.txt_home), R.drawable.app_main_tab_icon1_background_selector));
        mTabLayout.getTabAt(1).setCustomView(setIndicator(CustomerMainView.this, getString(R.string.txt_appointment), R.drawable.app_main_tab_icon2_background_selector));
        mTabLayout.getTabAt(2).setCustomView(setIndicator(CustomerMainView.this, getString(R.string.txt_account), R.drawable.app_main_tab_icon3_background_selector));

    }

    public void updateNewMessage(int count){
        CustomTextView countTxt = ((CustomTextView)mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.txt_count));
        countTxt.setText(count + "");
        countTxt.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }



    private View setIndicator(Context ctx
            , String string, int drawable) {
        View customeView = LayoutInflater.from(ctx).inflate(R.layout.tab_customer_main_item, null);
        CustomTextView tv = (CustomTextView) customeView.findViewById(R.id.txt_tabtxt);
        ImageView iv = (ImageView) customeView.findViewById(R.id.ic_icon);
        tv.setText(string);
        iv.setImageResource(drawable);
        return customeView;
    }

    public void startBookAppo() {
        mPager.setCurrentItem(0);
    }

    public void startBookAppintment(ServiceData serviceData) {
        bookedAppointment = new AppointmentModel();
        bookedAppointment.setServiceData(serviceData);
        Intent in = new Intent(getAppCon(), BookAppointmentActivity.class);
        in.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, bookedAppointment);
        startActivity(in);

    }

    public void rateAppointment(AppointmentModel appointmentModel) {
        Intent intent = new Intent(getApplicationContext(), RateAppointmentActivity.class);
        intent.putExtra("isCancelable", true);
        intent.putExtra("isProf", 0);
        intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
        startActivityForResult(intent, RATE_APPOINTMENT_ACTIVITY);
    }



    public void initialisePaging() {


        mViewPagerAdapter = new ViewPagerAdapterCustomer(
                super.getSupportFragmentManager());


        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                hideKeyboard(null);
                currentTab = i;
                switch (currentTab) {
                    case 0:
                        mLayoutSearchBar.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                    case 2:
                        mLayoutSearchBar.setVisibility(View.GONE);
                        break;
                    default:
                        mLayoutSearchBar.setVisibility(View.GONE);
                        break;
                }

                if (currentTab == 1) {

                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(CustomerMainView.this);
                    Intent intnet = new Intent("APPOINTMENT_REFRESH");
                    lbm.sendBroadcast(intnet);

                }

                //mViewPagerAdapter.notifyDataSetChanged();
                mTabLayout.getTabAt(currentTab).select();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mPager.setAdapter(mViewPagerAdapter);

        //set tablayout with viewpager
        mTabLayout.setupWithViewPager(mPager);

        // adding functionality to tab and viewpager to manage each other when a page is changed or when a tab is selected
        //mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        //Setting tabs from adpater
        mTabLayout.setTabsFromPagerAdapter(mViewPagerAdapter);

    }


    public void showMessageScreen(AppointmentModel appointmentModel, boolean b) {
        Intent intent = new Intent(getApplicationContext(), ChatScreenActivity.class);
        intent.putExtra(Constants.APPOINTMENT_REQUESTS, appointmentModel);
        startActivityForResult(intent, CHAT_SCREEN_ACTIVITY);
    }



    public ArrayList<ServiceData> getMenServices() {
        return menServices;
    }

    public ArrayList<ServiceData> getWomenServices() {
        return womenServices;
    }


    public void showNextAppointment(AppointmentModel appointmentModel) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.txt_next_appo_schedule)+" ");


        Calendar calendar = SupportedClass.stringToCalender(appointmentModel.getAppoTime());
        switch (Integer.parseInt(appointmentModel.getAppoRecureStatus())) {
            case 1:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                break;
            case 3:
                calendar.add(Calendar.DAY_OF_MONTH, 21);
                break;
            case 4:
                calendar.add(Calendar.DAY_OF_MONTH, 28);
                break;
        }
        stringBuilder.append(SupportedClass.getDateString(calendar, getAppCon()));
        stringBuilder.append("\nat " + SupportedClass.getTimeString(calendar, Integer.parseInt(appointmentModel.getServiceData().getServiceDuration())));
        bindErrorClass.setText(stringBuilder.toString());
        bindErrorClass.changeAlignment(true);
        mErrorDialog.show();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onResume() {
        super.onResume();
        receiver = new MyReceiver();
        registerReceiver(receiver, new IntentFilter(Constants.OPEN_APPO_TAB));
    }

    MyReceiver receiver;

    public void helpAppointment(AppointmentModel appointmentModel) {
        Intent intent = new Intent(getApplicationContext(), HelpAppointmentActivity.class);
        intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
        startActivityForResult(intent, HELP_APPOINTMENT_ACTIVITY);
    }





    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notification_id = intent.getIntExtra("notification_id", -1);
            openPreviousAppointmentTab = intent.getBooleanExtra(Constants.OPEN_COMPLETE_TAB, false);
            Log.d("openPreviousAppointmen", openPreviousAppointmentTab+"");
            SupportedClass.setBadge(CustomerMainView.this, 0);
            if (notification_id != -1) {
                NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nMgr.cancelAll();
            }
            if(mPager.getCurrentItem() != 1)
                mPager.setCurrentItem(1);
            else{
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(CustomerMainView.this);
                Intent intnet = new Intent("APPOINTMENT_REFRESH");
                lbm.sendBroadcast(intnet);

            }

        }
    }


}
