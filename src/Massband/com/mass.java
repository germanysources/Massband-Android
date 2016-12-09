package Massband.com;

import java.lang.Math.*;
import java.util.*;
import android.hardware.SensorManager;
import android.util.Log;
import android.content.Context;

public class mass{
    public final static String CALIB = "C", SPEEDM = "S", DISTANCEM = "D";//was wird gemessen	  

    Context context;

    float calib_val[] = {0, 0, 0}; // Kalibrierungswerte
    float distance[] = new float[4];
    float speed[] = new float[3];
    float distancePart[] = new float[3];
    float amem[] = new float[3]; //Zwischenspeicher durchschnitt Beschleunigung
    float amemlast[] = new float[3];//Zwischenspeicher durchschnitt Beschleunigung letzte Messungen
    float timSumMem;
    float tim_sum = 0; // Zeitsumme nach jeder Messung in Sekunden
    String action;
    
    public mass(String action, float[] calib_val, Context c){
	distance = new float[4];
	speed = new float[3];
	distancePart = new float[3];
	amem = new float[3];
	amemlast = new float[3];
	tim_sum = 0;
	timSumMem = 0;
	this.calib_val = calib_val;
	this.action = action;
	context = c;
   }
    public void calibs(List<float[]> valb, float deltime) throws
	RuntimeException{
	// ein Schritt zum Kalibrieren
	// Beschleunigung zur Geschwindigkeit integrieren
	// deltime: Zeitdifferenz fuer Aenderung der Besschleunigung	
	int len = valb.size() - 1;
	float be1[] = beschl(valb.get(len-1));
	float be2[] = beschl(valb.get(len));       
		
	tim_sum += deltime;
	for(int i= 0; i<3;i++){
	    speed[i] += deltime * 0.5f * (be1[i]+be2[i]);	      
	}
    }
 
   protected float[] berechneRotationsmatrix(float[] w){
      float r = (float) Math.sqrt(w[0] * w[0] + w[1] * w[1] + w[2] * w[2]);
      Log.d("gui_massb", "r: " + r);
      if(Math.abs(r - 9.81) > 0.1)
	 throw new RuntimeException(context.getString(R.string.Ecalib) + "\t" + r);
      float theta = (float) Math.acos(w[2] / r);
      float phi1 = (float) Math.asin(w[1] / r / Math.sin(theta));
      float phi2 = (float) Math.acos(w[0] / r / Math.sin(theta));
      Log.d("gui_massb", "phi1,2,theta: " + phi1 + "\t" + phi2 + "\t" + theta);
      Log.d("gui_massb", "w: " + w[0] + "\t" + w[1] + "\t" + w[2]);
      Log.d("gui_massb", "x1,x2,y1,y2: " + r * Math.sin(theta) * Math.cos(phi1) + "\t" +  r * Math.sin(theta) * Math.cos(phi2) + "\t" + r * Math.sin(theta) * Math.sin(phi1) + "\t" +  r * Math.sin(theta) * Math.sin(phi2));
      float phi;
      if(Math.sin(theta) * Math.cos(phi1) >= 0.0 && w[0] < 0)
	 phi = phi2;
      else
	 phi = phi1;
      Log.d("gui_massb", "x',y': " + r * Math.sin(theta) * Math.cos(phi) + "\t" + r * Math.sin(theta) * Math.sin(phi));
      float[] ret = new float[9];
      ret[0] = (float) (Math.cos(theta) * Math.cos(phi1));
      ret[1] = (float) -Math.sin(theta);
      ret[2] = (float) (Math.cos(theta) * Math.sin(phi1));
      ret[3] = (float) (Math.sin(theta) * Math.cos(phi1));
      ret[4] = (float) (Math.cos(theta));
      ret[5] = (float) (Math.sin(theta) * Math.sin(phi1));
      ret[6] = (float) -Math.sin(phi1);
      ret[7] = 0;
      ret[8] = (float) Math.cos(phi1);
      return ret;
   }

