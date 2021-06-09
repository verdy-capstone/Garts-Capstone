package garts.domain.com.garts.selledit.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import garts.domain.com.garts.BuildConfig;
import garts.domain.com.garts.MapActivity;
import garts.domain.com.garts.R;
import garts.domain.com.garts.common.MediaPickerDialog;
import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.filters.CategoriesActivity;
import garts.domain.com.garts.filters.SubcategoriesActivity;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.FileUtils;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.utils.PermissionsUtils;
import garts.domain.com.garts.utils.RealPathUtil;
import garts.domain.com.garts.utils.ToastUtils;
import garts.domain.com.garts.utils.UIUtils;


public class SellEditItemActivity extends BaseActivity implements LocationListener {

    public static final String EDIT_AD_OBJ_ID_EXTRA_KEY = "EDIT_AD_OBJ_ID_EXTRA_KEY";

    private static final int UPLOADING_IMAGE_SIZE = 800;

    private static final int SELECT_LOCATION_REQ_CODE = 11;

    private static final int SELECT_CATEGORY_REQ_CODE = 12;
    private static final int SELECT_SUBCATEGORY_REQ_CODE = 13;

    private static final int LOCATION_PERMISSION_REQ_CODE = 13;
    private static final int PHOTO_CAMERA_PERMISSION_REQ_CODE = 14;
    private static final int VIDEO_CAMERA_PERMISSION_REQ_CODE = 15;
    private static final int PHOTO_GALLERY_PERMISSION_REQ_CODE = 16;
    private static final int VIDEO_GALLERY_PERMISSION_REQ_CODE = 17;

    private static final int TAKE_IMAGE1_REQ_CODE = 18;
    private static final int TAKE_IMAGE2_REQ_CODE = 19;
    private static final int TAKE_IMAGE3_REQ_CODE = 20;
    private static final int PICK_IMAGE1_REQ_CODE = 21;
    private static final int PICK_IMAGE2_REQ_CODE = 22;
    private static final int PICK_IMAGE3_REQ_CODE = 23;
    private static final int VIDEO_REQ_CODE = 24;

    private String[] locationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private String[] cameraPermissions = {Manifest.permission.CAMERA};
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

    /* Views */
    private TextView screenTitleTV;
    private ImageView backIV;
    private ImageView doneIV;
    private FrameLayout addImage1FL;
    private FrameLayout addImage2FL;
    private FrameLayout addImage3FL;
    private ImageView addImage1IV;
    private ImageView addImage2IV;
    private ImageView addImage3IV;
    private TextView addImage1TV;
    private TextView addImage2TV;
    private TextView addImage3TV;
    private FrameLayout addVideoFL;
    private ImageView addVideoIV;
    private TextView addVideoTV;
    private TextView categoryTV;
    private TextView subcategoryTV;
    private EditText titleET;
    private EditText priceET;
    private RadioGroup conditionRG;
    private RadioButton newConditionRB;
    private RadioButton usedConditionRB;
    private EditText descriptionET;
    private LinearLayout locationLL;
    private TextView locationTV;
    private TextView deleteAdTV;

    private Bitmap image1Bmp;
    private Bitmap image2Bmp;
    private Bitmap image3Bmp;
    private Bitmap videoThumbnail;

    private String videoPath = null;
    private Uri videoURI = null;
    private String imageTakenPath;

    /* Variables */
    private ParseObject adObj;
    private Location currentLocation;
    private LocationManager locationManager;
    private String selectedCategory = "";
    private String selectedSubcategory = "";
    private String condition = "";

    private int lastAskedPermissionsReqCode = -1;

