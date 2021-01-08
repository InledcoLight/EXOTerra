package com.inledco.exoterra.main.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotConsts;
import com.inledco.exoterra.aliot.Device;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.common.SimpleAdapter;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.util.GroupUtil;
import com.inledco.exoterra.util.SensorUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class GroupsAdapter extends SimpleAdapter<Group, GroupsAdapter.GroupViewHolder> {
    private static final String TAG = "GroupsAdapter";

    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;
    private String mSelectedGroupid;
    private GroupViewHolder mSelectedHolder;

    private final int mOffset;

    public GroupsAdapter(@NonNull Context context, List<Group> data) {
        super(context, data);
        mOffset = TimeZone.getDefault().getRawOffset();
        mDateFormat = new SimpleDateFormat("MMM d");
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
        holder.icon.setImageResource(GroupUtil.getGroupIcon(group.remark2));

        if (selected) {
            mSelectedHolder = holder;
            showDetail(holder, group);
            holder.set.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
        } else {
            holder.title.setBackgroundResource(group.getDeviceCount() == 0 ? R.drawable.shape_roundrect_red : R.drawable.shape_roundrect_gradient);
            holder.set.setVisibility(View.INVISIBLE);
            holder.more.setVisibility(View.GONE);
            holder.warn.setVisibility(View.GONE);
        }

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(mSelectedGroupid, groupid)) {
                    mSelectedGroupid = null;
                    mSelectedHolder = null;
                    holder.set.setVisibility(View.INVISIBLE);
                    holder.more.setVisibility(View.GONE);
                    holder.warn.setVisibility(View.GONE);
                } else {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.set.setVisibility(View.INVISIBLE);
                        mSelectedHolder.more.setVisibility(View.GONE);
                        mSelectedHolder.warn.setVisibility(View.GONE);
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
        if (group == null) {
            return;
        }
        holder.title.setBackgroundResource(group.getDeviceCount() == 0 ? R.drawable.shape_roundrect_red : R.drawable.shape_roundrect_gradient);
        final int zone = group.getZone();
        final int sunrise = group.getSunrise();
        final int sunset = group.getSunset();

        long time = System.currentTimeMillis();
        TimeZone tz = new SimpleTimeZone(zone*60000, "");
        mTimeFormat.setTimeZone(tz);
        mDateFormat.setTimeZone(tz);
        mSelectedHolder.time.setText(mTimeFormat.format(time));
        mSelectedHolder.date.setText(mDateFormat.format(time));
        //        String daynight = mContext.getString(R.string.nighttime);
        //        int minutes = (int) ((time / 60000 + 1440 + zone) % 1440);
        //        @DrawableRes int icon = R.drawable.ic_moon;
        //        if (sunrise <= sunset) {
        //            if (minutes >= sunrise && minutes < sunset) {
        //                daynight = mContext.getString(R.string.daytime);
        //                icon = R.drawable.ic_sun;
        //            }
        //        } else {
        //            if (minutes >= sunrise || minutes < sunset) {
        //                daynight = mContext.getString(R.string.daytime);
        //                icon = R.drawable.ic_sun;
        //            }
        //        }
        mTimeFormat.setTimeZone(new SimpleTimeZone(0, ""));
        holder.sunrise.setText(mTimeFormat.format(sunrise*60000));
        holder.sunset.setText(mTimeFormat.format(sunset*60000));

        if (group.getDeviceCount() == 0) {
            holder.devcnt.setVisibility(View.GONE);
            holder.sensor1.setText(null);
            holder.sensor2.setText(null);
            holder.sen_container.setVisibility(View.GONE);
            holder.warn.setVisibility(View.VISIBLE);
            holder.warn_msg.setText(R.string.no_habitat_device_warning);
            return;
        }
        holder.devcnt.setText(mContext.getString(R.string.habitat_devcnt, group.getDeviceCount()));
        holder.devcnt.setVisibility(View.VISIBLE);
        holder.warn.setVisibility(View.GONE);

        for (Group.Device dev : group.devices) {
            if (TextUtils.equals(dev.product_key, AliotConsts.PRODUCT_KEY_EXOSOCKET)) {
                String key = dev.product_key + "_" + dev.device_name;
                Device device = DeviceManager.getInstance().getDevice(key);
                if (device != null && device instanceof ExoSocket) {
                    ExoSocket socket = (ExoSocket) device;
                    boolean res = false;
                    if (socket.isOnline() && socket.getSensorAvailable()) {
                        ExoSocket.Sensor[] sensors = socket.getSensor();
                        if (sensors == null) {
                            holder.sensor1.setText(null);
                            holder.sensor2.setText(null);
                            continue;
                        }
                        if (sensors.length >= 1) {
                            ExoSocket.Sensor sensor1 = sensors[0];
                            int value1 = sensor1.getValue();
                            int type1 = sensor1.getType();
                            String s1text = SensorUtil.getSensorValueText(value1, type1) + "\n" + SensorUtil.getSensorUnit(type1);
                            holder.sensor1.setText(s1text);
                            res = true;
                        }
                        if (sensors.length >= 2) {
                            ExoSocket.Sensor sensor2 = sensors[1];
                            int value2 = sensor2.getValue();
                            int type2 = sensor2.getType();
                            String s2text = SensorUtil.getSensorValueText(value2, type2) + "\n" + SensorUtil.getSensorUnit(type2);
                            holder.sensor2.setText(s2text);
                            res = true;
                        }
                    }
                    if (res) {
                        holder.sen_container.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
        }
        holder.sensor1.setText(null);
        holder.sensor2.setText(null);
        holder.sen_container.setVisibility(View.GONE);
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
        mSelectedHolder = null;
        notifyDataSetChanged();
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
            long time = System.currentTimeMillis();
            TimeZone tz = new SimpleTimeZone(zone*60000, "");
            mDateFormat.setTimeZone(tz);
            mTimeFormat.setTimeZone(tz);
            mSelectedHolder.time.setText(mTimeFormat.format(time));
            mSelectedHolder.date.setText(mDateFormat.format(time));
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout title;
        private ImageView icon;
        private TextView name;
        private ImageButton set;
        private LinearLayout more;
        private TextView sensor1;
        private TextView sensor2;
        private TextView time;
        private TextView date;
        private TextView sunrise;
        private TextView sunset;
        private TextView devcnt;
        private LinearLayout sen_container;
        private View warn;
        private TextView warn_msg;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_habitat_title);
            icon = itemView.findViewById(R.id.item_habitat_icon);
            name = itemView.findViewById(R.id.item_habitat_name);
            set = itemView.findViewById(R.id.item_habitat_set);
            more = itemView.findViewById(R.id.item_habitat_more);
            sensor1 = itemView.findViewById(R.id.item_habitat_sensor1);
            sensor2 = itemView.findViewById(R.id.item_habitat_sensor2);
            time = itemView.findViewById(R.id.item_habitat_time);
            date = itemView.findViewById(R.id.item_habitat_date);
            sunrise = itemView.findViewById(R.id.item_habitat_sunrise);
            sunset = itemView.findViewById(R.id.item_habitat_sunset);
            devcnt = itemView.findViewById(R.id.item_habitat_devcnt);
            sen_container = itemView.findViewById(R.id.item_habitat_ll);
            warn = itemView.findViewById(R.id.item_habitat_warning);
            warn_msg = warn.findViewById(R.id.warning_tv_msg);
        }
    }
}
