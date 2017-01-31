package Massband.com;

import android.hardware.*;
import android.content.Context;

public class BerechnungWeg extends AttrSchrittMessung implements SchrittMessung{
    /* Messung mittels Integration Beschleunigungssensor */

    /** Anzahl Messungen werden jeweils gemittelt **/
    private final int AnzahlMess = 10;

    private KalibrierungMessung kalibrierung; 
    private MassbandGui GuiInterface;
    /** Buffer fuer Beschleunigungswerte **/
    private float Buffer[];
    /** Historie Buffer **/
    private float LetzterBuffer[];
    /** Anzahl Messung **/
    private int zaehler;
    /** Distanz **/
    private float Distance[];
    /** nur zur Berechnung Distanz **/
    private float PartDistance[];
    /** Zeit in Summe **/
    private float SumTime;
    /** Geschwindigkeit **/
    private float[] speed;
    /** Zeitstempel Messung Gyroscope **/
    private float TimeStampGyroScope;
    /** true, wenn Erste Messung Gyroscope **/
    private boolean ErsteMessungGyro;

    public BerechnungWeg(KalibrierungMessung kalibrierung, MassbandGui gui){
	/* */
	this.kalibrierung = kalibrierung;
	this.ErsteMessung = true;
	this.ErsteMessungGyro = true;
	this.GuiInterface = gui;
	LetzteMessung = new float[3];
	Buffer = new float[3];
	LetzterBuffer = new float[3];
	Distance = new float[4];
	PartDistance = new float[3];
	Berechnungsfunktionen = new mass((Context) gui);
    }
    
    @Override
    public boolean OnSensorChanged(SensorEvent event) throws RuntimeException{
	/* Sensor-Werte eines Sensors haben sich geaendert */
	switch(event.sensor.getType()){
	case Sensor.TYPE_ACCELEROMETER:
	    return AcceleroMeterChanged(event);
	case Sensor.TYPE_GYROSCOPE:
	    return GyroScopeChanged(event);
	default:
	    throw new RuntimeException(((Context)GuiInterface).getString(R.string.ESensorType)); //unbekannter Sensor
	}
	
    }

    private boolean AcceleroMeterChanged(SensorEvent event) throws RuntimeException{
	/* Sensor-Werte des Accelerometers haben sich geaendert */
	if(!ErsteMessung){	    
	    for(int i=0;i<Buffer.length;i++){
		Buffer[i] += (LetzteMessung[i] + event.values[i])*(event.timestamp - TimeStamp) * NanoToSek;		
	    }
	    TimeStamp = event.timestamp;
	}else{
	    ErsteMessung = false;
	    TimeStamp = event.timestamp;
	    TimeStampBegin = event.timestamp;
	}
	LetzteMessung = event.values;
	zaehler++;
	if(zaehler % AnzahlMess == 0 && !ErsteMessung){
	    /* Mittelwert aus den Buffer-Werten bilden */
	    float DifferenzTime = TimeStamp - TimeStampBegin;
	    NewDirection(DifferenzTime);
	    TimeStampBegin = TimeStamp;
	    Buffer = new float[3];
	}
	return false;
    }
    private void NewDirection(float DifferenzTime) throws RuntimeException{
	/* Neuen Weg berechnen, dazu die Beschleunigungswerte
	 zweimal integrieren */	
	float GlobalValues[] = Berechnungsfunktionen.beschl(Buffer); // Umrechnen lokale in globale Beschleunigung
	SumTime += DifferenzTime;
	for(int i=0;i<PartDistance.length;i++){
	    speed[i] += GlobalValues[i];
	    GlobalValues[i] = GlobalValues[i] / (2*DifferenzTime) + kalibrierung.add[i];
	    PartDistance[i] = (LetzterBuffer[i]*(SumTime-DifferenzTime) + GlobalValues[i]*SumTime)* DifferenzTime;	
	    Distance[i] = SumTime*speed[i] - PartDistance[i];
	}
	Distance[3] = (float)Math.sqrt(Distance[0]*Distance[0] + Distance[1]*Distance[1] + Distance[2]*Distance[2]);
	GuiInterface.setDistance(Distance);
	LetzterBuffer = GlobalValues;
	
    }
    
    /**
     * Sensor Gyroscope hat Werte geaendert
     * @return true wenn Messung beendet werden soll
     **/
    private boolean GyroScopeChanged(SensorEvent event){
	if(ErsteMessungGyro){
	    ErsteMessungGyro = !ErsteMessungGyro;
	    TimeStampGyroScope = event.timestamp;
	}else{
	    float DeltaTime = (event.timestamp - TimeStampGyroScope)*NanoToSek;
	    Berechnungsfunktionen.dreh(event.values, DeltaTime);
	}
	return false;
    }
    
    @Override
    public float[] Ende(){
	return new float[0];
    }
    @Override
    public mass getFunktionen(){
	return Berechnungsfunktionen;
    }
}