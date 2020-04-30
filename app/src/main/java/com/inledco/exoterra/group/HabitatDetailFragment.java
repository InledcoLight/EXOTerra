package com.inledco.exoterra.group;

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
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSON;
import com.inledco.exoterra.GlobalSettings;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotClient;
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
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;
import com.inledco.exoterra.util.FavouriteUtil;
import com.inledco.exoterra.util.GroupUtil;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.util.TimeFormatUtil;
import com.inledco.exoterra.view.GradientCornerButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    private ImageButton habitat_detail_delete;
    private RecyclerView habitat_detail_rv;
    private GradientCornerButton habitat_detail_back;

    private static final String KEY_GROUPID = "groupid";
    private String mGroupid;
    private Group mGroup;

    private final int defaultZone = TimeZone.getDefault().getRawOffset()/60000;
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
        habitat_detail_delete = view.findViewById(R.id.habitat_detail_delete);
        habitat_detail_rv = view.findViewById(R.id.habitat_detail_rv);
        habitat_detail_back = view.findViewById(R.id.habitat_detail_back);

        habitat_detail_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        mDateFormat = GlobalSettings.getDateTimeFormat();
        mTimeFormat = GlobalSettings.getTimeFormat();
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
                long time = System.currentTimeMillis();
                int habitatTime = (int) ((time / 60000 + mZone) % 1440);
                showTimePickerDialog(habitatTime, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int currTime = (int) ((System.currentTimeMillis() / 60000) % 1440);
                        int setTime = hourOfDay*60 + minute;
                        int zone = setTime - currTime;
                        if (zone < -720) {
                            zone += 1440;
                        } else if (zone > 720) {
                            zone -= 1440;
                        }
                        setGroupRemark1(zone, mSunrise, mSunset);
                    }
                });
            }
        });

        habitat_detail_sunrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(mSunrise, new TimePickerDialog.OnTimeSetListener() {
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
                showTimePickerDialog(mSunset, new TimePickerDialog.OnTimeSetListener() {
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
        return TimeFormatUtil.formatMinutesTime(mTimeFormat, time);
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
        habitat_detail_localdatetime.setText(mDateFormat.format(time));

        long habitatTime = time + (mZone-defaultZone)*60000;
        habitat_detail_datetime.setText(mDateFormat.format(habitatTime));
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

    private void showTimePickerDialog(int time, final TimePickerDialog.OnTimeSetListener listener) {
        TimePickerDialog  dialog = new TimePickerDialog(getContext(), listener, time/60, time%60, GlobalSettings.is24HourFormat());
        dialog.show();
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
                AliotClient.getInstance().invite(result.data.invitee, result.data.invite_id, mGroupid, mGroup.name);
            }
        });
    }

    private void showDeleteHabitatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_habitat)
               .setMessage(getString(R.string.msg_delete, mGroup.name))
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       final HttpCallback<UserApi.Response> callback = new HttpCallback<UserApi.Response>() {
                           @Override
                           public void onError(String error) {
                               showToast(error);
                           }

                           @Override
                           public void onSuccess(UserApi.Response result) {
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
                   }
               })
               .setCancelable(false)
               .show();
    }
}
