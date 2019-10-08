package com.zkc.commandmcu.Adapter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.zkc.commandmcu.IDModels;
import com.zkc.commandmcu.R;
import com.zkc.commandmcu.WebServiceConnection.HTTPConnection;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.zkc.commandmcu.SoundPoolUtil.context;

/**
 * Created by leoxu on 2017/8/29.
 */

public class ListViewEPCAdapter extends BaseAdapter implements LocationListener {
    private List<IDModels> mList;
    private Context mContext;
    private String mprovider;
    Double longitude;
    Double latitude;

    public ListViewEPCAdapter(Context pContext, List<IDModels> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public  List<IDModels> getList(){
        return mList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
        convertView = _LayoutInflater.inflate(R.layout.item_list_epc, null);
        if (convertView != null) {
            TextView text_rssi = (TextView) convertView
                    .findViewById(R.id.text_rssi);
            TextView text_epc = (TextView) convertView
                    .findViewById(R.id.text_epc);

            TextView text_number = (TextView) convertView
                    .findViewById(R.id.text_number);

            text_rssi.setText(mList.get(position).getRSSI()+"");
            text_epc.setText(mList.get(position).getEPC().toString().replace(" ","")+"");
            text_number.setText(mList.get(position).getNumber()+"");

            String code = mList.get(position).getEPC().toString().replace(" ","")+"";
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // get the last know location from your location manager.
            if (mprovider != null && !mprovider.equals("")) {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return null;
                }else {

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    // now get the lat/lon from the location and do something with it.
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    String longy = longitude.toString();
                    String lat = latitude.toString();

                    new TagUpdateTask().execute(code, longy, lat);
                }
            }



        }
        return convertView;
    }
    public void onLocationChanged(Location loc) {
        String message = String.format(
                "New Location \n Longitude: %1$s \n Latitude: %2$s",
                loc.getLongitude(), loc.getLatitude()
        );
        //Toast.makeText(LbsGeocodingActivity.this, message, Toast.LENGTH_LONG).show();
    }
    public void onProviderDisabled(String arg0) {

    }
    public void onProviderEnabled(String provider) {

    }
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private class TagUpdateTask extends AsyncTask<String, String, String> {

        String code;
        String lat;
        String longy;

        @Override
        protected String doInBackground(String[] param) {
//            page = param[0];

            String encodedData = getServerData(code, longy, lat);

            HTTPConnection urlConnection = new HTTPConnection();
            return urlConnection.getStringFromURL(HTTPConnection.TAGGING_URL, encodedData);
        }

        @Override
        protected void onPostExecute(String result) {
//          showSpinner(false);
            processReturnObject(result);
        }
    }
    public String getServerData(String code, String longy, String lat){
        String data = null;

        try {
            data = URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(code, "UTF-8");
            data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longy, "UTF-8");
            data += "&" + URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(lat, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return data;
    }


    private void processReturnObject(String result){
        if (result == null){

            return;
        }

        return;
    }

}
