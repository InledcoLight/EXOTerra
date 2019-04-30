package com.liruya.exoterra.device;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.liruya.exoterra.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TimeZoneFragment extends DialogFragment {
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_time_zone, null, false);
//        ListView timezone_lv = view.findViewById(R.id.timezone_lv);
////        timezone_lv.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_timezone, R.id.item_timezone_name, TimeZone.getAvailableIDs()));
//        List<Map<String, Object>> zoneList = new ArrayList<>();
//        String[] names = TimeZone.getAvailableIDs();
//        for (String name : names) {
//            Map<String, Object> zoneMap = new HashMap<>();
//            zoneMap.put("name", name);
//            zoneMap.put("offset", TimeZone.getTimeZone(name).getRawOffset()/1000);
//            zoneList.add(zoneMap);
//        }
//        timezone_lv.setAdapter(new SimpleAdapter(getContext(), zoneList, R.layout.item_timezone, new String[] {"name", "offset"}, new int[]{R.id.item_timezone_name,
//                                                                                                                                            R.id.item_timezone_offset}));
//        builder.setView(view);
//        return builder.create();
////        return super.onCreateDialog(savedInstanceState);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_time_zone, container, false);
        Toolbar toolbar = view.findViewById(R.id.timezone_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        ListView timezone_lv = view.findViewById(R.id.timezone_lv);
        List<Map<String, Object>> zoneList = new ArrayList<>();
        String[] names = TimeZone.getAvailableIDs();
        for (String name : names) {
            Map<String, Object> zoneMap = new HashMap<>();
            int offset = TimeZone.getTimeZone(name).getRawOffset()/60000;   //minute
            DecimalFormat df = new DecimalFormat("00");
            String s = "GMT+";
            int a = offset;
            if (offset<0) {
                s = "GMT-";
                a = -offset;
            }
            s = s + df.format(a/60) + ":" + df.format(a%60);
            zoneMap.put("name", name);
            zoneMap.put("desc", s );
            zoneMap.put("offset", offset);
            zoneList.add(zoneMap);
        }
        Collections.sort(zoneList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int v1 = (int) o1.get("offset");
                int v2 = (int) o2.get("offset");
                return v1 - v2;
            }
        });
        timezone_lv.setAdapter(new SimpleAdapter(getContext(), zoneList, R.layout.item_timezone, new String[] {"name", "desc"}, new int[]{R.id.item_timezone_name,
                                                                                                                                            R.id.item_timezone_offset}));
        timezone_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }
}
