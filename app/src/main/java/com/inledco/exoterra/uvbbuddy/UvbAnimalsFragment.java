package com.inledco.exoterra.uvbbuddy;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inledco.exoterra.R;
import com.inledco.exoterra.base.BaseFragment;
import com.inledco.exoterra.common.OnItemClickListener;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class UvbAnimalsFragment extends BaseFragment {
    private RecyclerView uvb_animal_rv;

    private List<Animal> mAnimals;
    private AnimalsAdapter mAdapter;

    private AnimalViewModel mAnimalViewModel;
    private Animal mAnimal;

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
        return R.layout.fragment_uvb_animal;
    }

    @Override
    protected void initView(View view) {
        uvb_animal_rv = view.findViewById(R.id.uvb_animal_rv);
        uvb_animal_rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    @Override
    protected void initData() {
        mAnimalViewModel = ViewModelProviders.of(getActivity()).get(AnimalViewModel.class);

        mAnimals = UvbDatabase.getAnimals();
        try {
            for (Animal animal : mAnimals) {
                String[] array = getContext().getAssets().list("animals");
                int idx = new Random().nextInt(array.length);
                animal.setIcon(array[idx]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAdapter = new AnimalsAdapter(getContext(), mAnimals);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mAnimal = mAnimals.get(position);
                List<DistanceUvbLight> lights = UvbDatabase.getDistanceUvbLights(mAnimal);
                if (lights == null || lights.size() == 0) {
                    showToast(R.string.donot_need_uvb);
                    return;
                }
                mAnimalViewModel.setData(mAnimal);
                addFragmentToStack(R.id.uvbmain_root, new UvbDistanceFragment());
            }
        });
        uvb_animal_rv.setAdapter(mAdapter);

    }

    @Override
    protected void initEvent() {

    }
}
