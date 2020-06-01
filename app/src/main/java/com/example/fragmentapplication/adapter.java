package com.example.fragmentapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.MyViewHolder> {

    private static final String TAG = adapter.class.getSimpleName();

    private Context mContext;

    private List<GroupDataBean> mList = new ArrayList<>();

    public adapter(Context context){
        this.mContext = context;
    }

    public void setGroupDataList(List<GroupDataBean> list) {
        mList = list;

        notifyDataSetChanged();
    }

    @Override
    public adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(adapter.MyViewHolder holder, int position) {
        holder.tvTeam.setText(mList.get(position).getTeam());
        holder.tvPlayer.setText(mList.get(position).getPlayer());

        if (position == 0) {
            holder.tvTeam.setVisibility(View.VISIBLE);
        } else {
            if (mList.get(position).getTeam().equals(mList.get(position - 1).getTeam())) {
                holder.tvTeam.setVisibility(View.GONE);
            } else {
                holder.tvTeam.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvTeam, tvPlayer;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvTeam = itemView.findViewById(R.id.TV_Team);
            tvPlayer = itemView.findViewById(R.id.TV_Player);
        }
    }
}
