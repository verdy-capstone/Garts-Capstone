package garts.domain.com.garts;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import garts.domain.com.garts.ads.activities.AdDetailsActivity;
import garts.domain.com.garts.filters.models.ReportType;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.wizard.WizardActivity;
import garts.domain.com.garts.R;

public class UserProfile extends AppCompatActivity {

    /* Variables */
    ParseUser userObj;
    List<ParseObject> userAdsArray;

    // ON CREATE ------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String objectID = extras.getString("objectID");
        userObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, objectID);
        try {
            userObj.fetchIfNeeded().getParseUser(Configs.USER_CLASS_NAME);

            // Call query
            getUserDetails();

            // MARK: - CHECK FEEDBACKS BUTTON ------------------------------------
            Button cfButt = findViewById(R.id.upCheckFeedbacksButt);
            cfButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(UserProfile.this, Feedbacks.class);
                    Bundle extras = new Bundle();
                    extras.putString("userObjectID", userObj.getObjectId());
                    i.putExtras(extras);
                    startActivity(i);
                }
            });

            // MARK: - OPTIONS BUTTON ------------------------------------
            Button opButt = findViewById(R.id.upOptionsButt);
            opButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // USER IS LOGGED IN
                    if (ParseUser.getCurrentUser().getUsername() != null) {

                        // Check blocked users array
                        final ParseUser currUser = ParseUser.getCurrentUser();
                        final List<String> hasBlocked = currUser.getList(Configs.USER_HAS_BLOCKED);

                        // Set blockUser  Action title
                        String blockTitle = null;
                        if (hasBlocked.contains(userObj.getObjectId())) {
                            blockTitle = getString(R.string.inbox_unblock_user_title);
                        } else {
                            blockTitle = getString(R.string.inbox_block_user_title);
                        }


                        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfile.this);
                        final String finalBlockTitle = blockTitle;

                        alert.setMessage(getString(R.string.select_option_title))
                                .setTitle(R.string.app_name)


                                // REPORT USER ------------------------------------------------------------
                                .setPositiveButton(getString(R.string.inbox_report_user_option), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Pass objectID to the other Activity
                                        Intent in = new Intent(UserProfile.this, ReportAdOrUserActivity.class);
                                        Bundle extras = new Bundle();
                                        extras.putString(ReportAdOrUserActivity.USER_OBJECT_ID_EXTRA_KEY, userObj.getObjectId());
                                        extras.putString(ReportAdOrUserActivity.REPORT_TYPE_EXTRA_KEY, ReportType.USER.getValue());
                                        in.putExtras(extras);
                                        startActivity(in);
                                    }
                                })

                                // BLOCK/UNBLOCK THIS USER ------------------------------------------------
                                .setNegativeButton(blockTitle, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Block User
                                        if (finalBlockTitle.matches(getString(R.string.inbox_block_user_title))) {
                                            hasBlocked.add(userObj.getObjectId());
                                            currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);
                                            currUser.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(UserProfile.this);
                                                    alert.setMessage(getString(R.string.inbox_block_success, userObj.getString(Configs.USER_USERNAME)))
                                                            .setTitle(R.string.app_name)
                                                            .setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    finish();
                                                                }
                                                            })
                                                            .setIcon(R.drawable.logo);
                                                    alert.create().show();
                                                }
                                            });

                                            // Unblock User
                                        } else {
                                            hasBlocked.remove(userObj.getObjectId());
                                            currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);
                                            currUser.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Configs.simpleAlert(getString(R.string.inbox_unblocked_user, userObj.getString(Configs.USER_USERNAME)), UserProfile.this);
                                                }
                                            });
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.alert_cancel_button, null)
                                .setIcon(R.drawable.logo);
                        alert.create().show();
                    } else {
                        // USER IS NOT LOGGED IN
                        startActivity(new Intent(UserProfile.this, WizardActivity.class));
                    }
                }
            });

            // MARK: - WEBSITE BUTTON ------------------------------------
            final Button webButt = findViewById(R.id.upWebButt);
            if (userObj.getString(Configs.USER_WEBSITE) != null) {
                webButt.setText(userObj.getString(Configs.USER_WEBSITE));
            } else {
                webButt.setText("");
                webButt.setEnabled(false);
            }

            webButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(userObj.getString(Configs.USER_WEBSITE))));
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.upBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }// end onCreate()

    // MARK: - GET USER'S DETAILS -------------------------------------------------------
    void getUserDetails() {

        // Get username
        TextView usernTxt = findViewById(R.id.upUsernameTxt);
        usernTxt.setTypeface(Configs.titSemibold);
        usernTxt.setText(getString(R.string.username_formatted, userObj.getString(Configs.USER_USERNAME)));

        // Get fullname
        TextView fnTxt = findViewById(R.id.upFullNameTxt);
        fnTxt.setTypeface(Configs.titSemibold);
        fnTxt.setText(userObj.getString(Configs.USER_FULLNAME));

        // Get about me
        TextView aboutTxt = findViewById(R.id.upAboutTxt);
        aboutTxt.setTypeface(Configs.titRegular);
        if (userObj.getString(Configs.USER_ABOUT_ME) != null) {
            aboutTxt.setText(userObj.getString(Configs.USER_ABOUT_ME));
        } else {
            aboutTxt.setText(R.string.user_profile_bio_error);
        }

        // Get joined since
        TextView joinedTxt = findViewById(R.id.upJoinedTxt);
        joinedTxt.setTypeface(Configs.titRegular);
        joinedTxt.setText(Configs.timeAgoSinceDate(userObj.getCreatedAt()));

        // Get verified
        TextView verifTxt = findViewById(R.id.upVerifiedTxt);
        verifTxt.setTypeface(Configs.titRegular);
        if (userObj.get(Configs.USER_EMAIL_VERIFIED) != null) {
            if (userObj.getBoolean(Configs.USER_EMAIL_VERIFIED)) {
                verifTxt.setText(R.string.user_profile_verified_yes);
            } else {
                verifTxt.setText(R.string.user_profile_verified_no);
            }
        } else {
            verifTxt.setText(R.string.user_profile_verified_no);
        }

        // Get avatar
        final ImageView avImg = findViewById(R.id.upAvatarImg);
        Configs.getParseImage(avImg, userObj, Configs.USER_AVATAR);


        // Call query
        queryUserAds();
    }


    // MARK: - QUERY MY ADS ------------------------------------------------------------------
    void queryUserAds() {
        Configs.showPD(getString(R.string.user_profile_loading_ads), UserProfile.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
        query.whereEqualTo(Configs.ADS_SELLER_POINTER, userObj);
        query.orderByDescending(Configs.ADS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    userAdsArray = objects;
                    Configs.hidePD();

                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;

                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }

                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_myads, null);
                            }

                            // Get Parse object
                            ParseObject adObj = userAdsArray.get(position);

                            // Get ad title
                            TextView titletxt = cell.findViewById(R.id.cmaAdTitleTxt);
                            titletxt.setTypeface(Configs.titSemibold);
                            titletxt.setText(adObj.getString(Configs.ADS_TITLE));

                            // Get ad price
                            TextView priceTxt = cell.findViewById(R.id.cmaPricetxt);
                            priceTxt.setTypeface(Configs.titRegular);
                            priceTxt.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

                            // Get date
                            TextView dateTxt = cell.findViewById(R.id.cmaDatetxt);
                            dateTxt.setTypeface(Configs.titRegular);
                            dateTxt.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));

                            // Get Image
                            final ImageView anImage = cell.findViewById(R.id.cmaImage);
                            Configs.getParseImage(anImage, adObj, Configs.ADS_IMAGE1);

                            return cell;
                        }

                        @Override
                        public int getCount() {
                            return userAdsArray.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return userAdsArray.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }
                    }

                    // Init ListView and set its adapter
                    ListView aList = findViewById(R.id.upUserAdsListView);
                    aList.setAdapter(new ListAdapter(UserProfile.this, userAdsArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            ParseObject adObj = userAdsArray.get(position);
                            Intent i = new Intent(UserProfile.this, AdDetailsActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString(AdDetailsActivity.AD_OBJ_ID_KEY, adObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);
                        }
                    });
                } else {
                    // Error in query
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), UserProfile.this);
                }
            }
        });
    }
}//@end
