package Massband.com;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.view.View.*;
import java.util.*;
import android.content.*;
import android.content.res.*;
import java.io.*;
import android.util.Log;

import java.io.*;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.content.Context;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import android.util.Log;



public class gui_massb extends Activity{
    refresh_dist rd;
   final long refresht = 2000; //Zeit zum aktualisieren GUI
   final long calibt = 5000; // Zeit Kalibrierung
    protected void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);	
	try{
	    rd = new refresh_dist(refresht, (Activity) this);
	    rd.massb.load_calib();
	}
	catch(Exception e){
	    String st = e.toString();
	    StackTraceElement[] ste = e.getStackTrace();
	    for(int i = 0; i < ste.length; i++){
		st = st.concat(ste[i].toString() + "\n");
	    }
	    Fehler fe = new Fehler(st, this);
	    fe.show();
	}	
    }
   public void onPause(){
      super.onPause();
      rd.started = false;
      rd.massb.mess_end();
   }
   public void startm(View view){
      try{
	 is_calibrated();
      }catch(RuntimeException e){
	 Fehler fe = new Fehler(e.getMessage(), this);
	 fe.show();
	 return;
      }
	try{	   
	   
	   rd.started =! rd.started;
	   if(rd.started){
	    	for(int i = 0; i < rd.dist.length; i++){
	    	    rd.dist[i].setText("");
	    	}		
	    	rd.massb.mess_beg(mass.DISTANCEM);
	   	if(rd.getState() == Thread.State.NEW)
	   	   rd.start();
	   	else{
	   	   synchronized(rd){
	   	      rd.notify();
	   	   }
	   	}
	    }
	    else{
	    	rd.massb.mess_end();
	    }
	}
	catch(Exception e){
	    String st = e.toString();
	    StackTraceElement[] ste = e.getStackTrace();
	    for(int i = 0; i < ste.length; i++){
		st = st.concat(ste[i].toString() + "\n");
	    }
	    Fehler fe = new Fehler(st, this);
	    fe.show();
	}

    }
   private void is_calibrated() throws RuntimeException{
      if(rd.massb.corf_mass.action.equals(mass.CALIB)){
	  throw new RuntimeException(new Integer(R.string.Estart).toString()); //gerade am Kalibrieren
      }
   }
}
class refresh_dist extends Thread{
    public boolean started;
    public TextView dist[];
    public massband massb;
    private Activity act;
    private long refresht;
    public refresh_dist(long refresht, Activity ct) throws
	RuntimeException, InterruptedException, FileNotFoundException,
    IOException{
	dist = new TextView[4];
	dist[0] = (TextView) ct.findViewById(R.id.distx);
	dist[1] = (TextView) ct.findViewById(R.id.disty);
	dist[2] = (TextView) ct.findViewById(R.id.disty);
	dist[3] = (TextView) ct.findViewById(R.id.distc);	
	massb = new massband((Context) ct);
	
	this.refresht = refresht;
	this.act = ct;
    }
    public void run(){
	while(true){
	    try{
	       if(!started){
		  synchronized(this){
		     wait();
		  }
	       }
	       this.sleep(refresht);
	    }
	    catch(InterruptedException e){
		String st = e.toString();
		StackTraceElement[] ste = e.getStackTrace();
		for(int i = 0; i < ste.length; i++){
		    st = st.concat(ste[i].toString() + "\n");
		}
		Fehler fe = new Fehler(st, act);
		fe.show();
		break;
	    }
	    act.runOnUiThread(new Runnable() {
		  public void run(){
		     for(int i = 0; i < dist.length; i++){
			dist[i].setText(new Float(massb.corf_mass.distance[i]).toString());
		     }
		  }
	       });
	}
    }
}