    private ArrayList<String> subcategories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_edit);

        initViews();
        setUpViews();

        // Get objectID for adObj
        String objectID = getIntent().getStringExtra(EDIT_AD_OBJ_ID_EXTRA_KEY);
        Log.i("log-", "OBJECT ID: " + objectID);

        // YOU'RE EDITING AN ITEM ------------------
        if (objectID != null) {
            adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
            try {
                adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

                screenTitleTV.setText(R.string.sell_edit_editing_title);
                deleteAdTV.setVisibility(View.VISIBLE);

                // Call query
                showAdDetails();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // YOU'RE SELLING A NEW ITEM ---------------
        } else {
            adObj = new ParseObject(Configs.ADS_CLASS_NAME);
            screenTitleTV.setText(R.string.sell_edit_selling_title);
            deleteAdTV.setVisibility(View.GONE);

            // Set default variables
            condition = getString(R.string.sell_edit_default_condition);
        }

        if (PermissionsUtils.hasPermissions(this, locationPermissions)) {
            loadCurrentLocation();
        } else {
            lastAskedPermissionsReqCode = LOCATION_PERMISSION_REQ_CODE;
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQ_CODE);
        }
    }

    // IMAGE/VIDEO PICKED DELEGATE ------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_CATEGORY_REQ_CODE:
                    selectedCategory = data.getStringExtra(CategoriesActivity.SELECTED_CATEGORY_EXTRA_KEY);
                    categoryTV.setText(selectedCategory);
                    querySubcategories();
                    break;
                case SELECT_SUBCATEGORY_REQ_CODE:
                    selectedSubcategory = data.getStringExtra(SubcategoriesActivity.IN_OUT_SELECTED_SUBCATEGORY_EXTRA_KEY);
                    if (TextUtils.isEmpty(selectedSubcategory)) {
                        setSubcategoryInputEnabled(false);
                        subcategoryTV.setText(R.string.sell_edit_subcategory_edit);
                    } else {
                        setSubcategoryInputEnabled(true);
                        subcategoryTV.setText(selectedSubcategory);
                    }
                    break;
                case SELECT_LOCATION_REQ_CODE:
                    Location chosenLocation = data.getParcelableExtra(MapActivity.CHOSEN_LOCATION_EXTRA_KEY);
                    if (chosenLocation != null) {
                        currentLocation = chosenLocation;
                    }
                    loadCityCountryNames();
                    break;
                case TAKE_IMAGE1_REQ_CODE:
                    image1Bmp = onCaptureImageResult(imageTakenPath);
                    showAdImage(addImage1IV, addImage1TV, image1Bmp);
                    break;
                case TAKE_IMAGE2_REQ_CODE:
                    image2Bmp = onCaptureImageResult(imageTakenPath);
                    showAdImage(addImage2IV, addImage2TV, image2Bmp);
                    break;
                case TAKE_IMAGE3_REQ_CODE:
                    image3Bmp = onCaptureImageResult(imageTakenPath);
                    showAdImage(addImage3IV, addImage3TV, image3Bmp);
                    break;
                case PICK_IMAGE1_REQ_CODE:
                    image1Bmp = onSelectFromGalleryResult(data);
                    showAdImage(addImage1IV, addImage1TV, image1Bmp);
                    break;
                case PICK_IMAGE2_REQ_CODE:
                    image2Bmp = onSelectFromGalleryResult(data);
                    showAdImage(addImage2IV, addImage2TV, image2Bmp);
                    break;
                case PICK_IMAGE3_REQ_CODE:
                    image3Bmp = onSelectFromGalleryResult(data);
                    showAdImage(addImage3IV, addImage3TV, image3Bmp);
                    break;
                case VIDEO_REQ_CODE:
                    videoURI = data.getData();
                    videoPath = getRealPathFromURI(videoURI);
                    Log.i("log-", "VIDEO PATH: " + videoPath);
                    Log.i("log-", "VIDEO URI: " + videoURI);

                    // Check video duration
                    MediaPlayer mp = MediaPlayer.create(this, videoURI);
                    int videoDuration = mp.getDuration();
                    mp.release();
                    Log.i("log-", "VIDEO DURATION: " + videoDuration);

                    if (videoPath == null) {
                        Configs.simpleAlert(getString(R.string.sell_edit_video_error), this);
                        break;
                    }
                    if (videoDuration < Configs.MAXIMUM_DURATION_VIDEO * 1100) {
//                        Video duration is within the allowed seconds
//                        Set video thumbnail
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                        videoThumbnail = thumbnail;
                        if (thumbnail == null) {
                            addVideoIV.setVisibility(View.GONE);
                            addVideoTV.setVisibility(View.VISIBLE);
                            String filename = getFilenameFromPath(videoPath);
                            addVideoTV.setText(filename + "\n" + formatDuration(videoDuration));
                        } else {
                            addVideoTV.setText("+\nVIDEO");
                            showAdImage(addVideoIV, addVideoTV, thumbnail);
                        }
                    } else {
//                        Video exceeds the maximum allowed duration
                        Configs.simpleAlert(getString(R.string.sell_edit_video_too_long_error,
                                String.valueOf(Configs.MAXIMUM_DURATION_VIDEO)), this);
//                        Reset variables and image
                        videoPath = null;
                        videoURI = null;
                        showAdImagePlaceholder(addVideoIV, addVideoTV);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case LOCATION_PERMISSION_REQ_CODE:
                    loadCurrentLocation();
                    break;
                case PHOTO_CAMERA_PERMISSION_REQ_CODE:
                    if (lastAskedPermissionsReqCode != -1) {
                        openCamera(lastAskedPermissionsReqCode);
                    }
                    break;
                case VIDEO_CAMERA_PERMISSION_REQ_CODE:
                    openVideoCamera();
                    break;
                case PHOTO_GALLERY_PERMISSION_REQ_CODE:
                    if (lastAskedPermissionsReqCode != -1) {
                        openGallery(lastAskedPermissionsReqCode);
                    }
                    break;
                case VIDEO_GALLERY_PERMISSION_REQ_CODE:
                    if (lastAskedPermissionsReqCode != -1) {
                        openGallery(lastAskedPermissionsReqCode);
                    }
                    break;
            }
            lastAskedPermissionsReqCode = -1;
        }
    }

    private void initViews() {
        screenTitleTV = findViewById(R.id.ase_screen_title_tv);
        backIV = findViewById(R.id.ase_back_iv);
        doneIV = findViewById(R.id.ase_done_iv);
        addImage1FL = findViewById(R.id.ase_add_image1_fl);
        addImage2FL = findViewById(R.id.ase_add_image2_fl);
        addImage3FL = findViewById(R.id.ase_add_image3_fl);
        addImage1IV = findViewById(R.id.ase_add_image1_iv);
        addImage2IV = findViewById(R.id.ase_add_image2_iv);
        addImage3IV = findViewById(R.id.ase_add_image3_iv);
        addImage1TV = findViewById(R.id.ase_add_image1_tv);
        addImage2TV = findViewById(R.id.ase_add_image2_tv);
        addImage3TV = findViewById(R.id.ase_add_image3_tv);
        addVideoFL = findViewById(R.id.ase_add_video_fl);
        addVideoIV = findViewById(R.id.ase_add_video_iv);
        addVideoTV = findViewById(R.id.ase_add_video_tv);
        categoryTV = findViewById(R.id.ase_category_tv);
        subcategoryTV = findViewById(R.id.ase_subcategory_tv);
        titleET = findViewById(R.id.ase_item_title_et);
        priceET = findViewById(R.id.ase_item_price_et);
        conditionRG = findViewById(R.id.ase_condition_rg);
        newConditionRB = findViewById(R.id.ase_new_condition_rb);
        usedConditionRB = findViewById(R.id.ase_used_condition_rb);
        descriptionET = findViewById(R.id.ase_item_description_et);
        locationLL = findViewById(R.id.ase_location_ll);
        locationTV = findViewById(R.id.ase_location_tv);
        deleteAdTV = findViewById(R.id.ase_delete_ad_tv);
    }

    private void setUpViews() {
        // MARK: - BACK BUTTON ------------------------------------
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        conditionRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.ase_new_condition_rb:
                        setConditionRB(newConditionRB, true);
                        setConditionRB(usedConditionRB, false);
                        condition = getString(R.string.filter_condition_new);
                        break;
                    case R.id.ase_used_condition_rb:
                        setConditionRB(newConditionRB, false);
                        setConditionRB(usedConditionRB, true);
                        condition = getString(R.string.filter_condition_used);
                        break;
                }
            }
        });

        // MARK: - UPLOAD IMAGE 1 ----------------------------------------------------------------
        addImage1FL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog(TAKE_IMAGE1_REQ_CODE, PICK_IMAGE1_REQ_CODE);
            }
        });

        // MARK: - UPLOAD IMAGE 2 ----------------------------------------------------------------
        addImage2FL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog(TAKE_IMAGE2_REQ_CODE, PICK_IMAGE2_REQ_CODE);
            }
        });

        // MARK: - UPLOAD IMAGE 3 ----------------------------------------------------------------
        addImage3FL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog(TAKE_IMAGE3_REQ_CODE, PICK_IMAGE3_REQ_CODE);
            }
        });

        // MARK: - UPLOAD VIDEO ----------------------------------------------------------------
        addVideoFL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoPickerDialog();
            }
        });

        categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categoriesIntent = new Intent(SellEditItemActivity.this, CategoriesActivity.class);
                categoriesIntent.putExtra(CategoriesActivity.SELECTED_CATEGORY_EXTRA_KEY, selectedCategory);
                categoriesIntent.putExtra(CategoriesActivity.HAS_FILTER_ROLE_EXTRA_KEY, false);
                startActivityForResult(categoriesIntent, SELECT_CATEGORY_REQ_CODE);
            }
        });

        subcategoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subcategories == null || subcategories.isEmpty()) {
                    ToastUtils.showMessage(getString(R.string.subcategories_empty_for_current_category));
                    return;
                }
                Intent subcategoriesIntent = new Intent(SellEditItemActivity.this, SubcategoriesActivity.class);
                subcategoriesIntent.putStringArrayListExtra(SubcategoriesActivity.IN_SUBCATEGORIES_EXTRA_KEY, subcategories);
                subcategoriesIntent.putExtra(SubcategoriesActivity.IN_OUT_SELECTED_SUBCATEGORY_EXTRA_KEY, selectedSubcategory);
                subcategoriesIntent.putExtra(SubcategoriesActivity.IN_INCLUDE_ALL_SUBCATEGORY_EXTRA_KEY, false);
                startActivityForResult(subcategoriesIntent, SELECT_SUBCATEGORY_REQ_CODE);
            }
        });

        // MARK: - CHOOSE LOCATION BUTTON ------------------------------------
        locationTV.setTypeface(Configs.titSemibold);
        locationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationIntent = new Intent(SellEditItemActivity.this, MapActivity.class);
                startActivityForResult(locationIntent, SELECT_LOCATION_REQ_CODE);
            }
        });

        // MARK: - SUBMIT AD BUTTON -----------------------------------------------------------------
        doneIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIUtils.hideKeyboard(SellEditItemActivity.this);

                // You haven't filled all required the fields
                if (addImage1IV.getDrawable() == null || TextUtils.isEmpty(titleET.getText()) ||
                        TextUtils.isEmpty(condition) || TextUtils.isEmpty(descriptionET.getText()) ||
                        TextUtils.isEmpty(selectedCategory) || TextUtils.isEmpty(priceET.getText())) {
                    Configs.simpleAlert(getString(R.string.sell_edit_input_validation_error), SellEditItemActivity.this);
                    // You can submit your Ad!
                } else {
                    showLoading();
                    submitAd();
                }
            }
        });

        // MARK: - DELETE AD BUTTON
        deleteAdTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItemActivity.this);
                alert.setMessage(R.string.sell_edit_delete_alert_title)
                        .setTitle(R.string.app_name)
                        .setPositiveButton(R.string.sell_edit_delete_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteAdInOtherClasses();
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_cancel_button), null)
                        .setIcon(R.drawable.logo);
                alert.create().show();

            }
        });
    }

    private void setConditionRB(RadioButton radioButton, boolean isChecked) {
        if (isChecked) {
            radioButton.setTextColor(UIUtils.getColor(R.color.whiteTextColor));
            radioButton.setBackgroundColor(UIUtils.getColor(R.color.main_color));
        } else {
            radioButton.setTextColor(UIUtils.getColor(R.color.blackTextColor));
            radioButton.setBackgroundResource(R.drawable.edit_text_shape_bg);
        }
    }

    private void submitAd() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint userGP = loadUserLocationGeoPoint();

        adObj.put(Configs.ADS_SELLER_POINTER, currentUser);
        adObj.put(Configs.ADS_TITLE, titleET.getText().toString());
        adObj.put(Configs.ADS_CATEGORY, selectedCategory);
        if (selectedSubcategory != null) {
            adObj.put(Configs.ADS_SUBCATEGORY, selectedSubcategory);
        }
        adObj.put(Configs.ADS_CONDITION, condition);
        adObj.put(Configs.ADS_DESCRIPTION, descriptionET.getText().toString());
        adObj.put(Configs.ADS_LOCATION, userGP);
        adObj.put(Configs.ADS_PRICE, Double.parseDouble(priceET.getText().toString()));
        adObj.put(Configs.ADS_CURRENCY, Configs.CURRENCY);
        List<String> empty = new ArrayList<>();
        adObj.put(Configs.ADS_LIKED_BY, empty);

        // Add keywords
        List<String> keywords = new ArrayList<>();
        String[] a = titleET.getText().toString().toLowerCase().split(" ");
        String[] b = descriptionET.getText().toString().toLowerCase().split(" ");
        keywords.addAll(Arrays.asList(a));
        keywords.addAll(Arrays.asList(b));
        keywords.add(condition.toLowerCase());
        keywords.add("@" + currentUser.getString(Configs.USER_USERNAME).toLowerCase());
        adObj.put(Configs.ADS_KEYWORDS, keywords);

        // In case this is a new Ad
        if (adObj.getObjectId() == null) {
            adObj.put(Configs.ADS_IS_REPORTED, false);
            adObj.put(Configs.ADS_LIKES, 0);
            adObj.put(Configs.ADS_COMMENTS, 0);
        }

        // Save video
        if (videoURI != null) {
            ParseFile videoFile = new ParseFile("video.mp4", convertVideoToBytes(videoURI));
            adObj.put(Configs.ADS_VIDEO, videoFile);

            // Save thumbnail
            if (videoThumbnail != null) {
                ByteArrayOutputStream st = new ByteArrayOutputStream();
                videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, st);
                byte[] byteArr = st.toByteArray();
                ParseFile thumbFile = new ParseFile("thumb.jpg", byteArr);
                adObj.put(Configs.ADS_VIDEO_THUMBNAIL, thumbFile);
            }
        }

        // Saving block
        adObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Save image1
                    if (image1Bmp != null) {
                        ImageLoadingUtils.saveImage(image1Bmp, adObj, Configs.ADS_IMAGE1);
                    }

                    // Save image2
                    if (image2Bmp != null) {
                        ImageLoadingUtils.saveImage(image2Bmp, adObj, Configs.ADS_IMAGE2);
                    }

                    // Save image3
                    if (image3Bmp != null) {
                        ImageLoadingUtils.saveImage(image3Bmp, adObj, Configs.ADS_IMAGE3);
                    }

                    // Save images now
                    adObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                hideLoading();

                                // Fire an alert
                                AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItemActivity.this);
                                alert.setMessage(R.string.sell_edit_submit_success)
                                        .setTitle(R.string.app_name)
                                        .setPositiveButton(getString(R.string.alert_ok_button), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .setIcon(R.drawable.logo);
                                alert.create().show();

                                // error
                            } else {
                                hideLoading();
                                ToastUtils.showMessage(e.getMessage());
                            }
                        }
                    });
                    // error on saving
                } else {
                    hideLoading();
                    ToastUtils.showMessage(e.getMessage());
                }
            }
        });
    }

    private ParseGeoPoint loadUserLocationGeoPoint() {
        ParseGeoPoint userGP;

        // Current location is detected
        if (currentLocation != null) {
            userGP = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            // No current location detected
        } else {
            currentLocation = new Location("provider");
            currentLocation.setLatitude(Configs.DEFAULT_LOCATION.latitude);
            currentLocation.setLongitude(Configs.DEFAULT_LOCATION.longitude);
            userGP = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        }

        Log.i("log-", "USER GEOPOINT: " + userGP.getLatitude() + " -- " + userGP.getLongitude());
        return userGP;
    }

    private void showVideoPickerDialog() {
        MediaPickerDialog dialog = new MediaPickerDialog(this);
        dialog.setCameraOptionTitle(getString(R.string.take_video_title));
        dialog.setOnOptionSelectedListener(new MediaPickerDialog.OnOptionSelectedListener() {
            @Override
            public void onOptionSelected(int index) {
                if (index == MediaPickerDialog.CAMERA_OPTION_INDEX) {
                    if (PermissionsUtils.hasPermissions(SellEditItemActivity.this, cameraPermissions)) {
                        openVideoCamera();
                    } else {
                        ActivityCompat.requestPermissions(SellEditItemActivity.this,
                                cameraPermissions, VIDEO_CAMERA_PERMISSION_REQ_CODE);
                        lastAskedPermissionsReqCode = VIDEO_CAMERA_PERMISSION_REQ_CODE;
                    }
                } else if (index == MediaPickerDialog.GALLERY_OPTION_INDEX) {
                    if (PermissionsUtils.hasPermissions(SellEditItemActivity.this, galleryPermissions)) {
                        openVideoGallery();
                    } else {
                        ActivityCompat.requestPermissions(SellEditItemActivity.this,
                                galleryPermissions, VIDEO_GALLERY_PERMISSION_REQ_CODE);
                        lastAskedPermissionsReqCode = VIDEO_GALLERY_PERMISSION_REQ_CODE;
                    }
                }
            }
        });
        dialog.show();
    }

    // MARK: - SHOW AD's DETAILS ------------------------------------------------------------------
    void showAdDetails() {
        ImageLoadingUtils.loadImage(adObj, Configs.ADS_IMAGE1, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                image1Bmp = bitmap;
                showAdImage(addImage1IV, addImage1TV, bitmap);
            }

            @Override
            public void onImageLoadingError() {
                showAdImagePlaceholder(addImage1IV, addImage1TV);
            }
        });
        ImageLoadingUtils.loadImage(adObj, Configs.ADS_IMAGE2, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                image2Bmp = bitmap;
                showAdImage(addImage2IV, addImage2TV, bitmap);
            }

            @Override
            public void onImageLoadingError() {
                showAdImagePlaceholder(addImage2IV, addImage2TV);
            }
        });
        ImageLoadingUtils.loadImage(adObj, Configs.ADS_IMAGE3, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                image3Bmp = bitmap;
                showAdImage(addImage3IV, addImage3TV, bitmap);
            }

            @Override
            public void onImageLoadingError() {
                showAdImagePlaceholder(addImage3IV, addImage3TV);
            }
        });

        // Get video thumbnail
        if (adObj.getParseFile(Configs.ADS_VIDEO) != null) {
            ImageLoadingUtils.loadImage(adObj, Configs.ADS_VIDEO_THUMBNAIL, new ImageLoadingUtils.OnImageLoadListener() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    videoThumbnail = bitmap;
                    showAdImage(addVideoIV, addVideoTV, bitmap);
                }

                @Override
                public void onImageLoadingError() {
                    showAdImagePlaceholder(addVideoIV, addVideoTV);
                }
            });
        }

        // Get category
        selectedCategory = adObj.getString(Configs.ADS_CATEGORY);
        selectedSubcategory = adObj.getString(Configs.ADS_SUBCATEGORY);
        categoryTV.setText(selectedCategory);
        if (!TextUtils.isEmpty(selectedSubcategory)) {
            subcategoryTV.setText(selectedSubcategory);
            querySubcategories();
        }

        // Get Title
        titleET.setText(adObj.getString(Configs.ADS_TITLE));

        // Get Price
        priceET.setText(String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

        // Get condition
        condition = adObj.getString(Configs.ADS_CONDITION);
        if (condition.equals(getString(R.string.filter_condition_new))) {
            conditionRG.check(R.id.ase_new_condition_rb);
        } else {
            conditionRG.check(R.id.ase_used_condition_rb);
        }

        // Get description
        descriptionET.setText(adObj.getString(Configs.ADS_DESCRIPTION));

        // Get location
        ParseGeoPoint adGeoPoint = adObj.getParseGeoPoint(Configs.ADS_LOCATION);
        if (adGeoPoint != null) {
            currentLocation = new Location("provider");
            currentLocation.setLatitude(adGeoPoint.getLatitude());
            currentLocation.setLongitude(adGeoPoint.getLongitude());
            loadCityCountryNames();
        }
    }

    private void showAdImage(ImageView imageIV, TextView buttonTV, Bitmap bitmap) {
        imageIV.setVisibility(View.VISIBLE);
        buttonTV.setVisibility(View.GONE);
        imageIV.setImageBitmap(bitmap);
    }

    private void showAdImagePlaceholder(ImageView imageIV, TextView buttonTV) {
        imageIV.setVisibility(View.GONE);
        buttonTV.setVisibility(View.VISIBLE);
    }

    // MARK: - SHOW ALERT FOR UPLOADING IMAGES -----------------------------------------------------
    void showImagePickerDialog(final int cameraReqCode, final int galleryReqCode) {
        MediaPickerDialog dialog = new MediaPickerDialog(this);
        dialog.setCameraOptionTitle(getString(R.string.take_picture_title));
        dialog.setOnOptionSelectedListener(new MediaPickerDialog.OnOptionSelectedListener() {
            @Override
            public void onOptionSelected(int index) {
                if (index == MediaPickerDialog.CAMERA_OPTION_INDEX) {
                    if (PermissionsUtils.hasPermissions(SellEditItemActivity.this, cameraPermissions)) {
                        openCamera(cameraReqCode);
                    } else {
                        lastAskedPermissionsReqCode = cameraReqCode;
                        ActivityCompat.requestPermissions(SellEditItemActivity.this,
                                cameraPermissions, PHOTO_CAMERA_PERMISSION_REQ_CODE);
                    }
                } else if (index == MediaPickerDialog.GALLERY_OPTION_INDEX) {
                    if (PermissionsUtils.hasPermissions(SellEditItemActivity.this, cameraPermissions)) {
                        openGallery(galleryReqCode);
                    } else {
                        lastAskedPermissionsReqCode = galleryReqCode;
                        ActivityCompat.requestPermissions(SellEditItemActivity.this,
                                galleryPermissions, PHOTO_GALLERY_PERMISSION_REQ_CODE);
                    }
                }
            }
        });
        dialog.show();
    }

    // IMAGE/VIDEO HANDLING METHODS ------------------------------------------------------------------------

    // OPEN CAMERA
    public void openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageTakenFile = createEmptyFile();
        imageTakenPath = imageTakenFile.getAbsolutePath();

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +
                ".provider", imageTakenFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, requestCode);
    }

    private File createEmptyFile() {
        File storageDir = getCacheDir();
        File myDir = null;
        try {
            myDir = File.createTempFile("image", ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("log-", "Image file creation failure: " + e.getMessage());
            e.printStackTrace();
        }
        return myDir;
    }

    // OPEN GALLERY
    public void openGallery(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image_chooser_title)), requestCode);
    }

    // OPEN VIDEO CAMERA
    public void openVideoCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Configs.MAXIMUM_DURATION_VIDEO);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, VIDEO_REQ_CODE);
    }

    // OPEN VIDEO GALLERY
    public void openVideoGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Configs.MAXIMUM_DURATION_VIDEO);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video_chooser_title)), VIDEO_REQ_CODE);
    }

    // GET VIDEO PATH AS A STRING -------------------------------------
    public String getRealPathFromURI(Uri contentUri) {
        return RealPathUtil.getRealPathFromUri(this, contentUri);
    }

    public String getFilenameFromPath(String path) {
        if (path == null) {
            return "";
        }
        int lastIndexOfSlash = path.lastIndexOf("/");
        if (lastIndexOfSlash == -1 || lastIndexOfSlash > path.length() - 1) {
            return "";
        }

        return path.substring(lastIndexOfSlash + 1);
    }

    public static String formatDuration(int durationMillis) {
        long seconds = durationMillis / 1000;
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    // CONVERT VIDEO TO BYTES -----------------------------------
    private byte[] convertVideoToBytes(Uri uri) {
        byte[] videoBytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(uri)));

            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoBytes;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!PermissionsUtils.hasPermissions(this, locationPermissions)) {
            return;
        }

        //remove location callback:
        locationManager.removeUpdates(this);
        currentLocation = location;

        if (currentLocation != null) {
            Log.i("log-", "CURRENT LOCATION FOUND! " + currentLocation.getLatitude());
            // NO GPS location found!
        } else {
            Configs.simpleAlert(getString(R.string.sell_edit_get_location_failure), this);
            // Set the default currentLocation
            currentLocation = new Location("dummyprovider");
            currentLocation.setLatitude(Configs.DEFAULT_LOCATION.latitude);
            currentLocation.setLongitude(Configs.DEFAULT_LOCATION.longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private Bitmap onCaptureImageResult(String photoPath) {
        Bitmap bitmap = FileUtils.getPictureFromPath(photoPath, UPLOADING_IMAGE_SIZE);

        if (bitmap == null) {
            ToastUtils.showMessage(getString(R.string.sell_edit_picture_failure));
            return null;
        }
        return bitmap;
    }

    private Bitmap onSelectFromGalleryResult(Intent data) {
        Bitmap bitmap = FileUtils.decodeIntentData(data.getData(), UPLOADING_IMAGE_SIZE);

        if (bitmap == null) {
            ToastUtils.showMessage(getString(R.string.sell_edit_picture_failure));
            return null;
        }
        return bitmap;
    }

    // MARK: - GET CURRENT LOCATION ------------------------------------------------------
    @SuppressLint("MissingPermission")
    protected void loadCurrentLocation() {
        try{
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        String provider = locationManager.getBestProvider(criteria, true);

        if (!PermissionsUtils.hasPermissions(this, locationPermissions)) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {
            Log.i("log-", "CURRENT LOCATION FOUND! " + currentLocation.getLatitude());
        } else {
            // Try to get current Location one more time
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
        }catch (NullPointerException nullExcep){
            nullExcep.printStackTrace();
        } catch (IllegalArgumentException illExcep){
            illExcep.printStackTrace();
        } catch (IllegalStateException stateExcep){
            stateExcep.printStackTrace();
        } catch (Exception excep){
            excep.printStackTrace();
        }
    }

    private void loadCityCountryNames() {
        try {
            Geocoder geocoder = new Geocoder(SellEditItemActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (Geocoder.isPresent() && addresses != null && !addresses.isEmpty()) {
                Address returnAddress = addresses.get(0);
                String city = returnAddress.getLocality();
                String country = returnAddress.getCountryName();

                if (city == null) {
                    city = "";
                }
                // Show City/Country
                locationTV.setText(city + ", " + country);
            } else {
                Toast.makeText(getApplicationContext(), R.string.sell_edit_location_failure, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            ToastUtils.showMessage(e.getMessage());
        }
    }

    // MARK: - DELETE AD IN OTHER CLASSES ------------------------------------------------------
    private void deleteAdInOtherClasses() {
        // Delete adPointer in Chats class
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CHATS_CLASS_NAME);
        query.whereEqualTo(Configs.CHATS_AD_POINTER, adObj);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
                }
            }
        });

        // Delete adPointer in Comments class
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
        query2.whereEqualTo(Configs.COMMENTS_AD_POINTER, adObj);
        query2.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
                }
            }
        });

        // Delete adPointer in InBox class
        ParseQuery<ParseObject> query3 = ParseQuery.getQuery(Configs.INBOX_CLASS_NAME);
        query3.whereEqualTo(Configs.INBOX_AD_POINTER, adObj);
        query3.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
                }
            }
        });

        // Delete adPointer in Likes class
        ParseQuery<ParseObject> query4 = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
        query4.whereEqualTo(Configs.LIKES_AD_LIKED, adObj);
        query4.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        ParseObject obj = objects.get(i);
                        obj.deleteInBackground();
                    }
                }
            }
        });

        // Lastly, delete the Ad
        adObj.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SellEditItemActivity.this);
                    alert.setMessage(R.string.sell_edit_item_delete_success)
                            .setTitle(R.string.app_name)
                            .setPositiveButton(getString(R.string.alert_ok_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .setIcon(R.drawable.logo);
                    alert.create().show();
                } else {
                    Configs.simpleAlert(e.getMessage(), SellEditItemActivity.this);
                }
            }
        });
    }

    private void querySubcategories() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.SUBCATEGORIES_CLASS_NAME);
        query.whereEqualTo(Configs.SUBCATEGORIES_CATEGORY, selectedCategory);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects != null && !objects.isEmpty()) {
                        subcategories = new ArrayList<>();
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject cObj = objects.get(i);
                            subcategories.add(cObj.getString(Configs.SUBCATEGORIES_SUBCATEGORY));
                        }

                        setSubcategoryInputEnabled(true);
                    } else {
                        subcategories = null;
                        setSubcategoryInputEnabled(false);
                    }
                } else {
                    ToastUtils.showMessage(e.getMessage());
                }
            }
        });
    }

    private void setSubcategoryInputEnabled(boolean enabled) {
        subcategoryTV.setClickable(enabled);

        if (enabled) {
            subcategoryTV.setBackgroundResource(R.drawable.edit_text_shape_bg);
            subcategoryTV.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else {
            selectedSubcategory = null;
            subcategoryTV.setText(R.string.sell_edit_subcategory_edit);
            subcategoryTV.setBackgroundResource(R.drawable.edit_text_disabled_shape_bg);
            subcategoryTV.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }
    }
}
