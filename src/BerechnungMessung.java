package eu.domob.angulo;

public class BerechnungMessung implements SchrittMessung{
    /* Normale Messung mit Triangulation */
    private final int AnzahlMess = 10;
    
    private KalibrierungMessung kalibrierung;
    private TriAngulo GuiInterface;
    private Buffer[];
    private int zaehler;
    public BerechnungMessung(KalibrierungMessung kalibrierung, TriAngulo gui){
	/* add: Kalibrierungswerte, action:  */
	this.kalibrierung = kalibrierung;
	this.GuiInterface = gui;
	this.ErsteMessung = true;
	LetzteMessung = new float[3];
	Buffer = new float[3];
    }
    @Override
    public boolean OnSensorChanged(SensorEvent event) throws RuntimeException{
	/* return true: TextViews mit neuen Werten koennen upgedatet werden */
	if(!ErsteMessung){	    
	    for(int i=0;i<Buffer.length;i++){
		Buffer[i] += (LetzteMessung[i] + event.values[i])*(evt.timestamp - TimeStamp);
		
	    }
	    TimeStamp = evt.timestamp;
	}else{
	    TimeStamp = evt.timestamp;
	    ErsteMessung = false;
	    TimeStampBegin = evt.timestamp;
	}
	LetzteMessung = event.values;
	zaehler++;
	if(zaehler % AnzahlMess == 0 && !ErsteMessung){
	    /* Mittelwert aus den Buffer-Werten bilden */
	    for(int i=0;i<Buffer.length;i++){
		Buffer[i] = Buffer[i]/(2*(TimeStamp - TimeStampBeginn)) + kalibrierung.add[i];
	    }
	    newDirection(new Vector(Buffer));
	    TimeStampBegin = TimeStamp;
	    Buffer = new float[3];
	}
    }
    private void NewDirection(Vector val) throws RuntimException{
    /* We use a rectangular triangle for triangulation:  The horizontal floor
       up to the focused point, the orthogonal side corresponding to the user's
       height, and then the unknown / measured angle.  Thus the reference
       direction is always the third one (z-axis).  */
	final float[] refVals = {0.0f, 0.0f, 1.0f};
	final Vector ref = new Vector (refVals);
	final float angle = Math.abs (Vector.angle (ref, val));
	float dist;
	boolean isHeight;

    /* If the device is pointing up instead of down, measure heigth.  */
	if (angle >= Math.PI / 2.0f)
	    {
	  // andere Kalkulation jetzt wird Hoehe aus Laenge und Winkel bestimmt
	  // tan(angle - pi/2)*leng=x
		dist = GuiInterface.getLength() * Math.tan(angle - Math.PI/2.0);
		isHeight = true;
	    }
	else{
	    /* Do the calculation:

	   height / x = tan (pi/2 - angle)
	   x = height / tan (pi/2 - angle)
	*/

	    final float height = GuiInterface.getHeight ();
	    dist = height / Math.tan (Math.PI / 2.0 - angle);
	    isHeight = false;
	}
	GuiInterface.setLastState(dist, isHeight);
    }  

}