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
    /** View for distance display.  */
    public TextView distance;
 
    /** View for additional info display.  */
    private TextView info;
    /** true, if mesaurement is height, false is length **/
    private boolean isHeight;
    /** Laenge letzter Messung **/
    private float length;
    /** true, if length was hold **/
    private float LengthIsSet;
	
    /**
     * Construct it with the proper help dialog IDs.
     */
    public TriAngulo ()
    {
	super (R.layout.help_triangulo, R.id.help_triangulo_link);
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
     * Laenge aus letzer Messung holen
     * @return Laenge aus letzer Messung
     **/
    public float getLength() throws RuntimeException{
	if(!LengthIsSet){
	    throw new RuntimeException("Lenght wasn't set");
	}
	return length;
    }
    /**
     * Laenge aus Messung setzen
     **/
    public void setLastState(float length; boolean isHeight){
	String fmt = getString (R.string.measure_distance);
	distance.setText (String.format (fmt, dist));
	fmt = getString (R.string.measure_info);
	info.setText (String.format (fmt, height));

	if(LengthIsSet){
	    return;
	}
	this.length = length;
	this.isHeight = isHeight;
    }
    /**
     * Hold the length to measure height later
     **/
    public void HoldLength(){
	if(length == 0 || isHeight){
	    /*Laenge > 0 setzen */
	    return;
	}
	LengthIsSet = true;
    }
    
    /**
     * Get the measurement height from preferences.
     * @return Measurement height for calculation.
     */
    public float getHeight ()
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
