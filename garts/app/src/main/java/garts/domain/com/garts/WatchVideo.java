package garts.domain.com.garts;

import android.content.pm.ActivityInfo;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;

public class WatchVideo extends AppCompatActivity {

    /* Variables */
    ParseObject adObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_video);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        adObj = ParseObject.createWithoutData(Configs.ADS_CLASS_NAME, objectID);
        try {
            adObj.fetchIfNeeded().getParseObject(Configs.ADS_CLASS_NAME);

            // Get Video URL
            ParseFile videoFile = adObj.getParseFile(Configs.ADS_VIDEO);
            String videoURL = videoFile.getUrl();
            Log.i("log-", "VIDEO URL: " + videoURL);

            WebView webView = findViewById(R.id.wvWebView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.getSettings().setSupportMultipleWindows(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    setProgressBarIndeterminateVisibility(false);
                    super.onPageFinished(view, url);
                }
            });
            webView.loadUrl(videoURL);
            webView.setVisibility(View.VISIBLE);


        } catch (ParseException e) {
            e.printStackTrace();
        }

        // MARK: - CANCEL BUTTON ------------------------------------
        Button cancButt = findViewById(R.id.wvBackButt);
        cancButt.setOnClickListener(new View.OnClickListener() {
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
}//end
