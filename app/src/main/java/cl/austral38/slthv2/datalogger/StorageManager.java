package cl.austral38.slthv2.datalogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import cl.austral38.slthv2.SLT1;
import cl.austral38.slthv2.SLT1HID;
import cl.austral38.slthv2.models.Data;
import cl.austral38.slthv2.models.Geo;
import cl.austral38.slthv2.models.MessageTxt;
import cl.austral38.slthv2.models.Samples;
import cl.austral38.slthv2.models.Sensor;
import cl.austral38.slthv2.utils.CountDownTimer;

/**
 * Created by eDelgado on 12-05-14.
 */
public class StorageManager {

    private Handler handler;
    private Context mContext;
    private Geo location;

    private ArrayList<Samples> mSamples;
    private SLT1 mDevice;
    private Activity mActivity;
    private SharedPreferences mPreferences;
    private CountDownTimer mTimer;
    private final String TAG = "STORAGE_MANAGER";
    private long interval = 0;
    private long samples = 0;
    private int count=0;
    private ArrayList<String> mSensors;
    Thread thread;


    public StorageManager(Context context, SLT1 device , Handler handler){
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mDevice = device;
        this.handler = handler;
        mSamples = new ArrayList<Samples>();
        location = new Geo();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        ////////////////////////////////////////
        mSensors = new ArrayList<String>();
        mSensors.add(Sensor.TEMPERATURA);
        mSensors.add(Sensor.LUMINOSIDAD);
        mSensors.add(Sensor.HUMEDAD);

    }

    final Handler mHandler = new Handler();
    Timer timer = new Timer(false);
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Samples sam = new Samples();

                    sam.temperature =  SLT1HID.temperature;
                    sam.light = SLT1HID.light;
                    sam.humidity =   SLT1HID.humidity;

                    mSamples.add(sam);
                    Log.e("Thread storage", " Data:  \n "+
                            "t :"+ sam.temperature + "\n" +
                            "l :"+ sam.light +"\n" +
                            "h :"+ sam.humidity );
                    Log.d("Samples ", " datos capturados "+ mSamples.size());
                    count--;
                    if( count==0){
                        handler.obtainMessage(0, new MessageTxt(3)).sendToTarget();
                        Log.d(TAG,"Task Complete... ");
                    }

                }
            });


        }
    };
    // 1000 = 1 second.
    /** **/
    public void startCapture(){

        interval = mPreferences.getInt("pref_sample_interval", 0) * 1000 ;
        count = mPreferences.getInt("pref_sample_number",0);
        samples = count * 1000;
        Log.d(TAG,"intervalo y muestras "+ interval + " "+ samples);
        timer.scheduleAtFixedRate(timerTask, 1000, interval);

    }

    /**
     * ***/
    public void save() {
        if(mSamples!= null){

            Data mData = new Data();
            mData.setData(mSamples); //listado de datos
            mData.setLocation(location);
            mData.setSensors(mSensors);
            Log.e(TAG,"Iniciando guardado");
            mData.setTitle(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())+".xls"); //nombre con la fecha y hora actuales
            boolean saved = ExportToExcel.write(mData, mContext);

            if(saved){
                Log.e(TAG, "Archivo "+ mData.title+ " guardado");
                mSamples.clear();
            }
        }
    }

    /** **/
    public void stopCapture(){

            if (timer != null) {
                timer.cancel();
                timerTask.cancel();
                timer = null;
                Toast.makeText(mContext, "Stop capture ", Toast.LENGTH_SHORT).show();
            }

    }

    /** destruye las propiedades de la clase **/
    public void destroyStorage(){
        stopCapture();
        mSamples = null;
        mDevice = null;
        mPreferences = null;
        Toast.makeText(mContext, "Storage destroy " ,Toast.LENGTH_SHORT).show();
    }

    /** Getter and Setter **/
    public ArrayList<Samples> getSamples() {
        return mSamples;
    }


    public SLT1 getDevice() {
        return mDevice;
    }

    public void setDevice(SLT1HID mDevice) {
        this.mDevice = mDevice;
    }

   /* @Override
    public void run() {

        Log.e("Storage","Init Capture");
        mSamples = new ArrayList<Samples>();

        //obtengo las preferencias del usuario intervalo de medicion y
        // numero de muestras en milisegundos,
        interval = mPreferences.getInt("pref_sample_interval", 0) * 1000 -1;
        samples = mPreferences.getInt("pref_sample_number",0) * 1000 + interval;
        Toast.makeText(mContext," interval " + interval + " samples: " + samples, Toast.LENGTH_SHORT).show();

        mTimer = new CountDownTimer(samples, interval) {
            @Override
            public void onTick(long l) {
                Samples sam = new Samples();

                sam.temperature =  SLT1HID.temperature;
                sam.light = SLT1HID.light;
                sam.humidity =   SLT1HID.humidity;

                mSamples.add(sam);
                Log.e("Thread storage", "seconds remaining: " + l/ 1000 +"\n "+
                        "t :"+ sam.temperature + "\n" +
                        "l :"+ sam.light +"\n" +
                        "h :"+ sam.humidity );
            }

            @Override
            public void onFinish() {

                boolean autoSave = mPreferences.getBoolean("pref_storage", true);
                //comunica a la actividad que se termino la lectura
                handler.obtainMessage(0, new MessageTxt(3)).sendToTarget();
            }
        };

        mTimer.start();
    }*/

}
