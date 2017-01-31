package Massband.com;

import android.hardware.*;

public interface SchrittMessung{

    public boolean OnSensorChanged(SensorEvent event)
	throws RuntimeException;
    public float[] Ende() throws RuntimeException;
    public mass getFunktionen();
}
