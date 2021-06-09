package garts.domain.com.garts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import garts.domain.com.garts.ads.activities.AdDetailsActivity;
import garts.domain.com.garts.filters.models.ReportType;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.MarshMallowPermission;
import garts.domain.com.garts.R;

public class InboxActivity extends AppCompatActivity {

    /* Views */
    Button optionsButt, sendButt, uploadPicButt;
    TextView usernameTxt, adTitleTxt, adPriceTxt;
    EditText messageTxt;
    ListView iListView;
    ImageView imgPreview;

    /* Variables */
    ParseUser userObj;
    ParseObject adObj;
    List<ParseObject> inboxArray;
    List<ParseObject> chatsArray;
    String lastMessageStr = null;
    Timer refreshTimerForInboxQuery = new Timer();
    Bitmap imageToSend = null;
    MarshMallowPermission mmp = new MarshMallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_activity);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Init views
        optionsButt = findViewById(R.id.inOptionsButt);
        uploadPicButt = findViewById(R.id.inUploadPicButt);
        sendButt = findViewById(R.id.inSendButt);
        sendButt.setTypeface(Configs.titSemibold);
        usernameTxt = findViewById(R.id.inUsernameTxt);
        usernameTxt.setTypeface(Configs.titSemibold);
        adTitleTxt = findViewById(R.id.inAdTitleTxt);
        adTitleTxt.setTypeface(Configs.titSemibold);
        adPriceTxt = findViewById(R.id.inAdPriceTxt);
        adPriceTxt.setTypeface(Configs.titRegular);
        messageTxt = findViewById(R.id.inMessageTxt);
        messageTxt.setTypeface(Configs.titRegular);

        iListView = findViewById(R.id.inInboxListView);

        // Hide imgPreview
        imgPreview = findViewById(R.id.inImagePreview);
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imgPreview.getLayoutParams());
        marginParams.setMargins(0, 2000, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        imgPreview.setLayoutParams(layoutParams);

        // Hide imgPreview on click
        imgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imgPreview.getLayoutParams());
                marginParams.setMargins(0, 2000, 0, 0);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                imgPreview.setLayoutParams(layoutParams);
            }
        });

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String sellerID = extras.getString("sellerObjectID");
        String adID = extras.getString("adObjectID");

        // Get userObj
        userObj = (ParseUser) ParseUser.createWithoutData(Configs.USER_CLASS_NAME, sellerID);
        try {
            userObj.fetchIfNeeded().getParseUser(Configs.USER_CLASS_NAME);

            // Get adObj
            adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, adID);
            try {
                adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

                // Call query
                queryInbox();

                // Get User's username
                usernameTxt.setText(getString(R.string.username_formatted, userObj.getString(Configs.USER_USERNAME)));

                // Get AD details
                adTitleTxt.setText(adObj.getString(Configs.ADS_TITLE));
                adPriceTxt.setText(adObj.get(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

                // Get Image
                final ImageView adImage = findViewById(R.id.inAdImg);
                Configs.getParseImage(adImage, adObj, Configs.ADS_IMAGE1);

                // TAP AD BOX -> SHOW AD DETAILS
                RelativeLayout adBox = findViewById(R.id.inAdBoxLayout);
                adBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(InboxActivity.this, AdDetailsActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(AdDetailsActivity.AD_OBJ_ID_KEY, adObj.getObjectId());
                        i.putExtras(extras);
                        startActivity(i);
                    }
                });

                // Start refresh Timer for Inbox query
                startRefreshTimer();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // MARK: - UPLOAD IMAGE BUTTON ------------------------------------
        uploadPicButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(InboxActivity.this);
                alert.setMessage(R.string.select_image_source_alert_title)
                        .setTitle(R.string.app_name)
                        .setPositiveButton(getString(R.string.take_picture_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!mmp.checkPermissionForCamera()) {
                                    mmp.requestPermissionForCamera();
                                } else {
                                    openCamera();
                                }
                            }
                        })
                        .setNegativeButton(R.string.pick_image_from_gallery_title, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!mmp.checkPermissionForReadExternalStorage()) {
                                    mmp.requestPermissionForReadExternalStorage();
                                } else {
                                    openGallery();
                                }
                            }
                        })
                        .setNeutralButton(getString(R.string.alert_cancel_button), null)
                        .setIcon(R.drawable.logo);
                alert.create().show();
            }
        });


        // MARK: - SEND MESSAGE BUTTON ---------------------------------------------
        sendButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageTxt.getText().toString().matches("")) {
                    Configs.simpleAlert(getString(R.string.inbox_input_validation_error), InboxActivity.this);
                } else {
                    sendMessage();
                }
            }
        });

        // OPTIONS BUTTON -------------------------------------------------
        optionsButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ParseUser currUser = ParseUser.getCurrentUser();
                final List<String> hasBlocked = currUser.getList(Configs.USER_HAS_BLOCKED);

                // Set blockUser  Action title
                String blockTitle = null;
                if (hasBlocked.contains(userObj.getObjectId())) {
                    blockTitle = getString(R.string.inbox_unblock_user_title);
                } else {
                    blockTitle = getString(R.string.inbox_block_user_title);
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(InboxActivity.this);
                final String finalBlockTitle = blockTitle;
                alert.setMessage(R.string.inbox_options_alert_title)
                        .setTitle(R.string.app_name)

                        // REPORT USER -------------------------------------------------------------
                        .setPositiveButton(R.string.inbox_report_user_option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                // Pass objectID to the other Activity
                                Intent in = new Intent(InboxActivity.this, ReportAdOrUserActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString(ReportAdOrUserActivity.AD_OBJECT_ID_EXTRA_KEY, adObj.getObjectId());
                                extras.putString(ReportAdOrUserActivity.USER_OBJECT_ID_EXTRA_KEY, userObj.getObjectId());
                                extras.putString(ReportAdOrUserActivity.REPORT_TYPE_EXTRA_KEY, ReportType.USER.getValue());
                                in.putExtras(extras);
                                startActivity(in);
                            }
                        })
                        // BLOCK/UNBLOCK USER ------------------------------------------------------
                        .setNegativeButton(blockTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Block User
                                if (finalBlockTitle.matches(getString(R.string.inbox_block_user_option))) {
                                    hasBlocked.add(userObj.getObjectId());
                                    currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(InboxActivity.this);
                                            alert.setMessage(getString(R.string.inbox_block_success, userObj.getString(Configs.USER_USERNAME)))
                                                    .setTitle(R.string.app_name)
                                                    .setPositiveButton(getString(R.string.alert_ok_button), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            finish();
                                                        }
                                                    })
                                                    .setIcon(R.drawable.logo);
                                            alert.create().show();
                                        }
                                    });
                                } else {
                                    // Unblock User
                                    hasBlocked.remove(userObj.getObjectId());
                                    currUser.put(Configs.USER_HAS_BLOCKED, hasBlocked);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Configs.simpleAlert(getString(R.string.inbox_unblocked_user, userObj.getString(Configs.USER_USERNAME)), InboxActivity.this);
                                        }
                                    });
                                }
                            }
                        })
                        // DELETE CHAT -------------------------------------------------------------
                        .setNeutralButton(R.string.inbox_delete_chat_option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(InboxActivity.this);
                                alert.setMessage(getString(R.string.inbox_delete_chat_alert_title, userObj.getString(Configs.USER_USERNAME)))
                                        .setTitle(R.string.app_name)
                                        .setPositiveButton(R.string.inbox_delete_chat_option, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Delete all Inbox messages
                                                for (int j = 0; j < inboxArray.size(); j++) {
                                                    ParseObject iObj = new ParseObject(Configs.INBOX_CLASS_NAME);
                                                    iObj = inboxArray.get(j);
                                                    iObj.deleteInBackground(new DeleteCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            Log.i("log-", "DELETED INBOX ITEM: ");
                                                        }
                                                    });
                                                }

                                                // Delete Chat in Chats class
                                                String inboxId1 = ParseUser.getCurrentUser().getObjectId() + userObj.getObjectId();
                                                String inboxId2 = userObj.getObjectId() + ParseUser.getCurrentUser().getObjectId();

                                                final ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CHATS_CLASS_NAME);
                                                String[] ids = {inboxId1, inboxId2};
                                                query.whereContainedIn("chatID", Arrays.asList(ids));
                                                query.whereContainedIn("chatID", Arrays.asList(ids));

                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    public void done(List<ParseObject> objects, ParseException error) {
                                                        if (error == null) {
                                                            chatsArray = objects;
                                                            ParseObject chatsObj = new ParseObject(Configs.CHATS_CLASS_NAME);
                                                            chatsObj = chatsArray.get(0);
                                                            chatsObj.deleteInBackground(new DeleteCallback() {
                                                                @Override
                                                                public void done(ParseException e) {
                                                                    if (e == null) {
                                                                        Log.i("log-", "CHAT DELETED IN 'Chats' CLASS");
                                                                        finish();
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }
                                                });

                                            }
                                        })// end alert
                                        .setNegativeButton("Cancel", null)
                                        .setIcon(R.drawable.logo);
                                alert.create().show();
                            }
                        })
                        .setIcon(R.drawable.logo);
                alert.create().show();
            }
        });

        // BACK BUTTON -------------------------------------------------
        Button backButt = findViewById(R.id.inBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshTimerForInboxQuery.cancel();
                imageToSend = null;
                finish();
            }
        });

        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }// end onCreate()

    // TIMER THAT RECALLS THE QUERY FOR INBOX MESSAGES
    void startRefreshTimer() {
        int delay = 15000;
        refreshTimerForInboxQuery.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Call query
                        queryInbox();

                    }
                });
            }
        }, delay, delay);
    }

    // MARK: - QUERY INBOX --------------------------------------------------------
    void queryInbox() {
        String inboxId1 = ParseUser.getCurrentUser().getObjectId() + userObj.getObjectId();
        String inboxId2 = userObj.getObjectId() + ParseUser.getCurrentUser().getObjectId();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.INBOX_CLASS_NAME);
        String[] ids = {inboxId1, inboxId2};
        query.whereContainedIn("inboxID", Arrays.asList(ids));
        query.whereContainedIn("inboxID", Arrays.asList(ids));

        query.whereEqualTo(Configs.INBOX_AD_POINTER, adObj);

        query.orderByAscending(Configs.INBOX_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    inboxArray = objects;

                    // Scroll listView to bottom after 1.5 sec
                    if (objects.size() != 0) {
                        int delay = 1500;
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollListViewToBottom();
                                    }
                                });
                            }
                        }, delay, delay);
                    }

                    // Init ListView and set its adapter
                    iListView.setAdapter(new InboxListAdapter(InboxActivity.this, inboxArray));
                    // Error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), InboxActivity.this);
                }
            }
        });
    }

    void scrollListViewToBottom() {
        iListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        iListView.setStackFromBottom(true);
    }

    // INBOX LIST ADAPTER ----------------------------------------------------------------------
    class InboxListAdapter extends BaseAdapter {
        private Context context;

        public InboxListAdapter(Context context, List<ParseObject> objects) {
            super();
            this.context = context;
        }

        // CONFIGURE CELL
        @Override
        public View getView(int position, View cell, ViewGroup parent) {
            if (cell == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                cell = inflater.inflate(R.layout.cell_inbox, null);
            }
            final View finalCell = cell;

            // Get Parse object
            final ParseObject iObj = inboxArray.get(position);

            // Init cells layouts
            final RelativeLayout senderCell = finalCell.findViewById(R.id.senderCell);
            final RelativeLayout receiverCell = finalCell.findViewById(R.id.receiverCell);

            // Get userPointer
            iObj.getParseObject(Configs.INBOX_SENDER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject userPointer, ParseException e) {
                    // CELL WITH MESSAGE FROM CURRENT USER (SENDER) -------------------------------
                    if (userPointer.getString(Configs.USER_USERNAME).matches(ParseUser.getCurrentUser().getUsername())) {
                        senderCell.setVisibility(View.VISIBLE);
                        receiverCell.setVisibility(View.INVISIBLE);

                        // Get username
                        TextView usernTxt = finalCell.findViewById(R.id.sUsernameTxt);
                        usernTxt.setTypeface(Configs.titSemibold);
                        usernTxt.setText(getString(R.string.username_formatted, userPointer.getString(Configs.USER_USERNAME)));

                        // Get message
                        TextView messTxt = finalCell.findViewById(R.id.sMessTxt);
                        messTxt.setTypeface(Configs.titLight);
                        messTxt.setText(iObj.getString(Configs.INBOX_MESSAGE));

                        // Get date
                        TextView dateTxt = finalCell.findViewById(R.id.sDateTxt);
                        dateTxt.setTypeface(Configs.titLight);
                        dateTxt.setText(Configs.timeAgoSinceDate(iObj.getCreatedAt()));

                        // Get avatar
                        final ImageView anImage = finalCell.findViewById(R.id.sAvatarImg);
                        Configs.getParseImage(anImage, userPointer, Configs.USER_AVATAR);


                        // THIS MESSAGE HAS AN IMAGE ------------------------
                        if (iObj.get(Configs.INBOX_IMAGE) != null) {

                            final ImageView inImage = finalCell.findViewById(R.id.sImage);
                            inImage.setClipToOutline(true);
                            inImage.setVisibility(View.VISIBLE);
                            messTxt.setVisibility(View.INVISIBLE);

                            // Get the image
                            final Bitmap[] bm = {null};
                            ParseFile fileObject2 = iObj.getParseFile(Configs.INBOX_IMAGE);
                            fileObject2.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            inImage.setImageBitmap(bmp);
                                            bm[0] = bmp;
                                        }
                                    }
                                }
                            });

                            inImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imgPreview.getLayoutParams());
                                    marginParams.setMargins(0, 0, 0, 0);
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                                    imgPreview.setLayoutParams(layoutParams);
                                    imgPreview.setImageBitmap(bm[0]);

                                    Toast.makeText(InboxActivity.this, R.string.inbox_image_message_tap_to_close, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // CELL WITH MESSAGE FROM RECEIVER ---------------------------------------------
                        senderCell.setVisibility(View.INVISIBLE);
                        receiverCell.setVisibility(View.VISIBLE);

                        // Get username
                        TextView usernTxt = finalCell.findViewById(R.id.rUsernameTxt);
                        usernTxt.setTypeface(Configs.titSemibold);
                        usernTxt.setText(getString(R.string.username_formatted, userPointer.getString(Configs.USER_USERNAME)));

                        // Get message
                        TextView messTxt = finalCell.findViewById(R.id.rMessTxt);
                        messTxt.setTypeface(Configs.titLight);
                        messTxt.setText(iObj.getString(Configs.INBOX_MESSAGE));

                        // Get date
                        TextView dateTxt = finalCell.findViewById(R.id.rDateTxt);
                        dateTxt.setTypeface(Configs.titLight);
                        dateTxt.setText(Configs.timeAgoSinceDate(iObj.getCreatedAt()));

                        // Get avatar
                        final ImageView anImage = finalCell.findViewById(R.id.rAvatarImg);
                        Configs.getParseImage(anImage, userPointer, Configs.USER_AVATAR);

                        // THIS MESSAGE HAS AN IMAGE -------------------
                        if (iObj.get(Configs.INBOX_IMAGE) != null) {

                            final ImageView inImage = finalCell.findViewById(R.id.rImage);
                            inImage.setClipToOutline(true);
                            inImage.setVisibility(View.VISIBLE);
                            messTxt.setVisibility(View.INVISIBLE);

                            // Get the image
                            final Bitmap[] bm = {null};
                            ParseFile fileObject2 = iObj.getParseFile(Configs.INBOX_IMAGE);
                            fileObject2.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            inImage.setImageBitmap(bmp);
                                            bm[0] = bmp;
                                        }
                                    }
                                }
                            });

                            inImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imgPreview.getLayoutParams());
                                    marginParams.setMargins(0, 0, 0, 0);
                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
                                    imgPreview.setLayoutParams(layoutParams);
                                    imgPreview.setImageBitmap(bm[0]);

                                    Toast.makeText(InboxActivity.this, getString(R.string.inbox_image_message_tap_to_close), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });// end userPointer

            return cell;
        }

        @Override
        public int getCount() {
            return inboxArray.size();
        }

        @Override
        public Object getItem(int position) {
            return inboxArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }


    // MARK: - SEND MESSAGE ------------------------------------------------------------------------
    void sendMessage() {
        Configs.showPD(getString(R.string.inbox_message_sending_loading), InboxActivity.this);

        final ParseObject iObj = new ParseObject(Configs.INBOX_CLASS_NAME);
        final ParseUser currentUser = ParseUser.getCurrentUser();

        // Save Message to Inbox Class
        iObj.put(Configs.INBOX_SENDER, currentUser);
        iObj.put(Configs.INBOX_RECEIVER, userObj);
        iObj.put(Configs.INBOX_AD_POINTER, adObj);
        iObj.put(Configs.INBOX_INBOX_ID, currentUser.getObjectId() + userObj.getObjectId());
        iObj.put(Configs.INBOX_MESSAGE, messageTxt.getText().toString());
        String messageStr = messageTxt.getText().toString();
        lastMessageStr = messageStr;


        // SEND AN IMAGE (if it exists) ------------------
        if (imageToSend != null) {
            Configs.showPD(getString(R.string.inbox_image_sending_loading), InboxActivity.this);

            // Save image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageToSend.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ParseFile imageFile = new ParseFile("image.jpg", byteArray);
            iObj.put(Configs.INBOX_IMAGE, imageFile);

            iObj.put(Configs.INBOX_MESSAGE, "[Picture]");
            lastMessageStr = "[Picture]";
        }

        // Saving block
        iObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Configs.hidePD();
                    dismisskeyboard();
                    imageToSend = null;
                    Log.i("log-", "IMAGE TO SEND: " + imageToSend);

                    // Call save LastMessage
                    saveLastMessageInChats();

                    // Add message to the array (it's temporary, before a new query gets automatically called)
                    inboxArray.add(iObj);
                    iListView.invalidateViews();
                    iListView.refreshDrawableState();
                    scrollListViewToBottom();

                    // Send push notification
                    final String pushMessage = "@" + ParseUser.getCurrentUser().getString(Configs.USER_USERNAME) + " | '" + adObj.getString(Configs.ADS_TITLE) + "':\n" + lastMessageStr;

                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("someKey", userObj.getObjectId());
                    params.put("data", pushMessage);

                    ParseCloud.callFunctionInBackground("pushAndroid", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String object, ParseException e) {
                            if (e == null) {
                                Log.i("log-", "PUSH SENT TO: " + userObj.getString(Configs.USER_USERNAME)
                                        + "\nMESSAGE: "
                                        + pushMessage);

                                // error in Cloud Code
                            } else {
                                Configs.simpleAlert(e.getMessage(), InboxActivity.this);
                            }
                        }
                    });
                } else {
                    // error on Saving
                    Configs.hidePD();
                    Configs.simpleAlert(e.getMessage(), InboxActivity.this);
                }
            }
        });
    }

    // MARK: - SAVE LAST MESSAGE IN CHATS CLASS -------------------------------------------------
    void saveLastMessageInChats() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        String inboxId1 = ParseUser.getCurrentUser().getObjectId() + userObj.getObjectId();
        String inboxId2 = userObj.getObjectId() + ParseUser.getCurrentUser().getObjectId();

        final ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CHATS_CLASS_NAME);
        String[] ids = {inboxId1, inboxId2};
        query.whereContainedIn("chatID", Arrays.asList(ids));
        query.whereContainedIn("chatID", Arrays.asList(ids));

        query.whereEqualTo(Configs.CHATS_AD_POINTER, adObj);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    chatsArray = objects;

                    ParseObject chatsObj = new ParseObject(Configs.CHATS_CLASS_NAME);

                    if (chatsArray.size() != 0) {
                        chatsObj = chatsArray.get(0);
                    }

                    Log.i("log-", "CHATS ARRAY: " + chatsArray);

                    // Update Last message
                    chatsObj.put(Configs.CHATS_LAST_MESSAGE, lastMessageStr);
                    chatsObj.put(Configs.CHATS_USER_POINTER, currentUser);
                    chatsObj.put(Configs.CHATS_OTHER_USER, userObj);
                    chatsObj.put(Configs.CHATS_ID, currentUser.getObjectId() + userObj.getObjectId());
                    chatsObj.put(Configs.CHATS_AD_POINTER, adObj);

                    // Saving block
                    chatsObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.i("log-", "LAST MESS SAVED: " + lastMessageStr);
                            } else {
                                Configs.simpleAlert(e.getMessage(), InboxActivity.this);
                            }
                        }
                    });

                    // error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), InboxActivity.this);
                }
            }
        });
    }

    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;

    // OPEN CAMERA
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }

    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }

    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {

                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        angle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        angle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        angle = 270;
                    }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                } catch (IOException | OutOfMemoryError e) {
                    Log.i("log-", e.getMessage());
                }
            } else if (requestCode == GALLERY) {
                // Image from Gallery
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Set images based on the pictureTag
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(450, bm);
            imageToSend = scaledBm;

            // Send message with image
            sendMessage();
        }
    }

    // RESET imageToSend TO NULL
    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageToSend = null;
    }

    // MARK: - DISMISS KEYBOARD
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageTxt.getWindowToken(), 0);
        messageTxt.setText("");
    }
}//@end
