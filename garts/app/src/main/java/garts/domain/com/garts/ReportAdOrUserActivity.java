package garts.domain.com.garts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.filters.FilterAdapter;
import garts.domain.com.garts.filters.models.ReportType;
import garts.domain.com.garts.home.activities.HomeActivity;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ToastUtils;
import garts.domain.com.garts.R;

public class ReportAdOrUserActivity extends BaseActivity {

    public static final String AD_OBJECT_ID_EXTRA_KEY = "AD_OBJECT_ID_EXTRA_KEY";
    public static final String USER_OBJECT_ID_EXTRA_KEY = "USER_OBJECT_ID_EXTRA_KEY";
    public static final String REPORT_TYPE_EXTRA_KEY = "REPORT_TYPE_EXTRA_KEY";

    /* Views */
    private TextView titleTV;
    private ImageView backIV;
    private RecyclerView reportRV;

    /* Variables */
    private ParseObject adObj;
    private ParseUser userObj;
    private String reportType = "";
    private List<String> reportArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_ad_or_user);

        // Get extras from previous .java
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String adObjectID = extras.getString(AD_OBJECT_ID_EXTRA_KEY);
        String userObjectID = extras.getString(USER_OBJECT_ID_EXTRA_KEY);
        reportType = extras.getString(REPORT_TYPE_EXTRA_KEY);

        initViews();

        if (reportType != null && reportType.equals(ReportType.USER.getValue())) {
            userObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, userObjectID);
            try {
                userObj.fetchIfNeeded().getParseUser(Configs.USER_CLASS_NAME);
                reportArray = new ArrayList<>(Arrays.asList(Configs.reportUserOptions));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, adObjectID);
            try {
                adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);
                reportArray = new ArrayList<>(Arrays.asList(Configs.reportAdOptions));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        setUpViews();
    }

    private void initViews() {
        backIV = findViewById(R.id.araou_back_iv);
        titleTV = findViewById(R.id.araou_title_tv);
        reportRV = findViewById(R.id.araou_report_rv);
    }

    private void setUpViews() {
        titleTV = findViewById(R.id.araou_title_tv);
        titleTV.setTypeface(Configs.titSemibold);
        titleTV.setText(getString(R.string.report_title_formatted, reportType));

        reportRV.setLayoutManager(new LinearLayoutManager(this));
        reportRV.setAdapter(new FilterAdapter(reportArray, "", new FilterAdapter.OnFilterSelectedListener() {
            @Override
            public void onFilterSelected(String filter) {
                showReportDialog(filter);
            }
        }));

        // MARK: - BACK BUTTON ------------------------------------
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showReportDialog(final String reportMessage) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUserActivity.this);
        alert.setMessage(getString(R.string.report_alert_warning_title, reportType, reportMessage))
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.report_warning_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showLoading();

                        if (reportType.equals(ReportType.AD.getValue())) {
                            reportAd(reportMessage);
                        } else {
                            reportUser(reportMessage);
                        }
                    }
                })
                .setNegativeButton(R.string.alert_cancel_button, null)
                .setIcon(R.drawable.logo);
        alert.create().show();
    }

    private void reportAd(String message) {
        adObj.put(Configs.ADS_IS_REPORTED, true);
        adObj.put(Configs.ADS_REPORT_MESSAGE, message);

        // Saving block
        adObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    hideLoading();

                    AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUserActivity.this);
                    alert.setMessage(getString(R.string.report_success, reportType))
                            .setTitle(R.string.app_name)
                            .setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Go back to BrowseFragment
                                    startActivity(new Intent(ReportAdOrUserActivity.this, HomeActivity.class));
                                }
                            })
                            .setIcon(R.drawable.logo);
                    alert.create().show();
                } else {
                    hideLoading();
                    ToastUtils.showMessage(e.getMessage());
                }
            }
        });
    }

    private void reportUser(String message) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userObj.getObjectId());
        params.put("reportMessage", message);

        ParseCloud.callFunctionInBackground("reportUser", params, new FunctionCallback<ParseUser>() {
            public void done(ParseUser user, ParseException error) {
                if (error == null) {
                    hideLoading();
                    Configs.hidePD();

                    AlertDialog.Builder alert = new AlertDialog.Builder(ReportAdOrUserActivity.this);
                    alert.setMessage(getString(R.string.report_success, reportType))
                            .setTitle(R.string.app_name)
                            .setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Go back to BrowseFragment
                                    startActivity(new Intent(ReportAdOrUserActivity.this, HomeActivity.class));
                                }
                            })
                            .setIcon(R.drawable.logo);
                    alert.create().show();

                    // Query all Ads posted by this User...
                    ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
                    query.whereEqualTo(Configs.ADS_SELLER_POINTER, userObj);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                // ...and report Ads them one by one
                                for (int i = 0; i < objects.size(); i++) {
                                    ParseObject adObj;
                                    adObj = objects.get(i);
                                    adObj.put(Configs.ADS_IS_REPORTED, true);
                                    adObj.put(Configs.ADS_REPORT_MESSAGE, getString(R.string.report_automatically_reporting));
                                    adObj.saveInBackground();
                                }
                            }
                        }
                    });
                    // Error in Cloud Code
                } else {
                    hideLoading();
                    ToastUtils.showMessage(error.getMessage());
                }
            }
        });
    }
}
