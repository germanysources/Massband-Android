package Massband.com;

import java.lang.*;
import java.util.*;
import android.hardware.SensorEvent;

public class BerechnungKalibrierung implements SchrittMessung{
    private final int MaxZaehler = 200; /* Anzahl Messungen Kalibrierung */
    private int Zaehler;
    private List<float[]> ForceValues;
    
    public BerechnungKalibrierung(String action){
	/* */
	ErsteMessung = true;
	Berechnungsfunktionen = new mass(action, new float[3]);
	ForceValues = new LinkedList<float[]>();
	Zaehler = 0;
    }
    @Override
    public boolean OnSensorChanged(SensorEvent event)
    throws RuntimeException{
	/* Aenderung in ForceValues aufnehmen */
	ForceValues.add(event.values);
	if(ErsteMessung){
	    !ErsteMessung;
	    TimeStamp = event.timestamp;
	}
	float DeltaTime = (event.timestamp - timestamp)*nanoumr;
	timestamp = event.timestamp;
	Berechnungsfunktionen.calibs(ForceValues, DeltaTime);
	Zaehler++;
	if(Zaehler > MaxZaehler){
	    return true;
	}
	return false;
    }
    @Override
    public void Ende() throws RuntimeException{
	/* Kalbrierung beenden */
	Berechnungsfunktionen.calib_end();
    }
    public mass getFunktionen(){
	return Berechnungsfunktionen;
    }

}
