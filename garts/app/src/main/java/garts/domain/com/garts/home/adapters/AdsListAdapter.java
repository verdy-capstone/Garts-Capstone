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

public class AdsListAdapter extends RecyclerView.Adapter<AdsListAdapter.AdsListVH> {

    private List<ParseObject> adsList;
    private OnAdClickListener onAdClickListener;

    public AdsListAdapter(List<ParseObject> adsList, OnAdClickListener onAdClickListener) {
        this.adsList = adsList;
        this.onAdClickListener = onAdClickListener;
    }

    public void addMoreAds(List<ParseObject> moreAds) {
        int itemCountBeforeAdding = getItemCount();
        this.adsList.addAll(moreAds);
        notifyItemRangeInserted(itemCountBeforeAdding, this.adsList.size());
    }

    public void clearAds() {
        if (adsList != null) {
            adsList.clear();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdsListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_ads_list, parent, false);
        return new AdsListVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdsListVH holder, int position) {
        final ParseObject adObj = adsList.get(position);

        holder.titleTV.setTypeface(Configs.titSemibold);
        holder.titleTV.setText(adObj.getString(Configs.ADS_TITLE));
        holder.priceTV.setTypeface(Configs.titSemibold);
        holder.priceTV.setText(adObj.getString(Configs.ADS_CURRENCY) + String.valueOf(adObj.getNumber(Configs.ADS_PRICE)));

        holder.loadImage();
    }

    @Override
    public int getItemCount() {
        return adsList != null ? adsList.size() : 0;
    }

    public class AdsListVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleTV;
        private ImageView imageIV;
        private TextView priceTV;

        public AdsListVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            titleTV = itemView.findViewById(R.id.ial_title_tv);
            imageIV = itemView.findViewById(R.id.ial_image_iv);
            priceTV = itemView.findViewById(R.id.ial_price_tv);
        }

        private void loadImage() {
            final ParseObject adObj = adsList.get(getAdapterPosition());
            ImageLoadingUtils.loadImage(adObj, Configs.ADS_IMAGE1, new ImageLoadingUtils.OnImageLoadListener() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    imageIV.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadingError() {
                    imageIV.setImageResource(R.drawable.logo);
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (onAdClickListener != null) {
                ParseObject clickedAdObj = adsList.get(getAdapterPosition());
                onAdClickListener.onAdClicked(clickedAdObj);
            }
        }
    }

    public interface OnAdClickListener {

        void onAdClicked(ParseObject adObj);
    }
}
