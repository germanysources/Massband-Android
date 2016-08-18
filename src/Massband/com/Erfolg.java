package Massband.com;

import android.app.*;
import android.widget.*;
import android.view.*;
import android.content.*;

public class Erfolg extends AlertDialog implements DialogInterface.OnClickListener{
   Erfolg(String s, Context m){
      super(m);
      setTitle("Erfolg");
      setMessage(s);
      setButton(BUTTON_POSITIVE, "OK", (OnClickListener) this);
   }
   public void onClick(DialogInterface d, int w){
      if(w == BUTTON_POSITIVE){
	 dismiss();
      }
   }
}