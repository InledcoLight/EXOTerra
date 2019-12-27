package com.inledco.exoterra.group;

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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.event.HomeChangedEvent;
import com.inledco.exoterra.event.HomesRefreshedEvent;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HabitatDetailFragment extends BaseFragment {

    private Toolbar habitat_detail_toolbar;
    private TextView habitat_detail_name;
    private TextView habitat_detail_localdatetime;
    private TextView habitat_detail_datetime;
    private TextView habitat_detail_sunrise;
    private TextView habitat_detail_sunset;
    private Switch habitat_detail_favourite;
    private TextView habitat_detail_share;
    private Button habitat_detail_delete;

    private static final String KEY_HOMEID = "homeid";
    private String mHomeid;
    private Home mHome;

    private int defaultZone = TimeZone.getDefault().getRawOffset()/60000;
    private int mZone = defaultZone;
    private int mSunrise = 360;
    private int mSunset = 1080;

    private BroadcastReceiver mTimeReceiver;

    private boolean mHomeAdmin;

    public static HabitatDetailFragment newInstance(@NonNull final String homeid) {
        Bundle args = new Bundle();
        HabitatDetailFragment fragment = new HabitatDetailFragment();
        args.putString(KEY_HOMEID, homeid);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initData();
        initEvent();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        habitat_detail_toolbar = view.findViewById(R.id.habitat_detail_toolbar);
        habitat_detail_name = view.findViewById(R.id.habitat_detail_name);
        habitat_detail_localdatetime = view.findViewById(R.id.habitat_detail_localdatetime);
        habitat_detail_datetime = view.findViewById(R.id.habitat_detail_datetime);
        habitat_detail_sunrise = view.findViewById(R.id.habitat_detail_sunrise);
        habitat_detail_sunset = view.findViewById(R.id.habitat_detail_sunset);
        habitat_detail_favourite = view.findViewById(R.id.habitat_detail_favourite);
        habitat_detail_share = view.findViewById(R.id.habitat_detail_share);
        habitat_detail_delete = view.findViewById(R.id.habitat_detail_delete);

        habitat_detail_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        habitat_detail_datetime.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        habitat_detail_sunrise.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        habitat_detail_sunset.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
        habitat_detail_share.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_share_white_24dp, 0);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mHomeid = args.getString(KEY_HOMEID);
            mHome = HomeManager.getInstance().getHome(mHomeid);
            if (mHome != null) {
                mTimeReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (intent.getAction()) {
                            case Intent.ACTION_TIME_TICK:
                                refreshTime();
                                break;
                        }
                    }
                };
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
                getActivity().registerReceiver(mTimeReceiver, filter);

                for (HomeApi.HomesResponse.Home.User usr : mHome.getHome().userList) {
                    if (usr.userId == XLinkUserManager.getInstance().getUid()) {
                        if (usr.role == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN || usr.role == XLinkRestfulEnum.HomeUserType.ADMIN) {
                            mHomeAdmin = true;
                            break;
                        }
                    }
                }

                refreshData();
            }
        }
    }

    @Override
    protected void initEvent() {
        habitat_detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if (mHome == null) {
            return;
        }
        habitat_detail_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });

        habitat_detail_datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        habitat_detail_sunrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        habitat_detail_sunset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        habitat_detail_favourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

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

    private String getTimeText(int time) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(time/60) + ":" + df.format(time%60);
    }

    private void refreshData() {
        String name = mHome.getHome().name;
        habitat_detail_name.setText(name);
        refreshTime();
        if (mHome.getProperty() != null) {
            mSunrise = mHome.getProperty().getSunrise();
            mSunset = mHome.getProperty().getSunset();
        }
        habitat_detail_sunrise.setText(getString(R.string.sunrise_time, getTimeText(mSunrise)));
        habitat_detail_sunset.setText(getString(R.string.sunset_time, getTimeText(mSunset)));
    }

    private void refreshTime() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (mHome.getProperty() != null) {
            mZone = mHome.getProperty().getZone();
        }

        long time = System.currentTimeMillis();
        habitat_detail_localdatetime.setText(getString(R.string.local_datetime, df.format(new Date(time))));

        long habitatTime = time + (mZone-defaultZone)*60000;
        habitat_detail_datetime.setText(getString(R.string.habitat_datetime, df.format(new Date(habitatTime))));
    }

    private void showRenameDialog() {
        String name = mHome.getHome().name;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.habitat_name));
        et_name.setText(name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_device)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                XlinkCloudManager.getInstance().renameHome(mHomeid, name, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        dialog.dismiss();
                        mHome.getHome().name = name;
                        habitat_detail_name.setText(name);
                        EventBus.getDefault().post(new HomeChangedEvent(mHomeid));
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
                                          .setPositiveButton(R.string.share, null)
                                          .show();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    shareHome(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }

    private void shareHome(@NonNull final String email) {
        XlinkCloudManager.getInstance().inviteHomeMember(mHome.getHome().id, email, new XlinkRequestCallback<HomeApi.UserInviteResponse>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(HomeApi.UserInviteResponse response) {
                Toast.makeText(getContext(), "Share Habitat Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void showDeleteHabitatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_habitat)
               .setMessage(getString(R.string.msg_delete, mHome.getHome().name))
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       final XlinkRequestCallback<String> callback = new XlinkRequestCallback<String>() {
                           @Override
                           public void onError(String error) {
                               Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                                    .show();
                           }

                           @Override
                           public void onSuccess(String s) {
                               Log.e(TAG, "onSuccess: " + mHomeid);
                               HomeManager.getInstance().remove(mHomeid);
                               EventBus.getDefault().post(new HomesRefreshedEvent());
                               getActivity().getSupportFragmentManager().popBackStack(null, 1);
                           }
                       };
                       if (mHomeAdmin) {
                           XlinkCloudManager.getInstance().deleteHome(mHomeid, callback);
                       } else {
                           XlinkCloudManager.getInstance().deleteHomeUser(mHomeid, XLinkUserManager.getInstance().getUid(), callback);
                       }
                   }
               })
               .setCancelable(false)
               .show();
    }
}
