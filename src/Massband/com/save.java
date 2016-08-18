package Massband.com;

import android.provider.BaseColumns;
import android.database.sqlite.*;
import android.content.*;
import java.lang.*;

public class save extends PrepareSave{    
    public save(Context context){
	super(context);
    }
    private void SaveMessung(String project, String description, float[] args, String unit, Context context)
	throws SQLiteException, IndexOutOfBoundsException, RuntimeException{
	/* Nur Messwerte ablegen in Datenbank, Bild wird ausserhalb gespeichert */
	SQLiteDatabase db = getWritableDatabase();
	// fuer Tabelle mappen
	ContentValues values = new ContentValues();
	
	values.put(TheColumns.TimeStamp, System.currentTimeMillis());
	values.put(TheColumns.Project, project);
	values.put(TheColumns.Description, description);
	values.put(TheColumns.Distancex, args[0]);
	values.put(TheColumns.Distancey, args[1]);
	values.put(TheColumns.Distancez, args[2]);
	values.put(TheColumns.Unit, unit);
	
	db.beginTransaction();
	long newRowId = db.insert(TheColumns.TableName, null, values);
	if(newRowId == -1){
	    throw new RuntimeException("Datenbankupdate fehlgeschlagen");
	}
	else{
	    db.setTransactionSuccessful();
	}
	db.endTransaction();
    }
}    
