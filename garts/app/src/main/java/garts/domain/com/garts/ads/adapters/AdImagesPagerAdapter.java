package garts.domain.com.garts.ads.adapters;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.ParseObject;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.utils.ImageLoadingUtils;
import garts.domain.com.garts.R;

public class AdImagesPagerAdapter extends PagerAdapter {

    private ParseObject adObject;
    private OnImageClickListener onImageClickListener;
    private Bitmap firstImageBmp = null;

    public AdImagesPagerAdapter(ParseObject adObject, OnImageClickListener onImageClickListener) {
        this.adObject = adObject;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.item_ad_image, container, false);
        final AdImages adImage = AdImages.values()[position];
        final ImageView imageIV = layout.findViewById(R.id.iai_image_iv);

        ImageLoadingUtils.loadImage(adObject, adImage.imageFieldKey, new ImageLoadingUtils.OnImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                if (firstImageBmp == null) {
                    firstImageBmp = bitmap;
                }
                imageIV.setImageBitmap(bitmap);
                imageIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onImageClickListener != null) {
                            onImageClickListener.onImageClicked(adImage.imageFieldKey);
                        }
                    }
                });
            }

            @Override
            public void onImageLoadingError() {
                imageIV.setImageBitmap(null);
                imageIV.setOnClickListener(null);
            }
        });

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return AdImages.values().length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    public Bitmap getFirstImageBmp() {
        return firstImageBmp;
    }

    public enum AdImages {

        IMAGE1(Configs.ADS_IMAGE1),
        IMAGE2(Configs.ADS_IMAGE2),
        IMAGE3(Configs.ADS_IMAGE3);

        private String imageFieldKey;

        AdImages(String imageFieldKey) {
            this.imageFieldKey = imageFieldKey;
        }
    }

    public interface OnImageClickListener {

        void onImageClicked(String imageFieldKey);
    }
}
