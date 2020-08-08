package com.inledco.exoterra.uvbbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class UvbLightFragment extends BaseFragment {

    private CheckedTextView uvb_light_reptile;
    private CheckedTextView uvb_light_linear;
    private CheckedTextView uvb_light_sunray;
    private CheckedTextView uvb_light_solarglo;
    private CheckedTextView uvb_light_solarray;
    private CheckedTextView uvb_light_turtle;
    private List<CheckedTextView> uvb_lights;
    private Button uvb_light_back;
    private Button uvb_light_next;

    private CheckedTextView selected_light;

    private String mDistance;
    private AnimalViewModel mAnimalViewModel;
    private Animal mAnimal;

    public static UvbLightFragment newInstance(String distance) {
        Bundle args = new Bundle();
        args.putString("distance", distance);
        UvbLightFragment fragment = new UvbLightFragment();
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
        return R.layout.fragment_uvb_light;
    }

    @Override
    protected void initView(View view) {
        uvb_lights = new ArrayList<>();
        uvb_light_reptile = view.findViewById(R.id.uvb_light_reptile);
        uvb_light_linear = view.findViewById(R.id.uvb_light_linear);
        uvb_light_sunray = view.findViewById(R.id.uvb_light_sunray);
        uvb_light_solarglo = view.findViewById(R.id.uvb_light_solarglo);
        uvb_light_solarray = view.findViewById(R.id.uvb_light_solarray);
        uvb_light_turtle = view.findViewById(R.id.uvb_light_turtle);
        uvb_lights.add(uvb_light_reptile);
        uvb_lights.add(uvb_light_linear);
        uvb_lights.add(uvb_light_sunray);
        uvb_lights.add(uvb_light_solarglo);
        uvb_lights.add(uvb_light_solarray);
        uvb_lights.add(uvb_light_turtle);
        uvb_light_back = view.findViewById(R.id.uvb_light_back);
        uvb_light_next = view.findViewById(R.id.uvb_light_next);
    }

    @Override
    protected void initData() {
        Bundle args = getArguments();
        if (args != null) {
            mDistance = args.getString("distance");
            mAnimalViewModel = ViewModelProviders.of(getActivity()).get(AnimalViewModel.class);
            mAnimal = mAnimalViewModel.getData();
            if (mAnimal != null && mDistance != null) {
                List<DistanceUvbLight> distanceUvbLights = UvbDatabase.getDistanceUvbLights(mAnimal);
                for (DistanceUvbLight dul : distanceUvbLights) {
                    if (mDistance.equals(dul.getDistance())) {
                        for (CheckedTextView ctv : uvb_lights) {
                            ctv.setEnabled(false);
                            String type = ctv.getText().toString().toLowerCase();
                            for (String light : dul.getUvbLights()) {
                                if (light.startsWith("(*)")) {
                                    light = light.substring("(*)".length());
                                }
                                if (light.toLowerCase().startsWith(type)) {
                                    ctv.setEnabled(true);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void initEvent() {
        for (int i = 0; i < uvb_lights.size(); i++) {
            int[] attrs = new int[] {android.R.attr.selectableItemBackground};
            TypedArray ta = getContext().obtainStyledAttributes(attrs);
            final Drawable bg = ta.getDrawable(0);
            final CheckedTextView ctv = uvb_lights.get(i);
            ctv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ctv.isChecked() == false) {
                        if (selected_light != null) {
                            selected_light.setChecked(false);
                            selected_light.setBackground(bg);
                        }
                        ctv.setChecked(true);
                        ctv.setBackgroundResource(R.drawable.shape_roundrect_gradient);
                        selected_light = ctv;
                    }
                }
            });
        }
        uvb_light_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        uvb_light_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_light == null) {
                    showToast(R.string.msg_choose_light);
                    return;
                }
                addFragmentToStack(R.id.uvbmain_root, new UvbResultFragment());
            }
        });
    }
}
