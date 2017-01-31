package Massband.com;

import java.util.*;
import java.lang.*;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.Sensor;

public class KalibrierungMessung extends LayerMessung{
    /* Kalibrierungs-Klasse als Zwischen-Instanz, um Kalibrierung und Messungen durchzufuehren*/

    private SchrittMessung Schritt;
    public boolean isInAction;
    public KalibrierungMessung(Context context){	
	super(context);
	get_sensors(context);
    }     
    protected synchronized void get_sensors(Context context) throws RuntimeException{	
	int MustHave[] = {Sensor.TYPE_ACCELEROMETER};
	int Optional[] = new int[0];
	super.get_sensors(context, MustHave, Optional);
    }
    @Override
    protected void new_messung(){
	Schritt = new BerechnungKalibrierung(context);
    }
    @Override
    public synchronized void mess_beg(){
	isInAction = true;
	super.mess_beg();
    }
    //@Override
    public synchronized void mess_end(RuntimeException exp){
	/* Kalibrierung beenden */
	isInAction = !isInAction;
	super.mess_end(); // SensorEventListener ausschalten
	
	if(exp == null){
	    try{
		add = Schritt.Ende();
	    }catch(RuntimeException e){
		Fehler fe = new Fehler(context.getString(R.string.Ecalib), context);
		fe.show();
		return;
	    }
	    try{
		write_calib(context); // Als SharedPreferences ablegen
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
	    Fehler fe = new Fehler(exp.getMessage(), context);
	    fe.show();
	} 
    }
    @Override 
    public void onSensorChanged(SensorEvent event) throws RuntimeException{
	/*SensorWerte haben sich geaendert */
	try{
	    boolean AtEnd = Schritt.OnSensorChanged(event);
	    if(AtEnd){
		/*Kalibrierung ist beendet */
		mess_end(null);
	    }
	}catch(RuntimeException e){
	    /*Nur waehrend Kalibrierung zu werfen, wenn diese fehlerhaft */
	    mess_end(e);
	}	
    }
}
