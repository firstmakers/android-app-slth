package cl.austral38.slthv2;

import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cl.austral38.slthv2.datalogger.StorageManager;
import cl.austral38.slthv2.models.Humidity;
import cl.austral38.slthv2.models.Light;
import cl.austral38.slthv2.models.MessageTxt;
import cl.austral38.slthv2.models.Temperature;

public class MainActivity extends ActionBarActivity {
	
	protected static final CharSequence RESET = "0.0";
	protected static final CharSequence INICIAR = "INICIAR";
	protected static final CharSequence DETENER = "DETENER";
	protected static final String DECIMALES = "%.1f";
	private SLT1 slt1 = null;
	private PendingIntent pendingIntent = null;
	private TextView tvTempertura;
	private TextView tvLuz;
	private TextView tvHumedad;
	private Button btnIniciar;
	private Button btnAustral;
    private StorageManager mStoredData;
    Toast toast = null;
    private static String  USB_PERMISSION = "";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_device);
        USB_PERMISSION = getPackageName() + ".USB_PERMISSION";
	      final android.app.ActionBar actionBar = getActionBar();
	        actionBar.setCustomView(R.layout.custom_actionbar);
	        actionBar.setDisplayShowTitleEnabled(false);
	        actionBar.setDisplayShowHomeEnabled(false);
	        actionBar.setDisplayShowCustomEnabled(true);


	}

	/*
	 * Evitar que se reinicie la actividad cada vez que se gira la pantalla
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {		
		super.onConfigurationChanged(newConfig);
	}

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            /*** Desde el intent de conexión ***/
            Log.d("ON_RESUME", "Evento USB ATTACHED");
            UsbDevice device = (UsbDevice) intent
                    .getParcelableExtra(UsbManager.EXTRA_DEVICE);
            slt1 = loadSLT1(device);

        } else {
            /**clickeando app **/
            Log.d("ON_RESUME", "Listando dispositivos");
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            while (deviceIterator.hasNext()) {
                slt1 = loadSLT1(deviceIterator.next());
                if (slt1 != null) {
                    break;
                }
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(receiver, filter);

        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(getPackageName() + ".USB_PERMISSION"), 0);
    }


    @Override
	protected void onPause() {
        if(slt1 != null) {
            slt1.close();
        }if(mStoredData!=null)
            mStoredData.destroyStorage();
		slt1 = null;
    	unregisterReceiver(receiver);
		super.onPause();
	}
	
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		if(msg.obj.getClass().equals(MessageTxt.class)){
    			raiseMessage(((MessageTxt)msg.obj).message);
    		}
    		else if(msg.obj.getClass().equals(Temperature.class)){
				setTemperature(((Temperature)msg.obj).temperature);
			}
            else if(msg.obj.getClass().equals(Light.class)) {
				setLight(((Light)msg.obj).light);
            }
            else if(msg.obj.getClass().equals(Humidity.class)){
                setHumidity(((Humidity)msg.obj).humidity);
            }
    	} //handler

    };
	
    BroadcastReceiver receiver = new BroadcastReceiver() {    
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();

    		if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (device != null) {
    				synchronized(slt1) {
    					if(slt1 != null) {
    						slt1.close();
		    				slt1 = null;
    					}

                        if(mStoredData!=null)
                            mStoredData.destroyStorage();
    				}
    		        setContentView(R.layout.no_device);
    			}
    		}
    	}
    };
	
	private SLT1 loadSLT1(UsbDevice device) {

		SLT1 tempSLT1 = SLT1.loadSLT1(this.getApplicationContext(), device,
				handler);
		if (tempSLT1 != null) {
			setContentView(R.layout.activity_main);
			tvTempertura = (TextView)findViewById(R.id.tv_valor_temperatura);
			tvLuz = (TextView)findViewById(R.id.tv_valor_luminosidad);
			tvHumedad = (TextView)findViewById(R.id.tv_valor_humedad);
			btnIniciar = (Button) findViewById(R.id.btn_iniciar);
			btnAustral = (Button) findViewById(R.id.btn_austral);
			setButtonStart();//configura la funci�n del boton iniciar.
			setButtonLink();
		}
		return tempSLT1;
	}
	
	private void setButtonLink(){
		btnAustral.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.austral38.cl"));
				startActivity(viewIntent);  	
			}
		});
	}
	
	private void setButtonStart(){
		btnIniciar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(slt1!= null){
					if(btnIniciar.getText().equals(INICIAR)){
                        start();
					}else {
                        stop();
					}					
				}
			}
		});
	}

    private void start(){

        mStoredData = new StorageManager(this, slt1, handler);
        btnIniciar.setText(DETENER);
        mStoredData.startCapture();
        slt1.start();
    }
    private void stop(){
        btnIniciar.setText(INICIAR);
        slt1.stop();
        mStoredData.stopCapture();
        showSaveDialog();

    }

    private void showSaveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_dialog);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mStoredData.save();
                //mStoredData = null;
            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mStoredData = null;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

	private void setLight(double light) {
		String l = String.format(DECIMALES, light);
		tvLuz.setText(l+ " Lux");
		//guardarlo en una coleccion
	}

	private void setTemperature( double temperature) {
		String t = String.format(DECIMALES, temperature);//dos decimales
		tvTempertura.setText(t+ " °");
		//guardarlo en una colección
	}

	private void raiseMessage(String[] message) {

		if(message[0].equals("1") && toast == null){
			toast = Toast.makeText(this, message[1], Toast.LENGTH_LONG);
            toast.show();
		}
        if(message[0].equals("3")){
            stop();
        }
	}

    private void setHumidity(double humidity){
        String h = String.format(DECIMALES,humidity);
        if(humidity < 4)
            tvHumedad.setTextColor(getResources().getColor(R.color.dry));
        else if(humidity > 4 && humidity < 8)
            tvHumedad.setTextColor(getResources().getColor(R.color.moist));
        else
            tvHumedad.setTextColor(getResources().getColor(R.color.wet));
        tvHumedad.setText(h);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if(id == R.id.list_data){
            startActivity(new Intent(this, ListFile.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void GetUSBPermission(UsbDevice usbDevice){
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbManager.requestPermission(usbDevice, pendingIntent);
    }
}


