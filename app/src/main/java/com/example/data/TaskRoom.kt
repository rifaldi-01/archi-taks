package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val project: String,
    val designer: String,
    val phase: String,       // e.g. "Concept", "Modeling", "Rendering", "Working Docs"
    val priority: String,    // e.g. "High", "Medium", "Low"
    val status: String,      // e.g. "Todo", "In Progress", "In Review", "Completed"
    val progress: Float,     // 0f to 100f
    val deadline: Long,      // timestamp in mills
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 60,
    val notes: String = "",
    val cloudSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "architask_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insert(task: Task) = taskDao.insertTask(task)

    suspend fun update(task: Task) = taskDao.updateTask(task)

    suspend fun delete(task: Task) = taskDao.deleteTask(task)

    suspend fun clear() = taskDao.clearAll()
}
