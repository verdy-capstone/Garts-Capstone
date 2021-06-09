package garts.domain.com.garts.home.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import garts.domain.com.garts.common.fragments.BaseFragment;
import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;
import garts.domain.com.garts.UserProfile;
import garts.domain.com.garts.home.adapters.ActivitiesAdapter;

public class ActivityFragment extends BaseFragment implements ActivitiesAdapter.ActivityItemListener {

    private RecyclerView activitiesRV;
    private TextView tipTV;

    // Pagination
    private ActivitiesAdapter activityListAdapter;
    private ParseQuery<ParseObject> activityQuery;
    private int querySkip = 0;
    private boolean isNextPageLoading;
    private boolean allActivitiesLoaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        queryActivity();
    }

    private void initViews() {
        activitiesRV = getActivity().findViewById(R.id.fa_activities_rv);
        tipTV = getActivity().findViewById(R.id.fa_tip_tv);
    }

    // MARK: - QUERY ACTIVITY ---------------------------------------------------------------
    private void queryActivity() {
        showLoading();

        querySkip = 0;
        allActivitiesLoaded = false;

        activityQuery = ParseQuery.getQuery(Configs.ACTIVITY_CLASS_NAME);
        activityQuery.whereEqualTo(Configs.ACTIVITY_CURRENT_USER, ParseUser.getCurrentUser());
        activityQuery.setLimit(Configs.DEFAULT_PAGE_SIZE);
        activityQuery.setSkip(querySkip);
        activityQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    hideLoading();
                    setUpActivityList(objects);
                } else {
                    // Error in query
                    hideLoading();
                    Configs.simpleAlert(error.getMessage(), getActivity());
                }
            }
        });
    }

    private void loadMoreActivities() {
        if (activityQuery == null) {
            Log.d("log-", "Last query is null");
            return;
        }

        if (isNextPageLoading) {
            Log.d("log-", "Next page is loading");
            return;
        }

        if (allActivitiesLoaded) {
            Log.d("log-", "All ads are already loaded");
            return;
        }

        isNextPageLoading = true;
        querySkip += Configs.DEFAULT_PAGE_SIZE;
        activityQuery.setLimit(Configs.DEFAULT_PAGE_SIZE);
        activityQuery.setSkip(querySkip);
        activityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                isNextPageLoading = false;

                if (e == null) {
                    if (objects.isEmpty()) {
                        allActivitiesLoaded = true;
                        return;
                    }

                    activityListAdapter.addMoreActivities(objects);
                } else {
                    Log.d("log-", "Error loading more ads: " + e.getMessage());
                }
            }
        });
    }

    private void setUpActivityList(List<ParseObject> activityList) {
        if (activityList.isEmpty()) {
            tipTV.setVisibility(View.GONE);
            return;
        }

        activityListAdapter = new ActivitiesAdapter(activityList, this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        activitiesRV.setLayoutManager(layoutManager);
        activitiesRV.setAdapter(activityListAdapter);
        activitiesRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (lastVisibleItem != -1 &&
                        lastVisibleItem + Configs.DEFAULT_PAGE_THRESHOLD >= totalItemCount) {
                    Log.d("log-", "Total item count: " + totalItemCount +
                            " last visible item pos: " + lastVisibleItem + " Need to load more");
                    loadMoreActivities();
                }
            }
        });
    }

    @Override
    public void onActivityClicked(ParseObject activityObj) {
        // MARK: - TAP A CELL TO SEE USER'S PROFILE -----------------------------------
        // Get userPointer
        activityObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject userPointer, ParseException e) {
                // Pass objectID to the other Activity
                Intent i = new Intent(getActivity(), UserProfile.class);
                Bundle extras = new Bundle();
                extras.putString("objectID", userPointer.getObjectId());
                i.putExtras(extras);
                startActivity(i);

            }
        });
    }

    @Override
    public void onActivityLongClicked(final ParseObject activityObj) {
        // MARK: - LONG PRESS TO DELETE AN ACTIVITY ------------------------------------
        activityObj.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                activityListAdapter.removeActivity(activityObj);
                Toast.makeText(getActivity(), R.string.activity_remove_success, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
