package cl.austral38.slthv2.datalogger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import cl.austral38.slthv2.models.Data;
import cl.austral38.slthv2.models.Samples;
import cl.austral38.slthv2.models.Sensor;

/**
 * Created by eDelgado on 14-05-14.
 */
public class ExportToExcel {

    public final static String ROOT_DIRECTORY ="/SLTH/";
    private static  int cellTemp = -1;
    private static int cellLum = -1;
    private static int cellHum = -1;
    private static int cell = 0;

    public static boolean write( Data mData, Context context) {
        Workbook workbook = null;
        cell=0;

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            Toast.makeText(context, "La memoria externa no está disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mData.title.endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (mData.title.endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            Toast.makeText(context, "La extensión del archivo debe ser xls o xlsx", Toast.LENGTH_SHORT).show();
            return false;
        }

        Sheet sheet = workbook.createSheet("Muestras");
        Iterator<Samples> iterator = mData.data.iterator();

        int rowIndex = 0;
        //título del archivo;
        Row rowtitle = sheet.createRow(rowIndex++);
        Cell celltitle1 = rowtitle.createCell(0);
        celltitle1.setCellValue("Nombre :");
        Cell celltitle2 = rowtitle.createCell(1);
        celltitle2.setCellValue(mData.getTitle());
        //GeoReferencia Latitud
        Row rowGeoLat = sheet.createRow(rowIndex++);
        Cell celllat =rowGeoLat.createCell(0);
        celllat.setCellValue("Latitud :");
        Cell cellGeo0 =rowGeoLat.createCell(1);
        cellGeo0.setCellValue(mData.getLocation().latitude);
        //GeoReferencia Longitud
        Row rowGeoLon = sheet.createRow(rowIndex++);
        Cell cellGeolon = rowGeoLon.createCell(0);
        cellGeolon.setCellValue("Longitud :");
        Cell cellGeo1 = rowGeoLon.createCell(1);
        cellGeo1.setCellValue(mData.getLocation().longitude);

        rowIndex++;
        Row header = sheet.createRow(rowIndex++);
        //style header
        CellStyle cs = workbook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        if(mData.sensors.contains(Sensor.TEMPERATURA)){
            cellTemp = cell++;
            Cell temp = header.createCell(cellTemp);
            temp.setCellValue(Sensor.TEMPERATURA);
            temp.setCellStyle(cs);
        }
        if(mData.sensors.contains(Sensor.LUMINOSIDAD)){
            cellLum = cell++;
            Cell lum = header.createCell(cellLum);
            lum.setCellValue(Sensor.LUMINOSIDAD);
            lum.setCellStyle(cs);
        }
        if(mData.sensors.contains(Sensor.HUMEDAD)){
            cellHum = cell++;
            Cell hum = header.createCell(cellHum);
            hum.setCellValue(Sensor.HUMEDAD);
            hum.setCellStyle(cs);
        }
        Cell fecha0 = header.createCell(cell);
        fecha0.setCellValue("Fecha/Hora");
        fecha0.setCellStyle(cs);


        while (iterator.hasNext()) {
            Samples sample = iterator.next();
            Row row = sheet.createRow(rowIndex++);
            if(cellTemp > -1){
                Cell cell0 = row.createCell(cellTemp);
                cell0.setCellValue(sample.getTemperature());
            }
            if(cellLum> -1){
                Cell cell1 = row.createCell(cellLum);
                cell1.setCellValue(sample.getLight());
            }
            if(cellHum > -1){
                Cell cell1 = row.createCell(cellHum);
                cell1.setCellValue(sample.getHumidity());
            }

            Cell fecha = row.createCell(cell);
            fecha.setCellValue(sample.time);
        }

        String root_sd = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root_sd + ROOT_DIRECTORY);
        if(!myDir.exists())
            myDir.mkdirs();

        File file = new File(root_sd + ROOT_DIRECTORY, mData.title);
        FileOutputStream os = null;

        try {
            cellHum = -1;
            cellLum = -1;
            cellTemp = -1;
            os = new FileOutputStream(file);
            workbook.write(os);
            Toast.makeText(context, "Writing file " + file, Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            Toast.makeText(context , "Error writing " + file +" "+ e, Toast.LENGTH_SHORT).show();
            Log.e("Export" , e.toString());
        } catch (Exception e) {
            Toast.makeText(context, "Failed to save file " + e, Toast.LENGTH_SHORT);
            Log.e("Export" , e.toString());
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                Log.e("Export" , ex.toString());
            }
        }
        return true;
    }


    /**
     *  */
    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
