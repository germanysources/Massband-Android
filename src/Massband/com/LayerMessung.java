package Massband.com;

import java.io.*;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import android.util.Log;

public abstract class LayerMessung implements SensorEventListener{
   private final String trenn = ";", filename = "Massband.com.KalibrierungBeschleunigungSensor";

   // Allgemeine Funktion wie Abspeichern der Kalibrierung    
   public volatile float add[], err[];
   public volatile SensorManager sman;
   
   protected Context context;
   protected volatile List<Sensor> sensors; 

   public LayerMessung(Context context) throws RuntimeException, InterruptedException, FileNotFoundException, IOException {
      sensors = new ArrayList<Sensor>();
      this.get_sensors(context);
      this.add = new float[3];
      this.err = new float[3];
      this.context = context;	
      new_messung("");

   }
    protected abstract void new_messung(String action);

   public void load_calib(){
      // try{
      // 	 this.read_calib(add, err, context);
      // }
      // catch(RuntimeException e){
      // 	  Erfolg ef = new Erfolg(e.getMessage(), context);
      // 	  ef.show();
	  calib_start();
      // }   
 
   }

    protected abstract void get_sensors(Context context) throws RuntimeException;

   /*   public void calib_start(Context context) { 
      // executor.execute(new Runnable() {
      // 			  @Override
      // 			  public void run() {
      // 			     try {
				// Kalibrieren
				float[] init_a = {0, 0, 0};
				// Log.d("gui_massb", "Thread begonnen\t" + Thread.currentThread());
				mess_beg(mass.CALIB, init_a);
	 // 		     } catch (RuntimeException e) {
	 // 			Log.w("gui_massb", e.getMessage());
	 // 		     }
	 // 		  }
	 // }
	 // );
	 }*/
    public abstract void calib_start();

    public abstract void calib_end(byte return_code);

   public synchronized void read_calib(float[] add, float[] err, Context context) throws RuntimeException {
      // Kalibrierungsdaten aus SharedPreferences lesen
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

    
   public synchronized void mess_beg(String action) {
      new_messung(action);
      for(Sensor s:sensors) {
	 boolean sevt = sman.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
	 Log.d("gui_massb", "Messung begonnen mit Aktion " + action);
	 if(sevt == false){
	     throw new RuntimeException(context.getString(R.string.Ereg));
	 }
      }
   }

   public synchronized void mess_end() {
      for(Sensor s:sensors){
	 sman.unregisterListener(this, s);
      }
   }    
    public abstract void onSensorChanged(SensorEvent event)
	throws RuntimeException;

    public void onAccuracyChanged(Sensor sensor, int accuracy){	
    }

   public synchronized void write_calib(Context context){
      // Kalibrierungsdaten und durchschnittlichen Messfehler als SharedPreferences speichern
      
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



