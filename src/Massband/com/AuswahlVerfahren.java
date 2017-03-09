package Massband.com;

import android.content.*;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import java.lang.*;

public class AuswahlVerfahren extends Activity{
    /**
     * Bietet die Verfahrensauswahl aus, welche Messmethode soll gewaehlt werden
     **/    
    Intent MethodChooser;
    public final String TriAng = "TriAngulo",
	mass = "MassbandGui";
    /*    TriAngulo TriAng;
	  MassbandGui mass;*/
    
    public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	setContentView(R.layout.auswahl);
    }

    /**
     * Auwahl TriAngulo Messung
     **/
    public void onTriAnguloClicked() throws ClassNotFoundException{
	MethodChooser = new Intent((Context)this, Class.forName(TriAng));
	startActivity(MethodChooser);
    }

    /**
     * Auswahl Massband Messung
     **/
    public void onMassbandClicked() throws ClassNotFoundException{
	MethodChooser = new Intent((Context)this, Class.forName(mass));
	startActivity(MethodChooser);	    
    }


}