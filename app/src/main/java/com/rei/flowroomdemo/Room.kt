package com.rei.flowroomdemo

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val id: Int,
    val qty: Int
)

@Entity(tableName = "dummy")
data class DummyEntity(
    @PrimaryKey val id: Int,
    val qty: Int
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    fun getCart(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity): Long

    @Query("DELETE FROM cart WHERE id = :id")
    suspend fun delete(id: Int)
}


@Database(
    entities = [
        CartEntity::class,
        DummyEntity::class
    ], version = 1, exportSchema = true
)
abstract class LocalDB : RoomDatabase() {
    abstract fun cartDao(): CartDao
}