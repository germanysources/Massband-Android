package Massband.com;

import java.io.*;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.content.Context;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import android.util.Log;

public class massband extends LayerMessung{
    private final float cmax_bew[] = {0.2f, 0.02f}; //{maximale Drehung, maximale Weg} bei Kalibrierung
    private final float nanoumr = 1E-9f; // Umrechnung Nanosekunden in Sekunden
    private static class calib{
	public final static byte error = 1; // Fehler bei Kalibrierung
	public final static byte success = 2; // Kalibrierung erfolgreich
    } 

   // Massband-Funktion fuer Handys mit Winkel und Beschleunigungssensor       
   protected long timestamp; 
   protected float[] ist_rot; // Ist-Bewegung {Distanz, Drehwinkel}
   private List<float[]> valb; // Sensor-Werte
   public mass corf_mass; // Klasse Berechnungen
   protected int zaehler; // Zaehler fuer Anzahl Messungen bei Kalibrierung
   private final static int maxzaehler = 200; // maximale Anzahl Messungen
   private boolean erste_messung;

   public massband(Context context) throws RuntimeException, InterruptedException, FileNotFoundException, IOException {
       super(context);
       ist_rot = new float[3];
       zaehler = 0;
   }
    @Override
   protected void new_messung(String action){
      timestamp = 0;
      valb = new LinkedList<float[]>();
      float[] init_a = {0, 0, 0};
      valb.add(init_a);
      corf_mass = new mass(action, add);
      erste_messung = true;
   }
   @Override 
   protected synchronized void get_sensors(Context context) throws RuntimeException{	
      sman = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
      List<Sensor> sensors_tmp = sman.getSensorList(Sensor.TYPE_ALL);
      sensors = new ArrayList<Sensor>();
      int len = sensors_tmp.size();
      for(Sensor s:sensors_tmp) {
	 switch(s.getType()) {
	 case Sensor.TYPE_ACCELEROMETER:
	    sensors.add(s);
	    break;
	 case Sensor.TYPE_GYROSCOPE:
	    sensors.add(s);
	    break;
	 default:
	    break;
	 }
      }
      if(sensors.size() == 0){
	  throw new RuntimeException(context.getString(R.string.Esensor)); // Sensoren fehlen, mindestens ein Beschleunigungssensor notwendig
      }
   }
   @Override 
       public synchronized void calib_start(){
      mess_beg(mass.CALIB);
   }
    @Override
   public synchronized void calib_end(byte return_code){
      if(return_code == calib.success){
	 mess_end();
	 try{
	    corf_mass.calib_end();
	 }catch(RuntimeException e){
	     Fehler fe = new Fehler(context.getString(R.string.Ecalib), context);
	    fe.show();
	    return;
	 }
	 add = corf_mass.calib_val;
	 corf_mass.action = "";
	 try{
	    write_calib(context);
	 }
	 catch(Exception e){
	     Fehler fe = new Fehler(context.getString(R.string.Esave) + e.getMessage(), context);
	    fe.show();
	    return;
	 }
 	 Erfolg fe = new Erfolg(context.getString(R.string.Ssave), context);
	 fe.show();
      }
      else{
	 mess_end();
	 corf_mass.action = "";
	 Fehler fe = new Fehler(context.getString(R.string.Ecalib_end), context);
	 fe.show();
      } 
   }
    
    @Override
	public synchronized void mess_end(){
	    super.mess_end();
	    corf_mass.test_end_log();
	    Log.d("gui_massb", "Messung beendet");

	}	
    @Override
    public void onSensorChanged(SensorEvent event)
    throws RuntimeException{
       if(erste_messung == true){
	  erste_messung = false;
	  timestamp = event.timestamp;
       }
	float del_time = (event.timestamp - timestamp)*nanoumr;
	timestamp = event.timestamp;
	       Log.d("gui_massb", "Zeiten" + "\t" + timestamp + "\t" + System.currentTimeMillis());
	       Log.d("gui_massb", "Werte" + "\t" + event.values[0] +
		     "\t" + event.values[1] + "\t" + event.values[2]);
	if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	    valb.add(event.values);
	    if(corf_mass.action.equals(mass.CALIB)){
	       try{
		  corf_mass.calibs(valb, del_time);
	       }catch(RuntimeException e){
		  Log.d("gui_massb",e.getMessage());
		  calib_end(calib.error);
	       }
		zaehler++;
		if(zaehler > maxzaehler){
		   Log.d("gui_massb", "Zaehler \t" + zaehler);
		   calib_end(calib.success);
		}
	    }
	    else{
		corf_mass.inte_beschl(valb, del_time);
	    }
	    /*	    if(corf_mass.action.equals(mass.CALIB)){
		for(int i = 0; i<3; i++){
		    if(Math.abs(corf_mass.distance[i]) > cmax_bew[1]){
		       rc_calib = 2;
		       Log.d("gui_massb", "Handy bewegt");
		       calib_end();
		    }
		}
		}*/

	}
	else{
	   if(corf_mass.action.equals(mass.CALIB)){
		ist_rot[0] += event.values[0] * del_time;
		ist_rot[1] += event.values[1] * del_time;
		ist_rot[2] += event.values[2] * del_time;
		for(int i = 0;i<3;i++){
		   if(Math.abs(ist_rot[i]) > cmax_bew[0]){		      
		      calib_end(calib.error);
		   }
		}
	    }
	    else{
		corf_mass.dreh(event.values, del_time);
	    }
	}
	
    }
   
}



