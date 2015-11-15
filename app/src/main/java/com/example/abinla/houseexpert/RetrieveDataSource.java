package com.example.abinla.houseexpert;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by abinla on 2015/11/7.
 */
public class RetrieveDataSource extends AsyncTask<String,Void,String> {
        ;
        public RetrieveDataSource.DataSourceAsyncResponse delegate = null;


        public RetrieveDataSource(RetrieveDataSource.DataSourceAsyncResponse asyncResponse)
        {
            delegate = asyncResponse;
        }
        @Override
        protected String doInBackground(String...address) {


            String uri = address[0];// "http://houseprice.azurewebsites.net/";


            StringBuilder stringBuilder = new StringBuilder();
            int cp;
            HttpURLConnection httpURLConnection = null;

            try {
                URL requestUrl = new URL(uri);
                httpURLConnection = (HttpURLConnection) requestUrl.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(isr);

                while ((cp = reader.read()) != -1) {
                    stringBuilder.append((char) cp);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    httpURLConnection.disconnect();
                    Log.d("Robin", stringBuilder.toString());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return stringBuilder.toString();

        }

    @Override
    protected void onPostExecute(String s) {
        delegate.getDataSourceFinished(s);
    }

    public interface DataSourceAsyncResponse {
        void getDataSourceFinished(String dataSource);
    }
}
