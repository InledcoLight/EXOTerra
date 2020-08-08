package com.inledco.exoterra.uvbbuddy;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

//@Database(entities = {Animal.class, DistanceLight.class}, version = 1, exportSchema = false)
public abstract class ExoTerraDatabase extends RoomDatabase {
    private static final String DB_NAME = "exoterra.db";
    private static volatile ExoTerraDatabase INSTANCE;

    public static ExoTerraDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, ExoTerraDatabase.class, DB_NAME).build();
        }
        return INSTANCE;
    }

    public abstract AnimalDao getAnimalDao();

    public abstract DistanceLightDao getDistanceLightDao();
}
