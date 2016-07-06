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

public class massband implements SensorEventListener{
   private final String trenn = ";", filename = "massbandcalib";
   private final float cmax_bew[] = {0.2f, 0.02f}; //{maximale Drehung, maximale Weg} bei Kalibrierung
   private final float nanoumr = 1E-9f; // Umrechnung Nanosekunden in Sekunden

   // Massband-Funktion fuer Handys mit Winkel und Beschleunigungssensor    
   private volatile List<Sensor> sensors;
   private volatile float[] gbeg = new float[3];    
   public volatile float add[], err[];
   public volatile SensorManager sman;
   
   protected Context context;
   protected long timestamp; 
   protected float[] ist_rot; // Ist-Bewegung {Distanz, Drehwinkel}
   List<float[]> valb; // Sensor-Werte
   public mass corf_mass; // Klasse Berechnungen
   public byte rc_calib; //TRUE: Handy bei Kalibrierung bewegt
   protected int zaehler; // Zaehler fuer Anzahl Messungen bei Kalibrierung
   private final static int maxzaehler = 200; // maximale Anzahl Messungen
   private boolean erste_messung;

   public massband(Context context) throws RuntimeException, InterruptedException, FileNotFoundException, IOException {
      sensors = new ArrayList<Sensor>();
      this.get_sensors(context);
      this.add = new float[3];
      this.err = new float[3];
      this.context = context;	
      rc_calib = 0;
      ist_rot = new float[3];
      zaehler = 0;
      new_messung("");

   }
   private void new_messung(String action){
      timestamp = 0;
      valb = new LinkedList<float[]>();
      float[] init_a = {0, 0, 0};
      valb.add(init_a);
      corf_mass = new mass(action, add);
   }
   public void load_calib(){
      try{
	 this.read_calib(add, err, context);
      }
      catch(FileNotFoundException e){
	 calib_start();
      }
      catch(IOException e){
	 calib_start();
      }
      catch(IndexOutOfBoundsException e){
	 calib_start();
      }
   }

   private synchronized void get_sensors(Context context) throws RuntimeException{	
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
	  throw new RuntimeException(new Integer(R.string.Esensor).toString()); // Sensoren fehlen, mindestens ein Beschleunigungssensor notwendig
      }
   }

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
   public synchronized void calib_start(){
      mess_beg(mass.CALIB);
   }

   public synchronized void calib_end(){
      if(rc_calib == 1){
	 mess_end();
	 try{
	    corf_mass.calib_end();
	 }catch(RuntimeException e){
	    Fehler fe = new Fehler(e.getMessage(), context);
	    fe.show();
	    return;
	 }
	 add = corf_mass.calib_val;
	 corf_mass.action = "";
	 try{
	    write_calib(context);
	 }
	 catch(Exception e){
	     Fehler fe = new Fehler(new Integer(R.string.Esave).toString() + e.getMessage(), context);
	    fe.show();
	    return;
	 }
 	 Fehler fe = new Fehler(new Integer(R.string.Ssave).toString(), context);
	 fe.show();
      }
      else{
	 mess_end();
	 corf_mass.action = "";
	 Fehler fe = new Fehler(new Integer(R.string.Ecalib).toString(), context);
	 fe.show();
      } 
   }

   public synchronized void read_calib(float[] add, float[] err, Context context) throws FileNotFoundException, IOException {
      // Kalibrierungsdaten aus internal Storage lesen
      FileInputStream inputStream;	
      int pos2, pos1, j;
      char[] calibfile = new char[2];
      String l_mem;
		
      inputStream = context.openFileInput(filename);
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
	       
      l_mem = br.readLine();
      l_mem.getChars(0, l_mem.length(),calibfile,0);
      pos2 = calibfile.length - 1;
      pos1 = pos2;
      j = 6;
      for(int i=pos1; i>0; i++){
	 if(new String(calibfile, i, 1) == trenn){
	    l_mem = new String(calibfile, i+1, pos2 - i);
	    j--;
	    if(j > 2){
	       err[j-3] = Float.parseFloat(l_mem);
	    }
	    else{
	       add[j] = Float.parseFloat(l_mem);
	    }
	    pos2 = i - 1;
	 }
      }

   }

    
   public synchronized void mess_beg(String action) {
      new_messung(action);
      for(Sensor s:sensors) {
	 erste_messung = true;
	 boolean sevt = sman.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
	 Log.d("gui_massb", "Messung begonnen mit Aktion " + action);
	 if(sevt == false){
	     throw new RuntimeException(new Integer(R.string.Ereg).toString());
	 }
      }
   }

   public synchronized void mess_end() {
      for(Sensor s:sensors){
	 sman.unregisterListener(this, s);
      }
      corf_mass.test_end_log();
      Log.d("gui_massb", "Messung beendet");
   }    
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
		  rc_calib = 2;
		  Log.d("gui_massb",e.getMessage());
		  calib_end();
	       }
		zaehler++;
		if(zaehler > maxzaehler){
		   Log.d("gui_massb", "Zaehler \t" + zaehler);
		   rc_calib = 1;
		   calib_end();
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
		      rc_calib = 2;
		      calib_end();
		   }
		}
	    }
	    else{
		corf_mass.dreh(event.values, del_time);
	    }
	}
	
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy){
	
    }

   public synchronized void write_calib(Context context) throws FileNotFoundException, IOException{
      // Kalibrierungsdaten auf interal Storage speichern
      // Integral - g (add), und Integral(Messwert-g)^2 (err) und daraus Wurzel
      
      StringBuilder filecon = new StringBuilder();
      for(int i = 0;i < add.length; i++) {
	 filecon.append(add[i]);
	 filecon.append(trenn);
      }
      for(int i = 0;i < err.length; i++) {
	 filecon.append(err[i]);
	 filecon.append(trenn);
      }      
      FileOutputStream outputStream;
	       
      outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
      outputStream.write(filecon.toString().getBytes());
      outputStream.close();
   }
   
}



