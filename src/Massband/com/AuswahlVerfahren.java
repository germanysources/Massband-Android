package Massband.com;

import android.content.Intent;
import android.content

public class AuswahlVerfahren extends Activity{
    /**
     * Bietet die Verfahrensauswahl aus, welche Messmethode soll gewaehlt werden
     **/    
    Intent MethodChooser;
    TriAngulo triAng;
    MassbandGui mass;
    
    public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.auswahl);
    }

    /**
     * Auwahl TriAngulo Messung
     **/
    public void onTriAnguloClicked(){
	TriAng = new TriAngulo();
	MethodChooser = new Intent((Context)this, triang);
	startActivity(MethodChooser);
    }

    /**
     * Auswahl Massband Messung
     **/
    public void onMassbandClicked(){
	mass = new MassbandGui();
	MethodChooser = new Intent((Context)this, mass);
	startActivity(MethodChooser);	    
    }


}