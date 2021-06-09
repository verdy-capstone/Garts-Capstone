package garts.domain.com.garts.home.adapters;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.R;

public class BrowseCategoriesAdapter extends RecyclerView.Adapter<BrowseCategoriesAdapter.BrowseCategoryVH> {

    private List<ParseObject> categories;
    private OnCategorySelectedListener onCategorySelectedListener;

    public BrowseCategoriesAdapter(List<ParseObject> categories, OnCategorySelectedListener onCategorySelectedListener) {
        this.categories = categories;
        this.onCategorySelectedListener = onCategorySelectedListener;
    }

    @NonNull
    @Override
    public BrowseCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_browse_category, parent, false);
        return new BrowseCategoryVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrowseCategoryVH holder, int position) {
        ParseObject cObj = categories.get(position);

        holder.titleTV.setTypeface(Configs.qsBold);
        holder.titleTV.setText(cObj.getString(Configs.CATEGORIES_CATEGORY).toUpperCase());
        holder.loadImage();
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public class BrowseCategoryVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleTV;
        private ImageView imageIV;
        private ProgressBar loadingPB;

        public BrowseCategoryVH(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            titleTV = itemView.findViewById(R.id.ibc_title_tv);
            imageIV = itemView.findViewById(R.id.ibc_image_iv);
            loadingPB = itemView.findViewById(R.id.ibc_loading_pb);
        }

        private void loadImage() {
            loadingPB.setVisibility(View.VISIBLE);

            ParseObject categoryObj = categories.get(getAdapterPosition());
            ImageLoadingUtils.loadImage(categoryObj, Configs.CATEGORIES_IMAGE, new ImageLoadingUtils
                    .OnImageLoadListener() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    loadingPB.setVisibility(View.GONE);
                    imageIV.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadingError() {
                    loadingPB.setVisibility(View.GONE);
                    imageIV.setImageBitmap(null);
                    //todo show placeholder
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (onCategorySelectedListener != null) {
                ParseObject categoryObj = categories.get(getAdapterPosition());
                onCategorySelectedListener.onCategorySelected(categoryObj);
            }
        }
    }

    public interface OnCategorySelectedListener {

        void onCategorySelected(ParseObject categoryObj);
    }
}
