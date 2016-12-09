package Massband.com;

import java.util.*;
import java.lang.*;
import android.content.Context;
import android.hardware.SensorEvent;

public class TriAnguloMessung extends LayerMessung{
    /* Zwischen-Instanz fuer Triangulation, um Messungen zu beginnen und anfangen */

    private TriAngulo GuiInterface; /* Gui Interface */
    private SchrittMessung Schritt;
    public TriAnguloMessung(TriAngulo GuiInterface, SchrittMessung kalibrierung){
	super.LayerMessung((Context) GuiInterface);
	this.Schritt = kalibrierung;
	this.GuiInterface = GuiInterface;
    }
    @Override 
    protected synchronized void get_sensors(Context context) throws RuntimeException{	
	int MustHave[] = {Sensor.TYPE_ACCELEROMETER};
	int Optional[] = new int[0];
	super.get_sensors(context, MustHave, Optional);
    }
    @Override
    protected void new_messung(){
	Schritt = new BerechnungMessung(action, Schritt.getFunktionen().calib_val);
    }
    @Override 
    public void onSensorChanged(SensorEvent event) throws RuntimeException{
	/*SensorWerte haben sich geaendert */
	boolean AtEnd = Schritt.onSensorChanged(event);
    }
}
