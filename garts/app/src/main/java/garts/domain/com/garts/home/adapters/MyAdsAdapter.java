package garts.domain.com.garts.home.adapters;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.R;

public class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.MyAdsVH> {

    private List<ParseObject> myAds;
    private OnMyAdClickListener onMyAdClickListener;

    public MyAdsAdapter(List<ParseObject> myAds, OnMyAdClickListener onMyAdClickListener) {
        this.myAds = myAds;
        this.onMyAdClickListener = onMyAdClickListener;
    }

    @NonNull
    @Override
    public MyAdsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_my_ad, parent, false);
        return new MyAdsVH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyAdsVH holder, int position) {
        ParseObject adObj = myAds.get(position);

        // Get ad title
        holder.titleTV.setTypeface(Configs.titSemibold);
        holder.titleTV.setText(adObj.getString(Configs.ADS_TITLE));

        // Get ad price
        holder.priceTV.setTypeface(Configs.titRegular);
        holder.priceTV.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

        // Get date
        holder.timeAgoTV.setTypeface(Configs.titRegular);
        holder.timeAgoTV.setText(Configs.timeAgoSinceDate(adObj.getCreatedAt()));

        // Get Image
        ImageLoadingUtils.loadImage(adObj, Configs.ADS_IMAGE1, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                holder.pictureIV.setImageBitmap(bitmap);
            }

            @Override
            public void onImageLoadingError() {
                holder.pictureIV.setImageResource(R.drawable.logo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myAds != null ? myAds.size() : 0;
    }

    class MyAdsVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView pictureIV;
        private TextView titleTV;
        private TextView priceTV;
        private TextView timeAgoTV;

        public MyAdsVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            pictureIV = itemView.findViewById(R.id.ima_picture_iv);
            titleTV = itemView.findViewById(R.id.ima_title_tv);
            priceTV = itemView.findViewById(R.id.ima_price_tv);
            timeAgoTV = itemView.findViewById(R.id.ima_time_tv);
        }

        @Override
        public void onClick(View v) {
            if (onMyAdClickListener != null) {
                ParseObject adObj = myAds.get(getAdapterPosition());
                onMyAdClickListener.onAdClicked(adObj);
            }
        }
    }

    public interface OnMyAdClickListener {

        void onAdClicked(ParseObject adObject);
    }
}
