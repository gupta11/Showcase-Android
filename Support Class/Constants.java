package applabs.vabo.support;

public class Constants {

    public static final String STRIPE_KEY_TEST = "pk_test_aUS7zJ7MxEOwoIqzJ9LDzbBJ";
    public static final String STRIPE_KEY_DEVELOP = "pk_test_aUS7zJ7MxEOwoIqzJ9LDzbBJ";

    public static final String PROFILE_IMAGE = "UserProfile.jpg";
    public static final String IS_EXIST = "is_exist";
    public static final String GROUP_NAME = "group_name";
    public static final String SERVICE_DATA = "service_data";
    public static final String OPEN_APPO_TAB = "open_appo_tab";
    public static final String BOOKD_APPOINTMENT_EXTRA = "booked_appo";
    public static final String EMAIL = "email_data";
    public static final String PASSWORD_DATA = "password_data";
    public static final String OPEN_COMPLETE_TAB = "open_previous_tab";
    public static final String OPEN_NEW_APP_TAB = "open_new_appo_tab";

    // tables name
    public static final String USER_TABLE = "Users";
    public static final String BOOK_APPO= "BookAppointment";
    public static final String PROFESSIONAL_TABLE= "ProfessionalData";
    public static final String SERVICES= "ServiceData";
    public static final String APPOINTMENT_REQUESTS= "AppointmentRequests";
    public static final String ACCEPTED_APPOINTMENT_REQUESTS = "AcceptedAppointmentRequests";
    public static final String CARD_DETAILS = "CardDetails";
    public static final String MESSAGES = "Messages";
    public static final String ACCOUNT_DETAILS = "AccountDetails";
    public static final String HELP_QUESTIONS = "HelpQuestions";
    public static final String TRANSACTION_DETAILS = "TransactionDetails";


    //flags
    public static final String RESCHEDULE = "reschedule";
    public static final String UPDATE_APPO = "updateDone";
    public static final String DELETE_APPO = "deleteAppo";
    public static final String UPDATE_USER_DATA = "updateProfile";
    public static final String IS_OTP = "is_otp";
    public static final String OTP = "otp_code";
    public static final String TITLE = "title_page";
    public static final String REMEMBER_CARD = "remember_card";

    //preferences
    public static final String BECOME_PROFESSIONA_PREF = "become_prof_tag";
    public static final String EMAIL_ID_PREF = "emailid_tag";
    public static final String CURRENT_USER_ID_PREF = "userId";
    public static final String IS_AVAILABLE = "is_available";
    public static final String IS_PROFE = "isProfe";
    public static final String LOGIN_TOKEN = "login_token";
    public static final String NOTIFICATION_ENABLE = "enable_notification";
    public static final String IS_REGISTERED = "is_register";
    public static final String RUNNING_APPO_ID = "running_appo_id";
    public static final String START_TIME_MILLIS = "start_millis_time";
    public static final String CAMERA_IMAGE_PATH = "camera_image_path";

    //notificaton flags
    public static final String CREATE_APPOINTMENT = "CREATE_APPOINTMENT";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";
    public static final String ACCEPT_APPOINTMENT = "ACCEPT_APPOINTMENT";
    public static final String START_APPOINTMENT = "START_APPOINTMENT";
    public static final String COMPLETE_APPOINTMENT = "COMPLETE_APPOINTMENT";
    public static final String PROFESSIONAL_REVIEW = "PROFESSIONAL_REVIEW";
    public static final String CUSTOMER_REVIEW = "CUSTOMER_REVIEW";

    public static final String GOOGLE_GET_CITY_COUNTRY = "http://maps.google.com/maps/api/geocode/json?address=ZIP&ka&sensor=false";

}
