package applabs.vabo.customer;

import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import applabs.vabo.AddNewCardActivity;
import applabs.vabo.BaseActivity;
import applabs.vabo.R;
import applabs.vabo.UserProfileActivity;
import applabs.vabo.adapter.SpinnerListItemAdapter;
import applabs.vabo.custom.CustomEditText;
import applabs.vabo.custom.CustomSpinner;
import applabs.vabo.custom.CustomTextView;
import applabs.vabo.custom.CustomTimePickerDialog;
import applabs.vabo.model.AppointmentModel;
import applabs.vabo.model.UserInfo;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookAppointmentActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tv_header)
    CustomTextView mHeaderText;

    @Bind(R.id.main_layout)
    RelativeLayout mMainLayout;

    @Bind(R.id.layout_edit_date)
    RelativeLayout mLayoutEditDate;

    @Bind(R.id.layout_edit_time)
    RelativeLayout mLayoutEditTime;

    @Bind(R.id.ed_extranotes)
    CustomEditText mExtraNotes;

    @Bind(R.id.layout_recure_appointment)
    RelativeLayout mLayoutEditRecureAppointment;


    @Bind(R.id.txt_date_value)
    CustomTextView mSelectedDate;

    @Bind(R.id.txt_time_value)
    CustomTextView mSelectedTime;

    @Bind(R.id.txt_recure_appointment_value)
    CustomSpinner mRecureAppoSpinner;

    @Bind(R.id.txt_address)
    CustomTextView mAddress;

    @Bind(R.id.txt_address1)
    CustomTextView mAddress1;

    @Bind(R.id.txt_address2)
    CustomTextView mAddress2;

    @Bind(R.id.layout_recure_appo)
    RelativeLayout mRecureAppoLayout;

    @Bind(R.id.txt_next_recure_app_text)
    CustomTextView mRecureAppoText;


    @Bind(R.id.layout_loading)
    RelativeLayout mLoadingLayout;

    @Bind(R.id.loading_imageview)
    ImageView mLoadingImage;

    @Bind(R.id.layout_date_time)
    RelativeLayout mLayoutDateTime;

    @Bind(R.id.txt_service)
    TextView mServiceName;

    @Bind(R.id.txt_service_category)
    TextView mServiceCategory;

    @Bind(R.id.txt_service_duration)
    TextView mServiceDuration;

    @Bind(R.id.txt_service_price)
    TextView mServicePrice;


    public boolean isReschedule;
    int userId;
    int year = 0, month = 0, day = 0, hour = 0, min = 0;
    public ArrayList<String> popupItemsRecureAppointments;
    Calendar mApppointmentCalender = Calendar.getInstance();
    public AppointmentModel appointmentModel;


    @OnClick(R.id.txt_edi_address)
    public void editAddress() {

        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.putExtra("isNew", false);
        intent.putExtra(Constants.IS_PROFE, 0);
        /*intent.putExtra("address", mAddress.getText().toString());
        String[] addressParts = mAddress1.getText().toString().split(",");
        String[] subAddressParts = addressParts[1].trim().split(" ");
        intent.putExtra("city", addressParts[0]);
        intent.putExtra("country", subAddressParts[0]);
        intent.putExtra("zip", subAddressParts[1]);
        intent.putExtra("cellphone", mAddress2.getText().toString());
        */

        startActivityForResult(intent, 10);
    }

    @OnClick(R.id.txt_submit)
    public void submitButtonClicked() {
        if (TextUtils.equals(mSelectedDate.getText().toString(), getString(R.string.txt_select_date)))
            toastMessage(R.string.txt_select_date);
        else if (TextUtils.equals(mSelectedTime.getText().toString(), getString(R.string.txt_select_time)))
            toastMessage(R.string.txt_select_time);
        else {
            appointmentModel.setUserId(userId);
            appointmentModel.setAppoTime(SupportedClass.calendarToString(mApppointmentCalender));
            appointmentModel.setAppExtraNotes(mExtraNotes.getText().toString());
            appointmentModel.setAppoRecureStatus(String.valueOf(mRecureAppoSpinner.getSelectedItemPosition()));

            if (dbModel.getCardDetails(getCurrnrUserId()).size() > 0) {
                Intent intent;
                if (isReschedule) {
                    intent = new Intent(getAppCon(), ConfirmAppointmentActivity.class);
                    intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
                    startActivityForResult(intent, 1);
                } else {
                    //intent = new Intent(getAppCon(), PaymentMethodActivity.class);
                    //intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
                    //startActivity(intent);

                    // book new appointment
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage(getString(R.string.txt_create_appo));
                    FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                    formEncodingBuilder.add("service_id", "" + appointmentModel.getServiceData().getServiceId());
                    formEncodingBuilder.add("schedule_date", appointmentModel.getAppoTime());
                    formEncodingBuilder.add("recure_id", appointmentModel.getAppoRecureStatus());
                    formEncodingBuilder.add("notes", appointmentModel.getAppExtraNotes());

                    Log.d("data", appointmentModel.getServiceData().getServiceId() + " " + appointmentModel.getAppoTime() + "  " + "1");
                    if (apiRequests.isNetworkAvailable(getAppCon()))
                        new AppointmentCreateCalls(createAppoListner, 0).execute(formEncodingBuilder.build());
                    else
                        Snackbar.make(mMainLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
                }

            } else {

                openCardAlertDialog();
            }

        }

    }

    public ReturnListner createAppoListner = new ReturnListner() {
        @Override
        public void returnResult(String value) {
            try {
                JSONObject jsonObject = new JSONObject(value);
                if (TextUtils.equals(jsonObject.getString("status"), "Success")) {
                    appointmentModel.setAppId(jsonObject.getJSONArray("apporeqData").getJSONObject(0).getInt("appointment_request_id"));
                    appointmentModel.setRateByCust(jsonObject.getJSONArray("apporeqData").getJSONObject(0).getString("rate_by_cust"));
                    appointmentModel.setProfessionalName("");
                    appointmentModel.setMessageCount(jsonObject.getJSONArray("apporeqData").getJSONObject(0).getInt("message_count"));
                    appointmentModel.setRateByProf(jsonObject.getJSONArray("apporeqData").getJSONObject(0).getString("rate_by_prof"));
                    appointmentModel.setStatus("panding");
                    dbModel.addBookedAppo(SupportedClass.getBookedAppoContentValues(appointmentModel));
                    Intent intent = new Intent(getAppCon(), CustomerMainView.class);
                    intent.putExtra(Constants.OPEN_APPO_TAB, false);
                    intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, appointmentModel);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getAppCon(), "Error: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mNoConnectionErrorDialog.show();
            }
        }
    };

    private void openCardAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.txt_no_payment_method_set_cust));
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getAppCon(), AddNewCardActivity.class);
                intent.putExtra(Constants.IS_PROFE, 0);
                intent.putExtra("isNew", true);
                intent.putExtra("isSignUp", false);
                startActivityForResult(intent, 11);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && data != null) {
            switch (requestCode) {
                case 1:
                    if (data.hasExtra(Constants.UPDATE_APPO) && data.getBooleanExtra(Constants.UPDATE_APPO, true)) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.UPDATE_APPO, true);
                        intent.putExtra(Constants.BOOKD_APPOINTMENT_EXTRA, (AppointmentModel) data.getSerializableExtra(Constants.BOOKD_APPOINTMENT_EXTRA));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
                case 10:
                    setUserAddress();
                    break;

                case 11:

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUserAddress() {
        String emailId = SupportedClass.getPrefsString(getAppCon(), Constants.EMAIL_ID_PREF);
        userId = dbModel.getUserIdByEmail(emailId);
        UserInfo oldInfo = dbModel.getUserInfo(userId);
        mAddress.setText(oldInfo.getAddress());
        mAddress1.setText(oldInfo.getCity() + ", " + oldInfo.getProvince() + ",\n" + oldInfo.getCountry() + ", " + oldInfo.getZipCode());
        String number = "(" + oldInfo.getCellNumber().substring(0, 3) + ") " + oldInfo.getCellNumber().substring(3, 6) + "-" + oldInfo.getCellNumber().substring(6, oldInfo.getCellNumber().length());
        mAddress2.setText(number);
    }


    @OnClick(R.id.layout_edit_date)
    public void selectAppDate() {

        Calendar calendar = Calendar.getInstance();
        year = mApppointmentCalender.get(Calendar.YEAR);
        day = mApppointmentCalender.get(Calendar.DAY_OF_MONTH);
        month = mApppointmentCalender.get(Calendar.MONTH);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(pickerListener, year, month, day);
        Calendar minCalender = Calendar.getInstance();
        minCalender.add(Calendar.MILLISECOND, 24 * 60 * 60 * 1000);
        datePickerDialog.setMinDate(minCalender);
        datePickerDialog.show(getFragmentManager(), "DatePciker");
    }

    @Bind(R.id.ic_dropdown_recure)
    ImageView mIcDropDown;

    @OnClick(R.id.ic_dropdown_recure)
    public void dropDownIconClicked() {
        if (mRecureAppoSpinner.hasBeenOpened()) {
            mRecureAppoSpinner.performClosedEvent();
            mIcDropDown.setImageResource(R.drawable.arrow_drop_down_inactive_icon);
        } else {
            mRecureAppoSpinner.performClick();
            mIcDropDown.setImageResource(R.drawable.arrow_drop_up_active_icon);
        }
    }

    @OnClick(R.id.layout_edit_time)
    public void selectAppTime() {
        Calendar calendar = Calendar.getInstance();
        hour = mApppointmentCalender.get(Calendar.HOUR_OF_DAY);
        min = mApppointmentCalender.get(Calendar.MINUTE);
//        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(timePickerListener, hour, min, false);
//        timePickerDialog.setTimeInterval(1, 30);
//        timePickerDialog.show(getFragmentManager(),"TimePicker");


        CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(this, timePickerListener, hour, min, false);//TimePickerDialog.newInstance(timePickerListener, hour, min, false);
        timePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        timePickerDialog.setTitle(R.string.txt_settime);
        timePickerDialog.show();

    }

    @Bind(R.id.shadow_relative)
    View shadowRelative;

    int mainSelectedAreHeightPopup;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void changeRecureAppointment(int position) {
        if (position == 0 || TextUtils.equals(mSelectedDate.getText().toString(), getString(R.string.txt_select_date))) {
            mRecureAppoLayout.setVisibility(View.GONE);
        } else {
            mRecureAppoLayout.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mApppointmentCalender.getTimeInMillis());
            switch (position) {
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
            mRecureAppoText.setText("Next Appointment will be on " + calendar.get(Calendar.DAY_OF_MONTH) + " " + SupportedClass.getMonth(calendar.get(Calendar.MONTH), getAppCon()) + " " + calendar.get(Calendar.YEAR));

        }
    }

    boolean isCardAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appo_book);
        ButterKnife.bind(this);

        addMainLayout(mMainLayout);
        bindConnectionErrorClass();

        setToolbar(mToolbar, mHeaderText, R.string.txt_booking_an_appointment, true);

        setupUI(mMainLayout, this);
        mLayoutDateTime.setVisibility(View.GONE);
        if (dbModel.getCardDetails(getCurrnrUserId()).size() == 0) {
            if (apiRequests.isNetworkAvailable(getAppCon()))
                new GetStripeDetails(null, null, cardDetailsGet).execute();
//            else
//                Snackbar.make(mMainLayout, R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
        } else
            isCardAvailable = true;

        userId = dbModel.getUserIdByEmail(SupportedClass.getPrefsString(getAppCon(), Constants.EMAIL_ID_PREF));
        appointmentModel = (AppointmentModel) getIntent().getSerializableExtra(Constants.BOOKD_APPOINTMENT_EXTRA);
        popupItemsRecureAppointments = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.appointment_recure_time)));
        isReschedule = getIntent().getBooleanExtra(Constants.RESCHEDULE, false);

        spinnerListItemAdapter = new SpinnerListItemAdapter(getApplicationContext(), popupItemsRecureAppointments, 1);
        mRecureAppoSpinner.setAdapter(spinnerListItemAdapter);

        mRecureAppoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeRecureAppointment(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if (isReschedule) {
            mApppointmentCalender = SupportedClass.stringToCalender(appointmentModel.getAppoTime());
            Calendar calendar = SupportedClass.stringToCalender(appointmentModel.getAppoTime());
            mSelectedDate.setText(SupportedClass.getDateString(calendar, getAppCon()));
            String completetime = SupportedClass.getTimeString(calendar, Integer.parseInt(appointmentModel.getServiceData().getServiceDuration()));
            mSelectedTime.setText(completetime);
            Log.d("recure status", appointmentModel.getAppoRecureStatus() + "");
            mRecureAppoSpinner.setSelection(Integer.parseInt(appointmentModel.getAppoRecureStatus()));
            mExtraNotes.setText(appointmentModel.getAppExtraNotes());
        } else {
            mSelectedDate.setText(R.string.txt_select_date);
            mSelectedTime.setText(R.string.txt_select_time);
        }
        setUserAddress();
        setAppoData();
        //mRecureAppoSpinner.setSelection(0);
    }

    private void setAppoData() {
        mServiceName.setText(appointmentModel.getServiceData().getServiceName());
        mServiceCategory.setText(appointmentModel.getServiceData().getServiceGroup());
        mServiceDuration.setText("Duration " + appointmentModel.getServiceData().getServiceDuration() + " min");
        mServicePrice.setText(appointmentModel.getServiceData().getServicePrice());

        String serviceCategory = "";
        if (appointmentModel.getServiceData().getServiceName().isEmpty() || appointmentModel.getServiceData().getServiceName().equals(" "))
            serviceCategory = appointmentModel.getServiceData().getServiceGroup();
        else
            serviceCategory = appointmentModel.getServiceData().getServiceGroup() + "-" + appointmentModel.getServiceData().getServiceName();
        mServiceCategory.setText(serviceCategory);
        mServiceName.setText(appointmentModel.getServiceData().getServiceDesc());


    }

    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int selectedYear,
                              int selectedMonth, int selectedDay) {

            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            mApppointmentCalender.set(Calendar.YEAR, year);
            mApppointmentCalender.set(Calendar.MONTH, month);
            mApppointmentCalender.set(Calendar.DAY_OF_MONTH, day);
            String value = SupportedClass.getDateString(mApppointmentCalender, getApplicationContext());
            // Show selected date
            mSelectedDate.setText(value);
            changeRecureAppointment(mRecureAppoSpinner.getSelectedItemPosition());

        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            int hour;
            String am_pm;
            if (hourOfDay > 6 && hourOfDay < 22) {
                Log.d("hour of day", hourOfDay + "  " + minute);
                Calendar calendar = Calendar.getInstance();

                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                mApppointmentCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mApppointmentCalender.set(Calendar.MINUTE, minute);
                String completetime = SupportedClass.getTimeString(calendar, Integer.parseInt(appointmentModel.getServiceData().getServiceDuration()));
                mSelectedTime.setText(completetime);
            } else {
                Snackbar.make(mMainLayout, R.string.txt_time_limit, Snackbar.LENGTH_SHORT).show();
            }

        }

   /*     @Override
        public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute, int seconds) {
            int hour;
            String am_pm;
            Log.d("hour of day", hourOfDay + "  " + minute);
            Calendar calendar = Calendar.getInstance();

            minute = (minute/10) * 10;
            if(minute == 60) {
                minute = 0;
                hourOfDay = hourOfDay+1;
            }
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);


            mApppointmentCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mApppointmentCalender.set(Calendar.MINUTE, minute);



            String completetime = SupportedClass.getTimeString(calendar, Integer.parseInt(appointmentModel.getServiceData().getServiceDuration()));
            mSelectedTime.setText(completetime);

        }*/
    };

    ReturnListner cardDetailsGet = new ReturnListner() {
        @Override
        public void returnResult(String value) {
            Log.d("cardDetailsGet", value);
            try {
                JSONObject jsonObject = new JSONObject(value);
                if (TextUtils.equals("Success", jsonObject.getString("status")) && !TextUtils.equals(jsonObject.getString("message"), getString(R.string.txt_no_card_for_cust))) {
                    addCardsToDB(jsonObject);
                    isCardAvailable = true;
                } else {
                    isCardAvailable = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


}
