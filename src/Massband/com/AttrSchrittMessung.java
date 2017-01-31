package Massband.com;

public abstract class AttrSchrittMessung{
    
    /** Umrechnung Nanosekunden in Sekunden (Zeitstempel des Sensors sind in Nanosekunden **/
    protected final float NanoToSek = 1E-9f;

    protected boolean ErsteMessung;
    protected mass Berechnungsfunktionen;
    protected float LetzteMessung[];
    protected long TimeStamp; //Zeitstempel diese Messung
    protected long TimeStampBegin; //Zeitstempel letzte Messung

}