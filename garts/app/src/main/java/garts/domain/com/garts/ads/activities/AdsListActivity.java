package garts.domain.com.garts.ads.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import garts.domain.com.garts.common.activities.BaseActivity;
import garts.domain.com.garts.filters.CategoriesActivity;
import garts.domain.com.garts.filters.models.SortBy;
import garts.domain.com.garts.home.adapters.AdsListAdapter;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.Constants;
import garts.domain.com.garts.utils.PermissionsUtils;
import garts.domain.com.garts.utils.ToastUtils;
import garts.domain.com.garts.utils.UIUtils;
import garts.domain.com.garts.Chats;
import garts.domain.com.garts.DistanceMapActivity;
import garts.domain.com.garts.R;
import garts.domain.com.garts.SortByActivity;

public class AdsListActivity extends BaseActivity implements LocationListener {

    public static final String CATEGORY_NAME_KEY = "CATEGORY_NAME_KEY";
    public static final String SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY";

    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    private static final int CATEGORY_REQ_CODE = 2;
    private static final int SORT_BY_REQ_CODE = 3;
    private static final int SET_LOCATION_REQ_CODE = 4;

    private String[] locationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private EditText searchTxt;
    private TextView categoryTV, sortByTV, cityCountryTV, subcategoryTV;
    private RelativeLayout categoryButtonRL, sortByButtonRL, cityCountryButtonRL;
    private TextView distanceTxt, noSearchTxt;
    private RelativeLayout noResultsLayout;
    private ImageView backBtn;
    private AdView adView;
    private ImageView chatButt;
    private RecyclerView adsRV;

    private String searchString;
    private String selectedCategory;
    private String selectedSubcategory;
    private SortBy sortBy;

    private Location currentLocation;
    private LocationManager locationManager;
    private float distanceInMiles = Configs.distanceInMiles;

