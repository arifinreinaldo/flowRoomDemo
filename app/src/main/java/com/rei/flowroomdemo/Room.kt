package com.rei.flowroomdemo

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.flow.Flow


@Entity
data class CartEntity(
    @PrimaryKey val id: Int,
    val qty: Int
)

@Dao
interface CartDao {
    @Query("SELECT * FROM CartEntity")
    fun getCart(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity): Long

    @Query("DELETE FROM CartEntity WHERE id = :id")
    suspend fun delete(id: Int)
}


@Database(
    entities = [
        CartEntity::class
    ], version = 1, exportSchema = false
)
abstract class LocalDB : RoomDatabase() {
    abstract fun cartDao(): CartDao
}