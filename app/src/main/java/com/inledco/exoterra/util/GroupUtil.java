package com.inledco.exoterra.util;

import com.inledco.exoterra.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupUtil {

    private static final String IC_TYPE_1       = "ic_type_1";
    private static final String IC_TYPE_2       = "ic_type_2";
    private static final String IC_TYPE_3       = "ic_type_3";
    private static final String IC_TYPE_4       = "ic_type_4";
    private static final String IC_ANIMAL_1     = "ic_animal_1";
    private static final String IC_ANIMAL_2     = "ic_animal_2";
    private static final String IC_ANIMAL_3     = "ic_animal_3";
    private static final String IC_ANIMAL_4     = "ic_animal_4";
    private static final String IC_ANIMAL_5     = "ic_animal_5";
    private static final String IC_ANIMAL_6     = "ic_animal_6";
    private static final String IC_ANIMAL_7     = "ic_animal_7";
    private static final String IC_ANIMAL_8     = "ic_animal_8";
    private static final String IC_ANIMAL_9     = "ic_animal_9";
    private static final String IC_ANIMAL_10    = "ic_animal_10";
    private static final String IC_PLANT_1      = "ic_plant_1";
    private static final String IC_PLANT_2      = "ic_plant_2";
    private static final String IC_PLANT_3      = "ic_plant_3";
    private static final String IC_AQUATIC_1    = "ic_aquatic_1";
    private static final String IC_AQUATIC_2    = "ic_aquatic_2";
    private static final String IC_AQUATIC_3    = "ic_aquatic_3";
    private static final String IC_AQUATIC_4    = "ic_aquatic_4";

    private static final Map<String, Integer> mGroupIconMap;

    static {
        mGroupIconMap = new LinkedHashMap<>();
        mGroupIconMap.put(IC_TYPE_1, R.drawable.ic_type_1);
        mGroupIconMap.put(IC_TYPE_2, R.drawable.ic_type_2);
        mGroupIconMap.put(IC_TYPE_3, R.drawable.ic_type_3);
        mGroupIconMap.put(IC_TYPE_4, R.drawable.ic_type_4);
        mGroupIconMap.put(IC_ANIMAL_1, R.drawable.ic_animal_1);
        mGroupIconMap.put(IC_ANIMAL_2, R.drawable.ic_animal_2);
        mGroupIconMap.put(IC_ANIMAL_3, R.drawable.ic_animal_3);
        mGroupIconMap.put(IC_ANIMAL_4, R.drawable.ic_animal_4);
        mGroupIconMap.put(IC_ANIMAL_5, R.drawable.ic_animal_5);
        mGroupIconMap.put(IC_ANIMAL_6, R.drawable.ic_animal_6);
        mGroupIconMap.put(IC_ANIMAL_7, R.drawable.ic_animal_7);
        mGroupIconMap.put(IC_ANIMAL_8, R.drawable.ic_animal_8);
        mGroupIconMap.put(IC_ANIMAL_9, R.drawable.ic_animal_9);
        mGroupIconMap.put(IC_ANIMAL_10, R.drawable.ic_animal_10);
        mGroupIconMap.put(IC_PLANT_1, R.drawable.ic_plant_1);
        mGroupIconMap.put(IC_PLANT_2, R.drawable.ic_plant_2);
        mGroupIconMap.put(IC_PLANT_3, R.drawable.ic_plant_3);
        mGroupIconMap.put(IC_AQUATIC_1, R.drawable.ic_aquatic_1);
        mGroupIconMap.put(IC_AQUATIC_2, R.drawable.ic_aquatic_2);
        mGroupIconMap.put(IC_AQUATIC_3, R.drawable.ic_aquatic_3);
        mGroupIconMap.put(IC_AQUATIC_4, R.drawable.ic_aquatic_4);
    }

    public static int getGroupIcon(final String key) {
        if (mGroupIconMap.containsKey(key)) {
            return mGroupIconMap.get(key);
        }
        return R.drawable.ic_type_1;
    }

    public static List<String> getGroupIconNames() {
        return new ArrayList<>(mGroupIconMap.keySet());
    }

    public static List<Integer> getGroupIcons() {
        return new ArrayList<>(mGroupIconMap.values());
    }

    public static int getGroupIconsCount() {
        return mGroupIconMap.size();
    }

    public static String getDefaultIconName() {
        return IC_TYPE_1;
    }

    public static boolean contains(final String key) {
        return mGroupIconMap.containsKey(key);
    }
}
