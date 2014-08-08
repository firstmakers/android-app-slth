package cl.austral38.slthv2;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import cl.austral38.slthv2.models.Humidity;
import cl.austral38.slthv2.models.Light;
import cl.austral38.slthv2.models.MessageTxt;
import cl.austral38.slthv2.models.Temperature;

public class SLT1HID extends SLT1 implements Runnable, SLT1Interface {

	public static double temperature;
	public static double light;
    public static double humidity;
	private UsbDevice device = null;
	private UsbManager manager = null;
	private Handler handler = null;
	private boolean lastButtonState = false;
	private boolean buttonStatusInitialized = false;
	private Boolean closeRequested = new Boolean(false);
	private UsbDeviceConnection connection;
	private UsbInterface intf;
	private boolean connected = false;
	Thread thread;
	private int samples = 0;
	private boolean running = false;
	private int interval = 1;
	byte[] commandLightTemperature = new byte[]{(byte)0x81};
	//byte[] commandTemperature = new byte[]{(byte)0x82};
	//byte[] commandLiht = new byte[]{(byte)0x83};
    byte[] commandH = new byte[]{(byte)0x86};
    byte[] commandLTH = new byte[]{(byte)0x87};
	
	/*
	 * Constructor
	 * */
	public SLT1HID(Context context, UsbDevice device, Handler handler) {
		this.device = device;
		this.handler = handler;
		this.manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
       /* PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context.getPackageName() + ".USB_PERMISSION"), 0);

        manager.requestPermission(device,pendingIntent); //permiso explicito*/
		intf = device.getInterface(0);
		connection = manager.openDevice(device);
		//si falla la conexión
		if(connection == null) {
			return;
		}
		if(connection.claimInterface(intf, true) == true) {
			thread = new Thread(this);
			thread.start();
			connected = true;
		} else {
			connection.close();
		}
	}

	@Override
	public void start() {
		if(thread != null){			
			running = true;
		}
	}

	@Override
	public void stop() {
		if(thread != null){
			//thread.stop();
			running = false;
		}
		
	}

	@Override
	public void close() {
		connected = false;
		synchronized(closeRequested) {
			closeRequested = true;
		}
	}


	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public String getDeviceTitle() {
		if(device != null) {
			return device.getDeviceName();
		}	
		return null;
	}

	@Override
	public void setConfiguration(int samples, int interval) {
		this.samples = samples;
		this.interval = interval;
	}

    private byte[] writeUSB(byte[] command){
        int result = 0;
        byte[] out = new byte[64];
        int timeout = 2000;
        UsbEndpoint endpointOUT = intf.getEndpoint(1);
        UsbEndpoint endpointIN = intf.getEndpoint(0);

        do {
            //Log.i("HID "," enviando comando ");
            result = connection.bulkTransfer(endpointOUT,
                    command, command.length, 1000);
        } while ((result < 0) && (wasCloseRequested() == false));
        //Log.d("HID Result", result+"");
        do {
            //Log.i("HID ","esperando respuesta ");
            result = connection.bulkTransfer(endpointIN, out,
                    out.length, timeout);
        } while ((result < 0) && (wasCloseRequested() == false));
        //Log.i("HID "," respuesta desde slth " + result);
        return out;
    }

	@Override
	public void run() {

		byte[] inBuffer;
		while (true) {
			while (running) {
				if (wasCloseRequested() == true) {
					destroy();
					return;
				}
                inBuffer = writeUSB(commandLTH);
                //processLTH(inBuffer);
                //Log.e("Recibido", inBuffer.length +" ["+ inBuffer[0]+"][" +inBuffer[1]+"]");
                processLTH(inBuffer);
                /*inBuffer = writeUSB(commandH);
                processH(inBuffer);
				/* Sleep the thread for a while */
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

    private void processLTH(byte[] inBuffer){
        int temp;
        double luz;
        int humedad;


        if (inBuffer[0] == commandLTH[0]) {

            temp = ((inBuffer[2] & 0xFF) << 8) + (inBuffer[1] & 0xFF); // obtener
            // temperatura
            luz = ((inBuffer[4] & 0xFF) << 8) + (inBuffer[3] & 0xFF); // obtener

            humedad = ((inBuffer[6] & 0xFF) << 8) + (inBuffer[5] & 0xFF);

            if(temp == 32767){//desconexion de la sonda de temperatura
                handler.obtainMessage(0, new MessageTxt(1)).sendToTarget();

            }else{
                temperature = Math.round((temp * 0.0625)*10.0)/10.0;

            }
            if (temp > 32767) { // temperatura negativa

                String s = Integer.toBinaryString(~temp ).substring(16);
                int d = -(Integer.parseInt(s,2) + 1);
                temperature = Math.round((d/16) * 10.0)/10.0;
            }

            light = Math.round((luz / 1.2)*10.0)/10.0;
            humidity = (humedad / 78.7);
            Log.d("SLTH ","Tº "+ temperature + " Lux " +light +" Hs "+humidity);
            handler.obtainMessage(0, new Temperature(temperature))
                    .sendToTarget();
            handler.obtainMessage(0,new Humidity(humidity)).sendToTarget();
            handler.obtainMessage(0, new Light(light)).sendToTarget();
        }else{
            Log.e("SLTH ", "Error de comparación, el comando enviado es "+commandLTH[0]+ " el comando recibido "+ inBuffer[0]);
        }

    }



    private void processH(byte[] inBuffer) {
        int humedad;
        if(inBuffer[0]== commandH[0]){

            humedad = ((inBuffer[2] & 0xFF) << 8) + (inBuffer[1] & 0xFF);
            humidity = (humedad / 78.7);
            handler.obtainMessage(0,new Humidity(humidity)).sendToTarget();
        }
    }

    private void processLT(byte[] inBuffer) {
        int temp;
        double luz;

        if (inBuffer[0] == commandLightTemperature[0]) {

            temp = ((inBuffer[2] & 0xFF) << 8) + (inBuffer[1] & 0xFF); // obtener
                                                                        // temperatura
            luz = ((inBuffer[4] & 0xFF) << 8) + (inBuffer[3] & 0xFF); // obtener

            if(temp == 32767){//desconexion de la sonda de temperatura
                handler.obtainMessage(0, new MessageTxt(1)).sendToTarget();

            }															// luz
            if (temp > 32767) { // temperatura negativa
                temp = (temp ^ 0xFF) + 1;//complemeto a 2
            }


            temperature = (temp * 0.0625);//
            light = (luz / 1.2);

            handler.obtainMessage(0, new Temperature(temperature))
                    .sendToTarget();
            handler.obtainMessage(0, new Light(light)).sendToTarget();
        }

    }

    /**
	 * @return boolean Indicates if someone has requested to close
	 */
	private boolean wasCloseRequested()
	{
		synchronized(closeRequested){
			return closeRequested;
		}
	}
	
	/**
	 * Closes connections, releases resources, cleans up variables
	 */
	private void destroy(){
		/* Release the interface that was previously claimed and close
		 * the connection.
		 */
		connection.releaseInterface(intf);
		connection.close();
		
		/* Clear up all of the locals */
		device = null;
		manager = null;
		handler = null;
		lastButtonState = false;
		buttonStatusInitialized = false;
		closeRequested = false;
		connection = null;
		intf = null;
	}

}
