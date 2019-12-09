package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.HomeProperty;
import com.inledco.exoterra.common.SimpleAdapter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GroupsAdapter extends SimpleAdapter<Home, GroupsAdapter.GroupViewHolder> {
    private final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    private GroupViewHolder mSelectedHolder;

    private final int defaultZone;
    private final int defaultSunrise = 360;
    private final int defaultSunset = 1080;

    public GroupsAdapter(@NonNull Context context, List<Home> data) {
        super(context, data);
        defaultZone = TimeZone.getDefault().getRawOffset()/60000;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_habitat;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GroupViewHolder(createView(viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupViewHolder holder, int i) {
        final int positon = holder.getAdapterPosition();
        final boolean selected = (mSelectedHolder == holder);
        DecimalFormat df = new DecimalFormat("00");
        final Home home = mData.get(positon);
        final int zone;
        final int sunrise;
        final int sunset;
        if (home.getProperty() != null) {
            zone = home.getProperty().getZone();
            sunrise = home.getProperty().getSunrise();
            sunset = home.getProperty().getSunset();
        } else {
            zone = defaultZone;
            sunrise = defaultSunrise;
            sunset = defaultSunset;
        }
        holder.name.setText(home.getHome().name);
        holder.set.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        holder.more.setVisibility(selected ? View.VISIBLE : View.GONE);
        holder.sunrise.setText(df.format(sunrise/60) + ":" + df.format(sunrise%60));
        holder.sunset.setText(df.format(sunset/60) + ":" + df.format(sunset%60));
        holder.devcnt.setText(mContext.getString(R.string.habitat_devcnt, home.getDeviceCount()));
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedHolder == holder) {
                    mSelectedHolder = null;
                    holder.set.setVisibility(View.INVISIBLE);
                    holder.more.setVisibility(View.GONE);
                } else {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.set.setVisibility(View.INVISIBLE);
                        mSelectedHolder.more.setVisibility(View.GONE);
                    }
                    mSelectedHolder = holder;
                    long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
                    DateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
                    Date date = new Date(time);
                    mSelectedHolder.time.setText(format.format(date));
                    holder.set.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                }
            }
        });

//        holder.set.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                XlinkCloudManager.getInstance().deleteHome(home.getHome().id, new XlinkRequestCallback<String>() {
//                    @Override
//                    public void onError(String error) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        mData.remove(positon);
//                        notifyItemRemoved(positon);
//                    }
//                });
//            }
//        });
    }

    public void updateTime() {
        if (mSelectedHolder != null) {
            int zone = defaultZone;
            int position = mSelectedHolder.getAdapterPosition();
            HomeProperty property = mData.get(position).getProperty();
            if (property != null) {
                zone = property.getZone();
            }
            long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
            DateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
            Date date = new Date(time);
            mSelectedHolder.time.setText(format.format(date));
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout title;
        private TextView name;
        private ImageButton set;
        private LinearLayout more;
        private TextView sensor1;
        private TextView sensor2;
        private TextView time;
        private TextView sunrise;
        private TextView sunset;
        private TextView devcnt;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_habitat_title);
            name = itemView.findViewById(R.id.item_habitat_name);
            set = itemView.findViewById(R.id.item_habitat_set);
            more = itemView.findViewById(R.id.item_habitat_more);
            sensor1 = itemView.findViewById(R.id.item_habitat_sensor1);
            sensor2 = itemView.findViewById(R.id.item_habitat_sensor2);
            time = itemView.findViewById(R.id.item_habitat_time);
            sunrise = itemView.findViewById(R.id.item_habitat_sunrise);
            sunset = itemView.findViewById(R.id.item_habitat_sunset);
            devcnt = itemView.findViewById(R.id.item_habitat_devcnt);
        }
    }
}
