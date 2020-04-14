package com.inledco.exoterra.group;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.aliot.bean.XGroup;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;

public class HabitatMemberFragment extends BaseFragment {

    private static final String GROUPID    = "groupid";
    private static final String USERID     = "userid";

    private ImageView group_member_icon;
    private TextView group_member_name;
    private TextView group_member_usrid;
    private TextView group_member_email;
    private TextView group_member_role;
    private Button group_member_delete;
    private Button group_member_back;

//    private XLinkRestfulEnum.HomeUserType mHomeRole = XLinkRestfulEnum.HomeUserType.USER;
//    private XLinkRestfulEnum.HomeUserType mUserRole = XLinkRestfulEnum.HomeUserType.USER;
    private String mGroupid;
    private String mUserId;
//    private XLinkRestfulEnum.HomeUserType selectRole;


    public static HabitatMemberFragment newInstance(final String homeid, final String userid) {
        Bundle args = new Bundle();
        args.putString(GROUPID, homeid);
        args.putString(USERID, userid);
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
        group_member_icon = view.findViewById(R.id.home_member_icon);
        group_member_name = view.findViewById(R.id.home_member_name);
        group_member_usrid = view.findViewById(R.id.home_member_usrid);
        group_member_email = view.findViewById(R.id.home_member_email);
        group_member_role = view.findViewById(R.id.home_member_role);
        group_member_delete = view.findViewById(R.id.home_member_delete);
        group_member_back = view.findViewById(R.id.home_member_back);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mGroupid = args.getString(GROUPID);
            mUserId = args.getString(USERID);
            group_member_usrid.setText("" + mUserId);
            final Group group = GroupManager.getInstance().getGroup(mGroupid);
            if (group != null) {
                for (XGroup.User usr : group.users) {
                    if (usr.userid == UserManager.getInstance().getUserid()) {
//                        mHomeRole = usr.role;
                    }
                }

                for (XGroup.User usr : group.users) {
                    if (usr.userid == mUserId) {
//                        mUserRole = usr.role;
                        refreshData();
                    }
                }
            }
            getInfo();
        }
    }

    @Override
    protected void initEvent() {
//        group_member_role.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN) {
//                    if (mUserRole.getValue() > mHomeRole.getValue()) {
//                        showSelectableRolesDialog();
//                    }
//                } else if (mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
//                    if (mUserRole.getValue() > mHomeRole.getValue()) {
//                        showSelectableRolesDialog();
//                    }
//                }
//            }
//        });

        group_member_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        group_member_delete.setOnClickListener(new View.OnClickListener() {
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
//        XlinkCloudManager.getInstance().getHomeInfo(mGroupid, new XlinkRequestCallback<Home2>() {
//            @Override
//            public void onError(String error) {
//
//            }
//
//            @Override
//            public void onSuccess(Home2 home2) {
//                for (Home2.User user : home2.user_list) {
//                    if (mUserId == user.user_id) {
//                        String nickname = user.nickname;
//                        if (TextUtils.isEmpty(nickname)) {
//                            nickname = "" + mUserId;
//                        }
//                        group_member_email.setText(user.email);
//                        group_member_name.setText(nickname);
//                        break;
//                    }
//                }
//            }
//        });
    }

    private void deleteUser() {
//        XlinkCloudManager.getInstance().deleteHomeUser(mGroupid, mUserId, new XlinkRequestCallback<String>() {
//            @Override
//            public void onError(String error) {
//                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT)
//                     .show();
//            }
//
//            @Override
//            public void onSuccess(String s) {
//                getActivity().getSupportFragmentManager().popBackStack();
//            }
//        });
    }

    private void refreshData() {
//        switch (mUserRole) {
//            case SUPER_ADMIN:
//                group_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
//                group_member_role.setText(R.string.creator);
//                break;
//            case ADMIN:
//                group_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
//                group_member_role.setText(R.string.admin);
//                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN) {
//                    group_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
//                } else {
//                    group_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//                }
//                break;
//            case USER:
//                group_member_icon.setImageResource(R.drawable.ic_person_white_64dp);
//                group_member_role.setText(R.string.user);
//                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN || mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
//                    group_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
//                }
//                break;
//            case VISITOR:
//                group_member_icon.setImageResource(R.drawable.ic_person_white_64dp);
//                group_member_role.setText(R.string.visitor);
//                if (mHomeRole == XLinkRestfulEnum.HomeUserType.SUPER_ADMIN || mHomeRole == XLinkRestfulEnum.HomeUserType.ADMIN) {
//                    group_member_role.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_white_24dp, 0);
//                }
//                break;
//        }
    }
}
