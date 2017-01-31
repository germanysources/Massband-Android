package Massband.com;

import java.util.*;
import java.lang.*;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.Sensor;

public class IntegralMessung extends LayerMessung{
    /** Messung mit Integration Beschleunigungssensor beginnen und beenden  **/
    
    private MassbandGui GuiInterface; /* Gui Interface */
    private KalibrierungMessung kalibrierung;
    private SchrittMessung Schritt;
    public IntegralMessung(MassbandGui GuiInterface, KalibrierungMessung kalibrierung){
	super((Context) GuiInterface);
	this.kalibrierung = kalibrierung;
	this.GuiInterface = GuiInterface;
	get_sensors(context);
    }

    protected synchronized void get_sensors(Context context) throws RuntimeException{
	int MustHave[] = {Sensor.TYPE_ACCELEROMETER};
	int Optional[] = {Sensor.TYPE_GYROSCOPE};
	super.get_sensors(context, MustHave, Optional);
    }
    @Override
    protected void new_messung(){
	Schritt = new BerechnungWeg(kalibrierung, GuiInterface);	
    }
    @Override
    public void onSensorChanged(SensorEvent event) throws RuntimeException{
	boolean AtEnd = Schritt.OnSensorChanged(event);
    }
}