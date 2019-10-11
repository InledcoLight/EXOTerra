package com.inledco.exoterra.device.light;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.bean.EXOLedstrip;
import com.inledco.exoterra.bean.Profile;
import com.inledco.exoterra.bean.TimePoint;
import com.inledco.exoterra.util.LightUtil;
import com.inledco.exoterra.view.CustomTimePicker;
import com.inledco.exoterra.view.MultiPointSeekbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;

public class EditProFragment extends BaseFragment {
    private Toolbar edit_pro_toolbar;
    private LineChart edit_pro_chart;
    private MultiPointSeekbar edit_pro_mps;
    private RecyclerView edit_pro_rv;
    private ImageButton edit_pro_add;
    private ImageButton edit_pro_remove;

    private final int INTERVAL = 5;

    private final DataChangedEvent mEvent = new DataChangedEvent();

    private LightViewModel mLightViewModel;
    private EXOLedstrip mLight;
    private int mIndex;
    private Profile mProfile;
    private EditProAdapter mAdapter;

    public static EditProFragment newInstance(int index) {
        Bundle args = new Bundle();
        EditProFragment fragment = new EditProFragment();
        args.putInt("index", index);
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
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataChangedEvent(DataChangedEvent event) {
        LineChartHelper.setProfile(edit_pro_chart, mLight.getChannelCount(), mLight.getChannelNames(), mProfile);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_edit_profile;
    }

    @Override
    protected void initView(View view) {
        edit_pro_toolbar = view.findViewById(R.id.edit_profile_toolbar);
        edit_pro_chart = view.findViewById(R.id.edit_pro_chart);
        edit_pro_mps = view.findViewById(R.id.edit_pro_mps);
        edit_pro_rv = view.findViewById(R.id.edit_pro_rv);
        edit_pro_add = view.findViewById(R.id.edit_pro_add);
        edit_pro_remove = view.findViewById(R.id.edit_pro_remove);

        edit_pro_toolbar.inflateMenu(R.menu.menu_save);
        edit_pro_mps.setMaxLengthHint("00:00");
        edit_pro_mps.setGetTextImpl(new MultiPointSeekbar.GetTextImpl() {
            @Override
            public String getText(int progress) {
                DecimalFormat df = new DecimalFormat("00");
                return df.format(progress*INTERVAL/60) + ":" + df.format((progress*INTERVAL)%60);
            }
        });
        LineChartHelper.init(edit_pro_chart);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mIndex = args.getInt("index", -1);
            mLightViewModel = ViewModelProviders.of(getActivity())
                                                .get(LightViewModel.class);
            mLight = mLightViewModel.getData();
            mProfile = mLight.getProfile(mIndex);
            if (mProfile == null || mProfile.isValid() == false) {
                showImportDialog();
            }
            edit_pro_toolbar.setTitle(mLight.getProfileName(mIndex));
            init();
        }
    }

