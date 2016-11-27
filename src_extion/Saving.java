/*
  Speichern in SQL-Lite Datenbank Android
 */
public class Saving{
    
    private final String DATEITYP = 'JPG';
    private Context context;
    public Saving(Context context){
	this.context = context;
	createTable();
    }
    private void CreateTable{
	//Tabellen erstellen
	// Spalten: long Zeit(Key), Kurztext(Key), Projekt(Index) float Distanz X, Y,Z, 	
	// das Bild gesondert auf File-System als normale Datei ablegen
	// mit Dateinamen Schluessel von Datensatz
    }
    public void InsertMessung(String text, String Projekt, float[] distance, String bild)
	throws IOException, FileNotFoundException, RuntimeException{
	//neue Messung in Datenbank ablegen
	//commit work nur wenn Datei erfolgreich geschrieben
	long timestamp = System.getCurrentInMillis();
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
	    // Bestätigung das File ueberschrieben werden soll
	}
	outputStream = context.openFileOutput(filename, Context.MODE_PUBLIC);
	outputStream.write(bild.getBytes());
	outputStream.close();
    }    

}
