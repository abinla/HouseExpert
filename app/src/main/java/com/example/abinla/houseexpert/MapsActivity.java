package com.example.abinla.houseexpert;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,AdapterView.OnItemSelectedListener,RetrieveDataSource.DataSourceAsyncResponse {

    private GoogleMap mMap;
    private Spinner region,time;
    MenuItem regionMenuItem,timeMenuItem;
    private RetrieveDataSource getDataSourceTask;
    private HouseDBAdaptor dbAdapter = new HouseDBAdaptor(this);
    final static int SEARCH_DATABASE = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dbAdapter.open();
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // Add a marker in Auckland and move the camera
        LatLng auckland = new LatLng(-36.84, 174.74);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(auckland)      // Sets the center of the map to Mountain View
                .zoom(16)                   // Sets the zoom
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.addMarker(new MarkerOptions().position(auckland).title("Auckland Center"));
        try {
            tagAll(region.getSelectedItem().toString(), time.getSelectedItem().toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        regionMenuItem = menu.findItem(R.id.action_RegionFilter);
        region = (Spinner) MenuItemCompat.getActionView(regionMenuItem);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.regions, android.R.layout.simple_spinner_item);
        region.setOnItemSelectedListener(this);
        region.setAdapter(adapter);
        region.setSelection(0);



        timeMenuItem = menu.findItem(R.id.action_TimeFilter);
        time = (Spinner) MenuItemCompat.getActionView(timeMenuItem);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.period, android.R.layout.simple_spinner_item);
        time.setOnItemSelectedListener(this);
        time.setAdapter(timeAdapter);
        time.setSelection(1); //TODO select in content instead of index
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_OnlineImport) {
            getDataSourceTask = new RetrieveDataSource(this);
            String uri = "http://houseprice.azurewebsites.net/";
            getDataSourceTask.execute(uri);
        }*/
        if (id == R.id.action_MapExit) {
            finish();
            return true;
        }
        if(id == R.id.action_ShowDatabase) {
            Intent intent = new Intent(this,DBActivity.class);
            //startActivity(intent);
            startActivityForResult(intent,SEARCH_DATABASE);

        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result;

        if (requestCode == SEARCH_DATABASE) {
            if(resultCode == Activity.RESULT_OK){
                result=data.getStringExtra("tag keyword");
                if(result== null || result.length() == 0) return; //return if keyword is null or empty
                Cursor cursor = dbAdapter.searchInAddress(result);
                tagAll(cursor);
                cursor.close();
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String subSelected = region.getSelectedItem().toString();
        String timeSelected = time.getSelectedItem().toString();
        switch (parent.getId()){
            case R.id.action_RegionFilter:
            case R.id.action_TimeFilter:
                tagAll(subSelected, timeSelected);
//                Toast.makeText(getApplicationContext(), "You have selected " + subSelected + " in " + timeSelected, Toast.LENGTH_LONG).show();
                break;

        }
    }

    public void tagAll (Cursor cursor){
        String markerTitle,markerLatlng;
        LatLng markLocation;
        int markerNumber = 0;
        mMap.clear();

        try {
            Toast.makeText(this,"Total Row Number:" + cursor.getCount(),Toast.LENGTH_LONG).show();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS));
                String saleDate  = cursor.getString(cursor.getColumnIndex(HouseDBHelper.SALEDATE));
                if (address == null || address.length() == 0) {
                    continue;
                }

                markerTitle = cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS)) + " "
                        + cursor.getString(cursor.getColumnIndex(HouseDBHelper.PRICE))+ " "
                        + cursor.getString(cursor.getColumnIndex(HouseDBHelper.SALEDATE)
                );

                markerLatlng = cursor.getString(cursor.getColumnIndex(HouseDBHelper.LATLNG));
                if (markerLatlng == null || markerLatlng.length() == 0)
                    continue;

                markLocation = string2Latlng(markerLatlng);
                if (markLocation != null) {
                    mMap.addMarker(new MarkerOptions().position(markLocation).title(markerTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );
                    markerNumber++;
                    Log.e("Robin", "marker refer to record ID:" + cursor.getString(cursor.getColumnIndex(HouseDBHelper._ID)) + " added!");
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            Toast.makeText(this, markerNumber+" Marker added in " + cursor.getCount()+ " ROW" ,Toast.LENGTH_LONG).show();
        }
    }

    public void tagAll(String sub,String period) {
        String markerTitle,markerLatlng;
        LatLng markLocation;
        Cursor cursor=null;
        int markerNumber = 0;

        mMap.clear();

        try {
            if(sub.equals("All Regions")){
                cursor = dbAdapter.fetch();
            }else{
                cursor = dbAdapter.searchInSub(sub);
            }

//            Toast.makeText(this,"Total Row Number:" + cursor.getCount(),Toast.LENGTH_LONG).show();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS));
                String saleDate  = cursor.getString(cursor.getColumnIndex(HouseDBHelper.SALEDATE));
                if (address == null || address.length() == 0) {
                    continue;
                }

                if(!inPeriod(saleDate, period)) continue; //DO NOT show the Marker not in the specified period

                markerTitle = cursor.getString(cursor.getColumnIndex(HouseDBHelper.ADDRESS)) + " "
                        + cursor.getString(cursor.getColumnIndex(HouseDBHelper.PRICE))+ " "
                        + cursor.getString(cursor.getColumnIndex(HouseDBHelper.SALEDATE)
                );

                markerLatlng = cursor.getString(cursor.getColumnIndex(HouseDBHelper.LATLNG));
                if (markerLatlng == null || markerLatlng.length() == 0)
                    continue;

                markLocation = string2Latlng(markerLatlng);
                if (markLocation != null) {
                    mMap.addMarker(new MarkerOptions().position(markLocation).title(markerTitle));
                    markerNumber++;
                    Log.e("Robin", "marker refer to record ID:" + cursor.getString(cursor.getColumnIndex(HouseDBHelper._ID)) + " added!");
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            Toast.makeText(this, markerNumber+" Marker added in " + cursor.getCount()+ " ROW" ,Toast.LENGTH_LONG).show();
            cursor.close();
        }
    }

    boolean inPeriod(String date, String period)
    {
        Date saleDate,curDate;
        String strSaleDate;

        SimpleDateFormat dates = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            saleDate = dates.parse(date);
            int diffInDays = (int)( ( System.currentTimeMillis() - saleDate.getTime())
                    / (1000 * 60 * 60 * 24) );

            if(period.equals("1 Month"))
            {
                if(diffInDays>0 && diffInDays<= 30)
                    return true;
                else
                    return false;
            }
            else if(period.equals("3 Months"))
            {
                if(diffInDays>0 && diffInDays<= 90)
                    return true;
                else
                    return false;
            }else if(period.equals("6 Months"))
            {
                if(diffInDays<= 180)
                    return true;
                else
                    return false;
            }else if(period.equals("1 Year"))
            {
                if(diffInDays<= 365)
                    return true;
                else
                    return false;
            }else if(period.equals("All"))
            {
                return true;
            }
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
        return true;
    }
    public LatLng string2Latlng(String input){

        if(input == null||input.length()==0) return null;

        LatLng location=null;
        input = input.substring(input.indexOf('(')+1,input.indexOf(')'));
        String[] latlong =  input.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        location = new LatLng(latitude,longitude);
        return location;
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void getDataSourceFinished(String dataSource) {
        Log.d("Robin", dataSource);
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
            tagAll(region.getSelectedItem().toString(), time.getSelectedItem().toString());
            Toast.makeText(this,"Totoal:" + totalInsertNumber + " added",Toast.LENGTH_LONG).show();
         }
    }
}
