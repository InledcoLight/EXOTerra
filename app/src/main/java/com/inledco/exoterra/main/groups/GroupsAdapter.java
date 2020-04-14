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
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.aliot.bean.XDevice;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.util.TimeFormatUtil;

import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;

public class GroupsAdapter extends SimpleAdapter<Group, GroupsAdapter.GroupViewHolder> {
    private static final String TAG = "GroupsAdapter";

    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;
    private String mSelectedGroupid;
    private GroupViewHolder mSelectedHolder;

    private final int defaultZone;

    public GroupsAdapter(@NonNull Context context, List<Group> data) {
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
        final Group group = mData.get(positon);
        final String groupid = group.groupid;
        final boolean selected = TextUtils.equals(mSelectedGroupid, groupid);
        holder.name.setText(group.name);

        if (selected) {
            mSelectedHolder = holder;
            showDetail(holder, group);
            holder.set.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
        } else {
            holder.set.setVisibility(View.INVISIBLE);
            holder.more.setVisibility(View.GONE);
        }

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mSelectedGroupid, groupid)) {
                    mSelectedGroupid = null;
                    mSelectedHolder = null;
                    holder.set.setVisibility(View.INVISIBLE);
                    holder.more.setVisibility(View.GONE);
                } else {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.set.setVisibility(View.INVISIBLE);
                        mSelectedHolder.more.setVisibility(View.GONE);
                    }
                    mSelectedGroupid = groupid;
                    mSelectedHolder = holder;

                    showDetail(holder, group);

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

    private void showDetail(@NonNull final GroupViewHolder holder, @NonNull final Group group) {
        final int zone = group.getZone();
        final int sunrise = group.getSunrise();
        final int sunset = group.getSunset();

        for (XDevice dev : group.devices) {
            if (TextUtils.equals(dev.product_key, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
                String key = dev.product_key + "_" + dev.device_name;
                Device device = DeviceManager.getInstance().getDevice(key);
                if (device != null && device instanceof ExoSocket) {
                    ExoSocket socket = (ExoSocket) device;
                    boolean res = false;
                    if (socket.getSensorAvailable()) {
                        ExoSocket.Sensor[] sensors = socket.getSensor();
                        if (sensors == null) {
                            holder.sensor1.setText(null);
                            holder.sensor2.setText(null);
                            continue;
                        }
                        if (sensors.length >= 1) {
                            ExoSocket.Sensor sensor1 = socket.getSensor()[0];
                            String s1text = getSensorValueText(sensor1.getValue(), sensor1.getType()) + getSensorUnit(sensor1.getType());
                            holder.sensor1.setText(s1text);
                            res = true;
                        }
                        if (sensors.length >= 2) {
                            ExoSocket.Sensor sensor2 = socket.getSensor()[1];
                            String s2text = getSensorValueText(sensor2.getValue(), sensor2.getType()) + getSensorUnit(sensor2.getType());
                            holder.sensor1.setText(s2text);
                            res = true;
                        }
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
        holder.devcnt.setText(mContext.getString(R.string.habitat_devcnt, group.getDeviceCount()));
    }

    public void removeData(final int position) {
        if (position >= 0 && position < getItemCount()) {
            Group group = mData.get(position);
            if (TextUtils.equals(mSelectedGroupid, group.groupid)) {
                mSelectedGroupid = null;
                mSelectedHolder = null;
                mData.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

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
        if (mSelectedGroupid != null) {
            boolean flag = false;
            for (Group group : mData) {
                if (TextUtils.equals(group.groupid, mSelectedGroupid)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                mSelectedGroupid = null;
                mSelectedHolder = null;
                return;
            }
        }
        if (mSelectedHolder != null) {
            int position = mSelectedHolder.getAdapterPosition();
            int zone = mData.get(position).getZone();
            long time = System.currentTimeMillis() + (zone-defaultZone)*60000;
            mSelectedHolder.time.setText(mDateFormat.format(time));
        }
    }

    private String getSensorValueText(int value, int type) {
        String text = String.valueOf(value);
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = GlobalSettings.getTemperatureText(value);
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = value/10 + "." + value%10;
                break;
        }
        return text;
    }

    private String getSensorUnit(int type) {
        String text = "";
        switch (type) {
            case ExoSocket.SENSOR_TEMPERATURE:
                text = GlobalSettings.getTemperatureUnit();
                break;
            case ExoSocket.SENSOR_HUMIDITY:
                text = "%";
                break;
        }
        return text;
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
