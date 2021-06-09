package garts.domain.com.garts.home.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;


public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityVH> {

    private List<ParseObject> activityList;
    private ActivityItemListener activityItemListener;

    public ActivitiesAdapter(List<ParseObject> activityList, ActivityItemListener activityItemListener) {
        this.activityList = activityList;
        this.activityItemListener = activityItemListener;
    }

    public void addMoreActivities(List<ParseObject> moreActivities) {
        int itemCountBeforeAdding = getItemCount();
        this.activityList.addAll(moreActivities);
        notifyItemRangeInserted(itemCountBeforeAdding, this.activityList.size());
    }

    public void removeActivity(ParseObject activityObj) {
        int posToRemove = getActivityPos(activityObj);
        if (posToRemove < 0 || posToRemove > activityList.size() - 1) {
            return;
        }
        activityList.remove(posToRemove);
        notifyItemRemoved(posToRemove);
    }

    private int getActivityPos(ParseObject activityObj) {
        for (int i = 0; i < activityList.size(); i++) {
            ParseObject obj = activityList.get(i);
            if (obj == activityObj) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ActivityVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.cell_activity, parent, false);
        return new ActivityVH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityVH holder, int position) {
        ParseObject actObj = activityList.get(position);

        holder.actTxt.setText(actObj.getString(Configs.ACTIVITY_TEXT));
        holder.dateTxt.setText(Configs.timeAgoSinceDate(actObj.getCreatedAt()));

        holder.avImage.setImageResource(0);
        loadUserAvatar(actObj, holder.avImage);
    }

    @Override
    public int getItemCount() {
        return activityList != null ? activityList.size() : 0;
    }

    private void loadUserAvatar(ParseObject actObj, final ImageView avatarIV) {
        actObj.getParseObject(Configs.ACTIVITY_OTHER_USER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject userPointer, ParseException e) {
                Configs.getParseImage(avatarIV, userPointer, Configs.USER_AVATAR);
            }
        });
    }

    public class ActivityVH extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        TextView actTxt;
        TextView dateTxt;
        ImageView avImage;

        public ActivityVH(View itemView) {
            super(itemView);
            initViews();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void initViews() {
            actTxt = itemView.findViewById(R.id.actTextTxt);
            dateTxt = itemView.findViewById(R.id.actDateTxt);
            avImage = itemView.findViewById(R.id.actAvatarImg);

            actTxt.setTypeface(Configs.titRegular);
            dateTxt.setTypeface(Configs.titRegular);
        }

        @Override
        public void onClick(View v) {
            if (activityItemListener != null) {
                ParseObject activityObj = activityList.get(getAdapterPosition());
                activityItemListener.onActivityClicked(activityObj);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (activityItemListener != null) {
                ParseObject activityObj = activityList.get(getAdapterPosition());
                activityItemListener.onActivityLongClicked(activityObj);
            }
            return false;
        }
    }

    public interface ActivityItemListener {

        void onActivityClicked(ParseObject activityObj);

        void onActivityLongClicked(ParseObject activityObj);
    }
}
