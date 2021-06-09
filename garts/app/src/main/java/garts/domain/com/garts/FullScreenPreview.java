package garts.domain.com.garts;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import uk.co.senab.photoview.PhotoViewAttacher;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;

public class FullScreenPreview extends AppCompatActivity {

    /* Views */
    ImageView prevImage;

    /* Variables */
    ParseObject adObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_preview);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Init views
        prevImage = findViewById(R.id.fspImage);

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        String imageName = extras.getString("imageName");
        adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
        try {
            adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

            ParseFile fileObject = null;

            switch (imageName) {
                case "image1":
                    fileObject = adObj.getParseFile(Configs.ADS_IMAGE1);
                    break;
                case "image2":
                    fileObject = adObj.getParseFile(Configs.ADS_IMAGE2);
                    break;
                case "image3":
                    fileObject = adObj.getParseFile(Configs.ADS_IMAGE3);
                    break;
            }

            if (fileObject != null) {
                fileObject.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException error) {
                        if (error == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (bmp != null) {
                                prevImage.setImageBitmap(bmp);

                                // Attach image to PhotoViewAttacher to zoom image with pinch gesture and tap to close
                                PhotoViewAttacher pAttacher;
                                pAttacher = new PhotoViewAttacher(prevImage);
                                pAttacher.update();

                                pAttacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                                    @Override
                                    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                                        finish();
                                        return false;
                                    }

                                    @Override
                                    public boolean onDoubleTap(MotionEvent motionEvent) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                                        return false;
                                    }
                                });
                            }
                        }
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }// end onCreate()
}//@end
