package garts.domain.com.garts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;

public class SendFeedback extends AppCompatActivity {

    /* Views */
    Button sButt1, sButt2, sButt3, sButt4, sButt5;
    EditText reviewTxt;


    /* Variables */
    ParseObject adObj;
    ParseUser sellerObj;
    int starNr = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_feedback);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Init views
        reviewTxt = findViewById(R.id.sfReviewTxt);

        // MARK: - STAR BUTTONS
        sButt1 = findViewById(R.id.starButt1);
        sButt2 = findViewById(R.id.starButt2);
        sButt3 = findViewById(R.id.starButt3);
        sButt4 = findViewById(R.id.starButt4);
        sButt5 = findViewById(R.id.starButt5);

        sButt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starNr = 1;
                sButt1.setBackgroundResource(R.drawable.full_star);
                sButt2.setBackgroundResource(R.drawable.empty_star);
                sButt3.setBackgroundResource(R.drawable.empty_star);
                sButt4.setBackgroundResource(R.drawable.empty_star);
                sButt5.setBackgroundResource(R.drawable.empty_star);
            }
        });
        sButt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starNr = 2;
                sButt1.setBackgroundResource(R.drawable.full_star);
                sButt2.setBackgroundResource(R.drawable.full_star);
                sButt3.setBackgroundResource(R.drawable.empty_star);
                sButt4.setBackgroundResource(R.drawable.empty_star);
                sButt5.setBackgroundResource(R.drawable.empty_star);
            }
        });
        sButt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starNr = 3;
                sButt1.setBackgroundResource(R.drawable.full_star);
                sButt2.setBackgroundResource(R.drawable.full_star);
                sButt3.setBackgroundResource(R.drawable.full_star);
                sButt4.setBackgroundResource(R.drawable.empty_star);
                sButt5.setBackgroundResource(R.drawable.empty_star);
            }
        });
        sButt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starNr = 4;
                sButt1.setBackgroundResource(R.drawable.full_star);
                sButt2.setBackgroundResource(R.drawable.full_star);
                sButt3.setBackgroundResource(R.drawable.full_star);
                sButt4.setBackgroundResource(R.drawable.full_star);
                sButt5.setBackgroundResource(R.drawable.empty_star);
            }
        });
        sButt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starNr = 5;
                sButt1.setBackgroundResource(R.drawable.full_star);
                sButt2.setBackgroundResource(R.drawable.full_star);
                sButt3.setBackgroundResource(R.drawable.full_star);
                sButt4.setBackgroundResource(R.drawable.full_star);
                sButt5.setBackgroundResource(R.drawable.full_star);
            }
        });

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        String sellerID = extras.getString("sellerID");
        // Get adObj
        adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
        try {
            adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

            // Get sellerObj
            sellerObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, sellerID);
            try {
                sellerObj.fetchIfNeeded().getParseUser(Configs.USER_CLASS_NAME);
                TextView usernameTxt = findViewById(R.id.sfUsernameTxt);
                usernameTxt.setTypeface(Configs.titRegular);
                usernameTxt.setText(getString(R.string.send_feedback_to_username, sellerObj.getString(Configs.USER_USERNAME)));

                // MARK: - SEND FEEDBACK BUTTON
                Button sendButt = findViewById(R.id.sfSendButt);
                sendButt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissKeyboard();
                        Configs.showPD(getString(R.string.progress_dialog_loading), SendFeedback.this);

                        if (reviewTxt.getText().toString().matches("") || starNr == 0) {
                            Configs.hidePD();
                            Configs.simpleAlert(getString(R.string.send_feedback_input_validation_error), SendFeedback.this);
                        } else {
                            ParseObject fObj = new ParseObject(Configs.FEEDBACKS_CLASS_NAME);

                            fObj.put(Configs.FEEDBACKS_STARS, starNr);
                            fObj.put(Configs.FEEDBACKS_TEXT, reviewTxt.getText().toString());
                            fObj.put(Configs.FEEDBACKS_AD_TITLE, adObj.getString(Configs.ADS_TITLE));
                            fObj.put(Configs.FEEDBACKS_REVIEWER_POINTER, ParseUser.getCurrentUser());
                            fObj.put(Configs.FEEDBACKS_SELLER_POINTER, sellerObj);

                            fObj.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Configs.hidePD();

                                        // Send Push Notification
                                        final String pushMessage = "@" + ParseUser.getCurrentUser().getString(Configs.USER_USERNAME) + " sent you a " + starNr + "-star feedback for: '" + adObj.getString(Configs.ADS_TITLE) + "'";

                                        HashMap<String, Object> params = new HashMap<String, Object>();
                                        params.put("someKey", sellerObj.getObjectId());
                                        params.put("data", pushMessage);

                                        ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                            @Override
                                            public void done(String object, ParseException e) {
                                                if (e == null) {

                                                    Log.i("log-", "PUSH SENT TO: " + sellerObj.getString(Configs.USER_USERNAME)
                                                            + "\nMESSAGE: "
                                                            + pushMessage);

                                                    // Error in Cloud Code
                                                } else {
                                                    Configs.hidePD();
                                                    Configs.simpleAlert(e.getMessage(), SendFeedback.this);
                                                }
                                            }
                                        });

                                        // Save Activity
                                        ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                        actObj.put(Configs.ACTIVITY_CURRENT_USER, sellerObj);
                                        actObj.put(Configs.ACTIVITY_OTHER_USER, ParseUser.getCurrentUser());
                                        actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                        actObj.saveInBackground();

                                        // Fire Alert
                                        AlertDialog.Builder alert = new AlertDialog.Builder(SendFeedback.this);
                                        alert.setMessage(R.string.send_feedback_success)
                                                .setTitle(R.string.app_name)
                                                .setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                })
                                                .setCancelable(false)
                                                .setIcon(R.drawable.logo);
                                        alert.create().show();


                                        // error
                                    } else {
                                        Configs.hidePD();
                                        Configs.simpleAlert(e.getMessage(), SendFeedback.this);
                                    }
                                }
                            });
                        }/// end IF
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.sfBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }// end onCreate()

    // DISMISS KEYBOARD
    void dismissKeyboard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(reviewTxt.getWindowToken(), 0);
    }
}//@end
