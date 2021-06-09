package garts.domain.com.garts.home.fragments;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import garts.domain.com.garts.R;
import garts.domain.com.garts.ads.activities.AdDetailsActivity;
import garts.domain.com.garts.common.fragments.BaseFragment;
import garts.domain.com.garts.home.adapters.MyLikesAdapter;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ToastUtils;

public class MyLikesFragment extends BaseFragment {

    private RelativeLayout noLikesRL;
    private RecyclerView likesRV;
    private AdView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_my_likes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ParseUser.getCurrentUser().getUsername() == null) {
            return;
        }

        initViews();

        // Call query
        queryToGetLikes();

        // Init AdMob banner
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initViews() {
        noLikesRL = getActivity().findViewById(R.id.mlNoLikesLayout);
        adView = getActivity().findViewById(R.id.admobBanner);
        likesRV = getActivity().findViewById(R.id.fml_likes_rv);
    }

    // MARK: - QUERY ADS ------------------------------------------------------------------
    void queryToGetLikes() {
        noLikesRL.setVisibility(View.GONE);

        // Launch query
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.LIKES_CLASS_NAME);
        query.whereEqualTo(Configs.LIKES_CURR_USER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.LIKES_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    hideLoading();

                    // Show/hide noLikesView
                    if (objects.size() == 0) {
                        noLikesRL.setVisibility(View.VISIBLE);
                    } else {
                        noLikesRL.setVisibility(View.GONE);
                        setUpLikedAds(objects);
                    }
                } else {
                    showLoading();
                    ToastUtils.showMessage(error.getMessage());
                }
            }
        });
    }

    private void setUpLikedAds(List<ParseObject> adObjects) {
        likesRV.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        likesRV.setAdapter(new MyLikesAdapter(adObjects, new MyLikesAdapter.MyLikesClickListener() {
            @Override
            public void onAdClicked(String adObjId) {
                Intent adDetailsIntent = new Intent(getActivity(), AdDetailsActivity.class);
                adDetailsIntent.putExtra(AdDetailsActivity.AD_OBJ_ID_KEY, adObjId);
                startActivity(adDetailsIntent);
            }

            @Override
            public void onUnlikeClicked(ParseObject likeObject, final ParseObject adObject) {
                showLoading();
                likeObject.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        hideLoading();

                        // Decrement likes for the adObj
                        adObject.increment(Configs.ADS_LIKES, -1);
                        ParseUser currUser = ParseUser.getCurrentUser();

                        // Remove the user's objectID
                        List<String> likedByArr = adObject.getList(Configs.ADS_LIKED_BY);
                        likedByArr.remove(currUser.getObjectId());
                        adObject.put(Configs.ADS_LIKED_BY, likedByArr);
                        adObject.saveInBackground();

                        // Recall query
                        queryToGetLikes();
                    }
                });
            }
        }));
    }
}
