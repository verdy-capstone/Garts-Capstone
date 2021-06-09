package garts.domain.com.garts.home.fragments;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import garts.domain.com.garts.common.fragments.BaseFragment;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.Constants;
import garts.domain.com.garts.utils.ToastUtils;
import garts.domain.com.garts.utils.UIUtils;
import garts.domain.com.garts.Chats;
import garts.domain.com.garts.R;
import garts.domain.com.garts.ads.activities.AdsListActivity;
import garts.domain.com.garts.home.adapters.BrowseCategoriesAdapter;

public class BrowseFragment extends BaseFragment {

    private EditText searchTxt;
    private RecyclerView categoriesRV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setUpViews();
        queryCategories();
    }

    private void initViews() {
        searchTxt = getActivity().findViewById(garts.domain.com.garts.R.id.hSearchTxt);
        categoriesRV = getActivity().findViewById(R.id.fb_categories_rv);
    }

    private void setUpViews() {
        searchTxt.setTypeface(Configs.titRegular);
        // MARK: - SEARCH ADS BY KEYWORDS --------------------------------------------------------
        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchTxt.getText().toString().matches("")) {

                        UIUtils.hideKeyboard(searchTxt);

                        // Pass strings to AdsListActivity.java
                        Intent adsListIntent = new Intent(getActivity(), AdsListActivity.class);
                        adsListIntent.putExtra(AdsListActivity.SEARCH_QUERY_KEY, searchTxt.getText().toString());
                        adsListIntent.putExtra(AdsListActivity.CATEGORY_NAME_KEY, Constants.BrowseCategories.DEFAULT_CATEGORY_NAME);
                        startActivity(adsListIntent);

                        return true;
                    }

                    // No text -> No search
                } else {
                    ToastUtils.showMessage(getString(R.string.browse_search_warning));
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
        final Drawable imgClearButton = ContextCompat.getDrawable(getActivity(), R.drawable.close_white_ic);
        searchTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (searchTxt.getText().length() > 0) {
                    if (event.getX() > searchTxt.getWidth() - searchTxt.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
                        searchTxt.setText("");
                        return true;
                    }
                }
                return false;
            }
        });

        // MARK: - CHATS BUTTON ------------------------------------
        ImageView chatButt = getActivity().findViewById(R.id.hChatButt);
        chatButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser().getUsername() != null) {
                    startActivity(new Intent(getActivity(), Chats.class));
                } else {
                    Configs.loginAlert(getString(R.string.browse_chat_error), getActivity());
                }
            }
        });

        // Init AdMob banner
        AdView mAdView = getActivity().findViewById(garts.domain.com.garts.R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void queryCategories() {
        showLoading();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.CATEGORIES_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                hideLoading();
                if (error == null) {
                    categoriesRV.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    categoriesRV.setAdapter(new BrowseCategoriesAdapter(objects, new BrowseCategoriesAdapter.OnCategorySelectedListener() {
                        @Override
                        public void onCategorySelected(ParseObject categoryObj) {
                            String selectedCategoryName = categoryObj.getString(Configs.CATEGORIES_CATEGORY);
                            Intent adsListIntent = new Intent(getActivity(), AdsListActivity.class);
                            adsListIntent.putExtra(AdsListActivity.CATEGORY_NAME_KEY, selectedCategoryName);
                            startActivity(adsListIntent);
                        }
                    }));
                } else {
                    ToastUtils.showMessage(error.getMessage());
                }
            }
        });
    }
}
