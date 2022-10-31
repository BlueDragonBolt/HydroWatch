package com.example.hydrowatch

import android.provider.BaseColumns




class DatabaseContract private constructor() {
//    val SELECT_EMPLOYEE_WITH_EMPLOYER = "SELECT * " +
//            "FROM " + Employee.TABLE_NAME + " ee INNER JOIN " + Usage.TABLE_NAME + " er " +
//            "ON ee." + Employee.COLUMN_EMPLOYER_ID + " = er." + BaseColumns._ID + " WHERE " +
//            "ee." + Employee.COLUMN_FIRSTNAME + " like ? AND ee." + Employee.COLUMN_LASTNAME + " like ?"

    object Usage : BaseColumns {
        const val TABLE_NAME = "usage"
        const val COLUMN_DATETIME = "datetime"
        const val COLUMN_FLOW = "flow"
        const val COLUMN_ENERGY = "energy"
        const val _ID = BaseColumns._ID
        const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATETIME + " TEXT, " +
                COLUMN_FLOW + " REAL, " +
                COLUMN_ENERGY + " REAL" + ")"
    }
}