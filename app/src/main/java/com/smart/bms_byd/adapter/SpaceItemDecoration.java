package com.smart.bms_byd.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration  {

    private int normalTop;
    private Context mContext;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.top = normalTop;
//        int iPosition = parent.getChildAdapterPosition(view);
//        if (iPosition > 0) {
//            if (list != null && list.get(iPosition).isTitle() && iPosition != 0)
//                outRect.top = titleTop;
//            else
//                outRect.top = normalTop;
//        }


    }

    public SpaceItemDecoration(int normalTop, Context mContext) {
        this.normalTop = normalTop;
        this.mContext = mContext;
    }

}
