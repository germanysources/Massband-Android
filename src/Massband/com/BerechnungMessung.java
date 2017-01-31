package Massband.com;

import java.lang.*;
import java.util.*;
import android.hardware.SensorEvent;

public class BerechnungMessung extends AttrSchrittMessung implements SchrittMessung{
    /* Normale Messung mit Triangulation */
    private final int AnzahlMess = 10;
    
    private KalibrierungMessung kalibrierung;
    private TriAngulo GuiInterface;
    private float Buffer[];
    private int zaehler;
    public BerechnungMessung(KalibrierungMessung kalibrierung, TriAngulo gui){
	/*  */
	this.kalibrierung = kalibrierung;
	this.GuiInterface = gui;
	this.ErsteMessung = true;
	LetzteMessung = new float[3];
	Buffer = new float[3];
    }
    @Override
    public boolean OnSensorChanged(SensorEvent event) throws RuntimeException{
	/* Sensor-Werte haben sich geaendert */
	if(!ErsteMessung){	    
	    for(int i=0;i<Buffer.length;i++){
		Buffer[i] += (LetzteMessung[i] + event.values[i])*(event.timestamp - TimeStamp);		
	    }
	    TimeStamp = event.timestamp;
	}else{
	    TimeStamp = event.timestamp;
	    ErsteMessung = false;
	    TimeStampBegin = event.timestamp;
	}
	LetzteMessung = event.values;
	zaehler++;
	if(zaehler % AnzahlMess == 0 && !ErsteMessung){
	    /* Mittelwert aus den Buffer-Werten bilden */
	    for(int i=0;i<Buffer.length;i++){
		Buffer[i] = Buffer[i]/(2*(TimeStamp - TimeStampBegin)) + kalibrierung.add[i];
	    }
	    NewDirection(new Vector(Buffer));
	    TimeStampBegin = TimeStamp;
	    Buffer = new float[3];
	}
	return false;
    }
    private void NewDirection(Vector val) throws RuntimeException{
    /* We use a rectangular triangle for triangulation:  The horizontal floor
       up to the focused point, the orthogonal side corresponding to the user's
       height, and then the unknown / measured angle.  Thus the reference
       direction is always the third one (z-axis).  */
	final float[] refVals = {0.0f, 0.0f, 1.0f};
	final Vector ref = new Vector (refVals);
	final float angle = Math.abs (Vector.angle (ref, val));
	float dist;
	boolean isHeight;

    /* If the device is pointing up instead of down, measure heigth.  */
	if (angle >= Math.PI / 2.0f)
	    {
		/* andere Kalkulation jetzt wird Hoehe aus Laenge und Winkel bestimmt
		   tan(angle - pi/2)*leng=x */
		dist = GuiInterface.getLength() * (float)Math.tan(angle - Math.PI/2.0);
		isHeight = true;
	    }
	else{
	    /* Do the calculation:

	   height / x = tan (pi/2 - angle)
	   x = height / tan (pi/2 - angle)
	*/

	    final float height = GuiInterface.getHeight ();
	    dist = height / (float)Math.tan (Math.PI / 2.0 - angle);
	    isHeight = false;
	}
	GuiInterface.setLastState(dist, isHeight);
    }  

    @Override
	public float[] Ende(){
	return new float[0];
    }

    @Override
    public mass getFunktionen(){
	return null;
    }
}
