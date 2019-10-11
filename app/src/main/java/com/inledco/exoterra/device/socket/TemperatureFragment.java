package com.inledco.exoterra.device.socket;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.view.VerticalSeekBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TemperatureFragment extends BaseFragment {
    private final int TEMPERATURE_THRD_MIN = 10;
    private final int TEMPERATURE_THRD_MAX = 40;
    private LineChart temp_chart;
    private MarkerView mMarkerView;
    private Group temp_group;
    private ImageButton temp_ib_left;
    private ImageButton temp_ib_right;
    private VerticalSeekBar temp_seekbar;

    private int mSelectIndex = -1;
    private byte[] mTempArgs;

    public static TemperatureFragment newInstance(final byte[] tempArgs) {
        TemperatureFragment frag = new TemperatureFragment();
        Bundle args = new Bundle();
        args.putByteArray("month", tempArgs);
        frag.setArguments(args);
        return frag;
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
        return R.layout.fragment_temperature;
    }

    @Override
    protected void initView(View view) {
        temp_chart = view.findViewById(R.id.temp_chart);
        temp_group = view.findViewById(R.id.temp_group);
        temp_ib_left = view.findViewById(R.id.temp_ib_left);
        temp_ib_right = view.findViewById(R.id.temp_ib_right);
        temp_seekbar = view.findViewById(R.id.temp_seekbar);

        temp_seekbar.setMax(TEMPERATURE_THRD_MAX - TEMPERATURE_THRD_MIN);

        XAxis xAxis = temp_chart.getXAxis();
        YAxis axisLeft = temp_chart.getAxisLeft();
        YAxis axisRight = temp_chart.getAxisRight();
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setEnabled(true);
        xAxis.setAxisMaximum(24);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(13, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(((int) value)%24);
            }
        });

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat df = new DecimalFormat("##0");
                return df.format(value);
            }
        };
        axisLeft.setAxisMaximum(45);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(4, true);
        axisLeft.setValueFormatter(formatter);
        axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setDrawGridLines(false);
        axisLeft.setGridColor(0xFF9E9E9E);
        axisLeft.setGridLineWidth(0.75f);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisLineColor(Color.BLACK);
        axisLeft.setGranularity(1);
        axisLeft.setGranularityEnabled(true);
        axisLeft.setSpaceTop(0);
        axisLeft.setSpaceBottom(0);
        LimitLine l1 = new LimitLine(TEMPERATURE_THRD_MAX, String.valueOf(TEMPERATURE_THRD_MAX));
        LimitLine l2 = new LimitLine(TEMPERATURE_THRD_MIN, String.valueOf(TEMPERATURE_THRD_MIN));
        l1.setTextColor(l1.getLineColor());
        l2.setTextColor(l2.getLineColor());
        l1.enableDashedLine(10, 10, 0);
        l2.enableDashedLine(10, 10, 0);
        axisLeft.addLimitLine(l1);
        axisLeft.addLimitLine(l2);
        axisLeft.setEnabled(true);

//        axisRight.setAxisMaximum(45);
//        axisRight.setAxisMinimum(0);
//        axisRight.setLabelCount(4, true);
//        axisRight.setValueFormatter(formatter);
//        axisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        axisRight.setTextColor(Color.WHITE);
//        axisRight.setDrawGridLines(false);
//        axisRight.setGridColor(0xFF9E9E9E);
//        axisRight.setGridLineWidth(0.75f);
//        axisRight.setDrawAxisLine(false);
//        axisRight.setAxisLineColor(Color.BLACK);
//        axisRight.setGranularity(1);
//        axisRight.setGranularityEnabled(true);
//        axisRight.setSpaceTop(0);
//        axisRight.setSpaceBottom(0);
        axisRight.setEnabled(false);

        temp_chart.setTouchEnabled(true);
        temp_chart.setDragEnabled(false);
        temp_chart.setScaleEnabled(false);
        temp_chart.setPinchZoom(false);
        temp_chart.setDoubleTapToZoomEnabled(false);
        temp_chart.setBorderColor(Color.CYAN);
        temp_chart.setBorderWidth(1);
        temp_chart.setDrawBorders(false);
        temp_chart.setDrawGridBackground(true);
        temp_chart.setGridBackgroundColor(Color.TRANSPARENT);
        temp_chart.setDescription(null);
        temp_chart.getLegend().setTextColor(Color.WHITE);
        temp_chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        mMarkerView = new TempMarkerView(getContext());
