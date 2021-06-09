package garts.domain.com.garts;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import java.io.File;

import garts.domain.com.garts.common.MediaPickerDialog;
import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.landing.TermsOfUse;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.FileUtils;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.utils.PermissionsUtils;
import garts.domain.com.garts.utils.ToastUtils;
import garts.domain.com.garts.utils.UIUtils;

public class EditProfileActivity extends BaseActivity {

    private static final int CAMERA_PERMISSION_REQ_CODE = 11;
    private static final int GALLERY_PERMISSION_REQ_CODE = 12;

    private static final int CAMERA_REQ_CODE = 0;
    private static final int GALLERY_REQ_CODE = 1;

    private static final int IMAGE_SIZE = 800;

    private EditText usernameET;
    private EditText fullnameET;
    private EditText websiteET;
    private EditText aboutET;
    private ImageView avatarIV;
    private TextView changeAvatarTV;
    private TextView resetPasswordTV;
    private TextView termsTV;
    private ImageView backIV;
    private ImageView saveIV;

    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setUpViews();

        // Call query
        showMyDetails();
    }

    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQ_CODE:
                    onSelectFromGalleryResult(data);
                    break;
                case CAMERA_REQ_CODE:
                    onCaptureImageResult(currentPhotoPath);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CAMERA_PERMISSION_REQ_CODE:
                    openCamera();
                    break;
                case GALLERY_PERMISSION_REQ_CODE:
                    openGallery();
                    break;
            }
        }
    }

    private void initViews() {
        usernameET = findViewById(R.id.aep_username_et);
        fullnameET = findViewById(R.id.aep_fullname_et);
        websiteET = findViewById(R.id.aep_website_et);
        aboutET = findViewById(R.id.aep_about_et);
        avatarIV = findViewById(R.id.aep_avatar_iv);
        changeAvatarTV = findViewById(R.id.aep_change_avatar_tv);
        resetPasswordTV = findViewById(R.id.aep_reset_password_tv);
        termsTV = findViewById(R.id.aep_terms_tv);
        backIV = findViewById(R.id.aep_back_iv);
        saveIV = findViewById(R.id.aep_save_iv);
    }

    private void setUpViews() {
        usernameET.setTypeface(Configs.titRegular);
        fullnameET.setTypeface(Configs.titRegular);
        websiteET.setTypeface(Configs.titRegular);
        aboutET.setTypeface(Configs.titRegular);
        resetPasswordTV.setTypeface(Configs.titSemibold);
        termsTV.setTypeface(Configs.titSemibold);

        // MARK: - CHANGE AVATAR IMAGE ----------------------------------------
        View.OnClickListener changeAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeAvatarDialog();
            }
        };
        avatarIV.setOnClickListener(changeAvatarClickListener);
        changeAvatarTV.setOnClickListener(changeAvatarClickListener);

        // MARK: - SAVE PROFILE BUTTON ------------------------------------
        saveIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser currUser = ParseUser.getCurrentUser();

                if (usernameET.getText().toString().matches("") || fullnameET.getText().toString().matches("")) {
                    Configs.simpleAlert(getString(R.string.edit_profile_input_validation_error),
                            EditProfileActivity.this);

                } else {
                    showLoading();
                    UIUtils.hideKeyboard(EditProfileActivity.this);

                    currUser.put(Configs.USER_USERNAME, usernameET.getText().toString());
                    currUser.put(Configs.USER_FULLNAME, fullnameET.getText().toString());
                    if (!websiteET.getText().toString().matches("")) {
                        currUser.put(Configs.USER_WEBSITE, websiteET.getText().toString());
                    }
                    if (!aboutET.getText().toString().matches("")) {
                        currUser.put(Configs.USER_ABOUT_ME, aboutET.getText().toString());
                    }

                    // Save Avatar
                    Configs.saveParseImage(avatarIV, currUser, Configs.USER_AVATAR);

                    // Saving block
                    currUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                hideLoading();
                                ToastUtils.showMessage(getString(R.string.edit_profile_submit_success));
                            } else {
                                showLoading();
                                ToastUtils.showMessage(e.getMessage());
                            }
                        }
                    });
                }
            }
        });

        // MARK: - RESET PASSWORD BUTTON ------------------------------------
        resetPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditProfileActivity.this)
                        .setTitle(R.string.app_name)
                        .setIcon(R.drawable.logo)
                        .setMessage(R.string.edit_profile_reset_password_description);

                // Add an EditTxt
                final EditText editTxt = new EditText(EditProfileActivity.this);
                editTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                alert.setView(editTxt)
                        .setNegativeButton(getString(R.string.alert_cancel_button), null)
                        .setPositiveButton(R.string.edit_profile_reset_password_title, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Reset password
                                ParseUser.requestPasswordResetInBackground(editTxt.getText().toString(), new RequestPasswordResetCallback() {
                                    public void done(ParseException error) {
                                        if (error == null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                            builder.setMessage(R.string.edit_profile_reset_password_success)
                                                    .setTitle(R.string.app_name)
                                                    .setPositiveButton(getString(R.string.alert_ok_button), null);
                                            AlertDialog dialog = builder.create();
                                            dialog.setIcon(R.drawable.logo);
                                            dialog.show();
                                        } else {
                                            Configs.simpleAlert(error.getMessage(), EditProfileActivity.this);
                                        }
                                    }
                                });
                            }
                        });
                alert.show();
            }
        });

        // MARK: - TERMS OF SERVICE BUTTON ----------------------------------------------------------
        termsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, TermsOfUse.class));
            }
        });

        // MARK: - BACK BUTTON ------------------------------------
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showChangeAvatarDialog() {
        MediaPickerDialog dialog = new MediaPickerDialog(this);
        dialog.setOnOptionSelectedListener(new MediaPickerDialog.OnOptionSelectedListener() {
            @Override
            public void onOptionSelected(int index) {
                if (index == MediaPickerDialog.CAMERA_OPTION_INDEX) {
                    String[] permissions = {Manifest.permission.CAMERA};
                    if (PermissionsUtils.hasPermissions(EditProfileActivity.this, permissions)) {
                        openCamera();
                    } else {
                        ActivityCompat.requestPermissions(EditProfileActivity.this, permissions,
                                CAMERA_PERMISSION_REQ_CODE);
                    }
                } else if (index == MediaPickerDialog.GALLERY_OPTION_INDEX) {
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    if (PermissionsUtils.hasPermissions(EditProfileActivity.this, permissions)) {
                        openGallery();
                    } else {
                        ActivityCompat.requestPermissions(EditProfileActivity.this, permissions,
                                GALLERY_PERMISSION_REQ_CODE);
                    }
                }
            }
        });
        dialog.show();
    }

    // MARK: - SHOW MY DETAILS ----------------------------------------------------------------
    void showMyDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        usernameET.setText(currUser.getString(Configs.USER_USERNAME));
        fullnameET.setText(currUser.getString(Configs.USER_FULLNAME));
        if (currUser.getString(Configs.USER_WEBSITE) != null) {
            websiteET.setText(currUser.getString(Configs.USER_WEBSITE));
        }
        if (currUser.getString(Configs.USER_ABOUT_ME) != null) {
            aboutET.setText(currUser.getString(Configs.USER_ABOUT_ME));
        }

        // Get Avatar
        ImageLoadingUtils.loadImage(currUser, Configs.USER_AVATAR, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                avatarIV.setImageBitmap(bitmap);
            }

            @Override
            public void onImageLoadingError() {
                avatarIV.setImageResource(R.drawable.logo);
            }
        });
    }

    // OPEN CAMERA_REQ_CODE
    public void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = FileUtils.createEmptyFile("image.jpg", Configs.IMAGE_FORMAT);
        currentPhotoPath = photoFile.getAbsolutePath();

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +
                ".provider", photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(takePictureIntent, CAMERA_REQ_CODE);
    }

    // OPEN GALLERY_REQ_CODE
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image_chooser_title)), GALLERY_REQ_CODE);
    }

    private void onCaptureImageResult(String photoPath) {
        Bitmap bitmap = FileUtils.getPictureFromPath(photoPath, IMAGE_SIZE);

        if (bitmap == null) {
            ToastUtils.showMessage(getString(R.string.failed_to_retrieve_photo_error));
            return;
        }
        displayAvatar(photoPath, bitmap);
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bitmap = FileUtils.decodeIntentData(data.getData(), IMAGE_SIZE);

        if (bitmap == null) {
            ToastUtils.showMessage(getString(R.string.failed_to_retrieve_photo_error));
            return;
        }
        displayAvatar(data.getDataString(), bitmap);
    }

    private void displayAvatar(String path, Bitmap bitmap) {
        bitmap = FileUtils.processExif(path, bitmap);
        avatarIV.setImageBitmap(bitmap);
    }
}
