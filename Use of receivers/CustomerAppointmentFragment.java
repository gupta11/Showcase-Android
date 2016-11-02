package applabs.vabo.customer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import applabs.vabo.R;
import applabs.vabo.adapter.ViewPagerAdapter;
import applabs.vabo.custom.CustomTextView;
import applabs.vabo.customer.CustomerMainView;
import applabs.vabo.fragment.BaseFragment;
import applabs.vabo.model.AppointmentModel;
import applabs.vabo.model.ServiceData;
import applabs.vabo.professional.ProfessionalMainView;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CustomerAppointmentFragment  extends BaseFragment {

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    @Bind(R.id.tab_layout)
    public TabLayout mTabLayout;


    @Bind(R.id.layout_loading)
    RelativeLayout mLoadingLayout;

    @Bind(R.id.loading_imageview)
    ImageView mLoadingImage;


    ViewPagerAdapter mViewPagerAdapter;

    private static final String TAB_1_TAG = "UPCOMING";
    private static final String TAB_2_TAG = "PREVIOUS";

    int currentTab = 0;
    ArrayList<Fragment> homeFragments;
    private boolean createdTab = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);
        ButterKnife.bind(this, view);
        initViews();
        if(apiRequests.isNetworkAvailable(mActivity))
            new GetAllBookedAppointment().execute();
        else {
            ((CustomerMainView)mActivity).showConnectionSnackaBar();
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mActivity);
            Intent i = new Intent("REFRESH");
            lbm.sendBroadcast(i);
        }
        return view;
    }


    public void initViews() {
        initialisePaging();
        mTabLayout.getTabAt(0).setCustomView(setIndicator(mActivity, getString(R.string.txt_upcoming)));
        mTabLayout.getTabAt(1).setCustomView(setIndicator(mActivity, getString(R.string.txt_previois)));
    }


    private View setIndicator(Context ctx
                                         ,String string) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.tab_gender_item, null);
        CustomTextView tv = (CustomTextView) v.findViewById(R.id.txt_tabtxt);
        tv.setText(string);
        return v;

    }


    public void initialisePaging() {


        homeFragments = new ArrayList<>();
        homeFragments.add(new UpcomingAppointmentFragment());
        homeFragments.add(new PreviousAppointmentFragment());

        mViewPagerAdapter = new ViewPagerAdapter(
                getChildFragmentManager(), homeFragments);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                currentTab = i;
                mTabLayout.getTabAt(currentTab).select();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        //set tablayout with viewpager
        mTabLayout.setupWithViewPager(mViewPager);

    }


    public class GetAllBookedAppointment extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPostExecute(JSONObject s) {
            parseAppointments(s);
            ((CustomerMainView)mActivity).getUnRatedAppointment(mActivity);
            mLoadingLayout.setVisibility(View.GONE);
            super.onPostExecute(s);
        }

        @Override
        protected void onPreExecute() {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoadingImage.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.anim_rotate));
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return apiRequests.getAppointmentCust();
        }
    }

    public void parseAppointments(JSONObject jsonObject){
        try{
            ((CustomerMainView)mActivity).updateNewMessage(dbModel.getAllUnread(SupportedClass.getIntPrefData(mActivity, Constants.CURRENT_USER_ID_PREF)));
            ArrayList<AppointmentModel> allAppointments = new ArrayList<>();
            if(TextUtils.equals("Success", jsonObject.getString("status"))){
                Log.d("response", jsonObject.toString());
                dbModel.deleteTable(Constants.BOOK_APPO);
                JSONArray appoData = jsonObject.getJSONArray("appointment_requestData");
                for (int i=0; i<appoData.length(); i++){
                    AppointmentModel appointmentModel = new AppointmentModel();
                    JSONObject appbj = appoData.getJSONObject(i);
                    appointmentModel.setUserId(SupportedClass.getIntPrefData(mActivity, Constants.CURRENT_USER_ID_PREF));
                    appointmentModel.setAppId(appbj.getInt("appointment_request_id"));
                    appointmentModel.setAppoPaymentMethod("PAYPAL");//appbj.getString("PAYPAL"));
                    appointmentModel.setAppoTime(appbj.getString("schedule_date"));
                    appointmentModel.setAppExtraNotes(appbj.getString("notes"));
                    appointmentModel.setAppoRecureStatus(appbj.getString("recure_id"));
                    appointmentModel.setRateByCust(appbj.getString("rate_by_cust"));
                    appointmentModel.setRateByProf(appbj.getString("rate_by_prof"));
                    appointmentModel.setStatus(appbj.getString("status"));
                    appointmentModel.setMessageCount(appbj.getInt("message_count"));
                    appointmentModel.setProfessionalName(appbj.getString("professional_name"));
                    appointmentModel.setProfessionalNumber(appbj.getString("professional_number"));
                    ServiceData serviceData = new ServiceData(appbj.getInt("service_id"),
                            appbj.getInt("category_id"),
                            appbj.getString("catagory_name"),
                            appbj.getString("name"),
                            appbj.getString("duration"),
                            appbj.getString("price"),
                            appbj.getString("gender"),
                            appbj.getString("description"));
                    appointmentModel.setServiceData(serviceData);
                    dbModel.addBookedAppo(SupportedClass.getBookedAppoContentValues(appointmentModel));
                    dbModel.deleteMessageOfCompleteAppo();
                    allAppointments.add(appointmentModel);

                }
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mActivity);
                Intent i = new Intent("REFRESH");
                lbm.sendBroadcast(i);
                if(((CustomerMainView)mActivity).openPreviousAppointmentTab) {
                    mViewPager.setCurrentItem(1);
                    ((CustomerMainView)mActivity).openPreviousAppointmentTab = false;
                }

            }
            else
                Toast.makeText(mActivity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(receiver);
    }

    public void onResume() {
        super.onResume();
        ((CustomerMainView)mActivity).updateNewMessage(dbModel.getAllUnread(SupportedClass.getIntPrefData(mActivity, Constants.CURRENT_USER_ID_PREF)));
        receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(receiver,
                new IntentFilter("APPOINTMENT_REFRESH"));
    }

    MyReceiver receiver;


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(apiRequests.isNetworkAvailable(mActivity))
                new GetAllBookedAppointment().execute();
            else
                ((CustomerMainView)mActivity).showConnectionSnackaBar();


        }
    }


}
