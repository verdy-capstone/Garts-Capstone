package garts.domain.com.garts.utils;


import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import garts.domain.com.garts.wizard.WizardActivity;
import garts.domain.com.garts.R;


public class Configs extends Application {

    private static final String ONESIGNAL_APP_ID = "h40t0s00-ae00-4f21-8ef0-7e6f083b78f0";

    // IMPORTANT: Replace the red strings below with your own App ID and Client Key of your app on back4app.com
    public static String PARSE_APP_ID = "5HbuS9iC3KA3yU3AglOFBqG0AhzBQeROjgwr3AjG";
    public static String PARSE_CLIENT_KEY = "kvy95tPEBhBSucbSsEfs1jHOihK58DSiZSORrp3C";


    // THIS IS THE RED MAIN COLOR OF THIS APP
    public static String MAIN_COLOR = "#fa334a";


    // YOU CAN CHANGE THE CURRENCY SYMBOL AS YOU WISH
    public static String CURRENCY = "Rp.";


    // DECLARE FONT FILES (the font files are located into "app/src/main/assets/font" folder)
    public static Typeface qsBold, qsLight, qsRegular, titBlack, titLight, titRegular, titSemibold;


    // THIS IS THE MAX DURATION OF A VIDEO RECORDING FOR AN AD (in seconds)
    public static int MAXIMUM_DURATION_VIDEO = 10;


    // YOU CAN CHANGE THE DEFAULT LOCATION COORDINATES FOR ADS AS YOU WISH BY EDITING THE FLOAT VALUES BELOW:
    public static LatLng DEFAULT_LOCATION = new LatLng(40.7143528, -74.0059731);


    // YOU CAN CHANGE THE AD REPORT OPTIONS BELOW AS YOU WISH
    public static String[] reportAdOptions = {
            "Prohibited item",
            "Conterfeit",
            "Wrong category",
            "Keyword spam",
            "Repeated post",
            "Nudity/pornography/mature content",
            "Hateful speech/blackmail",
    };


    // YOU CAN CHANGE THE USER REPORT OPTIONS BELOW AS YOU WISH
    public static String[] reportUserOptions = {
            "Selling counterfeit items",
            "Selling prohibited items",
            "Items wrongly categorized",
            "Nudity/pornography/mature content",
            "Keyword spammer",
            "Hateful speech/blackmail",
            "Suspected fraudster",
            "No-show on meetup",
            "Backed out of deal",
            "Touting",
            "Spamming",
    };


    /***********  DO NOT EDIT THE CODE BELOW!! **********/

    public static String USER_CLASS_NAME = "_User";
    public static String USER_USERNAME = "username";
    public static String USER_EMAIL = "email";
    public static String USER_EMAIL_VERIFIED = "emailVerified";
    public static String USER_FULLNAME = "fullName";
    public static String USER_AVATAR = "avatar";
    public static String USER_LOCATION = "location";
    public static String USER_ABOUT_ME = "aboutMe";
    public static String USER_WEBSITE = "website";
    public static String USER_IS_REPORTED = "isReported";
    public static String USER_REPORT_MESSAGE = "reportMessage";
    public static String USER_HAS_BLOCKED = "hasBlocked";

    public static String CATEGORIES_CLASS_NAME = "Categories";
    public static String CATEGORIES_CATEGORY = "category";
    public static String CATEGORIES_IMAGE = "image";

    public static String SUBCATEGORIES_CLASS_NAME = "Subcategories";
    public static String SUBCATEGORIES_SUBCATEGORY = "subcategory";
    public static String SUBCATEGORIES_CATEGORY = "category";

    public static String ADS_CLASS_NAME = "Ads";
    public static String ADS_SELLER_POINTER = "sellerPointer";
    public static String ADS_LIKED_BY = "likedBy"; // Array
    public static String ADS_KEYWORDS = "keywords"; // Array
    public static String ADS_TITLE = "title";
    public static String ADS_PRICE = "price";
    public static String ADS_CURRENCY = "currency";
    public static String ADS_CATEGORY = "category";
    public static String ADS_SUBCATEGORY = "subcategory";
    public static String ADS_LOCATION = "location";
    public static String ADS_IMAGE1 = "image1";
    public static String ADS_IMAGE2 = "image2";
    public static String ADS_IMAGE3 = "image3";
    public static String ADS_VIDEO = "video";
    public static String ADS_VIDEO_THUMBNAIL = "videoThumbnail";
    public static String ADS_DESCRIPTION = "description";
    public static String ADS_CONDITION = "condition";
    public static String ADS_LIKES = "likes";
    public static String ADS_COMMENTS = "comments";
    public static String ADS_IS_REPORTED = "isReported";
    public static String ADS_REPORT_MESSAGE = "reportMessage";
    public static String ADS_CREATED_AT = "createdAt";


    public static String LIKES_CLASS_NAME = "Likes";
    public static String LIKES_CURR_USER = "currUser";
    public static String LIKES_AD_LIKED = "adLiked";
    public static String LIKES_CREATED_AT = "createdAt";

    public static String COMMENTS_CLASS_NAME = "Comments";
    public static String COMMENTS_USER_POINTER = "userPointer";
    public static String COMMENTS_AD_POINTER = "adPointer";
    public static String COMMENTS_COMMENT = "comment";
    public static String COMMENTS_CREATED_AT = "createdAt";

    public static String ACTIVITY_CLASS_NAME = "Activity";
    public static String ACTIVITY_CURRENT_USER = "currUser";
    public static String ACTIVITY_OTHER_USER = "otherUser";
    public static String ACTIVITY_TEXT = "text";
    public static String ACTIVITY_CREATED_AT = "createdAt";


