package com.inledco.exoterra.group;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.GroupChangedEvent;
import com.inledco.exoterra.event.GroupUserChangedEvent;
import com.inledco.exoterra.event.GroupsRefreshedEvent;
import com.inledco.exoterra.main.groups.GroupIconDialog;
import com.inledco.exoterra.manager.DeviceManager;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.FavouriteUtil;
import com.inledco.exoterra.util.GroupUtil;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.view.GradientCornerButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class HabitatDetailFragment extends BaseFragment {
    private ImageButton habitat_detail_icon;
    private TextView habitat_detail_name;
    private TextView habitat_detail_localdatetime;
    private TextView habitat_detail_datetime;
    private TextView habitat_detail_sunrise;
    private TextView habitat_detail_sunset;
    private Switch habitat_detail_favourite;
    private ImageButton habitat_detail_share;
    private TextView habitat_detail_exit;
    private ImageButton habitat_detail_delete;
    private RecyclerView habitat_detail_rv;
    private GradientCornerButton habitat_detail_back;

    private static final String KEY_GROUPID = "groupid";
    private String mGroupid;
    private Group mGroup;

    private final int mOffset = TimeZone.getDefault().getRawOffset() / 60000;
    private int mZone;
    private int mSunrise;
    private int mSunset;

    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;
    private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    refreshTime();
                    break;
            }
        }
    };

    private boolean mGroupAdmin;
    private final List<Group.User> mUsers = new ArrayList<>();
    private HabitatMembersAdapter mAdapter;

    public static HabitatDetailFragment newInstance(@NonNull final String groupid) {
        Bundle args = new Bundle();
        HabitatDetailFragment fragment = new HabitatDetailFragment();
        args.putString(KEY_GROUPID, groupid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        EventBus.getDefault().register(this);
        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mTimeReceiver != null) {
            getActivity().unregisterReceiver(mTimeReceiver);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_habitat_detail;
    }

    @Override
    protected void initView(View view) {
        habitat_detail_icon = view.findViewById(R.id.habitat_detail_icon);
        habitat_detail_name = view.findViewById(R.id.habitat_detail_name);
        habitat_detail_localdatetime = view.findViewById(R.id.habitat_detail_localdatetime);
        habitat_detail_datetime = view.findViewById(R.id.habitat_detail_datetime);
        habitat_detail_sunrise = view.findViewById(R.id.habitat_detail_sunrise);
        habitat_detail_sunset = view.findViewById(R.id.habitat_detail_sunset);
        habitat_detail_favourite = view.findViewById(R.id.habitat_detail_favourite);
        habitat_detail_share = view.findViewById(R.id.habitat_detail_share);
        habitat_detail_exit = view.findViewById(R.id.habitat_detail_exit);
        habitat_detail_delete = view.findViewById(R.id.habitat_detail_delete);
        habitat_detail_rv = view.findViewById(R.id.habitat_detail_rv);
        habitat_detail_back = view.findViewById(R.id.habitat_detail_back);

        habitat_detail_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mDateFormat = GlobalSettings.getDateTimeFormat();
        mTimeFormat = GlobalSettings.getTimeFormat();
        mTimeFormat.setTimeZone(new SimpleTimeZone(0, ""));
        Bundle args = getArguments();
        if (args != null) {
            mGroupid = args.getString(KEY_GROUPID);
            mGroup = GroupManager.getInstance().getGroup(mGroupid);
            if (mGroup != null) {
                mZone = mGroup.getZone();
                mSunrise = mGroup.getSunrise();
                mSunset = mGroup.getSunset();

                IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
                getActivity().registerReceiver(mTimeReceiver, filter);

                mGroupAdmin = TextUtils.equals(UserManager.getInstance().getUserid(), mGroup.creator);

                if (mGroupAdmin) {
                    habitat_detail_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                    habitat_detail_datetime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                    habitat_detail_sunrise.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                    habitat_detail_sunset.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                } else {
                    habitat_detail_exit.setText(R.string.exit_habitat);
                }
                habitat_detail_name.setEnabled(mGroupAdmin);
                habitat_detail_datetime.setEnabled(mGroupAdmin);
                habitat_detail_sunrise.setEnabled(mGroupAdmin);
                habitat_detail_sunset.setEnabled(mGroupAdmin);
                habitat_detail_share.setVisibility(mGroupAdmin ? View.VISIBLE : View.GONE);

                final Set<String> favourites = FavouriteUtil.getFavourites(getContext());
                habitat_detail_favourite.setChecked(favourites.contains(mGroupid));

                if (mGroup.users != null) {
                    mUsers.addAll(mGroup.users);
                    Collections.sort(mUsers, new Comparator<Group.User>() {
                        @Override
                        public int compare(Group.User o1, Group.User o2) {
                            if (TextUtils.equals(o1.role, "管理员")) {
                                return -1;
                            }
                            if (TextUtils.equals(o2.role, "管理员")) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                }
                mAdapter = new HabitatMembersAdapter(getContext(), mGroup.creator, mUsers);
                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Group.User user = mUsers.get(position);
                        addFragmentToStack(R.id.main_fl, HabitatMemberFragment.newInstance(mGroupid, user.userid));
                    }
                });
                habitat_detail_rv.setAdapter(mAdapter);

                refreshData();
            }
        }
    }

    @Override
    protected void initEvent() {
        habitat_detail_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        if (mGroup == null) {
            return;
        }
        habitat_detail_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupIconDialog();
            }
        });
        habitat_detail_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });

        habitat_detail_datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog();
            }
        });

        habitat_detail_sunrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(mSunrise/60, mSunrise%60, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int sunrise = hourOfDay*60 + minute;
                        setGroupRemark1(mZone, sunrise, mSunset);
                    }
                });
            }
        });

        habitat_detail_sunset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(mSunset/60, mSunset%60, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int sunset = hourOfDay*60 + minute;
                        setGroupRemark1(mZone, mSunrise, sunset);
                    }
                });
            }
        });

        habitat_detail_favourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FavouriteUtil.addFavourite(getContext(), mGroupid);
                } else {
                    FavouriteUtil.removeFavourite(getContext(), mGroupid);
                    EventBus.getDefault().post(new GroupsRefreshedEvent());
                }
            }
        });

        habitat_detail_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShareHabitatDialog();
            }
        });

        habitat_detail_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteHabitatDialog();
            }
        });
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupsRefreshedEvent(GroupsRefreshedEvent event) {
        if (event == null) {
            return;
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupChangedEvent(GroupChangedEvent event) {
        if (event == null) {
            return;
        }
        if (TextUtils.equals(mGroupid, event.getGroupid())) {
            mZone = mGroup.getZone();
            mSunrise = mGroup.getSunrise();
            mSunset = mGroup.getSunset();
            refreshData();
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onGroupUserChangedEvent(GroupUserChangedEvent event) {
        if (event == null || mAdapter == null) {
            return;
        }
        if (TextUtils.equals(mGroupid, event.getGroupid())) {
            mUsers.clear();
            mUsers.addAll(mGroup.users);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setGroupRemark1(final int zone, final int sunrise, final int sunset) {
        GroupManager.getInstance().setRemark1(mGroupid, zone, sunrise, sunset, new HttpCallback<UserApi.GroupResponse>() {
            @Override
            public void onError(final String error) {
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.GroupResponse result) {
                EventBus.getDefault().post(new GroupChangedEvent(mGroupid));
            }
        });
    }

    private String getTimeText(int time) {
        return mTimeFormat.format(time*60000);
    }

    private void refreshData() {
        habitat_detail_icon.setImageResource(GroupUtil.getGroupIcon(mGroup.remark2));
        habitat_detail_name.setText(mGroup.name);
        refreshTime();
        habitat_detail_sunrise.setText(getTimeText(mSunrise));
        habitat_detail_sunset.setText(getTimeText(mSunset));
    }

    private void refreshTime() {
        long time = System.currentTimeMillis();

        mDateFormat.setTimeZone(new SimpleTimeZone(mOffset*60000, ""));
        habitat_detail_localdatetime.setText(mDateFormat.format(time));

        mDateFormat.setTimeZone(new SimpleTimeZone(mZone*60000, ""));
        habitat_detail_datetime.setText(mDateFormat.format(time));
    }

    private void showGroupIconDialog() {
        GroupIconDialog dialog = new GroupIconDialog(getContext()) {
            @Override
            public void onChoose(final String name, final int res) {
                UserApi.GroupRequest request = new UserApi.GroupRequest();
                request.remark2 = name;
                AliotServer.getInstance().modifyGroupInfo(mGroupid, request, new HttpCallback<UserApi.GroupResponse>() {
                    @Override
                    public void onError(String error) {
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.GroupResponse result) {
                        mGroup.remark2 = name;
                        EventBus.getDefault().post(new GroupChangedEvent(mGroupid));
                    }
                });
            }
        };
        dialog.init(mGroup.remark2);
        dialog.show();
    }

    private void showDateTimePickerDialog() {
        long time = System.currentTimeMillis() + (mZone - mOffset) * 60000;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        final int yr = calendar.get(Calendar.YEAR);
        final int mon = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);
        showTimePickerDialog(hour, min, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                showDatePickerDialog(yr, mon, day, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        int zone = (int) (calendar.getTimeInMillis() / 60000 - System.currentTimeMillis() / 60000 + mOffset);
                        setGroupRemark1(zone, mSunrise, mSunset);
                    }
                });
            }
        });
//        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
//                TimePickerDialog timeDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        calendar.set(Calendar.YEAR, year);
//                        calendar.set(Calendar.MONTH, month);
//                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                        calendar.set(Calendar.MINUTE, minute);
//                        int zone = (int) (calendar.getTimeInMillis() / 60000 - System.currentTimeMillis() / 60000 + mOffset);
//                        setGroupRemark1(zone, mSunrise, mSunset);
//                    }
//                }, hour, min, GlobalSettings.is24HourFormat());
//                timeDialog.setCancelable(false);
//                timeDialog.show();
//            }
//        }, yr, mon, day);
//        dateDialog.setCancelable(false);
//        dateDialog.show();
    }

    private void showTimePickerDialog(int hour, int min, final TimePickerDialog.OnTimeSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_time_picker, null, false);
        final TimePicker tp = view.findViewById(R.id.dialog_time_picker);
        tp.setCurrentHour(hour);
        tp.setCurrentMinute(min);
        tp.setIs24HourView(GlobalSettings.is24HourFormat());
        final AlertDialog dialog = builder.setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setCancelable(false)
                                          .show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTimeSet(tp, tp.getCurrentHour(), tp.getCurrentMinute());
                }
                dialog.dismiss();
            }
        });
    }

    private void showDatePickerDialog(int year, int month, int day, final DatePickerDialog.OnDateSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_date_picker, null, false);
        final DatePicker dp = view.findViewById(R.id.dialog_date_picker);
        dp.init(year, month, day, null);
        final AlertDialog dialog = builder.setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setCancelable(false)
                                          .show();
        Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDateSet(dp, dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                }
                dialog.dismiss();
            }
        });
    }

    private void showRenameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.habitat_name));
        et_name.setText(mGroup.name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_habitat)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        et_name.requestFocus();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                AliotServer.getInstance().modifyGroupName(mGroupid, name, new HttpCallback<UserApi.GroupResponse>() {
                    @Override
                    public void onError(final String error) {
                        showToast(error);
                    }

                    @Override
                    public void onSuccess(UserApi.GroupResponse result) {
                        mGroup.name = name;
                        EventBus.getDefault().post(new GroupChangedEvent(mGroupid));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }

    private void showShareHabitatDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_share_til);
        final TextInputEditText et_email = view.findViewById(R.id.dialog_share_email);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.invite_member)
                                          .setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.invite, null)
                                          .setCancelable(false)
                                          .show();
        et_email.requestFocus();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    inviteUser(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }

    private void inviteUser(@NonNull final String email) {
        AliotServer.getInstance().inviteUserToGroup(mGroupid, email, new HttpCallback<UserApi.GroupInviteResponse>() {
            @Override
            public void onError(String error) {
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.GroupInviteResponse result) {
                Log.e(TAG, "onSuccess: " + JSON.toJSONString(result));
                showToast("Invite user success");
                AliotServer.getInstance().invite(result.data.invitee, result.data.invite_id, mGroupid, mGroup.name);
            }
        });
    }

    private void showDeleteHabitatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String title = mGroupAdmin ? getString(R.string.delete_habitat) : getString(R.string.exit_habitat);
        String msg = mGroupAdmin ? getString(R.string.msg_delete_habitat, mGroup.name) : getString(R.string.msg_exit_habitat, mGroup.name);
        builder.setTitle(title)
               .setMessage(msg)
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       final HttpCallback<UserApi.Response> callback = new HttpCallback<UserApi.Response>() {
                           @Override
                           public void onError(String error) {
                               dismissLoadDialog();
                               showToast(error);
                           }

                           @Override
                           public void onSuccess(UserApi.Response result) {
                               dismissLoadDialog();
                               if (!mGroupAdmin) {
                                   DeviceManager.getInstance().getSubscribedDevices();
                                   AliotServer.getInstance().exitGroup(mGroup.creator, mGroupid, mGroup.name);
                               } else {
                                   for (Group.User user : mGroup.users) {
                                       if (!TextUtils.equals(user.userid, mGroup.creator)) {
                                           AliotServer.getInstance().deleteGroup(user.userid, mGroupid, mGroup.name);
                                       }
                                   }
                               }
                               GroupManager.getInstance().removeGroup(mGroupid);
                               EventBus.getDefault().post(new GroupsRefreshedEvent());
                               getActivity().getSupportFragmentManager().popBackStack(null, 1);
                           }
                       };
                       if (mGroupAdmin) {
                           AliotServer.getInstance().deleteGroup(mGroupid, callback);
                       } else {
                           AliotServer.getInstance().exitGroup(mGroupid, callback);
                       }
                       showLoadDialog();
                   }
               })
               .setCancelable(false)
               .show();
    }
}
