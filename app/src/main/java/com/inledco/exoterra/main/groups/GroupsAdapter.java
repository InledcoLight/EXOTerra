package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.bean.Device;
import com.inledco.exoterra.bean.EXOSocket;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.util.TimeFormatUtil;
import com.inledco.exoterra.xlink.XlinkConstants;

import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.restful.api.app.HomeApi;

public class GroupsAdapter extends SimpleAdapter<Home, GroupsAdapter.GroupViewHolder> {
    private static final String TAG = "GroupsAdapter";

    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;
    private String mSelectedHomeid;
    private GroupViewHolder mSelectedHolder;

    private final int defaultZone;

    public GroupsAdapter(@NonNull Context context, List<Home> data) {
        super(context, data);
        defaultZone = TimeZone.getDefault().getRawOffset()/60000;
        mDateFormat = GlobalSettings.getDateTimeFormat();
        mTimeFormat = GlobalSettings.getTimeFormat();
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
        final Home home = mData.get(positon);
        final String homeid = home.getHome().id;
        final boolean selected = TextUtils.equals(mSelectedHomeid, homeid);
        holder.name.setText(home.getHome().name);

        if (selected) {
            mSelectedHolder = holder;
            showDetail(holder, home);
            holder.set.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
        } else {
            holder.set.setVisibility(View.INVISIBLE);
            holder.more.setVisibility(View.GONE);
        }

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mSelectedHomeid, homeid)) {
                    mSelectedHomeid = null;
                    mSelectedHolder = null;
                    holder.set.setVisibility(View.INVISIBLE);
                    holder.more.setVisibility(View.GONE);
                } else {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.set.setVisibility(View.INVISIBLE);
                        mSelectedHolder.more.setVisibility(View.GONE);
                    }
                    mSelectedHomeid = homeid;
                    mSelectedHolder = holder;

                    showDetail(holder, home);

                    holder.set.setVisibility(View.VISIBLE);
                    holder.more.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(positon);
                }
            }
        });
    }

    private void showDetail(@NonNull final GroupViewHolder holder, @NonNull final Home home) {
        final int zone = home.getZone();
        final int sunrise = home.getSunrise();
        final int sunset = home.getSunset();

        for (HomeApi.HomeDevicesResponse.Device dev : home.getDevices()) {
            if (TextUtils.equals(dev.productId, XlinkConstants.PRODUCT_ID_SOCKET)) {
                String tag = dev.productId + "_" + dev.mac;
                Device device = DeviceManager.getInstance().getDevice(tag);
                if (device != null && device instanceof EXOSocket) {
                    EXOSocket socket = (EXOSocket) device;
                    boolean res = false;
                    if (socket.getS1Available()) {
                        holder.sensor1.setText(GlobalSettings.getTemperatureText(socket.getS1Value()));
                        res = true;
                    } else {
                        holder.sensor1.setText(null);
                    }
                    if (socket.getS2Available()) {
                        holder.sensor2.setText(GlobalSettings.getHumidityText(socket.getS2Value()));
                        res = true;
                    } else {
                        holder.sensor2.setText(null);
                    }
                    if (res) {
                        break;
                    }
                }
            }
        }

        long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
        mSelectedHolder.time.setText(mDateFormat.format(time));
        String daynight = mContext.getString(R.string.nighttime);
        int minutes = (int) ((time / 60000 + 1440 + zone) % 1440);
        @DrawableRes int icon = R.drawable.ic_moon;
        if (sunrise <= sunset) {
            if (minutes >= sunrise && minutes < sunset) {
                daynight = mContext.getString(R.string.daytime);
                icon = R.drawable.ic_sun;
            }
        } else {
            if (minutes >= sunrise || minutes < sunset) {
                daynight = mContext.getString(R.string.daytime);
                icon = R.drawable.ic_sun;
            }
        }
        mSelectedHolder.daynight.setText(daynight);
        mSelectedHolder.daynight.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        holder.sunrise.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, sunrise));
        holder.sunset.setText(TimeFormatUtil.formatMinutesTime(mTimeFormat, sunset));
        holder.devcnt.setText(mContext.getString(R.string.habitat_devcnt, home.getDeviceCount()));
    }

//    public void removeData(final int position) {
//        if (position >= 0 && position < getItemCount()) {
//            Home home = mData.get(position);
//            if (TextUtils.equals(mSelectedHomeid, home.getHome().id)) {
//                mSelectedHomeid = null;
//                mSelectedHolder = null;
//                mData.remove(position);
//                notifyItemRemoved(position);
//            }
//        }
//    }

    public void refreshData() {
        notifyDataSetChanged();
        mSelectedHolder = null;
    }

    public void updateData(int position) {
        if (mSelectedHolder != null && mSelectedHolder.getAdapterPosition() == position) {
            showDetail(mSelectedHolder, mData.get(position));
        }
    }

    public void updateTime() {
        if (mSelectedHolder != null) {
            int position = mSelectedHolder.getAdapterPosition();
            int zone = mData.get(position).getZone();
            long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
            mSelectedHolder.time.setText(mDateFormat.format(time));
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
        private TextView daynight;
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
            daynight = itemView.findViewById(R.id.item_habitat_daynight);
            sunrise = itemView.findViewById(R.id.item_habitat_sunrise);
            sunset = itemView.findViewById(R.id.item_habitat_sunset);
            devcnt = itemView.findViewById(R.id.item_habitat_devcnt);
        }
    }
}
