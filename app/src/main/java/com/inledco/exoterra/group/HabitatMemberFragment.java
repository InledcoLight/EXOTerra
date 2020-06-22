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

import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.aliot.bean.Group;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.event.GroupUserChangedEvent;
import com.inledco.exoterra.manager.GroupManager;
import com.inledco.exoterra.manager.UserManager;

import org.greenrobot.eventbus.EventBus;

public class HabitatMemberFragment extends BaseFragment {

    private static final String GROUPID    = "groupid";
    private static final String USERID     = "userid";

    private ImageView habitat_member_icon;
    private TextView habitat_member_name;
    private TextView habitat_member_usrid;
    private TextView habitat_member_email;
    private TextView habitat_member_role;
    private Button habitat_member_delete;
    private Button habitat_member_back;

    private String mGroupid;
    private String mUserId;

    private boolean userGroupAdmin;             //用户是否分组管理员
    private boolean observedGroupAdmin;        //被查看的用户是否分组管理员

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
        return R.layout.fragment_habitat_member;
    }

    @Override
    protected void initView(View view) {
        habitat_member_icon = view.findViewById(R.id.habitat_member_icon);
        habitat_member_name = view.findViewById(R.id.habitat_member_name);
        habitat_member_usrid = view.findViewById(R.id.habitat_member_usrid);
        habitat_member_email = view.findViewById(R.id.habitat_member_email);
        habitat_member_role = view.findViewById(R.id.habitat_member_role);
        habitat_member_delete = view.findViewById(R.id.habitat_member_delete);
        habitat_member_back = view.findViewById(R.id.habitat_member_back);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mGroupid = args.getString(GROUPID);
            mUserId = args.getString(USERID);
            habitat_member_usrid.setText(mUserId);
            final Group group = GroupManager.getInstance().getGroup(mGroupid);
            if (group != null) {
                userGroupAdmin = TextUtils.equals(group.creator, UserManager.getInstance().getUserid());
                observedGroupAdmin = TextUtils.equals(group.creator, mUserId);

                for (Group.User user : group.users) {
                    if (TextUtils.equals(mUserId, user.userid)) {
                        habitat_member_name.setText(user.nickname);
                        habitat_member_email.setText(user.email);
                        break;
                    }
                }
                if (observedGroupAdmin) {
                    habitat_member_icon.setImageResource(R.drawable.ic_admin_white_64dp);
                    habitat_member_role.setText(R.string.creator);
                } else {
                    habitat_member_icon.setImageResource(R.drawable.ic_person_white_64dp);
                    habitat_member_role.setText(R.string.user);
                    if (userGroupAdmin) {
                        habitat_member_delete.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    protected void initEvent() {
        habitat_member_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        habitat_member_delete.setOnClickListener(new View.OnClickListener() {
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

    private void deleteUser() {
        AliotServer.getInstance().removeUserFromGroup(mGroupid, mUserId, new HttpCallback<UserApi.Response>() {
            @Override
            public void onError(String error) {
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.Response result) {
                Group group = GroupManager.getInstance().getGroup(mGroupid);
                if (group != null) {
                    group.removeUser(mUserId);
                    EventBus.getDefault().post(new GroupUserChangedEvent(mGroupid));
                    AliotServer.getInstance().removeUser(mUserId, mGroupid, group.name);
                }
                dismissLoadDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
        showLoadDialog();
    }
}
