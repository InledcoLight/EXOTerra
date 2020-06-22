package com.inledco.exoterra.device.socket;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.inledco.exoterra.R;
import com.inledco.exoterra.aliot.AliotServer;
import com.inledco.exoterra.aliot.ExoSocket;
import com.inledco.exoterra.aliot.HttpCallback;
import com.inledco.exoterra.aliot.SocketViewModel;
import com.inledco.exoterra.aliot.UserApi;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.util.SensorUtil;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class SensorHistoryFragment extends BaseFragment {
    private Toolbar sensor_history_toolbar;
    private LineChart sensor_history_chart;
    private TextView sensor_history_selected;
    private TextView sensor_history_time;

    private SocketViewModel mSocketViewModel;
    private ExoSocket mSocket;

    private long startTime;
    private long endTime;

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
        return R.layout.fragment_sensor_history;
    }

    @Override
    protected void initView(View view) {
        sensor_history_toolbar = view.findViewById(R.id.sensor_history_toolbar);
        sensor_history_chart = view.findViewById(R.id.sensor_history_chart);
        sensor_history_selected = view.findViewById(R.id.sensor_history_selected);
        sensor_history_time = view.findViewById(R.id.sensor_history_time);

        XAxis xAxis = sensor_history_chart.getXAxis();
        YAxis axisLeft = sensor_history_chart.getAxisLeft();
        YAxis axisRight = sensor_history_chart.getAxisRight();
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setEnabled(true);
        xAxis.setLabelCount(5, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
//                DateFormat df = new SimpleDateFormat("HH:mm");
//                long time = (long) value;
//                return df.format(new Date(time));
                DecimalFormat df = new DecimalFormat("00");
                long offset = TimeZone.getDefault().getRawOffset() / 1000;
                int time = ((int) (value + offset)) % 86400 / 60;
                return df.format(time/60) + ":" + df.format(time%60);
            }
        });
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat df = new DecimalFormat("##0");
                return df.format(value/10);
            }
        };
        axisLeft.setAxisMaximum(1000);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(5, true);
        axisLeft.setValueFormatter(formatter);
        axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setDrawGridLines(true);
        axisLeft.setGridColor(0xFF9E9E9E);
        axisLeft.setGridLineWidth(0.75f);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisLineColor(Color.BLACK);
        axisLeft.setGranularity(1);
        axisLeft.setGranularityEnabled(true);
        axisLeft.setSpaceTop(0);
        axisLeft.setSpaceBottom(0);
        axisLeft.setEnabled(true);

        axisRight.setAxisMaximum(1000);
        axisRight.setAxisMinimum(0);
        axisRight.setLabelCount(5, true);
        axisRight.setValueFormatter(formatter);
        axisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRight.setTextColor(Color.WHITE);
        axisRight.setDrawGridLines(true);
        axisRight.setGridColor(0xFF9E9E9E);
        axisRight.setGridLineWidth(0.75f);
        axisRight.setDrawAxisLine(false);
        axisRight.setAxisLineColor(Color.BLACK);
        axisRight.setGranularity(1);
        axisRight.setGranularityEnabled(true);
        axisRight.setSpaceTop(0);
        axisRight.setSpaceBottom(0);
        axisRight.setEnabled(true);
        sensor_history_chart.setTouchEnabled(true);
        sensor_history_chart.setDragEnabled(true);
        sensor_history_chart.setScaleXEnabled(true);
        sensor_history_chart.setScaleYEnabled(false);
        sensor_history_chart.setPinchZoom(false);
        sensor_history_chart.setDoubleTapToZoomEnabled(false);
//        sensor_history_chart.setBorderColor(Color.CYAN);
//        sensor_history_chart.setBorderWidth(1);
        sensor_history_chart.setDrawBorders(false);
        sensor_history_chart.setDrawGridBackground(true);
        sensor_history_chart.setGridBackgroundColor(Color.TRANSPARENT);
        sensor_history_chart.setDescription(null);
        sensor_history_chart.setMaxVisibleValueCount(0);
        sensor_history_chart.getLegend().setTextColor(Color.WHITE);
        sensor_history_chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

