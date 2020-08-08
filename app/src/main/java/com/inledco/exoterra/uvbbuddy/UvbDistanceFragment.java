package com.inledco.exoterra.uvbbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UvbDistanceFragment extends BaseFragment {

    private ImageView uvb_distance_animal;
    private ImageView uvb_distance_rate;
    private RecyclerView uvb_distance_rv;
    private Button uvb_distance_back;
    private Button uvb_distance_next;

    private DistanceAdapter mAdapter;

    private AnimalViewModel mAnimalViewModel;
    private Animal mAnimal;
    private List<DistanceUvbLight> mDistanceUvbLights;

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
        return R.layout.fragment_uvb_distance;
    }

    @Override
    protected void initView(View view) {
        uvb_distance_animal = view.findViewById(R.id.uvb_distance_animal);
        uvb_distance_rate = view.findViewById(R.id.uvb_distance_rate);
        uvb_distance_rv = view.findViewById(R.id.uvb_distance_rv);
        uvb_distance_back = view.findViewById(R.id.uvb_distance_back);
        uvb_distance_next = view.findViewById(R.id.uvb_distance_next);

        uvb_distance_rv.setLayoutManager(new GridLayoutManager(getContext(), 5));
    }

    @Override
    protected void initData() {
        int[] rateres = new int[] {R.drawable.ic_ratestar_0,
                                   R.drawable.ic_ratestar_1,
                                   R.drawable.ic_ratestar_2,
                                   R.drawable.ic_ratestar_3,
                                   R.drawable.ic_ratestar_4,
                                   R.drawable.ic_ratestar_5};
        mAnimalViewModel = ViewModelProviders.of(getActivity()).get(AnimalViewModel.class);
        mAnimal = mAnimalViewModel.getData();
        if (mAnimal != null) {
            try {
                InputStream stream = getContext().getAssets().open("animals/" + mAnimal.getIcon());
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                uvb_distance_animal.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int rate = mAnimal.getRate();
            if (rate >= 0 && rate < rateres.length) {
                uvb_distance_rate.setImageResource(rateres[rate]);
            }
            mDistanceUvbLights = UvbDatabase.getDistanceUvbLights(mAnimal);

            mAdapter = new DistanceAdapter(getContext(), mDistanceUvbLights);
            uvb_distance_rv.setAdapter(mAdapter);
        }
    }

    @Override
    protected void initEvent() {
        uvb_distance_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        uvb_distance_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedItem() == null) {
                    showToast(R.string.msg_choose_distance);
                    return;
                }
                String distance = mAdapter.getSelectedItem().getDistance();
                addFragmentToStack(R.id.uvbmain_root, UvbLightFragment.newInstance(distance));
            }
        });
    }
}
