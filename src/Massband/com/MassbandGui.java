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

public class MassbandGui extends AnguloBase{
    /* Distanz messen ueber Integration Accerolermeter  */
    
    public MassbandGui(){
	super(R.layout.help_massband, R.id.help_massband_link);
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	mess = new IntegralMessung(this, kmess);
    }
    
    @Override
    public void onStart(){
	super.onStart();
	startKalibrierung();
    }

    public void onPause(){
	super.onPause();
	endMessung();
    }
    
    /**
     * Distanz setzen
     * @Distance Distanzen
     **/
    public void setDistance(float[] Distance) throws IndexOutOfBoundsException{
	TextView distance = (TextView) findViewById(R.id.distx);
	distance.setText(new Float(Distance[0]).toString());
	distance = (TextView) findViewById(R.id.disty);
	distance.setText(new Float(Distance[1]).toString());
	distance = (TextView) findViewById(R.id.distz);
	distance.setText(new Float(Distance[2]).toString());
	distance = (TextView) findViewById(R.id.distc);
	distance.setText(new Float(Distance[3]).toString());		
    }
}