    // Pagination
    private AdsListAdapter adsListAdapter;
    private ParseQuery<ParseObject> lastAdsQuery;
    private int querySkip = 0;
    private boolean isNextPageLoading;
    private boolean allAdsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            searchString = extras.getString(SEARCH_QUERY_KEY);
            selectedCategory = extras.getString(CATEGORY_NAME_KEY);
        }

        sortBy = SortBy.RECENT;

        Log.i("log-", "SEARCH TEXT STRING: " + searchString +
                "\nCATEGORY: " + selectedCategory + "\nSUBCATEGORY" + selectedSubcategory);

        initViews();
        setUpViews();

        if (PermissionsUtils.hasPermissions(this, locationPermissions)) {
            loadAdsFromChosenLocation();
        } else {
            queryAds();
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CATEGORY_REQ_CODE:
                    selectedCategory = data.getStringExtra(CategoriesActivity.SELECTED_CATEGORY_EXTRA_KEY);
                    selectedSubcategory = data.getStringExtra(CategoriesActivity.SELECTED_SUBCATEGORY_EXTRA_KEY);
                    categoryTV.setText(selectedCategory);
                    subcategoryTV.setText(selectedSubcategory != null ? selectedSubcategory : getString(R.string.categories_all_title));
                    queryAds();
                    break;
                case SORT_BY_REQ_CODE:
                    String sortByValue = data.getStringExtra(SortByActivity.SELECTED_SORT_BY_EXTRA_KEY);
                    sortBy = SortBy.getForValue(sortByValue);
                    if (sortBy != null) {
                        sortByTV.setText(sortBy.getValue());
                    }
                    queryAds();
                    break;
                case SET_LOCATION_REQ_CODE:
                    Location chosenLocation = data.getParcelableExtra(DistanceMapActivity.LOCATION_EXTRA_KEY);
                    if (chosenLocation != null) {
                        currentLocation = chosenLocation;
                    }
                    distanceInMiles = data.getFloatExtra(DistanceMapActivity.DISTANCE_EXTRA_KEY, Configs.distanceInMiles);
                    loadAdsFromChosenLocation();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAdsFromChosenLocation();
            }
        }
    }

    private void initViews() {
        searchTxt = findViewById(R.id.alSearchTxt);
        distanceTxt = findViewById(R.id.alDistanceTxt);
        categoryTV = findViewById(R.id.alCategoryTV);
        subcategoryTV = findViewById(R.id.alSubcategoryTV);
        sortByTV = findViewById(R.id.alSortByTV);
        cityCountryTV = findViewById(R.id.alCityCountryTV);
        noSearchTxt = findViewById(R.id.alNoSearchTxt);
        noResultsLayout = findViewById(R.id.alNoResultsLayout);
        backBtn = findViewById(R.id.alBackButt);
        adView = findViewById(R.id.admobBanner);
        chatButt = findViewById(R.id.alChatButt);
        adsRV = findViewById(R.id.aal_ads_rv);
        categoryButtonRL = findViewById(R.id.aal_category_rl);
        sortByButtonRL = findViewById(R.id.aal_sort_rl);
        cityCountryButtonRL = findViewById(R.id.aal_location_rl);
    }

    private void setUpViews() {
        searchTxt.setTypeface(Configs.titRegular);
        distanceTxt.setTypeface(Configs.titRegular);
        categoryTV.setTypeface(Configs.titSemibold);
        sortByTV.setTypeface(Configs.titSemibold);
        cityCountryTV.setTypeface(Configs.titSemibold);
        noSearchTxt.setTypeface(Configs.titSemibold);
        noResultsLayout.setVisibility(View.GONE);

        // Set search variables for the query
        if (searchString != null) {
            searchTxt.setText(searchString);
        } else {
            searchTxt.setText(selectedCategory);
        }
        categoryTV.setText(selectedCategory);
        subcategoryTV.setText(selectedSubcategory);

        // Set sort By text
        sortByTV.setText(sortBy.getValue());

        // MARK: - SEARCH ADS BY KEYWORDS --------------------------------------------------------
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (!searchTxt.getText().toString().matches("")) {
                        selectedCategory = Constants.BrowseCategories.DEFAULT_CATEGORY_NAME;
                        selectedSubcategory = Constants.BrowseSubcategories.DEFAULT_SUBCATEGORY_NAME;
                        categoryTV.setText(selectedCategory);
                        subcategoryTV.setText(selectedSubcategory);
                        searchString = searchTxt.getText().toString();
                        UIUtils.hideKeyboard(searchTxt);

                        // Call query
                        queryAds();

                        return true;
                    }

                    // No text -> No search
                } else {
                    ToastUtils.showMessage(getString(R.string.ads_list_search_validation_error));
                }

                return false;
            }
        });

        // EditText TextWatcher delegate
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int closeDrawable;
                if (s.length() == 0) {
                    closeDrawable = 0;
                } else {
                    closeDrawable = R.drawable.close_white_ic;
                }
                searchTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, closeDrawable, 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        final Drawable imgClearButton = ContextCompat.getDrawable(this, R.drawable.close_white_ic);
        searchTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (searchTxt.getText().length() > 0) {
                    if (event.getX() > searchTxt.getWidth() - searchTxt.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
                        searchTxt.setText("");
                        searchString = null;
                        selectedCategory = Constants.BrowseCategories.DEFAULT_CATEGORY_NAME;
                        selectedSubcategory = Constants.BrowseSubcategories.DEFAULT_SUBCATEGORY_NAME;
                        categoryTV.setText(selectedCategory);
                        subcategoryTV.setText(selectedSubcategory);

                        queryAds();
                        return true;
                    }
                }
                return false;
            }
        });

        // MARK: - CHATS BUTTON ------------------------------------
        chatButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(AdsListActivity.this, Chats.class));
                } else {
                    Configs.loginAlert(getString(R.string.browse_chat_error), AdsListActivity.this);
                }
            }
        });

        // MARK: - CITY/COUNTRY BUTTON ------------------------------------
        cityCountryButtonRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("log-", "CURR LOCATION: " + currentLocation);

                // if (!distanceTxt.getText().toString().matches("loading...")) {
                if (currentLocation != null) {
                    Double lat = currentLocation.getLatitude();
                    Double lng = currentLocation.getLongitude();
                    Log.i("log-", "LATITUDE: " + lat + "\nLONGITUDE: " + lng);
                    Intent i = new Intent(AdsListActivity.this, DistanceMapActivity.class);
                    Bundle extras = new Bundle();
                    extras.putDouble("latitude", lat);
                    extras.putDouble("longitude", lng);
                    i.putExtras(extras);
                    startActivityForResult(i, SET_LOCATION_REQ_CODE);

                } else {
                    // Set default Location
                    Intent i = new Intent(AdsListActivity.this, DistanceMapActivity.class);
                    Bundle extras = new Bundle();
                    extras.putDouble("latitude", Configs.DEFAULT_LOCATION.latitude);
                    extras.putDouble("longitude", Configs.DEFAULT_LOCATION.longitude);
                    i.putExtras(extras);
                    startActivityForResult(i, SET_LOCATION_REQ_CODE);
                }
                // }
            }
        });

        // MARK: - CHOOSE CATEGORY BUTTON ------------------------------------
        categoryButtonRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent categoriesIntent = new Intent(AdsListActivity.this, CategoriesActivity.class);
                categoriesIntent.putExtra(CategoriesActivity.SELECTED_CATEGORY_EXTRA_KEY, selectedCategory);
                startActivityForResult(categoriesIntent, CATEGORY_REQ_CODE);
            }
        });

        // MARK: - SORT BY BUTTON ------------------------------------
        sortByButtonRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sortIntent = new Intent(AdsListActivity.this, SortByActivity.class);
                if (sortBy != null) {
                    sortIntent.putExtra(SortByActivity.SELECTED_SORT_BY_EXTRA_KEY, sortBy.getValue());
                }
                startActivityForResult(sortIntent, SORT_BY_REQ_CODE);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Init AdMob banner
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadAdsFromChosenLocation() {
        // Get ads from a chosen location
        if (currentLocation != null) {
            getCityCountryNames();
        } else {
            getCurrentLocation();
        }
    }

    // MARK: - GET CURRENT LOCATION ------------------------------------------------------
    @SuppressLint("WrongConstant")
    protected void getCurrentLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {
            getCityCountryNames();

            // Try to find your current Location one more time
        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (PermissionsUtils.hasPermissions(this, locationPermissions)) {
            return;
        }

        locationManager.removeUpdates(this);
        currentLocation = location;

        if (currentLocation != null) {
            getCityCountryNames();
            // NO GPS location found!
        } else {
            Configs.simpleAlert(getString(R.string.get_location_failure), AdsListActivity.this);

            // Set New York City as default currentLocation
            currentLocation = new Location("provider");
            currentLocation.setLatitude(Configs.DEFAULT_LOCATION.latitude);
            currentLocation.setLongitude(Configs.DEFAULT_LOCATION.longitude);

            // Set distance and city labels
            String distFormatted = String.format("%.0f", distanceInMiles);
            distanceTxt.setText(getString(R.string.ads_list_distance_formatted, distFormatted));
            cityCountryTV.setText(getString(R.string.not_available_text_placeholder));

            // Call query
            queryAds();
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

    // MARK: - GET CITY AND COUNTRY NAMES | CALL QUERY ADS -----------------------------------
    private void getCityCountryNames() {
        try {
            Geocoder geocoder = new Geocoder(AdsListActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (Geocoder.isPresent()) {
                Address returnAddress = addresses.get(0);
                String city = returnAddress.getLocality();
                String country = returnAddress.getCountryName();

                if (city == null) {
                    city = "";
                }
                // Show City/Country
                cityCountryTV.setText(city + ", " + country);

                // Set distance
                String distFormatted = String.format("%.0f", distanceInMiles);
                distanceTxt.setText(getString(R.string.ads_list_distance_formatted, distFormatted));

                // Call query
                queryAds();

            } else {
                Toast.makeText(getApplicationContext(), R.string.geocoder_not_present_error, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            ToastUtils.showMessage(e.getMessage());
        }
    }

    // MARK: - QUERY ADS ------------------------------------------------------------------
    private void queryAds() {
        noResultsLayout.setVisibility(View.GONE);
        showLoading();

        // Launch query
        lastAdsQuery = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
        // Reset to first page
        querySkip = 0;
        allAdsLoaded = false;

        // query by search text
        if (searchString != null) {
            // Get keywords
            String[] one = searchString.toLowerCase().split(" ");
            List<String> keywords = new ArrayList<>(Arrays.asList(one));
            Log.d("KEYWORDS", "\n" + keywords + "\n");

            lastAdsQuery.whereContainedIn(Configs.ADS_KEYWORDS, keywords);
        }

        // query by Category
        if (!selectedCategory.matches(Constants.BrowseCategories.DEFAULT_CATEGORY_NAME)) {
            lastAdsQuery.whereEqualTo(Configs.ADS_CATEGORY, selectedCategory);
        }

        if (!TextUtils.isEmpty(selectedSubcategory) && !selectedSubcategory.matches(Constants.BrowseSubcategories.DEFAULT_SUBCATEGORY_NAME)) {
            lastAdsQuery.whereEqualTo(Configs.ADS_SUBCATEGORY, selectedSubcategory);
        }

        // query nearby
        if (currentLocation != null) {
            ParseGeoPoint gp = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            lastAdsQuery.whereWithinMiles(Configs.ADS_LOCATION, gp, distanceInMiles);
        }

        // query sortBy
        switch (sortBy) {
            case RECENT:
                lastAdsQuery.orderByDescending(Configs.ADS_CREATED_AT);
                break;
            case LOWEST_PRICE:
                lastAdsQuery.orderByAscending(Configs.ADS_PRICE);
                break;
            case HIGHEST_PRICE:
                lastAdsQuery.orderByDescending(Configs.ADS_PRICE);
                break;
            case NEW:
                lastAdsQuery.whereEqualTo(Configs.ADS_CONDITION, getString(R.string.filter_condition_new));
                break;
            case USED:
                lastAdsQuery.whereEqualTo(Configs.ADS_CONDITION, getString(R.string.filter_condition_used));
                break;
            case MOST_LIKED:
                lastAdsQuery.orderByDescending(Configs.ADS_LIKES);
                break;
            default:
                break;
        }

        lastAdsQuery.whereEqualTo(Configs.ADS_IS_REPORTED, false);
        lastAdsQuery.setLimit(Configs.DEFAULT_PAGE_SIZE);
        lastAdsQuery.setSkip(querySkip);
        lastAdsQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    hideLoading();
                    setUpAdList(objects);
                } else {
                    hideLoading();
                    ToastUtils.showMessage(error.getMessage());
                }
            }
        });
    }

    private void loadMoreAds() {
        if (lastAdsQuery == null) {
            Log.d("log-", "Last query is null");
            return;
        }

        if (isNextPageLoading) {
            Log.d("log-", "Next page is loading");
            return;
        }

        if (allAdsLoaded) {
            Log.d("log-", "All ads are already loaded");
            return;
        }

        isNextPageLoading = true;
        querySkip += Configs.DEFAULT_PAGE_SIZE;
        lastAdsQuery.setLimit(Configs.DEFAULT_PAGE_SIZE);
        lastAdsQuery.setSkip(querySkip);
        lastAdsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                isNextPageLoading = false;

                if (e == null) {
                    if (objects.isEmpty()) {
                        allAdsLoaded = true;
                        return;
                    }

                    adsListAdapter.addMoreAds(objects);
                } else {
                    Log.d("log-", "Error loading more ads: " + e.getMessage());
                }
            }
        });
    }

    private void setUpAdList(List<ParseObject> adList) {
        // Show/hide noResult view
        if (adList.isEmpty()) {
            noResultsLayout.setVisibility(View.VISIBLE);
            if (adsListAdapter != null) {
                adsListAdapter.clearAds();
            }
        } else {
            noResultsLayout.setVisibility(View.GONE);

            final GridLayoutManager layoutManager = new GridLayoutManager(AdsListActivity.this, 2);
            adsListAdapter = new AdsListAdapter(adList, new AdsListAdapter.OnAdClickListener() {
                @Override
                public void onAdClicked(ParseObject adObj) {
                    Intent adDetailsIntent = new Intent(AdsListActivity.this, AdDetailsActivity.class);
                    adDetailsIntent.putExtra(AdDetailsActivity.AD_OBJ_ID_KEY, adObj.getObjectId());
                    startActivity(adDetailsIntent);
                }
            });
            adsRV.setLayoutManager(layoutManager);
            adsRV.setAdapter(adsListAdapter);
            adsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    if (lastVisibleItem != -1 &&
                            lastVisibleItem + Configs.DEFAULT_PAGE_THRESHOLD >= totalItemCount) {
                        Log.d("log-", "Total item count: " + totalItemCount +
                                " last visible item pos: " + lastVisibleItem + " Need to load more");
                        loadMoreAds();
                    }
                }
            });
        }
    }
}
