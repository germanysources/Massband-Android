package eu.domob.angulo;

public interface SchrittMessung{

    protected boolean ErsteMessung;
    protected mass Berechnungsfunktionen;
    protected float LetzteMessung[];
    protected long TimeStamp; //Zeitstempel diese Messung
    protected long TimeStampBegin; //Zeitstempel letzte Messung

    public boolean OnSensorChanged(SensorEvent event)
	throws RuntimeException;
    public void Ende() throws RuntimeException;
    public mass getFunktionen();
}