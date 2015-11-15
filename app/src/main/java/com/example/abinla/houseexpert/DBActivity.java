package com.example.abinla.houseexpert;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DBActivity extends AppCompatActivity implements RetrieveDataSource.DataSourceAsyncResponse {
    private HouseDBAdaptor dbAdapter = new HouseDBAdaptor(this);
    private List<HouseInfo> houseList = null;
    private ListView databaseListView = null;
    private EditText keywordsEdit = null;
    private RetrieveDataSource getDataSourceTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.open();

        setContentView(R.layout.activity_db);
        databaseListView = (ListView)findViewById(R.id.databaseListView);
        keywordsEdit = (EditText) findViewById(R.id.keywordsEditText);
        databaseListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Cursor cursor = dbAdapter.fetch();
        initalizeListView(cursor);
    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_db, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ExportDB) {
            exportDB("");
            return true;
        }else if(id == R.id.action_ImportDB){

            return true;
        }
        else if(id == R.id.action_updateDB)
        {
            getDataSourceTask = new RetrieveDataSource(this);
            String uri = "http://houseprice.azurewebsites.net/";
            getDataSourceTask.execute(uri);
            return true;
        }else if (id == R.id.action_DBExit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*
    //export the DATABASE to target folder
     */

    public boolean exportDB(String targetPath)
    {
/*                if(!direct.exists()) {
                    direct.mkdir();
                }*/
        try {
            File sd = Environment.getExternalStorageDirectory();
 //           File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = getApplicationContext().getDatabasePath(HouseDBHelper.DB_NAME).getAbsolutePath();
                File currentDB = new File(currentDBPath);

                String backupDBPath = sd + "/HouseExport/";
                File backupDir = new File(backupDBPath);
                if(!backupDir.exists())
                    backupDir.mkdir();

                File backupDB = new File(backupDBPath,HouseDBHelper.DB_NAME);


                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();
        }

        return true;
    }

    public void initalizeListView(Cursor cursor)
    {
//        Cursor cursor = dbAdapter.fetch();
        houseList = new ArrayList<HouseInfo>();

        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS));
                if (address == null || address.length() == 0) {
                    continue;
                }


                HouseInfo house = new HouseInfo(
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS)),
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.SUB)),
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.PRICE)),
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.STRUCTURE)),
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.SALEDATE)),
                        cursor.getString(cursor.getColumnIndex(HouseDBHelper.LATLNG))
                );

                houseList.add(house);
            }
            cursor.close();

            databaseListView.setAdapter(new ArrayAdapter<HouseInfo>(this, android.R.layout.simple_list_item_1, houseList));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void SearchAddress(View view) {
        String keyword = keywordsEdit.getText().toString();
        Cursor cursor = dbAdapter.searchInAddress(keyword);
        initalizeListView(cursor);

    }

    public void markSearchResult(View view) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("tag keyword", keywordsEdit.getText().toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void getDataSourceFinished(String dataSource) {
        Log.d("Robin", dataSource);
        if(dataSource == null||dataSource.length()==0) {
            Toast.makeText(this,"Data source website unavailable, Please try again later", Toast.LENGTH_LONG).show();
            return;
        }
        Document doc = Jsoup.parse(dataSource);
        Element script = doc.select("script").get(2); //hard code the position, could be better way?
        String data = script.data();
        String[] lines = data.split(System.getProperty("line.separator"));

        HouseInfo house = null;
        boolean newHouse = false;
        int totalInsertNumber = 0;
        long insertResult = -1;

        try {
            for (String line : lines) {

                if (line.contains("title")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    if (newHouse == false) {
                        newHouse = true;
                        house = new HouseInfo();
                        house.setAddress(subLine);
                    }
                    continue;
                } else if (line.contains("category")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    house.setSub(subLine);
                    continue;
                } else if (line.contains("lat")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    house.setLatlng("lat/lng:(" + subLine);
                    continue;
                } else if (line.contains("lng")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    house.setLatlng(house.getLatlng() + "," + subLine + ")");
                    continue;
                } else if (line.contains("lastSalePrice")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    subLine = subLine.substring(0, subLine.lastIndexOf("."));
                    house.setPrice(subLine);
                    continue;
                } else if (line.contains("LastSaleDate")) {
                    String subLine = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    house.setSaledate(subLine);
                    newHouse = false;
                    insertResult = dbAdapter.insert(house);
                    if(insertResult!= -1) totalInsertNumber++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(totalInsertNumber>=1 ){
                getDataSourceTask = new RetrieveDataSource(this);
                String uri = "http://houseprice.azurewebsites.net/";
                getDataSourceTask.execute(uri);
            }
//            tagAll(region.getSelectedItem().toString(), time.getSelectedItem().toString());
            Toast.makeText(this,totalInsertNumber + " Row added," +dbAdapter.QueryNumEntries()+" in total",Toast.LENGTH_LONG).show();
        }
    }


}
