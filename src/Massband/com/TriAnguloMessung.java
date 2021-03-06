package Massband.com;

import java.util.*;
import java.lang.*;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.Sensor;

public class TriAnguloMessung extends LayerMessung{
    /* Zwischen-Instanz fuer Triangulation, um Messungen zu beginnen und anfangen */

    private TriAngulo GuiInterface; /* Gui Interface */
    private SchrittMessung Schritt;
    private KalibrierungMessung kalibrierung;
    public TriAnguloMessung(TriAngulo GuiInterface, KalibrierungMessung kalibrierung){
	super((Context) GuiInterface);
	this.kalibrierung = kalibrierung;
	this.GuiInterface = GuiInterface;
	get_sensors(context);
    }
    //@Override 
    protected synchronized void get_sensors(Context context) throws RuntimeException{	
	int MustHave[] = {Sensor.TYPE_ACCELEROMETER};
	int Optional[] = new int[0];
	super.get_sensors(context, MustHave, Optional);
    }
    @Override
    protected void new_messung(){
	Schritt = new BerechnungMessung(kalibrierung, GuiInterface);
    }
    @Override 
    public void onSensorChanged(SensorEvent event) throws RuntimeException{
	/*SensorWerte haben sich geaendert */
	boolean AtEnd = Schritt.OnSensorChanged(event);
    }
}
