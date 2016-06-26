package Massband.com;

import java.lang.Math.*;
import java.util.*;
import android.hardware.SensorManager;
import android.util.Log;

public class mass{
    public final static String CALIB = "C", SPEEDM = "S", DISTANCEM = "D";//was wird gemessen	  
    private final float gc[] = {0.0f, 0.0f, -9.81f};
    private int LengthSensorValues = 10;//Sensor-Werte werden erst gemittelt und dann integriert zum Weg
    // dient zur Fehlerverbesserung, 10 Werte sind im Mittel genauer als nur 1 Wert

    float g[] = gc; // Erdbeschleunigung
    float calib_val[] = {0, 0, 0}; // Kalibrierungswerte
    float distance[] = new float[4];
    float speed[] = new float[3];
    float distancePart[] = new float[3];
    float amem[] = new float[3]; //Zwischenspeicher durchschnitt Beschleunigung
    float amemlast[] = new float[3];//Zwischenspeicher durchschnitt Beschleunigung letzte Messungen
    float timSumMem;
    float tim_sum = 0; // Zeitsumme nach jeder Messung in Sekunden
    String action;
    List<float[]> testc;//nur zum Test, damit Kalibrierungsmethode stimmt, sollte einigermassen Parabelfoermig sein

    public mass(String action, float[] calib_val){
	distance = new float[4];
	speed = new float[3];
	distancePart = new float[3];
	amem = new float[3];
	amemlast = new float[3];
	tim_sum = 0;
	timSumMem = 0;
	g[0] = gc[0];
	g[1] = gc[1];
	g[2] = gc[2];
	this.calib_val = calib_val;
	this.action = action;
	testc = new ArrayList<float[]>();
    }
    public void calibs(List<float[]> valb, float deltime) throws
	RuntimeException{
	// ein Schritt zum Kalibrieren
	// Beschleunigung zum Weg integrieren
	//deltime: Zeitdifferenz fuer Aenderung der Besschleunigung	
	int len = valb.size() - 1;
	float be1[] = beschl(valb.get(len-1));
	float be2[] = beschl(valb.get(len));       
		
	tim_sum += deltime;
	for(int i= 0; i<3;i++){
	    String mes = "Messwerte \t" + new Float(be1[i]).toString() + "\t" + new Float(be2[i]).toString() + "\t" + new Float(deltime).toString(); 
	    Log.d("gui_massb",mes);
	    speed[i] += deltime * 0.5f * (be1[i]+be2[i]);	      
	    mes = "Weg, Geschw \t" + new Float(distance[i]).toString() + "\t" + speed[i];
	    Log.d("gui_massb", mes);
	}
    }
 
    public void calib_end() throws RuntimeException{ 
	float rot[] = new float[9];
	float mrot[] = new float[9];
	float gemag[] = new float[3];
	gemag[0] = 20;
	gemag[1] = 0;
	gemag[2] = 44;
	for(int i = 0; i<3;i++){
	   calib_val[i] = speed[i] / tim_sum;
	}
	boolean rc = SensorManager.getRotationMatrix(rot, mrot, calib_val, gemag);
	if(rc == true){	
	    // Umrechnen in globalen Koordinaten, 
	    // Mittelwert Beschleunigung mit Gravitation-Komponenten
	    // Moeglich waere: Kalibrierungskomponente in Handykoordinationsystem
	    // + Gravitation
	    // Hier wird baer davon ausgegangen, dass Kalibrierungskomponente
	    // im globalen Koordinationsystem immer gleich ist
	    
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
	else{
	    throw new RuntimeException(new Integer(R.string.Ecalib_end).toString());
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
		valb.remove(i);
	    }
	    distance[3] = (float)Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+
					   distance[2]*distance[2]);    
	}
	
    }
    private void test_log(List<float[]> valb){
	// Nur zum Testen: mitloggen der Werte
	float t1[] = new float[4];
	Log.d("gui_massb", "Weg \t Gewschindigkeit \t Zeit");
	for(int i = 0;i<3;i++){
	    Log.d("gui_massb",distance[i] + "\t" + speed[i] + "\t" + tim_sum);
	    t1[i] = distance[i];	    
	    //Kalibrierungsmethode ist in Ordnung
	    //wenn hinterher der Parabelfit einigermassen passt
	}
	t1[3] = tim_sum;
	testc.add(t1);

    }
    public void test_end_log(){
	// Ende Logging Ausgleichsparabel bestimmen
	// und Standardabweichung berechnen
	float sum1[] = new float[3], sum2[] = new float[3], sum3[] = new float[3];
	for(int i=0;i<testc.size();i++){
	    for(int j =0;j<3;j++){
		sum1[j] += testc.get(i)[j]*testc.get(i)[3]*testc.get(i)[3];
		sum2[j] += (float)Math.pow(testc.get(i)[3], 4);
		sum3[j] += testc.get(i)[j]*testc.get(i)[j];
	    }
	}
	float pko[] = new float[3];
	for(int i =0;i<3;i++){
	    pko[i] = sum1[i]/sum2[i];
	    pko[i] = ( sum3[i] - pko[i]*2*sum1[i] + pko[i]*pko[i]*sum2[i])/testc.size(); 
	    Log.d("gui_massb", "Standardabweichung Parabel \t" + i + "\t" + pko[i]);
	}

    }
    public void inte_beschlo(List<float[]> valb, float deltime){
	// Beschleunigung zum Weg integrieren
	//deltime: Zeitdifferenz fuer Aenderung der Besschleunigung
	//ohne Zwischensumme
	int len = valb.size() - 1;
	float be1[] = beschl(valb.get(len-1));
	float be2[] = beschl(valb.get(len));       
	for(int i=0;i<3;i++){
	    be1[i] += getCalibVal(i) + g[i];
	    be2[i] += getCalibVal(i) + g[i];
	}
	tim_sum += deltime;
	for(int i= 0; i<3;i++){
	   //String mes = "Messwerte \t" + new Float(be1[i]).toString() + "\t" + new Float(be2[i]).toString() + "\t" + new Float(deltime).toString(); 
	   //Log.d("gui_massb",mes); \
	    speed[i] += deltime * 0.5f * (be1[i]+be2[i]);
	    distance[i] += tim_sum * speed[i] - 0.5f*((tim_sum-deltime)*be1[i]+tim_sum*be2[i])*deltime;
	    String mes = "Weg, Geschw \t" + new Float(distance[i]).toString() + "\t" + speed[i];
	    Log.d("gui_massb", mes);
	}
	distance[3] = (float)Math.sqrt(distance[0]*distance[0]+distance[1]*distance[1]+
				distance[2]*distance[2]);
	
    }
    private float koord[][] = {{1, 0, 0},{0,1,0},{0,0,1}};//Umrechnungsmatrix in
    // Spalten
    public float[] beschl(float[] sens_val){
	// Beschleunigung in Ursprungskoordinaten
	// sens_val: Beschleunigung vom Sensor
	// sens_valb: Winkelgeschwindigkeit vom Sensor
	// deltime: Zeitdifferenz
	
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

