package com.inledco.exoterra.main.homes;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.common.OnItemClickListener;
import com.inledco.exoterra.event.HomeMemberChangedEvent;
import com.inledco.exoterra.manager.Home2Manager;
import com.inledco.exoterra.util.RegexUtil;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HomeDetailFragment extends BaseFragment {

    private Toolbar home_detail_toolbar;
    private TextView home_detail_name;
    private LinearLayout home_detail_ll_name;
    private TextView home_detail_devcnt;
    private TextView home_detail_zonecnt;
    private RecyclerView home_detail_rv;
    private ImageButton home_detail_add_member;
    private Button home_detail_delete;

    private String mHomeId;
    private Home2 mHome2;
    private boolean mIsHomeAdmin;
    private final List<Home2.User> mUsers = new ArrayList<>();
    private HomeMembersAdapter mAdapter;

    public static HomeDetailFragment newInstance(final String homeid) {
        Bundle args = new Bundle();
        args.putString(AppConstants.HOME_ID, homeid);
        HomeDetailFragment fragment = new HomeDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= super.onCreateView(inflater, container, savedInstanceState);

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
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_home_detail;
    }

    @Override
    protected void initView(View view) {
        home_detail_toolbar = view.findViewById(R.id.home_detail_toolbar);
        home_detail_ll_name = view.findViewById(R.id.home_detail_ll_name);
        home_detail_name = view.findViewById(R.id.home_detail_name);
        home_detail_devcnt = view.findViewById(R.id.home_detail_devcnt);
        home_detail_zonecnt = view.findViewById(R.id.home_detail_zonecnt);
        home_detail_rv = view.findViewById(R.id.home_detail_rv);
        home_detail_add_member = view.findViewById(R.id.home_detail_add_member);
        home_detail_delete = view.findViewById(R.id.home_detail_delete);

        home_detail_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mHomeId = args.getString(AppConstants.HOME_ID);
        }

        mAdapter = new HomeMembersAdapter(getContext(), mUsers);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Home2.User user = mUsers.get(position);
                addFragmentToStack(R.id.main_fl, HomeMemberFragment.newInstance(mIsHomeAdmin, mHomeId, user.user_id, user.role, user.nickname, user.email));
            }
        });
        home_detail_rv.setAdapter(mAdapter);

        getHomeInfo();
    }

    @Override
    protected void initEvent() {
        home_detail_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        home_detail_ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsHomeAdmin) {
                    showRenameDialog();
                }
            }
        });
        home_detail_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInviteMemberDialog();
            }
        });

        home_detail_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentHomeId = Home2Manager.getInstance().getCurrentHomeId();
                if (TextUtils.equals(currentHomeId, mHomeId)) {
                    Toast.makeText(getContext(), R.string.error_cant_delete_current_home, Toast.LENGTH_SHORT)
                         .show();
                    return;
                }
                showDeleteHomeDialog();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeMemberChangedEvent(HomeMemberChangedEvent event) {
        if (event != null && TextUtils.equals(event.getHomeId(), mHomeId)) {
            getHomeInfo();
        }
    }

    private void getHomeInfo() {
        XlinkCloudManager.getInstance().getHomeInfo(mHomeId, new XlinkRequestCallback<Home2>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(Home2 home2) {
                mHome2 = home2;
                refreshData();
            }
        });
    }

    private void refreshData() {
        home_detail_toolbar.setTitle(mHome2.name);
        home_detail_name.setText(mHome2.name);
        home_detail_devcnt.setText("" + mHome2.rooms.size() + " " + getString(R.string.devices).toLowerCase());
        home_detail_zonecnt.setText("" + mHome2.zones.size() + " " + getString(R.string.habitats).toLowerCase());
        mUsers.clear();
        mUsers.addAll(mHome2.user_list);
        mAdapter.notifyDataSetChanged();

        int suAdmin = XLinkRestfulEnum.HomeUserType.SUPER_ADMIN.getValue();
        int admin = XLinkRestfulEnum.HomeUserType.ADMIN.getValue();
        boolean isHomeAdmin = false;
        for (Home2.User usr : mUsers) {
            if (usr.user_id == XLinkUserManager.getInstance().getUid()) {
                if (usr.role == suAdmin || usr.role == admin) {
                    isHomeAdmin = true;
                    break;
                }
            }
        }
        mIsHomeAdmin = isHomeAdmin;
        home_detail_add_member.setVisibility(mIsHomeAdmin ? View.VISIBLE : View.INVISIBLE);
    }
    private void showRenameDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_rename_til);
        final TextInputEditText et_name = view.findViewById(R.id.dialog_rename_et);
        til.setHint(getString(R.string.home_name));
        et_name.setText(home_detail_name.getText());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.rename_home)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.ok, null)
                                          .setView(view)
                                          .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    et_name.setError(getString(R.string.input_empty));
                    return;
                }
                XlinkCloudManager.getInstance().renameHome(mHomeId, name, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String response) {
                        getHomeInfo();
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private void showInviteMemberDialog() {
        if (TextUtils.isEmpty(mHomeId)) {
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, null, false);
        final TextInputLayout til = view.findViewById(R.id.dialog_share_til);
        final TextInputEditText et_email = view.findViewById(R.id.dialog_share_email);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog dialog = builder.setTitle(R.string.invite_member)
                                          .setView(view)
                                          .setNegativeButton(R.string.cancel, null)
                                          .setPositiveButton(R.string.invite, null)
                                          .show();
        Button btn_share = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                if (RegexUtil.isEmail(email)) {
                    inviteMember(email);
                    dialog.dismiss();
                } else {
                    til.setError(getString(R.string.error_email));
                }
            }
        });
    }

    private void inviteMember(@NonNull final String email) {
        XlinkCloudManager.getInstance().inviteHomeMember(mHomeId, email, new XlinkRequestCallback<HomeApi.UserInviteResponse>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(HomeApi.UserInviteResponse response) {
                Toast.makeText(getContext(), "Invite Success.", Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    private void showDeleteHomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_home)
               .setMessage(getString(R.string.msg_delete, mHome2.name))
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
                               getActivity().getSupportFragmentManager().popBackStack();
                           }
                       };
                       if (mIsHomeAdmin) {
                           XlinkCloudManager.getInstance().deleteHome(mHomeId, callback);
                       } else {
                           XlinkCloudManager.getInstance().deleteHomeUser(mHomeId, XLinkUserManager.getInstance().getUid(), callback);
                       }
                   }
               })
               .setCancelable(false)
               .show();
    }

}