    public void calib_end() throws RuntimeException{ 
       for(int i = 0; i<3;i++){
	  calib_val[i] = speed[i] / tim_sum;
       }
       float[] rot = berechneRotationsmatrix(calib_val);
       float[] gemag = new float[3];
       Log.d("gui_massb", "Geschwindigkeit");
       gemag = rotspeed(rot, speed);
       Log.d("gui_massb", "gemag[0]" + gemag[0] + "\t" + tim_sum + "\t" + System.currentTimeMillis());
       calib_val[0] = -gemag[0] / tim_sum;
       Log.d("gui_massb", "Werte Kalibrierung " + calib_val[0]);

       Log.d("gui_massb", "gemag[1]" + gemag[1] + "\t" + tim_sum + "\t" + System.currentTimeMillis());
       calib_val[1] = -gemag[1] / tim_sum;
       Log.d("gui_massb", "gemag[2]" + gemag[2] + "\t" + tim_sum + "\t" + System.currentTimeMillis());
       Log.d("gui_massb", "Werte Kalibrierung " + calib_val[1]);
       calib_val[2] = -gemag[2] / tim_sum;	
       Log.d("gui_massb", "Werte Kalibrierung " + calib_val[2]);
       // Rotationsmatrix als Klassenattribut speichern
       for(int i = 0; i <3;i++){
	  for(int j = 0;j<3;j++){
	     koord[i][j] = rot[i+3*j];
	  }
       }

    }
    private float[] rotspeed(float[] rotation, float[] speed){
	// Umrechnen in globalen Koordination
	float urspeed[] = new float[3];
	int j;
	for(int i = 0;i<3;i++){
	    for(j = 0; j<3;j++){
		urspeed[i] += rotation[3*i+j]*speed[j];
	    }
	}
	return urspeed;
    }
    private float getCalibVal(int i){
	//Kalibrierungswert holen
	return calib_val[i];
    }
    public void inte_beschl(List<float[]> valb, float deltime){
	//Beschleunigung zum Weg integrieren mit Zwischenmittelwerte
	//erst die Zwischenwerte der Beschleunigung (Anzahl LengthSensorValues) bilden und dann integrieren
	//deltime: Zeitdifferenz zwischen den Messungen
	int len = valb.size() - 1;
	float be1[] = beschl(valb.get(len - 1));
	float be2[] = beschl(valb.get(len));
	for(int i=0;i<3;i++){
	    be1[i] += getCalibVal(i);
	    be2[i] += getCalibVal(i);
	}
	timSumMem += deltime;
	for(int i=0;i<3;i++){
	    amem[i] += 0.5f*(be1[i]+be2[i])*deltime;
	}
	if(len % LengthSensorValues == 0){
	    tim_sum += timSumMem;
	    for(int i=0;i<3;i++){
		speed[i] += amem[i];
		amem[i] /= timSumMem;
		distancePart[i] += 0.5f*(amemlast[i]*(tim_sum-timSumMem)+amem[i]*tim_sum)*timSumMem;
		distance[i] = tim_sum*speed[i] - distancePart[i];
	    }
	    // Test-Methode
	    test_log(valb);
	    //Variablen initialisieren
	    timSumMem = 0;
	    amemlast = amem;
	    amem = new float[3];
	    for(int i=0;i<len;i++){
	       valb.remove(0);
	    }
	    distance[3] = (float)Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+
					   distance[2]*distance[2]);    
	}
	
    }

    private static float koord[][] = {{1, 0, 0},{0,1,0},{0,0,1}};//Umrechnungsmatrix in Spalten
    public float[] beschl(float[] sens_val){
	// Beschleunigung in Ursprungskoordinaten
	// sens_val: Beschleunigung vom Sensor
	
	float ret[] = new float[3];
	for(int i = 0; i < 3;i++){
	    ret[i] = sens_val[0]*koord[0][i]+sens_val[1]*koord[1][i]+sens_val[2]*koord[2][i];
	}
	return ret;
    }
    public void dreh(float[] sens_valb, float deltime){
	// Bei Drehung neues Koordinationsystem auf altes umrechnen
	float newc[][] = new_coord(sens_valb, deltime);
 	for(int i = 0; i < 3;i++){
	    for(int j = 0; j < 3; j++){
		koord[j][i] = koord[0][i]*newc[j][0] + koord[1][i]*newc[j][1] + koord[2][i]*newc[j][2];
	    } 
	}       
    }
    private float[][] new_coord(float[] sens_val, float deltime){
	// Bei Drehung auf neue Koordinaten umrechnen
	// sens_val: Winkelgeschwindigkeit
	// deltime: Zeitdifferenz in s
	float ret[][] = new float[3][3];
	
	sens_val[0] = sens_val[0] * deltime;
	sens_val[1] = sens_val[1] * deltime;
	sens_val[2] = sens_val[2] * deltime;
	float[] tanval = new float[3];
	tanval[0] = (float)Math.tan(sens_val[0]);
	tanval[1] = (float)Math.tan(sens_val[1]);
	tanval[2] = (float)Math.tan(sens_val[2]);
	ret[0][0] = (float)Math.sqrt(1/(1+tanval[1]*tanval[1]+tanval[2]*tanval[2]));
	ret[0][1] = tanval[2] * ret[0][0];
	ret[0][2] = -tanval[1] * ret[0][0];

	ret[1][1] = (float)Math.sqrt(1/(1+tanval[0]*tanval[0]+tanval[2]*tanval[2]));
	ret[1][0] = tanval[2] * ret[1][1];
	ret[1][2] = -tanval[0] * ret[1][1];

	ret[2][2] = (float)Math.sqrt(1/(1+tanval[0]*tanval[0]+tanval[1]*tanval[1]));
	ret[2][0] = tanval[1] * ret[2][2];
	ret[2][1] = -tanval[0] * ret[2][2];
	return ret;
    }

}

