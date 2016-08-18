package Massband.com;

import android.content.Context;
import android.database.sqlite.*;
import android.provider.BaseColumns;

public abstract class PrepareSave extends SQLiteOpenHelper{    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Massband.db";    
    public static abstract class TheColumns implements BaseColumns{
	public static final String TableName = "MESSUNGEN",
	    TimeStamp = "TIMESTAMP", // Zeitstempel Schluessel
	    Project = "PROJECT",  // Projekt Schluessel
	    Description = "DEST", // Beschreibung Messung Schluessel
	    Distancex = "DISTANCEX", // Distanz x
	    Distancey = "DISTANCEY", // Distanz y
	    Distancez = "DISTANCEZ", // Distanz z
	    Unit = "UNIT", // Einheit
	    OnExternalStorage = "STORAGE"; // die Bilder koennen extern oder intern gespeichert sein	

	// do to:
	// eventuell kann das Projekt mit den Aufgabe synchronisiert werden
	// d.h. Auswahl erfolgt ueber Aufgaben in einer Kalender-App
	// bzw. Notizen zum Projekt koennen angelegt werden
	// und Fotos koennen dazu geladen werden oder andere Files 

	// Index auf Projekt anlegen
    }
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS" + TheColumns.TableName;
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "  
	+ TheColumns.TableName + " (" + TheColumns._ID + " INTEGER " 
	+ TheColumns.TimeStamp + " DECIMAL(19,0) " + TheColumns.Project + " VARCHAR(10) " 
	+ TheColumns.Description + " VARCHAR(40) " + TheColumns.Distancex + " DECIMAL(10,3) "
	+ TheColumns.Distancey + " DECIMAL(10,3) " + TheColumns.Distancez + " DECIMAL(10,3) "
	+ TheColumns.Unit + " CHAR(3) " + TheColumns.OnExternalStorage + " CHAR(1) )" 
	+ " PRIMARY KEY (" + TheColumns._ID + " )"; 

    public PrepareSave(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
	
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}    
