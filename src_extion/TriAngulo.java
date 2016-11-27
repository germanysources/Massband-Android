/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2013-2014  Daniel Kraft <d@domob.eu>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.domob.angulo;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.hardware.Sensor;

import android.os.Bundle;

import android.util.AttributeSet;
import android.util.Log;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity for the triangulation-feature.  Display a live camera image with
 * overlaid cross-hair, and calculate the approximate distance to the target
 * using triangulation.
 */
public class TriAngulo extends AnguloBase
{
    
    // Anzahl Messwerte, nachdem neuer Wert ausgegeben wird
    private final int AnzahlMess = 10;
  /** View for distance display.  */
  private TextView distance;
    private double dist; // Distance to rembemer

  /** View for additional info display.  */
  private TextView info;
    //Laenge
    private float leng;
    private boolean MesLength; //true: Laenge gemessen
    //Activity von aussen gestartet
    private Activity ExtAct;
    // Kalibrierungswerte, sollten zu den Sensor-Werten addiert werden
    private float[] kalibrierung;  
    private float[] LetzteMessung; // Werte letzte Messung
    
  /**
   * Construct it with the proper help dialog IDs.
   */
  public TriAngulo ()
  {
    super (R.layout.help_triangulo, R.id.help_triangulo_link);
  }
    public TriAngulo(float leng, Activity act, SensorManager sm, float[] kalibrierung){
	super (R.layout.help_triangulo, R.id.help_triangulo_link);	
	this.leng = leng;
	this.ExtAct = act;
	this.sensorManager = sm;	
	this.kalibrierung = kalibrierung;
    }    
    
    public void StartFromExtern(){
	// Aufruf von Extern
	setContentView (R.layout.triangulo);
	     
	CameraPreview prev = (CameraPreview) findViewById (R.id.preview);
	prev.setActivity (ExtAct);

	distance = (TextView) findViewById (R.id.distance);
	info = (TextView) findViewById (R.id.info);
	resumeact();
    }
    public void BackToExtern(int layout){
	// Zurueck zu Extern
	stopact();
	setContentView(layout);
    }
  /**
   * Create the activity with initialisation of things.
   * @param savedInstanceState Saved data (if any).
   */
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    setContentView (R.layout.triangulo);

    CameraPreview prev = (CameraPreview) findViewById (R.id.preview);
    prev.setActivity (this);

    distance = (TextView) findViewById (R.id.distance);
    info = (TextView) findViewById (R.id.info);
  }

  /**
   * Handle new direction value to update the distance.
   * @param type Sensor type.
   * @param val New direction vector.
   */
  @Override
  protected void newDirectionValue (int type, float[] val, long DetlaTime)
  {
    super.newDirectionValue (type, val);
    // bestimmte Anzahl Werte buffern und Mittelwert daraus nehmen
    // Kalibrierungsdaten dazuzaehlen     
    if(!ErsteMessung){
	for(int i=0;i<Buffer.length;i++){
	    Buffer[i] += (LetzteMessung[i] + val[i])*DeltaTime;
	}	
    }   
    LetzeMessung = val;
    zahler++;
    if(zahler % AnzahlMess == 0 && !ErsteMessung){
	for(int i=0;i<Buffer.length;i++){
	    Buffer[i] = Buffer[i]/(2*(TimeStamp - TimeStampBeginn)) + kalibrierung[i];
	}
	newDirection(type, new Vector(val));
	TimeStampBeginn = TimeStamp;	
	Buffer = new float[3];
    }

  }
    private void newDirection(int type, Vector val){         
	// Nachdem ausreichend Werte gepuffert wurden, 
	// neuen Messwert mit Mittelwert der gepufferten Werte berechnen
    /* We only want gravity measurements here.  */
    if (type != Sensor.TYPE_ACCELEROMETER)
	Log.w(TAG, "Nur Sensor Accelerometer verwenden");
	return;

    /* We use a rectangular triangle for triangulation:  The horizontal floor
       up to the focused point, the orthogonal side corresponding to the user's
       height, and then the unknown / measured angle.  Thus the reference
       direction is always the third one (z-axis).  */
    final float[] refVals = {0.0f, 0.0f, 1.0f};
    final Vector ref = new Vector (refVals);
    final float angle = Math.abs (Vector.angle (ref, val));

    /* If the device is pointing up instead of down, measure heigth.  */
    if (angle >= Math.PI / 2.0f)
	{
	    if(leng == 0){
		distance.setText ("-");
		Log.w (TAG, "Length wasn't set.");
		return;
	    }
	  // andere Kalkulation jetzt wird Hoehe aus Laenge und Winkel bestimmt
	  // tan(angle - pi/2)*leng=x
	  dist = leng * Math.tan(angle - Math.PI/2.0);
	  MesLength = false;
	  
      }
    else{
	/* Do the calculation:

	   height / x = tan (pi/2 - angle)
	   x = height / tan (pi/2 - angle)
	*/

	final float height = getHeight ();
	dist = height / Math.tan (Math.PI / 2.0 - angle);
	MesLength = true;
    }

    String fmt = getString (R.string.measure_distance);
    distance.setText (String.format (fmt, dist));
    fmt = getString (R.string.measure_info);
    info.setText (String.format (fmt, height));
  }  
 
  /**
   * Get the measurement height from preferences.
   * @return Measurement height for calculation.
   */
  private float getHeight ()
  {
    final String val = pref.getString (SetHeight.PREF_KEY, "");
    try
      {
        return Float.parseFloat (val);
      }
    catch (NumberFormatException exc)
      {
        final String def = getString (R.string.height_default);
        Log.w (TAG,
               String.format ("Invalid height preference string %s, using %s.",
                              val, def));
        return Float.parseFloat (def);
      }
  }

  /* ************************************************************************ */
  /* OnTop.  */

  /**
   * View that does the "on top" drawing over the camera preview.
   */
  public static class OnTop extends ImageView
  {

    /**
     * Construct it.
     * @param c Our context.
     */
    public OnTop (Context c)
    {
      super (c);
    }

    /**
     * Construct it.
     * @param c Our context.
     * @param attr Attributes.
     */
    public OnTop (Context c, AttributeSet attr)
    {
      super (c, attr);
    }

    /**
     * Construct it.
     * @param c Our context.
     * @param attr Attributes.
     * @param ds DefStyle.
     */
    public OnTop (Context c, AttributeSet attr, int ds)
    {
      super (c, attr, ds);
    }

    /**
     * Update the on-top drawing.
     */
    @Override
    protected void onDraw (Canvas canv)
    {
      final int w = getWidth ();
      final int h = getHeight ();
      /*
      Log.v (AnguloBase.TAG,
             String.format ("Performing on-top drawing on %dx%d surface.",
                            w, h));
      */

      final Paint paint = new Paint (Paint.ANTI_ALIAS_FLAG);
      paint.setStyle (Paint.Style.STROKE);
      paint.setStrokeWidth (3);
      paint.setColor (Color.RED);

      final float cx = w / 2.0f;
      final float cy = h / 2.0f;
      final float rad = Math.min (w, h) / 20.0f;

      canv.drawCircle (cx, cy, rad, paint);
      canv.drawLine (0.0f, cy, cx - rad, cy, paint);
      canv.drawLine (cx + rad, cy, w, cy, paint);
      canv.drawLine (cx, h * 0.15f, cx, cy - rad, paint);
    }

  }

}
