package com.example.hydrowatch

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val context: Context
    init {
        this.context = context!!
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(DatabaseContract.Usage.CREATE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Usage.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "sample_database"
    }

    @Synchronized
    fun readFromDB(datetime: String): Cursor {

        val database: SQLiteDatabase = this.readableDatabase
        val projection = arrayOf(
            DatabaseContract.Usage._ID,
            DatabaseContract.Usage.COLUMN_DATETIME,
            DatabaseContract.Usage.COLUMN_FLOW,
            DatabaseContract.Usage.COLUMN_ENERGY
        )
        val selection: String = DatabaseContract.Usage.COLUMN_DATETIME + " = ?"
        val selectionArgs = arrayOf(
            datetime
        )
        Log.d("Query table", DatabaseContract.Usage.TABLE_NAME)
        for (i in projection) {
            Log.d("Query projection", i)
        }

        Log.d("Query selection", selection)
        for (i in selectionArgs) {
            Log.d("Query selection arg", i)
        }
        return database.query(
            DatabaseContract.Usage.TABLE_NAME,  // The table to query
            projection,  // The columns to return
            selection,  // The columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,  // don't group the rows
            null,  // don't filter by row groups
            null // don't sort
        )
    }

    @Synchronized
    fun saveToDB(flow: Double, energy: Double) {

        val calendar: Calendar = Calendar.getInstance()
        val datetime = DatabaseManager.dateFormat.format(calendar.time)
        val cursor = readFromDB(datetime)
        if (cursor.count >= 2) {
            throw Error("Multiple records for the same hour in database")
        }

        if (cursor.count == 0) {
            Log.d("saveToDB", "No existing record found")
            val values = ContentValues()
            values.put(DatabaseContract.Usage.COLUMN_FLOW, flow)
            values.put(DatabaseContract.Usage.COLUMN_ENERGY, energy)
            values.put(DatabaseContract.Usage.COLUMN_DATETIME, datetime)
            val database: SQLiteDatabase = this.writableDatabase
            Log.d("saveToDB", "Index added: ${database.insert(DatabaseContract.Usage.TABLE_NAME, null, values)}")
        } else {
            // 1 row in cursor
            cursor.moveToFirst()
            Log.d("saveToDB", "Record exists")
            Log.d("saveToDB", "Current flow: $flow")
            val readFlow = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_FLOW))
            Log.d("saveToDB", "Read flow: $readFlow")
            val values = ContentValues()
            values.put(
                DatabaseContract.Usage.COLUMN_FLOW,
                flow + readFlow
            )
            values.put(
                DatabaseContract.Usage.COLUMN_ENERGY,
                energy + cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_ENERGY))
            )
            val database: SQLiteDatabase = this.writableDatabase
            val selection: String = DatabaseContract.Usage._ID + " = ?"
            val selectionArgs = arrayOf(
                "${cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Usage._ID))}"
            )
            Log.d("saveToDB", "Index updated: ${database.update(
                DatabaseContract.Usage.TABLE_NAME,  // The table to update
                values,
                selection,
                selectionArgs
            )}")
//            database.update(
//                DatabaseContract.Usage.TABLE_NAME,  // The table to update
//                values,
//                selection,
//                selectionArgs
//            )
        }
    }
}
    @SuppressLint("StaticFieldLeak")
    object DatabaseManager {
        val dateFormat = SimpleDateFormat("HH-dd-MM-yyyy")
        private var instance: DatabaseHelper? = null
        @Synchronized
        fun getInstance(context: Context?): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context?.applicationContext)
                Log.d("DatabaseManager", "Creating new instance")
            }
            return instance as DatabaseHelper
        }
    }
