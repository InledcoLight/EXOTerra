package com.inledco.exoterra.group;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.inledco.exoterra.AppConstants;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.Home;
import com.inledco.exoterra.bean.Home2;
import com.inledco.exoterra.manager.HomeManager;
import com.inledco.exoterra.xlink.XlinkCloudManager;
import com.inledco.exoterra.xlink.XlinkRequestCallback;

import cn.xlink.restful.XLinkRestfulEnum;
import cn.xlink.restful.api.app.HomeApi;
import cn.xlink.sdk.v5.manager.XLinkUserManager;

public class HabitatMemberFragment extends BaseFragment {
    private ImageView home_member_icon;
    private TextView home_member_name;
    private TextView home_member_usrid;
    private TextView home_member_email;
    private TextView home_member_role;
    private Button home_member_delete;
    private Button home_member_back;

    private XLinkRestfulEnum.HomeUserType mHomeRole = XLinkRestfulEnum.HomeUserType.USER;
    private XLinkRestfulEnum.HomeUserType mUserRole = XLinkRestfulEnum.HomeUserType.USER;
    private String mHomeId;
    private int mUserId;
    private XLinkRestfulEnum.HomeUserType selectRole;


    public static HabitatMemberFragment newInstance(final String homeid, final int userid) {
        Bundle args = new Bundle();
        args.putString(AppConstants.HOME_ID, homeid);
        args.putInt(AppConstants.USER_ID, userid);
        HabitatMemberFragment fragment = new HabitatMemberFragment();
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
    protected int getLayoutRes() {
        return R.layout.fragment_home_member;
    }

    @Override
    protected void initView(View view) {
        home_member_icon = view.findViewById(R.id.home_member_icon);
        home_member_name = view.findViewById(R.id.home_member_name);
        home_member_usrid = view.findViewById(R.id.home_member_usrid);
        home_member_email = view.findViewById(R.id.home_member_email);
        home_member_role = view.findViewById(R.id.home_member_role);
        home_member_delete = view.findViewById(R.id.home_member_delete);
        home_member_back = view.findViewById(R.id.home_member_back);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mHomeId = args.getString(AppConstants.HOME_ID);
            mUserId = args.getInt(AppConstants.USER_ID);
            home_member_usrid.setText("" + mUserId);
            final Home home = HomeManager.getInstance().getHome(mHomeId);
            if (home != null) {
                for (HomeApi.HomesResponse.Home.User usr : home.getHome().userList) {
                    if (usr.userId == XLinkUserManager.getInstance().getUid()) {
                        mHomeRole = usr.role;
                    }
                }

                for (HomeApi.HomesResponse.Home.User usr : home.getHome().userList) {
                    if (usr.userId == mUserId) {
                        mUserRole = usr.role;
                        refreshData();
                    }
                }
            }
            getInfo();
        }
    }

    @Override
    protected void initEvent() {
        home_member_role.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN) {
                    if (mUserRole.getValue() > mHomeRole.getValue()) {
                        showSelectableRolesDialog();
                    }
                } else if (mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
                    if (mUserRole.getValue() > mHomeRole.getValue()) {
                        showSelectableRolesDialog();
                    }
                }
            }
        });

        home_member_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        home_member_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.delete_user)
                       .setNegativeButton(R.string.cancel, null)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               deleteUser();
                           }
                       })
                       .setCancelable(false)
                       .show();
            }
        });
    }

    private void getInfo() {
        XlinkCloudManager.getInstance().getHomeInfo(mHomeId, new XlinkRequestCallback<Home2>() {
            @Override
            public void onError(String error) {

            }

            @Override
            public void onSuccess(Home2 home2) {
                for (Home2.User user : home2.user_list) {
                    if (mUserId == user.user_id) {
                        String nickname = user.nickname;
                        if (TextUtils.isEmpty(nickname)) {
                            nickname = "" + mUserId;
                        }
                        home_member_email.setText(user.email);
                        home_member_name.setText(nickname);
                        break;
                    }
                }
            }
        });
    }

    private void deleteUser() {
        XlinkCloudManager.getInstance().deleteHomeUser(mHomeId, mUserId, new XlinkRequestCallback<String>() {
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                     .show();
            }

            @Override
            public void onSuccess(String s) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void showSelectableRolesDialog() {
        final String[] roles = new String[] {
            getString(R.string.admin),
            getString(R.string.user),
            getString(R.string.visitor)
        };
        final int idx = mUserRole.getValue() - 2;
        selectRole = mUserRole;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.change_role)
               .setSingleChoiceItems(roles, idx, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       selectRole = XLinkRestfulEnum.HomeUserType.values()[which+1];
                   }
               })
               .setNegativeButton(R.string.cancel, null)
               .setPositiveButton(R.string.ok, null)
               .setCancelable(false);
        final AlertDialog dialog = builder.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectRole == mUserRole) {
                    dialog.dismiss();
                    return;
                }
                XlinkCloudManager.getInstance().setHomeRole(mHomeId, mUserId, selectRole, new XlinkRequestCallback<String>() {
                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
                             .show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        HomeManager.getInstance().refreshHomeList(null);
                        mUserRole = selectRole;
                        refreshData();
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private void refreshData() {
        switch (mUserRole) {
            case SUPER_ADMIN:
                home_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
                home_member_role.setText(R.string.creator);
                break;
            case ADMIN:
                home_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
                home_member_role.setText(R.string.admin);
                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN) {
                    home_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                } else {
                    home_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                break;
            case USER:
                home_member_icon.setImageResource(R.drawable.ic_person_white_64dp);
                home_member_role.setText(R.string.user);
                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN || mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
                    home_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                }
                break;
            case VISITOR:
                home_member_icon.setImageResource(R.drawable.ic_person_white_64dp);
                home_member_role.setText(R.string.visitor);
                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN || mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
                    home_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
                }
                break;
        }
    }
}
