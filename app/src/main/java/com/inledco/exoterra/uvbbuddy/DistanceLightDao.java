package com.inledco.exoterra.uvbbuddy;

import java.util.List;

//@Dao
public interface DistanceLightDao {
//    @Query ("SELECT * from distancelight")
    List<DistanceLight> getAll();

//    @Insert
    void insert(DistanceLight... distanceLights);

//    @Update
    void update(DistanceLight... distanceLights);

//    @Delete
    void delete(DistanceLight... distanceLights);
}
