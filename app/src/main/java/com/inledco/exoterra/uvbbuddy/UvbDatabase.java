package com.inledco.exoterra.uvbbuddy;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UvbDatabase {
    private static final String START = "#start";
    private static final String END = "#end";

    private static Map<Animal, List<DistanceUvbLight>> relations;
    private static List<Animal> animalList;
    private static boolean loaded;

    public static void loadDatabaseFromAssets(@NonNull final AssetManager manager, @NonNull final String path) {
        if (loaded) {
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(manager.open(path), "UTF-8"));

            relations = new LinkedHashMap<>();
            animalList = new ArrayList<>();
            boolean parsing = false;
            String line;
            List<Animal> animals = null;
            List<DistanceUvbLight> distanceUvbLights = null;
            while ((line = reader.readLine()) != null) {
                if (START.equals(line)) {
                    animals = new ArrayList<>();
                    distanceUvbLights = new ArrayList<>();
                    parsing = true;
                } else if (END.equals(line)) {
                    for (Animal animal : animals) {
                        relations.put(animal, new ArrayList<>(distanceUvbLights));
                    }
                    animalList.addAll(animals);
                    parsing = false;
                } else if (parsing) {
                    if (line.startsWith("[") && line.contains("]")) {
                        int idx = line.indexOf("]");
                        if (idx == line.lastIndexOf("]")) {
                            String name = line.substring(1, idx);
                            String[] array = line.substring(idx+1).split(",");
                            String latin_name = null;
                            String icon = null;
                            int rate = 0;
                            if (array.length > 0) {
                                latin_name = array[0];
                            }
                            if (array.length > 1) {
                                try {
                                    rate = Integer.parseInt(array[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                            if (array.length > 2) {
                                icon = array[2];
                            }
                            if (rate >= 0 && rate <= 5) {
                                Animal animal = new Animal(name, latin_name, icon, rate);
                                animals.add(animal);
                            }
                        }
                    } else if (line.startsWith("<") && line.contains(">")) {
                        int idx = line.indexOf(">");
                        if (idx == line.lastIndexOf(">")) {
                            String distance = line.substring(1, idx);
                            String[] array = line.substring(idx+1, line.length()-1).split(",");
                            DistanceUvbLight dul = new DistanceUvbLight(distance, array);
                            distanceUvbLights.add(dul);
                        }
                    }
                }
            }
            loaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Animal> getAnimals() {
        if (!loaded) {
            throw new RuntimeException("Uvb Database not loaded!");
        }
        return animalList;
    }

    public static List<DistanceUvbLight> getDistanceUvbLights(Animal animal) {
        if (!loaded) {
            throw new RuntimeException("Uvb Database not loaded!");
        }
        return relations.get(animal);
    }
}
