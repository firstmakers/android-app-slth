package cl.austral38.slthv2;



import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Handler;

public abstract class SLT1 implements SLT1Interface {

	static SLT1 loadSLT1(Context context, UsbDevice device, Handler handler){

		if((device.getVendorId() == (int)0x04D8) && (device.getProductId() == (int)0x003F)) {

			return new SLT1HID(context, device, handler);	
		}
		return null;
	}
	
}
