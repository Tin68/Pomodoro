package com.example.pomy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class TaskDB extends SQLiteOpenHelper {
    public static String DB_PATH = "";
    public static String DB_NAME = "Task";
    private SQLiteDatabase dbObj;
    private final Context context;

    @SuppressLint("SdCardPath")
    public TaskDB(Context context) {
        super(context,  DB_NAME , null, 3);
        this. context  = context;
        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
    }

    public boolean createDB() throws IOException {
        try{
            //InputStream ip =  context.getAssets().open(DB_NAME+".db");
            SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME +".db", Context.MODE_PRIVATE, null);
            db.close();
            //this.getReadableDatabase();
        } catch (Exception e) {
            //throw new Error("Error there is not database");
            //creo la base de datos vacia
            String TABLE_NAME= "task_table";
            String ID_COL = "taskId";
            String NAME_COL1 = "timeStart";
            String NAME_COL2 = "timeEnd";
            String NAME_COL3 = "task";
            String NAME_COL4 = "totalTime";

            String query = "CREATE TABLE " + TABLE_NAME + " ("
                    + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME_COL1 + " TEXT,"
                    + NAME_COL2 + " TEXT,"
                    + NAME_COL3 + " TEXT,"
                    + NAME_COL4 + " TEXT)";
            //context.openOrCreateDatabase(DB_NAME +".db", Context.MODE_PRIVATE, null);
            SQLiteDatabase db =context.openOrCreateDatabase(DB_NAME +".db", Context.MODE_PRIVATE, null);
            //SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(query);
            TABLE_NAME= "long_break";
            ID_COL = "timeId";
            NAME_COL1 = "timeBreak";
            query = "CREATE TABLE " + TABLE_NAME + " ("
                    + ID_COL + " INTEGER NOT NULL, "
                    + NAME_COL1 + " INTEGER NOT NULL DEFAULT 15)";
            db.execSQL(query);
            db.close();
        }

        Log.i("Readable ends....................","end");
        try {
            copyDB();
            Log.i("copy db ends....................","end");
        } catch (IOException e) {
            throw new Error("Error copying database");
        }
        return true;
    }

    public boolean checkDB(){

        SQLiteDatabase checkDB = null;
        try{
            String path = DB_PATH + DB_NAME;
            Log.i("myPath ......",path);
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            Log.i("myPath2 ......",path);
            if (checkDB!=null)
            {
                Log.i("checkDB!=null ......","true");
                try {
                    String sql ="SELECT * FROM Task_table";
                    @SuppressLint("Recycle") Cursor c = checkDB.rawQuery(sql, null);
                    if (c != null) {
                        c.moveToNext();
                        //c.moveToFirst();
                        String contents[]=new String[80];
                        int flag=0;

                        while(! c.isAfterLast()){
                            String temp="";
                            String s2=c.getString(0);
                            String s3=c.getString(1);
                            String s4=c.getString(2);
                            temp=temp+"\n Id:"+s2+"\tType:"+s3+"\tBal:"+s4;
                            contents[flag]=temp;
                            flag=flag+1;

                            Log.i("DB values.........",temp);
                            c.moveToNext();
                        }
                    }
                } catch (SQLException mSQLException) {
                    Log.e("jjj", "getTestData >>"+ mSQLException);
                    throw mSQLException;
                }
            }
            else
            {
                return false;
            }

        }catch(SQLiteException e){
            e.printStackTrace();
        }

        if(checkDB != null){
            Log.i("Close",DB_NAME);
            checkDB.close();
        }
        return checkDB != null;
    }

    @SuppressLint("LongLogTag")
    public void copyDB() throws IOException{
        try {
            Log.i("inside copyDB....................","start");

            InputStream ip =  context.getAssets().open(DB_NAME +".db");
            Log.i("Input Stream....",ip+"");
            String op=  DB_PATH  +  DB_NAME +".db";
            //OutputStream output = Files.newOutputStream(Paths.get(op));
            OutputStream output = new FileOutputStream (op);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = ip.read(buffer))>0){
                output.write(buffer, 0, length);
                Log.i("Content.... ",length+"");
            }
            output.flush();
            output.close();
            ip.close();
        }
        catch (IOException e) {
            Log.v("error", e.toString());
        }
    }

    public boolean openDB() throws SQLException {
        try {
            String myPath = DB_PATH + DB_NAME + ".db";
            dbObj = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            Log.i("open DB......", dbObj.toString());
            return true;
        } catch (SQLException e) {
            Log.v("error", e.toString());
            return false;
        }
    }



    @Override
    public synchronized void close() {

        if(dbObj != null)
            dbObj.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}