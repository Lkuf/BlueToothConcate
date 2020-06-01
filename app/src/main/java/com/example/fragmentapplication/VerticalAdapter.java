package com.example.fragmentapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder> {

    private static final String TAG = VerticalAdapter.class.getSimpleName();

    private Context mContext;

    private List<Map<String,Object>> mlistData;

    public VerticalAdapter(Context context){
        mContext = context;
    }

    public void setVerticalDataList(List<Map<String,Object>> listData) {
        Log.d(TAG, "setVerticalDataList: " + listData.size());

        mlistData = listData;

        notifyDataSetChanged();
    }


    @Override
    public VerticalViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ctn_item, parent, false);
        return new VerticalViewHolder(view);
    }

    @Override
    public void onBindViewHolder( VerticalAdapter.VerticalViewHolder holder, int position) {
        Map<String,Object> rec= mlistData.get(position);  //从适配器取记录
        holder.tvName.setText(rec.get("name").toString());
        holder.tvTel.setText(rec.get("tel").toString());
    }

    @Override
    public int getItemCount() {
        return mlistData == null ? 0 : mlistData.size();
    }

    public class VerticalViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvTel;

        public VerticalViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.TV_Name);
            tvTel = itemView.findViewById(R.id.TV_Tel);
        }
    }
}
