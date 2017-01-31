package Massband.com;

import java.util.*;
import java.io.*;
import java.lang.*;
import android.content.Context;

/*
  Bilder in File-System speichern
 */
public class Saving{
    
    private final String DATEITYP = "JPG";
    private Context context;
    public Saving(Context context){
	this.context = context;
    }

    public void InsertMessung(String text, String Projekt, float[] distance, String bild)
	throws IOException, FileNotFoundException, RuntimeException{
	//neue Messung in Datenbank ablegen
	//commit work nur wenn Datei erfolgreich geschrieben
	long timestamp = System.currentTimeMillis();
	boolean overwrite = true;
	
	// SQL Insert

	// Bild in Datei ablegen
	if(bild.length() == 0){
	    //d.h. Bild soll nicht gespeichert werden
	    return;
	}
	String filename = new Long(timestamp).toString() + text + Projekt;
	FileOutputStream outputStream;
	FileInputStream inputStream;
	
	try{
	    inputStream = context.openFileInput(filename);
	}catch(FileNotFoundException e){
	    overwrite = false;
	}
	if(overwrite){
	    // Bestaetigung das File ueberschrieben werden soll
	}
	outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
	outputStream.write(bild.getBytes());
	outputStream.close();
    }    

}
