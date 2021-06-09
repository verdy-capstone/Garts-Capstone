package garts.domain.com.garts;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;

import garts.domain.com.garts.utils.Configs;

public class Comments extends AppCompatActivity {

    /* Views */
    TextView adTitleTxt;
    EditText commEditText;
    ListView commListView;

    /* Variables */
    ParseObject adObj;
    List<ParseObject> commentsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Init views
        adTitleTxt = findViewById(R.id.commAdNameTxt);
        adTitleTxt.setTypeface(Configs.titRegular);
        commEditText = findViewById(R.id.commCommentEditText);
        commEditText.setTypeface(Configs.titRegular);
        commListView = findViewById(R.id.commListView);

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
        try {
            adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

            // Set Ad title
            adTitleTxt.setText(adObj.getString(Configs.ADS_TITLE));

            // Call query
            queryComments();

            // MARK: - SEND COMMENT BUTTON ------------------------------------
            Button sendButt = findViewById(R.id.commSendButt);
            sendButt.setTypeface(Configs.titSemibold);
            sendButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (commEditText.getText().toString().matches("")) {
                        Configs.simpleAlert(getString(R.string.comments_input_validation_error), Comments.this);
                    } else {
                        dismissKeyboard();
                        Configs.showPD(getString(R.string.progress_dialog_loading), Comments.this);

                        final ParseObject commObj = new ParseObject(Configs.COMMENTS_CLASS_NAME);
                        ParseUser currentUser = ParseUser.getCurrentUser();

                        commObj.put(Configs.COMMENTS_USER_POINTER, currentUser);
                        commObj.put(Configs.COMMENTS_AD_POINTER, adObj);
                        commObj.put(Configs.COMMENTS_COMMENT, commEditText.getText().toString());

                        // Saving block
                        commObj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {

                                    // Send push notification
                                    final ParseUser currUser = ParseUser.getCurrentUser();

                                    // Get userPointer
                                    adObj.getParseObject(Configs.ADS_SELLER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                        public void done(final ParseObject sellerPointer, ParseException e) {

                                            final String pushMessage = getString(R.string.comments_user_commented, currUser.getString(Configs.USER_USERNAME), adObj.getString(Configs.ADS_TITLE));

                                            HashMap<String, Object> params = new HashMap<String, Object>();
                                            params.put("someKey", sellerPointer.getObjectId());
                                            params.put("data", pushMessage);

                                            ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                                                @Override
                                                public void done(String object, ParseException e) {
                                                    if (e == null) {
                                                        Log.i("log-", "PUSH SENT TO: " + sellerPointer.getString(Configs.USER_USERNAME)
                                                                + "\nMESSAGE: "
                                                                + pushMessage);

                                                        // Error in Cloud Code
                                                    } else {
                                                        Configs.hidePD();
                                                        Configs.simpleAlert(e.getMessage(), Comments.this);
                                                    }
                                                }
                                            });

                                            // Update comments amount in Ads class
                                            adObj.increment(Configs.ADS_COMMENTS, 1);
                                            adObj.saveInBackground();

                                            // Save Activity
                                            ParseObject actObj = new ParseObject(Configs.ACTIVITY_CLASS_NAME);
                                            actObj.put(Configs.ACTIVITY_CURRENT_USER, sellerPointer);
                                            actObj.put(Configs.ACTIVITY_OTHER_USER, ParseUser.getCurrentUser());
                                            actObj.put(Configs.ACTIVITY_TEXT, pushMessage);
                                            actObj.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    Configs.hidePD();

                                                    // Lastly recall query
                                                    queryComments();
                                                    commEditText.setText("");
                                                }
                                            });

                                        }
                                    });// end sellerPointer

                                    // error on saving
                                } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(e.getMessage(), Comments.this);
                                }
                            }
                        });
                    }// end IF
                }
            });

            // MARK: - REFRESH BUTTON ------------------------------------
            Button refreshButt = findViewById(R.id.commRefreshButt);
            refreshButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissKeyboard();
                    queryComments();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.commBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }// end onCreate()


    // MARK: - QUERY COMMENTS --------------------------------------------------------------------
    void queryComments() {
        Configs.showPD(getString(R.string.progress_dialog_loading), Comments.this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
        query.whereEqualTo(Configs.COMMENTS_AD_POINTER, adObj);
        query.orderByDescending(Configs.COMMENTS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    commentsArray = objects;
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
                                cell = inflater.inflate(R.layout.cell_comment, null);
                            }
                            final View finalCell = cell;

                            // Get Parse object
                            final ParseObject cObj = commentsArray.get(position);

                            // Get userPointer
                            cObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {

                                    // Get username
                                    TextView uTxt = finalCell.findViewById(R.id.cCommUsernameTxt);
                                    uTxt.setTypeface(Configs.titSemibold);
                                    uTxt.setText(userPointer.getString(Configs.USER_USERNAME));

                                    // Get avatar
                                    final ImageView anImage = finalCell.findViewById(R.id.cCommAvatarImg);
                                    Configs.getParseImage(anImage, userPointer, Configs.USER_AVATAR);


                                    // Get comment
                                    TextView commTxt = finalCell.findViewById(R.id.cCommCommentTxt);
                                    commTxt.setTypeface(Configs.titRegular);
                                    commTxt.setText(cObj.getString(Configs.COMMENTS_COMMENT));

                                    // Get date
                                    TextView dateTxt = finalCell.findViewById(R.id.cCommDateTxt);
                                    dateTxt.setTypeface(Configs.titRegular);
                                    dateTxt.setText(Configs.timeAgoSinceDate(cObj.getCreatedAt()));
                                }
                            });// end userPointer

                            return cell;
                        }

                        @Override
                        public int getCount() {
                            return commentsArray.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return commentsArray.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }
                    }

                    // Init ListView and set its adapter
                    commListView.setAdapter(new ListAdapter(Comments.this, commentsArray));
                    commListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            // Get Parse object
                            final ParseObject cObj = commentsArray.get(position);

                            // Get userPointer
                            cObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject userPointer, ParseException e) {
                                    /*
                                    Intent i = new Intent(Comments.this, UserPorfile.class);
                                    Bundle extras = new Bundle();
                                    extras.putString("objectID", userPointer.getObjectId());
                                    i.putExtras(extras);
                                    startActivity(i);
                                    */
                                }
                            });// end userPointer

                        }
                    });

                    // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Comments.this);
                }
            }
        });
    }

    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commEditText.getWindowToken(), 0);
    }
}//@end