    @Override
    protected void initEvent() {
        edit_pro_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        edit_pro_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_save) {
                    mLightViewModel.setProfile(mIndex, mProfile);
                    getActivity().getSupportFragmentManager()
                                 .popBackStack();
                }
                return false;
            }
        });
        edit_pro_mps.setListener(new MultiPointSeekbar.Listener() {
            @Override
            public void onPointCountChanged(int pointCount) {

            }

            @Override
            public void onPointSelected(int index) {
                if (mProfile == null) {
                    return;
                }
                mAdapter.setBrights(mProfile.getPoints().get(index).getBrights());
            }

            @Override
            public void onStartPointTouch(int index) {

            }

            @Override
            public void onStopPointTouch(int index) {

            }

            @Override
            public void onPointProgressChanged(int index, int progress, boolean fromUser) {
                if (mProfile == null) {
                    return;
                }
                mProfile.getPoints().get(index).setTimer(progress*INTERVAL);
                EventBus.getDefault().post(mEvent);
            }
        });
        edit_pro_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeletePointDialog(edit_pro_mps.getSelectedPoint());
            }
        });
        edit_pro_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPointDialog();
            }
        });
    }

    private void init() {
        LineChartHelper.setProfile(edit_pro_chart, mLight.getChannelCount(), mLight.getChannelNames(), mProfile);
        if (mProfile != null && mProfile.isValid()) {
            edit_pro_mps.setPointCount(mProfile.getPointCount());
            List<TimePoint> points = mProfile.getPoints();
            for (int i = 0; i < points.size(); i++) {
                edit_pro_mps.setProgress(i, points.get(i).getTimer()/INTERVAL);
            }
            mAdapter = new EditProAdapter(mLight.getChannelNames(),
                                          mProfile.getPoints()
                                                  .get(0)
                                                  .getBrights());
            edit_pro_rv.setAdapter(mAdapter);
            edit_pro_mps.setSelectedPoint(0);
        }
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.imort_from_default);
        builder.setMessage(R.string.msg_import_from_default);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProfile = mLight.getProfile(0);
                init();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.show();
    }

    private int getPointIndex(final int point)
    {
        if (mProfile != null && mProfile.isValid() && point >= 0 && point < mProfile.getPointCount()) {
            int count = mProfile.getPointCount();
            int[] index = new int[count];
            int[] tmr = mProfile.getTimes();
            for ( int i = 0; i < count; i++ ) {
                index[i] = i;
            }
            for ( int i = count-1; i > 0; i-- ) {
                for ( int j = 0; j < i; j++ ) {
                    if ( tmr[index[j]] > tmr[index[j+1]] ) {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }
            for ( int i = 0; i < count; i++ ) {
                if ( index[i] == point ) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void showDeletePointDialog(final int point) {
        if (mProfile != null && mProfile.isValid() && point >= 0 && point < mProfile.getPointCount()) {
            if (mProfile.getPointCount() <= Profile.POINT_COUNT_MIN) {
                Toast.makeText(getContext(), R.string.msg_timepoints_min, Toast.LENGTH_SHORT)
                     .show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.title_delete_timepoint).replace("{index}", "" + (getPointIndex(point)+1)));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProfile.getPoints().remove(point);
                    edit_pro_mps.removePoint(point);
                    edit_pro_mps.setSelectedPoint(0);
                    LineChartHelper.setProfile(edit_pro_chart, mLight.getChannelCount(), mLight.getChannelNames(), mProfile);
                }
            });
            builder.show();
        }
    }

    private void showAddPointDialog() {
        if (mProfile != null && mProfile.isValid()) {
            if (mProfile.getPointCount() >= Profile.POINT_COUNT_MAX) {
                Toast.makeText(getContext(), R.string.msg_timepoints_max, Toast.LENGTH_SHORT)
                     .show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_time_picker, null, false);
            builder.setTitle(R.string.title_add_time);
//            final CustomTimePicker tp = view.findViewById(R.id.dialog_custom_tp);
            final CustomTimePicker tp = new CustomTimePicker(getContext());
            builder.setView(tp);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int hour = tp.getHour();
                    int minute = tp.getMinute();
                    addPoint(hour, minute);
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.show();
            final Button btn_ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_ok.setEnabled(isValidTime(tp.getHour()*60+tp.getMinute()));
            tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    int t = hourOfDay*60 + minute*tp.getInterval();
                    if (btn_ok != null) {
                        btn_ok.setEnabled(isValidTime(t));
                    }
                }
            });
        }
    }

    private boolean isValidTime(int tmr) {
        if (mProfile == null || tmr < 0 || tmr > 1439) {
            return false;
        }
        int[] timers = mProfile.getTimes();
        for (int i = 0; i < timers.length; i++) {
            if (tmr == timers[i]) {
                return false;
            }
        }
        return true;
    }

    private void addPoint(int hour, int minute) {
        if (mProfile == null || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return;
        }
        int tmr = hour*60+minute;
        TimePoint tp = new TimePoint(tmr, new byte[mLight.getChannelCount()]);
        if (tp.isValid()) {
            mProfile.getPoints().add(tp);
            int cnt = mProfile.getPointCount();
            edit_pro_mps.setPointCount(cnt);
            edit_pro_mps.setProgress(cnt-1, tmr/INTERVAL);
            edit_pro_mps.setSelectedPoint(cnt-1);
        }
    }

    class EditProAdapter extends RecyclerView.Adapter<EditproViewHolder>
    {
        private String[] mColors;
        private byte[] mBrights;

        public EditProAdapter(String[] colors, byte[] brights) {
            mColors = colors;
            mBrights = brights;
        }

        public void setBrights(byte[] brights) {
            mBrights = brights;
            notifyDataSetChanged();
        }

        public String getPercent(int percent)
        {
            if ( percent >= 0 && percent < 10 ) {
                return "  " + percent +"%";
            } else if ( percent >= 10 && percent < 100 ) {
                return " " + percent + "%";
            } else if ( percent == 100 ) {
                return "100%";
            }
            return "---%";
        }

        @NonNull
        @Override
        public EditproViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
        {
            EditproViewHolder holder = new EditproViewHolder( LayoutInflater.from( getContext() ).inflate( R.layout.item_point_bright, parent, false ) );
            return holder;
        }

        @Override
        public void onBindViewHolder( @NonNull final EditproViewHolder holder, final int position )
        {
            String name = ((mColors == null || mColors.length == 0) ? "" : mColors[position%mColors.length]);
            int color = LightUtil.getColorValue(name);

            //动态设置icon
            GradientDrawable icon = (GradientDrawable) holder.iv_icon.getDrawable();
            icon.setColor(color);
            //动态设置SeekBar progressDrawable
            holder.seekbar.setProgressDrawable(LightUtil.getProgressDrawable(getContext(), name));
            //动态设置SeekBar thumb
            GradientDrawable thumb = (GradientDrawable) holder.seekbar.getThumb();
            thumb.setColor(color);

            holder.seekbar.setProgress(mBrights[position]);
            holder.tv_percent.setText(getPercent(mBrights[position]));
            holder.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mBrights[position] = (byte) progress;
                    holder.tv_percent.setText(getPercent(progress));
                    EventBus.getDefault().post(mEvent);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return mBrights == null ? 0 : mBrights.length;
        }
    }

    class EditproViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView iv_icon;
        private SeekBar seekbar;
        private TextView tv_percent;

        public EditproViewHolder( View itemView )
        {
            super( itemView );
            iv_icon = itemView.findViewById( R.id.item_point_bright_icon );
            seekbar = itemView.findViewById( R.id.item_point_bright_seekbar );
            tv_percent = itemView.findViewById( R.id.item_point_bright_percent );
        }
    }

    class DataChangedEvent {

    }
}
