package Massband.com;

import android.app.*;
import android.widget.*;
import android.view.*;
import android.content.*;

public class Fehler extends AlertDialog implements DialogInterface.OnClickListener{
   Fehler(String s, Context m){
      super(m);
      setTitle("Fehler");
      setMessage(s);
      setButton(BUTTON_POSITIVE, "OK", (OnClickListener) this);
   }
   public void onClick(DialogInterface d, int w){
      if(w == BUTTON_POSITIVE){
	 dismiss();
      }
   }
}
