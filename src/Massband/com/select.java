package Massband.com;

import android.database.*;
import android.database.sqlite.*;
import android.content.*;
import java.util.*;
import java.text.*;
import java.lang.*;

public class select extends PrepareSave{
    public class ProjectStructure{
	public String project, description, datum;
	public float[] Messungen;
    }

    private Cursor cursor;
    public select(Context context){
	super(context);	
    }
    private long CalculateTimeStamp(String datum) throws ParseException{
	// Zeitstempel aus Datum und Uhrzeit berechnen
	DateFormat df;
	final int styles[] = {DateFormat.SHORT, DateFormat.DEFAULT, DateFormat.MEDIUM,
			      DateFormat.LONG};
	for(int style:styles){
	    try{
		df = DateFormat.getDateTimeInstance(style, style);
		Date date = df.parse(datum);
		return date.getTime();
	    }
	    catch(ParseException e){
		
	    }
	}
	df = DateFormat.getDateTimeInstance(styles[styles.length - 1],
					    styles[styles.length - 1]);
	Date date = df.parse(datum);
	return date.getTime();
    }
    
    public void query_messungen(List<ProjectStructure> projects, String[] dates)
	throws ParseException, IndexOutOfBoundsException{
	//Messungen abfragen von Datenbank und in cursor stellen
	//projects:Projekte
	//description:Beschreibungen
	//dates[0]: von Datum
	//dates[1]: bis Datum
	//dates[1]: leer immer mit Systemzeit fuellen
	long TimeMillis[] = new long[2];
	if(dates[1].equals("")){
	    TimeMillis[1] = System.currentTimeMillis();
	}
	else{
	    TimeMillis[1] = CalculateTimeStamp(dates[1]);
	}
	if(!dates[0].equals("")){
	    TimeMillis[0] = CalculateTimeStamp(dates[0]);
	}
	String SQLWhere = TheColumns.TimeStamp + " BETWEEN " +
	    TimeMillis[0] + " AND " + TimeMillis[1]; // SQL Where Bedingung
	/* hier wird ein select for all entries in projects ausgefuehrt
	   Die Beschreibung wird mit like gesucht
	 */
	for(int i=0;i<projects.size();i++){
	    ProjectStructure ps = projects.get(i);
	    //Replace * mit % wegen SQL Syntax
	    //am Anfang und Ende noch % anhaengen
	    SQLWhere += " AND " + TheColumns.Project + " = " + ps.project + " AND " + TheColumns.Description + " LIKE " + ps.description;
	}
	String SQLOrder = TheColumns.Project + ", " + TheColumns.Description + ", " + TheColumns.TimeStamp;
	SQLiteDatabase db = getReadableDatabase();
	cursor = db.query(false, TheColumns.TableName, null,
			  SQLWhere, null, null, null,
			  SQLOrder, null);
    }
    public ProjectStructure getMessungen(int style){
	//Messungen aus Cursor extrahieren
	//style: Aufbereitung Datum (DateFormat.DEFAULT, DateFormat.SHORT ...)
	ProjectStructure ret = new ProjectStructure();
	if(cursor.moveToNext() == false){
	    return null;
	}
	int i = cursor.getColumnIndex(TheColumns.Project);
	ret.project = cursor.getString(i);
	i = cursor.getColumnIndex(TheColumns.Description);
	ret.description = cursor.getString(i);
	ret.Messungen = new float[3];
	i = cursor.getColumnIndex(TheColumns.Distancex);
	ret.Messungen[0] = cursor.getFloat(i);
	i = cursor.getColumnIndex(TheColumns.Distancey);
	ret.Messungen[1] = cursor.getFloat(i);
	i = cursor.getColumnIndex(TheColumns.Distancez);
	ret.Messungen[1] = cursor.getFloat(i);
	i = cursor.getColumnIndex(TheColumns.TimeStamp);
	return ret;
	
	
    }
}