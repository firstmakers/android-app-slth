package cl.austral38.slthv2;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SplashScreenActivity extends Activity {
	TextView title1;
	TextView title2;
	private long splashDelay = 4000; //4 segundos
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_splash_screen);

		 
		    TimerTask task = new TimerTask() {
		      @Override
		      public void run() {
		        Intent mainIntent = new Intent().setClass(SplashScreenActivity.this, MainActivity.class);
		        startActivity(mainIntent);
		        //overridePendingTransition(R.anim.entrada, R.anim.salida);
		        finish();//Destruimos esta activity para prevenir que el usuario retorne aqui presionando el boton Atras.
		      }
		    };

		    Timer timer = new Timer();
		    timer.schedule(task, splashDelay);//Pasado los 5 segundos dispara la tarea
	}

}
