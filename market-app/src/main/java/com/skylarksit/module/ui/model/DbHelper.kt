package com.skylarksit.module.ui.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "products"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_SKU = "sku"
        const val COLUMN_NAME_LABEL = "label"
        const val COLUMN_NAME_BRAND = "brand"
        const val COLUMN_NAME_SUBCATEGORY = "subCategory"
        const val COLUMN_NAME_SERVICE = "service"
        const val COLUMN_NAME_KEYWORDS = "keywards"
    }
}

private const val SQL_CREATE_ENTRIES =
        "CREATE VIRTUAL TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} USING FTS3(" +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_ID} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_SKU} SKU," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_LABEL} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBCATEGORY} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_SERVICE} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_KEYWORDS} TEXT," +
                "${FeedReaderContract.FeedEntry.COLUMN_NAME_BRAND} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 7
        const val DATABASE_NAME = "FeedReader.db"
    }

}
