package garts.domain.com.garts.wizard;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import garts.domain.com.garts.R;
import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.home.activities.HomeActivity;
import garts.domain.com.garts.landing.LoginActivity;
import garts.domain.com.garts.landing.TermsOfUse;
import garts.domain.com.garts.utils.Configs;

public class WizardActivity extends BaseActivity {

    private static final int CHANGE_PAGE_TIME_MILLIS = 3000;
    private static final int[] dotsDrawableRes = {R.drawable.dots1, R.drawable.dots2, R.drawable.dots3};

    private Button facebookBtn;

    private ImageView dotsIV;
    private ViewPager contentVP;

    private Handler timerHandler = new Handler();
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        initViews();
        setUpChangePageTimer();
        setUpWizardViewPager();

        facebookBtn.setTypeface(Configs.titSemibold);
        facebookBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WizardActivity.this);
                alert.setTitle(R.string.wizard_terms_alert_title)
                        .setIcon(R.drawable.logo)
                        .setItems(new CharSequence[]{
                                getString(R.string.wizard_terms_alert_accept),
                                getString(R.string.wizard_terms_alert_read)
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // SIGN IN WITH FACEBOOK
                                    case 0:
                                        List<String> permissions = Arrays.asList("public_profile","email");
                                        showLoading();

                                        ParseFacebookUtils.logInWithReadPermissionsInBackground(WizardActivity.this, permissions, new LogInCallback() {
                                            @Override
                                            public void done(ParseUser user, ParseException e) {
                                                if (user == null) {
                                                    Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                                                    hideLoading();
                                                } else if (user.isNew()) {
                                                    getUserDetailsFromFB();
                                                } else {
                                                    Log.i("log-", "RETURNING User logged in through Facebook!");
                                                    hideLoading();
                                                    startActivity(new Intent(WizardActivity.this, HomeActivity.class));
                                                }
                                            }
                                        });
                                        break;


                                    // OPEN TERMS OF SERVICE
                                    case 1:
                                        startActivity(new Intent(WizardActivity.this, TermsOfUse.class));
                                        break;

                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_cancel_button), null);
                alert.create().show();


            }
        });

        // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {
        }

        // MARK: - SIGN UP BUTTON ------------------------------------
        Button signupButt = findViewById(R.id.signupButt);
        signupButt.setTypeface(Configs.titRegular);
        signupButt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WizardActivity.this, LoginActivity.class));
            }
        });

        // MARK: - TERMS OF SERVICE BUTTON ------------------------------------
        Button tosButt = findViewById(R.id.tosButt);
        tosButt.setTypeface(Configs.titRegular);
        tosButt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WizardActivity.this, TermsOfUse.class));
            }
        });

        // MARK: - DISMISS BUTTON ------------------------------------
        Button dismissButt = findViewById(R.id.wDismissButt);
        dismissButt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void initViews() {
        dotsIV = findViewById(R.id.dotsImg);
        contentVP = findViewById(R.id.viewpager);
        facebookBtn = findViewById(R.id.facebookButt);
    }

    private void setUpChangePageTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (page > 2) {
                    timerHandler.removeCallbacksAndMessages(null);
                } else {
                    contentVP.setCurrentItem(page++);
                }
            }
        }, CHANGE_PAGE_TIME_MILLIS);
    }

    private void setUpWizardViewPager() {
        contentVP.setAdapter(new WizardPagerAdapter());
        contentVP.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < dotsDrawableRes.length) {
                    dotsIV.setImageResource(dotsDrawableRes[position]);
                }
            }
        });
    }

    private void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String email = "";
                String username = "";

                try {
                    name = object.getString("name");
                    email = object.getString("email");
                    facebookID = object.getString("id");


                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) {
                        username += word;
                    }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "email: " + email + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalEmail = email;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);
                if (finalEmail != null) {
                    currUser.put(Configs.USER_EMAIL, finalEmail);
                } else {
                    currUser.put(Configs.USER_EMAIL, facebookID + "@facebook.com");
                }
                currUser.put(Configs.USER_FULLNAME, finalName);
                currUser.put(Configs.USER_IS_REPORTED, false);
                List<String> hasBlocked = new ArrayList<String>();
                currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("log-", "NEW USER signed up and logged in through Facebook...");


                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "... AND AVATAR SAVED!");
                                            hideLoading();
                                            startActivity(new Intent(WizardActivity.this, HomeActivity.class));
                                        }
                                    });
                                } catch (IOException error) {
                                    error.printStackTrace();
                                }

                            }
                        }, 1000); // 1 second


                    }
                }); // end saveInBackground

            }
        }); // end graphRequest

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email, name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
