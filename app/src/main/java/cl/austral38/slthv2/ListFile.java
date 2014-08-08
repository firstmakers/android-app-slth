package cl.austral38.slthv2;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.GridView;

import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;


import cl.austral38.slthv2.adapters.GridViewFileAdapter;
import cl.austral38.slthv2.datalogger.ExportToExcel;
import cl.austral38.slthv2.models.FileStorage;


/**
 * Created by eDelgado on 02-06-14.
 */
public class ListFile extends Activity {
    private File file;
    private ArrayList<FileStorage> myList;
    GridViewFileAdapter adapter;
    private GridView gridView;
    public String email = "";
    private TextView txt;
    private ArrayList<String> data;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file);
        gridView = (GridView) findViewById(R.id.file_grid);
        txt = (TextView)findViewById(R.id.text_file);
        txt.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ExportToExcel.isExternalStorageAvailable() || ExportToExcel.isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            Toast.makeText(this, "La memoria externa no está disponible", Toast.LENGTH_SHORT).show();

        }else {
            data = new ArrayList<String>();
            myList = new ArrayList<FileStorage>();
            String root_sd = Environment.getExternalStorageDirectory().toString();
            file = new File(root_sd + ExportToExcel.ROOT_DIRECTORY);
            if(!file.exists()) {
                file.mkdirs();
            }
            File list[] = file.listFiles();
            if(list.length == 0)
                txt.setVisibility(View.VISIBLE);

            for (int i = 0; i < list.length; i++) {
                FileStorage f = new FileStorage();
                f.setFile(list[i].getName());
                f.setSelected(false);
                myList.add(f);
            }

            for(FileStorage file :myList){
                data.add(file.getFile());
            }
            Log.d("DATA ","count adapter "+ data.size());
            adapter = new GridViewFileAdapter(this, R.layout.file_layout, data);
            gridView.setAdapter(adapter);
            gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    file = new File( file, myList.get( position ).getFile());
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
                    startActivity(intent);
                }
            });
        }
    }


    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {


        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int i, long l, boolean b) {
            int selectCount = gridView.getCheckedItemCount();
            Log.d("ActionMenu", " count " + selectCount +" position " + i);
            myList.get(i).setSelected(b);
            Log.d("ActionMenu", " selected " + myList.get(i).getFile());
            switch (selectCount) {
                case 1:
                    mode.setSubtitle(R.string.item_selected);
                    break;
                default:
                    String string = getString(R.string.many_items_selected);
                    mode.setSubtitle("" + selectCount + " "+ string);
                    break;
            }
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mode.setTitle(R.string.file_selected);
            mode.setSubtitle(R.string.item_selected);
            mode.getMenuInflater().inflate(R.menu.files, menu);


            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            switch (id) {
                case R.id.action_delete: {
                    Log.d("FileManager "," large of list " +myList.size());
                    ArrayList<FileStorage> tempDelete = new ArrayList<FileStorage>();
                    //preparando archivos seleccionados para borrar
                    for(FileStorage file : myList){
                        if(file.isSelected()){
                           tempDelete.add(file);
                        }
                    }
                    for(FileStorage delete: tempDelete){
                        data.remove(delete.getFile());
                        String root_sd = Environment.getExternalStorageDirectory().toString();
                        file = new File(root_sd + ExportToExcel.ROOT_DIRECTORY + delete.getFile());
                        file.delete();
                        Log.d("FileManager "," delete file " + delete.getFile());
                    }
                    adapter.notifyDataSetChanged();
                    if(data.size() == 0){
                        txt.setVisibility(View.VISIBLE);
                    }

                    mode.finish();
                    break;
                }
                case R.id.action_share: {

                    final Intent ei = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    ei.setType("plain/text");
                    ei.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
                    ei.putExtra(Intent.EXTRA_SUBJECT, "Reporte SLTH");

                    ArrayList<Uri> uris = new ArrayList<Uri>();

                    for(int i = 0; i< myList.size(); i++){
                        if(myList.get(i).isSelected()) {
                            String root_sd = Environment.getExternalStorageDirectory().toString();
                            file = new File(root_sd + ExportToExcel.ROOT_DIRECTORY);

                            Uri u = Uri.parse(file.toURI()+ myList.get(i).getFile());
                            Log.e("Adjuntos ", u.toString());
                            uris.add(u);
                        }
                    }
                    String title = getString(R.string.title_share_mail);
                    ei.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    startActivityForResult(Intent.createChooser(ei, title), 12345);

                    break;
                }
                default:
                    return false;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(android.view.ActionMode actionMode) {

        }
    }
}
