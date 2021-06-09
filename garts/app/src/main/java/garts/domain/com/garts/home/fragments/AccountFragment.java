package garts.domain.com.garts.home.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import garts.domain.com.garts.common.fragments.BaseFragment;
import garts.domain.com.garts.selledit.activities.SellEditItemActivity;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.Feedbacks;
import garts.domain.com.garts.R;
import garts.domain.com.garts.home.activities.HomeActivity;
import garts.domain.com.garts.home.adapters.MyAdsAdapter;
import garts.domain.com.garts.EditProfileActivity;

public class AccountFragment extends BaseFragment {

    private TextView usernameTV;
    private TextView fullnameTV;
    private ImageView avatarIV;
    private TextView joinedTV;
    private TextView verifiedTV;
    private TextView editProfileTV;
    private TextView feedbacksTV;
    private TextView logoutTV;
    private LinearLayout addAdsLL;
    private TextView addAdsTV;
    private RecyclerView myAdsRV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setUpViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void initViews() {
        usernameTV = getActivity().findViewById(R.id.fa_username_tv);
        fullnameTV = getActivity().findViewById(R.id.fa_fullname_tv);
        avatarIV = getActivity().findViewById(R.id.fa_avatar_iv);
        joinedTV = getActivity().findViewById(R.id.fa_joined_tv);
        verifiedTV = getActivity().findViewById(R.id.fa_verified_tv);
        editProfileTV = getActivity().findViewById(R.id.fa_edit_profile_tv);
        feedbacksTV = getActivity().findViewById(R.id.fa_feedbacks_tv);
        logoutTV = getActivity().findViewById(R.id.fa_logout_tv);
        addAdsLL = getActivity().findViewById(R.id.fa_myads_ll);
        addAdsTV = getActivity().findViewById(R.id.fa_add_ads_tv);
        myAdsRV = getActivity().findViewById(R.id.fa_myads_rv);
    }

    private void setUpViews() {
        feedbacksTV.setTypeface(Configs.titSemibold);
        editProfileTV.setTypeface(Configs.titSemibold);
        logoutTV.setTypeface(Configs.titSemibold);
        ViewCompat.setNestedScrollingEnabled(myAdsRV, false);

        // MARK: - FEEDBACKS BUTTON ------------------------------------
        feedbacksTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), Feedbacks.class);
                Bundle extras = new Bundle();
                extras.putString("userObjectID", ParseUser.getCurrentUser().getObjectId());
                i.putExtras(extras);
                startActivity(i);
            }
        });

        // MARK: - EDIT PROFILE BUTTON ------------------------------------
        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        // MARK: - LOGOUT BUTTON ------------------------------------
        logoutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setMessage(R.string.account_logout_warning_title)
                        .setTitle(R.string.app_name)
                        .setPositiveButton(getString(R.string.alert_ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Configs.showPD(getString(R.string.account_logout_loading), getActivity());

                                ParseUser.logOutInBackground(new LogOutCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Configs.hidePD();
                                        // Go back to BrowseFragment
                                        Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(homeIntent);
                                    }
                                });
                            }
                        })

                        .setNegativeButton(getString(R.string.alert_cancel_button), null)
                        .setIcon(R.drawable.logo);
                alert.create().show();
            }
        });
    }

    // MARK: - GET USER'S DETAILS ----------------------------------------------------------------
    private void getUserDetails() {
        ParseUser currUser = ParseUser.getCurrentUser();

        // get username
        usernameTV.setTypeface(Configs.titSemibold);
        usernameTV.setText(getString(R.string.username_formatted, currUser.getString(Configs.USER_USERNAME)));

        // Get fullname
        fullnameTV.setTypeface(Configs.titSemibold);
        fullnameTV.setText(currUser.getString(Configs.USER_FULLNAME));

        // Get joined since
        joinedTV.setTypeface(Configs.titRegular);
        joinedTV.setText(Configs.timeAgoSinceDate(currUser.getCreatedAt()));

        // Get verified
        verifiedTV.setTypeface(Configs.titRegular);
        if (currUser.get(Configs.USER_EMAIL_VERIFIED) != null) {
            if (currUser.getBoolean(Configs.USER_EMAIL_VERIFIED)) {
                verifiedTV.setText(R.string.account_verified);
            } else {
                verifiedTV.setText(R.string.account_not_verified);
            }
        } else {
            verifiedTV.setText(R.string.account_not_verified);
        }

        // Get avatar
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

        // Call query
        queryMyAds();
    }

    // MARK: - QUERY MY ADS ------------------------------------------------------------------
    private void queryMyAds() {
        showLoading();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.ADS_CLASS_NAME);
        query.whereEqualTo(Configs.ADS_SELLER_POINTER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.ADS_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    hideLoading();
                    if (objects.isEmpty()) {
                        addAdsTV.setVisibility(View.VISIBLE);
                        addAdsLL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startSellEditItemScreen(null);
                            }
                        });
                    } else {
                        addAdsTV.setVisibility(View.GONE);
                        addAdsLL.setOnClickListener(null);
                        myAdsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
                        myAdsRV.setAdapter(new MyAdsAdapter(objects, new MyAdsAdapter.OnMyAdClickListener() {
                            @Override
                            public void onAdClicked(ParseObject adObject) {
                                startSellEditItemScreen(adObject.getObjectId());
                            }
                        }));
                    }
                } else {
                    hideLoading();
                    Configs.simpleAlert(error.getMessage(), getActivity());
                }
            }
        });
    }

    private void startSellEditItemScreen(String adObjid) {
        Intent i = new Intent(getActivity(), SellEditItemActivity.class);
        Bundle extras = new Bundle();
        extras.putString(SellEditItemActivity.EDIT_AD_OBJ_ID_EXTRA_KEY, adObjid);
        i.putExtras(extras);
        startActivity(i);
    }
}