//        mMarkerView.setChartView(temp_chart);
//        temp_chart.setMarker(mMarkerView);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mTempArgs = args.getByteArray("month");
            refreshData();
            temp_chart.animateXY(1500, 1500);
        }
    }

    @Override
    protected void initEvent() {
        temp_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int idx = (int) e.getX();
                temp_chart.getXAxis().removeAllLimitLines();
//                temp_chart.setMarker(null);
                if (idx%2 == 1) {
                    LimitLine l1 = new LimitLine(idx-1);
                    LimitLine l2 = new LimitLine(idx+1);
                    l1.setLineWidth(2);
                    l2.setLineWidth(2);
                    l1.setLineColor(getContext().getResources().getColor(R.color.colorAccent));
                    l2.setLineColor(getContext().getResources().getColor(R.color.colorAccent));
                    temp_chart.getXAxis().addLimitLine(l1);
                    temp_chart.getXAxis().addLimitLine(l2);
//                    temp_chart.setMarker(mMarkerView);
                    temp_chart.invalidate();

                    mSelectIndex = idx/2;
                    temp_seekbar.setProgress(mTempArgs[mSelectIndex] - TEMPERATURE_THRD_MIN);
                    temp_group.setVisibility(View.VISIBLE);
                } else {
                    mSelectIndex = -1;
                    temp_group.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected() {
                temp_chart.getXAxis().removeAllLimitLines();
//                temp_chart.setMarker(null);
                mSelectIndex = -1;
                temp_group.setVisibility(View.GONE);
            }
        });

        temp_ib_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectIndex > 0 && mSelectIndex < 12) {
                    mSelectIndex--;
                    temp_chart.highlightValue(mSelectIndex*2+1, 0);
                }
            }
        });

        temp_ib_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectIndex >= 0 && mSelectIndex < 11) {
                    mSelectIndex++;
                    temp_chart.highlightValue(mSelectIndex*2+1, 0);
                }
            }
        });

        temp_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mSelectIndex >= 0 && mSelectIndex < 12) {
                        mTempArgs[mSelectIndex] = (byte) (progress + TEMPERATURE_THRD_MIN);
                        refreshData();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void refreshData() {
        if (mTempArgs != null && mTempArgs.length == 12) {
            List<ILineDataSet> dataSets = new ArrayList<>();
            List<Entry> entries = new ArrayList<>();
            byte[] temps = new byte[36];
            List<Integer> colors = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                temps[i] = mTempArgs[i];
                entries.add(new Entry(2*i, temps[i]));
                entries.add(new Entry(2*i+1, temps[i]));
                entries.add(new Entry(2*i+2, temps[i]));
                colors.add(Color.TRANSPARENT);
                colors.add(Color.WHITE);
                colors.add(Color.TRANSPARENT);
            }
            LineDataSet lineDataSet = new LineDataSet(entries, getString(R.string.temperature));
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "" + ((int) value);
                }
            });
            lineDataSet.setDrawValues(true);
            lineDataSet.setValueTextColors(colors);
            lineDataSet.setValueTextSize(9f);

            lineDataSet.setColor(Color.WHITE);

            lineDataSet.setHighlightEnabled(true);
            lineDataSet.setHighLightColor(Color.TRANSPARENT);

            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setDrawCircles(false);

            lineDataSet.setLineWidth(2.0f);
            dataSets.add(lineDataSet);
            LineData lineData = new LineData(dataSets);
            temp_chart.setData(lineData);
            temp_chart.invalidate();
        }
    }

    private class TempMarkerView extends MarkerView {
        private final TextView tv_content;

        public TempMarkerView(Context context) {
            super(context, R.layout.custom_marker_view);

            tv_content = findViewById(R.id.custom_marker_tv);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tv_content.setText(Utils.formatNumber(e.getY(), 0, true));
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
