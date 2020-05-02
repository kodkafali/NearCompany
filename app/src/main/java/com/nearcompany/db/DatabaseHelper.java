package com.nearcompany.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nearcompany.model.Company;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emre on 5/2/2020.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "nearcompany_db";

    private String CREATE_COMPANY_TABLE = "CREATE TABLE COMPANIES (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "company_name TEXT," +
            "company_class TEXT," +
            "scope TEXT," +
            "staf_count INTEGER," +
            "description TEXT," +
            "logo TEXT," +
            "web_link TEXT," +
            "foundation_year TEXT," +
            "phone TEXT," +
            "email TEXT," +
            "project_id INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COMPANY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_COMPANY_TABLE);
        onCreate(db);
    }

    public void setCompanyQuery(Company company) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //values.put("id", company.id);
        values.put("company_name", company.companyName);
        values.put("company_class", company.companyClass);
        values.put("description", company.description);
        values.put("staf_count", company.stafCount);
        values.put("scope", company.scope);
        values.put("email", company.email);
        values.put("web_link", company.webLink);
        values.put("phone", company.phone);
        values.put("foundation_year", company.foundationYear);
        values.put("logo", company.logo);
        values.put("project_id", company.id);

        db.insert("COMPANIES", null, values);
        db.close();
    }

    public List<Company> getCompaniesQuery() {

        List<Company> companies = new ArrayList<>();

        String selectQuery = "SELECT * FROM COMPANIES";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        while (cursor.moveToNext()) {
            Company company = new Company();
            company.id = cursor.getInt(cursor.getColumnIndex("id"));
            company.companyName = cursor.getString(cursor.getColumnIndex("company_name"));
            company.companyClass = cursor.getString(cursor.getColumnIndex("company_class"));
            company.description = cursor.getString(cursor.getColumnIndex("description"));
            company.stafCount = cursor.getInt(cursor.getColumnIndex("staf_count"));
            company.scope = cursor.getString(cursor.getColumnIndex("scope"));
            company.email = cursor.getString(cursor.getColumnIndex("email"));
            company.webLink = cursor.getString(cursor.getColumnIndex("web_link"));
            company.phone = cursor.getString(cursor.getColumnIndex("phone"));
            company.foundationYear = cursor.getString(cursor.getColumnIndex("foundation_year"));
            company.logo = cursor.getString(cursor.getColumnIndex("logo"));

            companies.add(company);
        }

        db.close();

        return companies;
    }

    public int getCompanyCount() {
        String query = "Select Count(*) from COMPANIES";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        int count = cursor.getInt(0);

        cursor.close();

        return count;
    }
}
