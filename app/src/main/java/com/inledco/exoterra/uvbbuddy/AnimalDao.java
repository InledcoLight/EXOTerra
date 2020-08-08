package com.inledco.exoterra.uvbbuddy;

import java.util.List;

//@Dao
public interface AnimalDao {

//    @Query ("SELECT * from animal")
    List<Animal> getAll();

//    @Insert
    void insert(Animal... animals);

//    @Update
    void update(Animal... animals);

//    @Delete
    void delete(Animal... animals);
}
