package Massband.com;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import java.util.List;
class SensorEventListenerMy implements SensorEventListener{
    protected long timestamp; 
    final float nanoumr = 1E-9f;
    protected float[] max_rot;
    protected float[] ist_rot;
    List<float[]> valb;
    public mass corf_mass;
    public SensorEventListenerMy(float[] max_rot, String action, float[] add){
	timestamp = 0;	
	this.max_rot = max_rot;
	ist_rot = new float[3];
	valb.clear();
	float init_a[] = new float[3];
	valb.add(init_a);
	corf_mass = new mass(action, add);
	
    }
    public void onSensorChanged(SensorEvent event)
    throws RuntimeException{
	float del_time = (event.timestamp - timestamp)*nanoumr;
	timestamp = event.timestamp;
	if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	    valb.add(event.values);
	    if(corf_mass.action == mass.CALIB){
		corf_mass.calibs(valb, del_time);
	    }
	    else{
		corf_mass.inte_beschl(valb, del_time);
	    }
	    if(corf_mass.action == mass.CALIB){
		for(int i = 0; i<3; i++){
		    if(Math.abs(corf_mass.distance[i]) > max_rot[1]){
			throw new RuntimeException("Handy waehrend Kalibrierung nicht bewegen");
		    }
		}
	    }

	}
	else{
	    if(corf_mass.action == mass.CALIB){
		ist_rot[0] += event.values[0] * del_time;
		ist_rot[1] += event.values[1] * del_time;
		ist_rot[2] += event.values[2] * del_time;
		if(Math.abs(ist_rot[0]) > max_rot[0]){
		    throw new RuntimeException("Handy waehrend Kalibrierung nicht bewegen");
		}
		if(Math.abs(ist_rot[1]) > max_rot[0]){
		    throw new RuntimeException("Handy waehrend Kalibrierung nicht bewegen");
		}
		if(Math.abs(ist_rot[2]) > max_rot[0]){
		    throw new RuntimeException("Handy waehrend Kalibrierung nicht bewegen");
		}
	    }
	    else{
		corf_mass.dreh(event.values, del_time);
	    }
	}
	
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy){
	
    }
}
