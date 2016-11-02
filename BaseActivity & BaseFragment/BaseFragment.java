package applabs.vabo.fragment;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;

import applabs.vabo.AddNewCardActivity;
import applabs.vabo.BaseActivity;
import applabs.vabo.MainApplication;
import applabs.vabo.R;
import applabs.vabo.SignInActivity;
import applabs.vabo.db.DBModel;
import applabs.vabo.network.ApiRequests;
import applabs.vabo.support.Constants;
import applabs.vabo.support.SupportedClass;
import butterknife.ButterKnife;

import android.content.Context;

import com.squareup.picasso.Picasso;

public class BaseFragment extends  Fragment{

    public ProgressDialog mProgressDialog;
    public ApiRequests apiRequests = new ApiRequests();
    public DBModel dbModel = DBModel.getInstance(MainApplication.mContext);
    public Activity mActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity =(Activity) context;
        }

    }

    public void logOutCalled() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
        dialogBuilder.setMessage(getString(R.string.txt_signout_string));
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton(R.string.txt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new android.os.AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        apiRequests.logout();
                        return null;
                    }
                }.execute();

                SupportedClass.clearPrefs();
                dbModel.deleteTables();

                Intent intent = new Intent(mActivity, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


}
