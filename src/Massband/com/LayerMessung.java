package Massband.com;

import java.io.*;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import android.util.Log;

public abstract class LayerMessung implements SensorEventListener{
    // Abspeichern der Kalibrierung    
    
    /** File-Name in der werden Kalibrierungswerte als SharedPreferences abgelegt **/
    private final String filename = "Massband.com.KalibrierungBeschleunigungSensor";
    
    /** Kalibrierungswerte, wobei err der durchschnittle Fehler ist **/
    public volatile float add[], err[];

    public volatile SensorManager sman;   
    protected SharedPreferences pref; 
    protected Context context;
    /** Sensoren **/
    protected volatile List<Sensor> sensors; 

   public LayerMessung(Context context) throws RuntimeException, InterruptedException, FileNotFoundException, IOException {
      sensors = new ArrayList<Sensor>();
      this.get_sensors(context);
      this.add = new float[3];
      this.err = new float[3];
      this.context = context;	

   }
    /**
     * Neue Messung anstossen
     **/
    protected abstract void new_messung(String action);
    
    /**
     * Kalibrierungswerte laden
     * @RuntimeException kein File gefunden
     **/
   public void load_calib() throws RuntimeException{
       	 this.read_calib(add, err, context); 
   }
    
    /**
     * Sensoren ermitteln
     * @context Interface zum GUI
     * @MustHave Sensoren muessen vorhanden sein
     * @Optional Optionale Sensoren
     **/
    protected void get_sensors(Context context, int[] MustHave, int[] Optional) throws RuntimeException{
	sman = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	List<Sensor> AllSensor = sman.getSensorList(Sensor.TYPE_ALL);
	for(int i=0;i<AllSensor.size();i++){
	    for(int j:MustHave){
		if(AllSensor.get(i).getType() == j){
		    sensors.add(AllSensor.get(i));
		}
	    }
	}
	if(sensors.size() != MustHave.length){
	    throws new RuntimeException("Nicht alle notwendigen Sensoren vorhanden");
	}    
	for(int i=0;i<AllSensor.size();i++){
	    for(int j:Optional){
		if(AllSensor.get(i).getType() == j){
		    sensors.add(AllSensor.get(i));
		}
	    }
	}
    }
    /**
     * Kalibrierungsdaten aus SharedPreferences lesen
     * @add zu Messwerten zu addieren
     * @err durchschnittlicher Fehler
     * @context fuer Android-App-Interface
     **/
   public synchronized void read_calib(float[] add, float[] err, Context context) throws RuntimeException {
       SharedPreferences sp = context.getSharedPreferences(filename,Context.MODE_PRIVATE);
       String key;       
       for(int i=0;i<add.length;i++){
	   key = "add" + new Integer(i).toString();
	   add[i] = sp.getFloat(key, 0);
	   if(!sp.contains(key)){
	       throw new RuntimeException(context.getString(R.string.Eread_calib));
	   }
       }
       for(int i=0;i<err.length;i++){
	   key = "err" + new Integer(i).toString();
	   err[i] = sp.getFloat(key, 0);
	   if(!sp.contains(key)){
	       throw new RuntimeException(context.getString(R.string.Eread_calib));
	   }
       }       

   }

    /**
     * Messung beginnen
     * @action was wird gemessen muss Konstante aus Klasse mass sein
     **/
   public synchronized void mess_beg(String action) throws RuntimeException{
      new_messung(action);
      /* get type of Delay */
      PreferenceManager.setDefaultValues (this, R.xml.preferences, false);
      pref = PreferenceManager.getDefaultSharedPreferences (this);
      final String rate = pref.getString ("rate", null);
      int sensorRate;
      if (rate.equals ("fastest"))
	  sensorRate = SensorManager.SENSOR_DELAY_FASTEST;
      else if (rate.equals ("game"))
	  sensorRate = SensorManager.SENSOR_DELAY_GAME;
      else if (rate.equals ("ui"))
	  sensorRate = SensorManager.SENSOR_DELAY_UI;
      else{
	  assert (rate.equals ("normal"));
	  sensorRate = SensorManager.SENSOR_DELAY_NORMAL;
      }

      for(Sensor s:sensors) {
	 boolean sevt = sman.registerListener(this, s, sensorRate);
	 if(sevt == false){
	     throw new RuntimeException(context.getString(R.string.Ereg));
	 }
      }
   }
    /**
     * Messung beenden
     **/
    public synchronized void mess_end() {
	for(Sensor s:sensors){
	    sman.unregisterListener(this, s);
	}
    }    
    /**
     * Werte haben sich geaendert
     **/ 
    public abstract void onSensorChanged(SensorEvent event)
	throws RuntimeException;
    
    /**
     * Genauigkeit geaendert, nicht notwendig
     **/
    public void onAccuracyChanged(Sensor sensor, int accuracy){	
    }
    
    /**
     * Kalibrierungsdaten und durchschnittlichen Messfehler als SharedPreferences speichern     
     **/
    public synchronized void write_calib(Context context){
      
       String key;
       SharedPreferences sp = context.getSharedPreferences(filename,Context.MODE_PRIVATE);
       SharedPreferences.Editor editor = sp.edit();
       for(int i=0;i<add.length;i++){
	   key = "add" + new Integer(i).toString();
	   editor.putFloat(key, add[i]);
       }
       for(int i=0;i<err.length;i++){
	   key = "err" + new Integer(i).toString();
	   editor.putFloat(key,err[i]);
       }
       editor.commit();
   }
   
}