//        sensor_history_chart.setMarker(new MyMarkerView(getContext(), R.layout.custom_marker_view));
        sensor_history_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                List<ILineDataSet> dataSets = sensor_history_chart.getData().getDataSets();
                for (int i = 0; i < dataSets.size(); i++) {
                    ILineDataSet dataSet = dataSets.get(i);
                    for (int j = 0; j < dataSet.getEntryCount(); j++) {
                        if (e.equalTo(dataSet.getEntryForIndex(j))) {
                            int start = (int) ((startTime / 1000) % 86400);
//                            long time = (sensor.time-startTime)/1000 + start;
                            long time = (long) ((e.getX() - start) * 1000 + startTime);
                            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            sensor_history_selected.setTextColor(dataSet.getColor());
                            sensor_history_selected.setText("" + e.getY()/10 + " " + SensorUtil.getSensorUnit(dataSet.getLabel()));
                            sensor_history_time.setText(df.format(time));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                sensor_history_selected.setText(null);
                sensor_history_time.setText(null);
            }
        });
    }

    @Override
    protected void initData() {
        mSocketViewModel = ViewModelProviders.of(getActivity()).get(SocketViewModel.class);
        mSocket = mSocketViewModel.getData();
        if (mSocket == null) {
            return;
        }

        getSensorHistory(24*60*60*1000);
    }

    @Override
    protected void initEvent() {
        sensor_history_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private List<Sensor[]> parseSensors(List<UserApi.KeyValue> values) {
        if (values == null) {
            return null;
        }
        List<Sensor[]> list = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            long time = Long.parseLong(values.get(i).Time);
            try {
                JSONArray ja = JSONArray.parseArray(values.get(i).Value);
                if (ja == null) {
                    return null;
                }
                ExoSocket.Sensor[] result = new ExoSocket.Sensor[ja.size()];
                Sensor[] sensors = new Sensor[ja.size()];
                for (int j = 0; j < ja.size(); j++) {
                    result[j] = ja.getObject(j, ExoSocket.Sensor.class);
                    sensors[j] = new Sensor();
                    sensors[j].time = time;
                    sensors[j].type = result[j].getType();
                    sensors[j].value = result[j].getValue();
                }
                list.add(sensors);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return list;
    }

    private void refreshData(List<Sensor[]> sensors) {
        if (sensors == null || sensors.size() == 0 || sensors.get(0) == null) {
            return;
        }
        long period = (endTime - startTime)/1000;
        int start = (int) ((startTime / 1000) % 86400);
        int end = (int) (start + period);

        int cnt = sensors.get(0).length;
        List<ILineDataSet> dataSets = new ArrayList<>();
        List<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
            int type = sensors.get(0)[i].type;
            int color = SensorUtil.getSensorColor(type);
            String name = SensorUtil.getSensorName(type);
            List<Entry> entries = new ArrayList<>();

//            for (int j = 0; j < sensors.size(); j++) {
//                Sensor sensor = sensors.get(j)[i];
//                long time = (sensor.time-startTime)/1000 + start;
//                entries.add(new Entry(time, sensor.value));
//            }
//            LineDataSet lineDataSet = new LineDataSet(entries, name);
//            lineDataSet.setFillFormatter(new DefaultFillFormatter());
//            lineDataSet.setDrawFilled(true);
//            lineDataSet.setFillColor(color);
//            lineDataSet.setFillAlpha(100);
//            lineDataSet.setColor(color);
//            lineDataSet.setCircleRadius(1);
//            lineDataSet.setCircleColor(0);
//            lineDataSet.setDrawCircleHole(false);
//            lineDataSet.setLineWidth(2.0f);
//            lineDataSet.setHighLightColor(color);
//            lineDataSet.enableDashedHighlightLine(10, 5, 0);
//            lineDataSet.setMode(LineDataSet.Mode.STEPPED);
//            dataSets.add(lineDataSet);

            long last = 0;
            int j = 0;
            while (j < sensors.size()) {
                Sensor sensor = sensors.get(j)[i];
                long time = (sensor.time-startTime)/1000 + start;
                if (time - last > 900 && entries.size() > 0) {
                    List<Entry> entryList = new ArrayList<>(entries);
                    LineDataSet lineDataSet = new LineDataSet(entryList, name);
                    lineDataSet.setFillFormatter(new DefaultFillFormatter());
                    lineDataSet.setDrawFilled(true);
                    lineDataSet.setFillColor(color);
                    lineDataSet.setFillAlpha(100);
                    lineDataSet.setColor(color);
                    lineDataSet.setCircleRadius(1);
                    lineDataSet.setCircleColor(0);
                    lineDataSet.setDrawCircleHole(false);
                    lineDataSet.setLineWidth(2.0f);
                    lineDataSet.setHighLightColor(color);
                    lineDataSet.enableDashedHighlightLine(10, 5, 0);
                    lineDataSet.setMode(LineDataSet.Mode.STEPPED);
                    dataSets.add(lineDataSet);
                    entries.clear();
                }
                entries.add(new Entry(time, sensor.value));
                last = time;
                j++;
            }
            LineDataSet lineDataSet = new LineDataSet(entries, name);
            lineDataSet.setCubicIntensity(0.5f);
            lineDataSet.setFillFormatter(new DefaultFillFormatter());
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillColor(color);
            lineDataSet.setFillAlpha(100);
            lineDataSet.setColor(color);
            lineDataSet.setCircleRadius(1);
            lineDataSet.setCircleColor(0);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setLineWidth(2.0f);
            lineDataSet.setHighLightColor(color);
            lineDataSet.enableDashedHighlightLine(10, 5, 0);
            lineDataSet.setMode(LineDataSet.Mode.STEPPED);
            dataSets.add(lineDataSet);

            LegendEntry le = new LegendEntry();
            le.label = name;
            le.form = Legend.LegendForm.SQUARE;
            le.formColor = color;
            legendEntries.add(le);
        }

        LineData lineData = new LineData(dataSets);
        sensor_history_chart.setData(lineData);
        XAxis xAxis = sensor_history_chart.getXAxis();
        xAxis.setAxisMinimum(start);
        xAxis.setAxisMaximum(end);
        sensor_history_chart.getLegend().setEntries(legendEntries);
        sensor_history_chart.invalidate();
    }

    private void getSensorHistory(final long period) {
        endTime = System.currentTimeMillis();
        endTime = endTime - endTime%1800000 + 1800000;
        startTime = endTime - period;
        final List<UserApi.KeyValue> totalValues = new ArrayList<>();
        final String pkey = mSocket.getProductKey();
        final String dname = mSocket.getDeviceName();
        final UserApi.DeviceHistoryPropertiesRequest request = new UserApi.DeviceHistoryPropertiesRequest();
        request.identifiers = new String[] {"Sensor"};
        request.asc = 1;
        request.pageSize = 100;
        request.startTime = startTime;
        request.endTime = endTime;
        final HttpCallback<UserApi.DeviceHistoryPropertiesResponse> callback = new HttpCallback<UserApi.DeviceHistoryPropertiesResponse>() {
            @Override
            public void onError(String error) {
                dismissLoadDialog();
                showToast(error);
            }

            @Override
            public void onSuccess(UserApi.DeviceHistoryPropertiesResponse result) {
                if (result.data.size() == 0) {
                    Log.e(TAG, "onSuccess: ");
                    dismissLoadDialog();
                    return;
                }
                UserApi.PropertyDataInfo info = result.data.get(0);
                List<UserApi.KeyValue> values = info.list;
                totalValues.addAll(values);
                if (values.size() < request.pageSize) {
                    dismissLoadDialog();
                    refreshData(parseSensors(totalValues));
                } else {
                    request.startTime = Long.parseLong(values.get(values.size()-1).Time) + 1;
                    AliotServer.getInstance().queryDeviceHistoryProperties(pkey, dname, request, this);
                }
            }
        };
        AliotServer.getInstance().queryDeviceHistoryProperties(pkey, dname, request, callback);
        showLoadDialog();
    }
}