    public static String INBOX_CLASS_NAME = "Inbox";
    public static String INBOX_AD_POINTER = "adPointer";
    public static String INBOX_SENDER = "sender";
    public static String INBOX_RECEIVER = "receiver";
    public static String INBOX_INBOX_ID = "inboxID";
    public static String INBOX_MESSAGE = "message";
    public static String INBOX_IMAGE = "image";
    public static String INBOX_CREATED_AT = "createdAt";

    public static String CHATS_CLASS_NAME = "Chats";
    public static String CHATS_LAST_MESSAGE = "lastMessage";
    public static String CHATS_USER_POINTER = "userPointer";
    public static String CHATS_OTHER_USER = "otherUser";
    public static String CHATS_ID = "chatID";
    public static String CHATS_AD_POINTER = "adPointer";
    public static String CHATS_CREATED_AT = "createdAt";

    public static String FEEDBACKS_CLASS_NAME = "Feedbacks";
    public static String FEEDBACKS_AD_TITLE = "adTitle";
    public static String FEEDBACKS_SELLER_POINTER = "sellerPointer";
    public static String FEEDBACKS_REVIEWER_POINTER = "reviewerPointer";
    public static String FEEDBACKS_STARS = "stars";
    public static String FEEDBACKS_TEXT = "text";


    /* Global Variables */
    public static float distanceInMiles = (float) 50;
    public static String IMAGE_FORMAT = ".jpg";
    // For query pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PAGE_THRESHOLD = 3;

    boolean isParseInitialized = false;

    private static Configs instance;

    public static Configs getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;

        if (!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_ID))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;


            // Init Facebook Utils
            ParseFacebookUtils.initialize(this);
        }

        // Init fonts
        qsBold = Typeface.createFromAsset(getAssets(), "font/Quicksand-Bold.ttf");
        qsLight = Typeface.createFromAsset(getAssets(), "font/Quicksand-Light.ttf");
        qsRegular = Typeface.createFromAsset(getAssets(), "font/Quicksand-Regular.ttf");
        titBlack = Typeface.createFromAsset(getAssets(), "font/Titillium-Black.otf");
        titLight = Typeface.createFromAsset(getAssets(), "font/Titillium-Light.otf");
        titRegular = Typeface.createFromAsset(getAssets(), "font/Titillium-Regular.otf");
        titSemibold = Typeface.createFromAsset(getAssets(), "font/Titillium-Semibold.otf");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        try{
            String userId = OneSignal.getDeviceState().getUserId();
            Log.i("consumer_user_id",userId);
        }catch (NullPointerException nullEx){
            nullEx.printStackTrace();
        }

        OneSignal.setNotificationWillShowInForegroundHandler(new OneSignal.OSNotificationWillShowInForegroundHandler() {
            @Override
            public void notificationWillShowInForeground(OSNotificationReceivedEvent notificationReceivedEvent) {
                String title = notificationReceivedEvent.getNotification().getBody();
                String body = notificationReceivedEvent.getNotification().getTitle();
                Log.i("push_data",title+" "+body);
            }
        });


    }// end onCreate()

    // SHORTCUT TO GET AN IMAGE FROM PARSE
    public static void getParseImage(final ImageView imgView, ParseObject obj, String columnName) {
        ParseFile fileObject = obj.getParseFile(columnName);
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            imgView.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }
    }


    // MARK: - SHORTCUT TO SAVE A PARSE IMAGE
    public static void saveParseImage(ImageView imgView, ParseObject parseObj, String columnName) {
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        ParseFile imageFile = new ParseFile("image.jpg", byteArray);
        parseObj.put(columnName, imageFile);
    }


    // MARK: - CUSTOM PROGRESS DIALOG -----------
    public static AlertDialog pd;

    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View dialogView = inflater.inflate(R.layout.pd, null);
        TextView messTxt = dialogView.findViewById(R.id.pdMessTxt);
        messTxt.setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }

    public static void hidePD() {
        pd.dismiss();
    }

    public static androidx.appcompat.app.AlertDialog buildProgressLoadingDialog(Context ctx) {
        androidx.appcompat.app.AlertDialog.Builder db = new androidx.appcompat.app.AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        assert inflater != null;
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        TextView messTxt = dialogView.findViewById(R.id.dp_title_tv);
        messTxt.setText(R.string.progress_dialog_loading);
        db.setView(dialogView);
        db.setCancelable(true);
        return db.create();
    }

    // MARK: - SCALE BITMAP TO MAX SIZE ---------------------------------------
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        return Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
    }


    // MARK: - GET TIME AGO SINCE DATE ------------------------------------------------------------
    public static String timeAgoSinceDate(Date date) {
        String dateString = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
            String sentStr = (df.format(date));
            Date past = df.parse(sentStr);
            Date now = new Date();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if (seconds < 60) {
                dateString = seconds + " seconds ago";
            } else if (minutes < 60) {
                dateString = minutes + " minutes ago";
            } else if (hours < 24) {
                dateString = hours + " hours ago";
            } else {
                dateString = days + " days ago";
            }
        } catch (Exception j) {
            j.printStackTrace();
        }
        return dateString;
    }


    // ROUND THOUSANDS NUMBERS ------------------------------------------
    public static String roundThousandsIntoK(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }


    // MARK: - SIMPLE ALERT ------------------------------
    public static void simpleAlert(String mess, Context activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(mess)
                .setTitle(R.string.app_name)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.logo);
        alert.create().show();
    }


    // MARK: - LOGIN ALERT ------------------------------
    public static void loginAlert(String mess, final Context activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(mess)
                .setTitle(R.string.app_name)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(activity, WizardActivity.class);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.logo);
        alert.create().show();
    }


    // Method to get URI of a stored image
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "image", null);
        return Uri.parse(path);
    }
}// @end
