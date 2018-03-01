package com.mukeshteckwani.crowdfire.wardrobe.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mukeshteckwani.crowdfire.wardrobe.R;
import com.mukeshteckwani.crowdfire.wardrobe.util.Utils;

import java.util.ArrayList;

/**
 * Created by mukeshteckwani on 31/01/18.
 */

public class ImagePagerAdapter extends PagerAdapter {
    private final Context mContext;
    private ArrayList<String> mImages;
    private final LayoutInflater layoutInflater;

    @Override
    public int getCount() {
        if (mImages == null)
            return 0;
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

     ImagePagerAdapter(Context context, ArrayList<String> images) {
        mImages = images;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.item, container, false);
        ImageView iv = itemView.findViewById(R.id.iv);
        iv.setImageBitmap(Utils.getBitmapObjectFromPath(mImages.get(position)));
        container.addView(itemView);
        return itemView;
    }

    void setImages(ArrayList<String> images) {
        mImages = images;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
