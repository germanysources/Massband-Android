package Massband.com;

import java.lang.*;
import java.util.*;
import android.hardware.SensorEvent;
import android.content.Context;

public class BerechnungKalibrierung extends AttrSchrittMessung implements SchrittMessung{
    /* Berechnungen waehrend Kalibrierung durchfuehren */
    private final int MaxZaehler = 200; /* Anzahl Messungen Kalibrierung */
    private int Zaehler;
    private List<float[]> ForceValues;
    
    public BerechnungKalibrierung(Context context){
	/* */
	ErsteMessung = true;
	Berechnungsfunktionen = new mass(context);
	ForceValues = new LinkedList<float[]>();
	Zaehler = 0;
    }
    @Override
    public boolean OnSensorChanged(SensorEvent event)
    throws RuntimeException{
	/* Aenderung in ForceValues aufnehmen */
	ForceValues.add(event.values);
	if(ErsteMessung){
	    ErsteMessung = !ErsteMessung;
	    TimeStamp = event.timestamp;
	}
	float DeltaTime = (event.timestamp - TimeStamp)*NanoToSek;
	TimeStamp = event.timestamp;
	Berechnungsfunktionen.calibs(ForceValues, DeltaTime);
	Zaehler++;
	if(Zaehler > MaxZaehler){
	    return true;
	}
	return false;
    }
    @Override
    public float[] Ende() throws RuntimeException{
	/* Kalbrierung beenden */
	return Berechnungsfunktionen.calib_end();
    }
    public mass getFunktionen(){
	return Berechnungsfunktionen;
    }

}
