package eu.domob.angulo;

import Massband.com;

public class Messung extends LayerMessung{
    /* Mess-Klasse als Zwischen-Instanz, um Messungen durchzufuehren */

    private TriAngulo GuiInterface; /* Gui Interface */
    private SchrittMessung Schritt;
    public Messung(Context context, TriAngulo GuiInterface, SchrittMessung kalibrierung){
	super.LayerMessung(context);
	this.distances = distances;	
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
