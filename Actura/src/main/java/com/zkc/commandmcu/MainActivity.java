package com.zkc.commandmcu;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

//import com.smartdevicesdk.utils.StringUtility;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.smartdevicesdk.utils.StringUtility;
import com.zkc.commandmcu.Adapter.DiySpinnerAdapter;
import com.zkc.commandmcu.Adapter.ListViewEPCAdapter;
import com.zkc.commandmcu.WebServiceConnection.HTTPConnection;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import static com.zkc.commandmcu.MyLocationListener.location;
import static com.zkc.commandmcu.SoundPoolUtil.context;

//public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
//    private final String TAG="MainActivity";

public class MainActivity extends AppCompatActivity  {
    private final String TAG="MainActivity";


//    @SerializedName("code")
//    @Expose
//    private String code;

    TabHost tabHost;
    Spinner spinner_workareas,spinner_workchannel,spinner_workautofh,spinner_workpower,spinner_workcontinnouswave,spinner_workmoden_mixer,spinner_workmoden_ifamp;
    EditText editText_recv,editText_send,editText_sbw,editText_dbd;
    TextView textView_epc,textview_total;
    Button button_startepc,button_search;
    ListView listView,listView_epc;
    Button button_tag;
    Button button_release;
    Button button_verify;
    Button button_utilise;
    EditText editText_sleep;
    TextView scannedItems;
    String serialPort_path="/dev/ttyMT0";
    int serialPort_buad=115200;
    String mprovider;
    Double latitude;
    Double longitude;

    private static String current_state = "tagging";

    private final String TAGGING_STATE = "tagging";
    private final String RELEASING_STATE = "releasing";
    private final String VERIFYING_STATE = "verifying";
    private final String UTILIZATION_STATE = "utilization";

    private FusedLocationProviderClient client;
    public int i;




    List<SpinnerItem>  lst_workmoden_ifamp,lst_workmoden_mixer,lst_workcontinnouswave,lst_workpower,lst_workautofh,lst_workareas,lst_workchannel;

    UhfHelper UHF;

    int mixer,ifamp;

    ThreadFindChannel threadFindChannel;

    /**
     * Read to EPC list
     */
    List<IDModels> lst_idModels=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing);



        button_tag=(Button)findViewById(R.id.tag_button);
        button_release=(Button)findViewById(R.id.release_button);
        button_verify = (Button)findViewById(R.id.verify);
        button_utilise = (Button)findViewById(R.id.utilize);
        scannedItems = (TextView)findViewById(R.id.scanned_number);

        UHF=new UhfHelper(serialPort_path, serialPort_buad,infoHandler);
        UHF.connect();

        SoundPoolUtil.initSoundPool(this);

        //client = LocationServices.getFusedLocationProviderClient(this);



        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                //设置数据
                Object obj= SharedPreferencesUtils.getParam(MainActivity.this,"i",0);
                infoHandler.obtainMessage(1021,obj).sendToTarget();

                SystemClock.sleep(500);
                Object selectIndex=SharedPreferencesUtils.getParam(MainActivity.this,"j", 0);
                infoHandler.obtainMessage(1022,selectIndex).sendToTarget();
            }
        }).start();




        spinner_workareas=(Spinner)findViewById(R.id.spinner_workareas);
        //spinner_workareas.setOnItemSelectedListener(this);
        spinner_workchannel=(Spinner)findViewById(R.id.spinner_workchannel);
        //spinner_workchannel.setOnItemSelectedListener(this);
        spinner_workautofh=(Spinner)findViewById(R.id.spinner_workautofh);
        // spinner_workautofh.setOnItemSelectedListener(this);
        spinner_workpower=(Spinner)findViewById(R.id.spinner_workpower);
        //spinner_workpower.setOnItemSelectedListener(this);
        spinner_workcontinnouswave=(Spinner)findViewById(R.id.spinner_workcontinnouswave);
        // spinner_workcontinnouswave.setOnItemSelectedListener(this);
        spinner_workmoden_mixer=(Spinner)findViewById(R.id.spinner_workmoden_mixer);
        //spinner_workmoden_mixer.setOnItemSelectedListener(this);
        spinner_workmoden_ifamp=(Spinner)findViewById(R.id.spinner_workmoden_ifamp);
        //spinner_workmoden_ifamp.setOnItemSelectedListener(this);

        button_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_release.setEnabled(false);
                button_verify.setEnabled(false);
                button_utilise.setEnabled(false);
                current_state = TAGGING_STATE;

                if (button_tag.getText().equals("Tagging")) {
                    button_tag.setText("Stop");
                    UHF.setAutoWork(true);
                    lst_idModels=new ArrayList<IDModels>();
                    IDModels idModels = new IDModels();


                } else {
                    button_tag.setText("Tagging");
                    button_release.setEnabled(true);
                    button_utilise.setEnabled(true);
                    button_verify.setEnabled(true);
                    UHF.setAutoWork(false);
                    String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                }

            }
        });



        button_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_tag.setEnabled(false);
                button_verify.setEnabled(false);
                button_utilise.setEnabled(false);
                current_state = RELEASING_STATE;

                if (button_release.getText().equals("Release")) {
                    button_release.setText("Stop");
                    UHF.setAutoWork(true);
                    lst_idModels=new ArrayList<IDModels>();

                } else {
                    button_release.setText("Release");
                    button_tag.setEnabled(true);
                    button_utilise.setEnabled(true);
                    button_verify.setEnabled(true);
                    UHF.setAutoWork(false);
                    String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                }


            }
        });

        button_utilise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_release.setEnabled(false);
                button_verify.setEnabled(false);
                button_tag.setEnabled(false);
                current_state = UTILIZATION_STATE;

                if (button_utilise.getText().equals("Utilization")) {
                    button_utilise.setText("Stop");
                    UHF.setAutoWork(true);
                    lst_idModels=new ArrayList<IDModels>();

                } else {
                    button_utilise.setText("Utilization");
                    button_release.setEnabled(true);
                    button_tag.setEnabled(true);
                    button_verify.setEnabled(true);
                    UHF.setAutoWork(false);
                    String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_release.setEnabled(false);
                button_tag.setEnabled(false);
                button_utilise.setEnabled(false);
                current_state = VERIFYING_STATE;

                if (button_verify.getText().equals("Verification")) {
                    button_verify.setText("Stop");
                    UHF.setAutoWork(true);
                    lst_idModels=new ArrayList<IDModels>();

                } else {
                    button_verify.setText("Verification");
                    button_release.setEnabled(true);
                    button_utilise.setEnabled(true);
                    button_tag.setEnabled(true);
                    UHF.setAutoWork(false);
                    String str = "send：" + UHF.totalSend + "，receive：" + UHF.totalRec;
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();

                }

            }
        });
        client = LocationServices.getFusedLocationProviderClient(this);

    }


    class ThreadFindChannel extends Thread{
        @Override
        public void run() {
            super.run();
            boolean flg=false;
            int i=0,j=0;

            int errNum=10;
            int bestIndexI=0;
            int bestIndexJ=0;

            for(i=0;i<lst_workareas.size();i++){
                Log.d(TAG, "run: "+i);
                flg= UHF.setWorkAreas(lst_workareas.get(i).getID());
                List<Double> listChannel=getChannelList(i);
                Log.d(TAG, "run: listChanne size="+listChannel.size()+",flg="+flg);
                for(j=0;j<listChannel.size();j+=10){
                    flg=UHF.setWorkChannel(j);
                    Log.d(TAG, "run: j="+j+",flg="+flg);
                    UHF.setAutoWork(true);
                    while(UHF.totalSend<10){
                        SystemClock.sleep(1);
                    }
                    UHF.setAutoWork(false);
                    int errOnline=UHF.totalSend-UHF.totalRec;
                    Log.d(TAG, "run: success="+errOnline+",i="+i+",j="+j);
                    if(errOnline<errNum){
                        errNum=errOnline;
                        bestIndexI=i;
                        bestIndexJ=j;
                    }
                }
            }
            infoHandler.obtainMessage(1021,bestIndexI).sendToTarget();
            SystemClock.sleep(1000);
            infoHandler.obtainMessage(1022,bestIndexJ).sendToTarget();

            SystemClock.sleep(1000);
            infoHandler.obtainMessage(1023,"").sendToTarget();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UHF.disconnect();
        SharedPreferencesUtils.setParam(MainActivity.this,"i", spinner_workareas.getSelectedItemPosition());
        SharedPreferencesUtils.setParam(MainActivity.this,"j", spinner_workchannel.getSelectedItemPosition());
    }

    public Handler infoHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1000://UHF消息
                    byte[] totalByte = (byte[]) msg.obj;
                    switch (totalByte[1]) {
                        case (byte) 0x02://通知帧
                            if (totalByte[2] == (byte) 0x22) {
                                UHF.totalRec++;
                                Log.d(TAG, "onDataReceivedListener: 读取到EPC");
                                //读到标签EPC
                                byte[] epcArray = new byte[12];
                                System.arraycopy(totalByte, 8, epcArray, 0, epcArray.length);
                                String epcStr = StringUtility.ByteArrayToString(epcArray, epcArray.length);
                                int cpc= totalByte[totalByte.length-3] & 0xff |(totalByte[totalByte.length-4] & 0xff) << 8;

                                IDModels idModels=new IDModels();
                                idModels.setRSSI(totalByte[5]);
                                idModels.setPC(totalByte[6]);
                                idModels.setEPC(epcStr);
                                idModels.setCRC(cpc);

                                String infoStr = epcStr + "(RSSI=" + idModels.getRSSI() + "dB)";

                                //                               textView_epc.setText(infoStr);

                                SoundPoolUtil.play(0);

                                if (current_state.equals(TAGGING_STATE)){
                                    updateList(idModels,TAGGING_STATE);
                                }
                                else if (current_state.equals(RELEASING_STATE)){
                                    updateList(idModels,RELEASING_STATE);
                                }
                                else if (current_state.equals(UTILIZATION_STATE)){
                                    updateList(idModels,UTILIZATION_STATE);
                                }
                                else if (current_state.equals(VERIFYING_STATE)){
                                    updateList(idModels,VERIFYING_STATE);
                                }

                            }
                            break;
                        case (byte) 0x01://响应帧
                            String epcStr = StringUtility.ByteArrayToString(totalByte, totalByte.length);
                            switch (totalByte[2]) {
                                case (byte) 0xff:
//                                    textView_epc.setText(epcStr);
                                    break;
                                default:
                                    //                                   textView_epc.setText(textView_epc.getText() + "\r\n" + epcStr);
                                    break;
                            }
                            break;
                    }
                    break;
                case 1011://自动扫描
                    //                 button_startepc.setEnabled(false);
                    //                  button_search.setEnabled(false);
//                    button_search.setText("searching...");

                    threadFindChannel = new ThreadFindChannel();
                    threadFindChannel.start();

                    spinner_workareas.setEnabled(false);
                    spinner_workchannel.setEnabled(false);
                    break;
                case 1021://设置地区
                    Log.d(TAG, "handleMessage: 1021设置地区");
                    int i = (int) msg.obj;
                    Log.d(TAG, "handleMessage: 1021,i=" + i);
                    spinner_workareas.setSelection(i);
                    break;
                case 1022://设置频率
                    Log.d(TAG, "handleMessage: 1022设置频率");
                    int j = (int) msg.obj;
                    Log.d(TAG, "handleMessage: 1022,j=" + j);
                    spinner_workchannel.setSelection(j);
                    break;
                case 1023://完成扫描
                    Log.d(TAG, "handleMessage: 1023完成扫描");
                    //  spinner_workareas.setEnabled(true);
                    //  spinner_workchannel.setEnabled(true);
                    //   button_search.setEnabled(true);
                    //  button_search.setText("SEARCH");
                    //    button_startepc.setEnabled(true);
                    //  button_startepc.performClick();

                    SharedPreferencesUtils.setParam(MainActivity.this,"i", spinner_workareas.getSelectedItemPosition());
                    SharedPreferencesUtils.setParam(MainActivity.this,"j", spinner_workchannel.getSelectedItemPosition());
                    break;
            }
            return false;
        }
    });

    private void updateList(final IDModels idModels, final String state) {
       // scannedItems.setText("update list!");
        idModels.setNumber(1);
        for (int i = 0; i < lst_idModels.size(); i++) {
            if (lst_idModels.get(i).getEPC().equals(idModels.getEPC())) {
                Log.d("debug1", "lst_idModels.size():" + "" + lst_idModels.size());
                Log.d("debug1", "code at update:" + "" + lst_idModels.get(i).getEPC().toString());


                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        int i = 0;

                        Log.d("debug1", "we are here at location 1");
                       // scannedItems.setText("location!");

                        Log.d("debug1", "we are here too at location 2");

                        if (location != null) {
                           // scannedItems.setText("location is not null!");
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            Log.d("debug1", "we are here three");

                            String longy = longitude.toString();
                            Log.d("debug1", "longy:" + longy);
                            String lat = latitude.toString();
                            Log.d("debug1", "lat:" + lat);
                            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://api.acturapp.com/").addConverterFactory(GsonConverterFactory.create()).build();
                            //scannedItems.setText("retrofit built!");
                            //               Retrofit retrofit = new Retrofit.Builder().baseUrl("https://evapurse-api.azurewebsites.net/").build();
                            if (state == TAGGING_STATE) {
                                PostData api = retrofit.create(PostData.class);
                                String code = lst_idModels.get(i).getEPC().toString();
                                String json = "{\n" +
                                        "            \"code\": \"" + code + "\",\n" +
                                        "            \"latitude\": \"" + lat + "\",\n" +
                                        "            \"longitude\": \"" + longy + "\"}";


                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                                api.tagUser(requestBody).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            Log.d("debug1", response.body().string());
                                            //scannedItems.setText(response.body().string() + " "+ "items scanned");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                                    }
                                });

                            } else if (state == RELEASING_STATE) {
                                PostData api = retrofit.create(PostData.class);
                                String code = lst_idModels.get(i).getEPC().toString();
                                String json = "{\n" +
                                        "            \"code\": \"" + code + "\",\n" +
                                        "            \"latitude\": \"" + lat + "\",\n" +
                                        "            \"longitude\": \"" + longy + "\"}";


                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                                api.releaseUser(requestBody).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            Log.d("debug1", response.body().string());
                                            //scannedItems.setText(response.body().string() + " "+ "items scanned");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                                    }
                                });
                            } else if (state == UTILIZATION_STATE) {
                                PostData api = retrofit.create(PostData.class);
                                String code = lst_idModels.get(i).getEPC().toString();
                                String json = "{\n" +
                                        "            \"code\": \"" + code + "\",\n" +
                                        "            \"latitude\": \"" + lat + "\",\n" +
                                        "            \"longitude\": \"" + longy + "\"}";


                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                                api.utilizeUser(requestBody).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            Log.d("debug1", response.body().string());
                                           // scannedItems.setText(response.body().string() + " "+ "items scanned");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                                    }
                                });
                            } else if (state == VERIFYING_STATE) {
                                PostData api = retrofit.create(PostData.class);
                                String code = lst_idModels.get(i).getEPC().toString();
                                String json = "{\n" +
                                        "            \"code\": \"" + code + "\",\n" +
                                        "            \"latitude\": \"" + lat + "\",\n" +
                                        "            \"longitude\": \"" + longy + "\"}";



                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                                api.verifyUser(requestBody).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            Log.d("debug1", response.body().string());
                                            //scannedItems.setText(response.body().string() + " "+ "items scanned");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                                    }
                                });
                            }


                        }else{

                            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://api.acturapp.com/").build();
                            PostData api = retrofit.create(PostData.class);
                            String code = lst_idModels.get(i).getEPC().toString();
                            String json= "{\n" +
                                    "            \"code\": \"0\",\n" +
                                    "            \"latitude\": \"0.0 \",\n" +
                                    "            \"longitude\": \"0.0\"}";


                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                            api.tagUser(requestBody).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try{
                                        Log.d("debug1","debug1" + response.body().string());
                                        scannedItems.setText("no item scanned");
                                    } catch (IOException e){
                                        e.printStackTrace();

                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                                }
                            });
                        }
                    }

                })
                ;

                int num = lst_idModels.get(i).getNumber() + 1;
                idModels.setNumber(num);//Update tag count
                lst_idModels.remove(i);//Remove existing tags


            }
        }

        lst_idModels.add(idModels);

        ListViewEPCAdapter adapter = new ListViewEPCAdapter(this, lst_idModels);
    }


    private void initView(){
        listView=(ListView)findViewById(R.id.listView);

        listView_epc=(ListView)findViewById(R.id.listView_epc);

        textView_epc=(TextView)findViewById(R.id.textView_epc);
        textview_total=(TextView)findViewById(R.id.textview_total);

        editText_sleep=(EditText)findViewById(R.id.editText_sleep);
        editText_sleep.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(!editText_sleep.getText().toString().equals("")) {
                    int sleepTime = Integer.parseInt(editText_sleep.getText().toString());
                    UHF.setSleepTime(sleepTime);
                }
                return false;
            }
        });

        // button_startepc=(Button)findViewById(R.id.button_startepc);
        //button_startepc.setOnClickListener(this);

        //button_search=(Button)findViewById(R.id.button_search);
        //button_search.setOnClickListener(this);

        //tabHost = (TabHost) findViewById(R.id.tabhost);

        spinner_workareas=(Spinner)findViewById(R.id.spinner_workareas);
        // spinner_workareas.setOnItemSelectedListener(this);
        spinner_workchannel=(Spinner)findViewById(R.id.spinner_workchannel);
        //spinner_workchannel.setOnItemSelectedListener(this);
        spinner_workautofh=(Spinner)findViewById(R.id.spinner_workautofh);
        //spinner_workautofh.setOnItemSelectedListener(this);
        spinner_workpower=(Spinner)findViewById(R.id.spinner_workpower);
        //spinner_workpower.setOnItemSelectedListener(this);
        spinner_workcontinnouswave=(Spinner)findViewById(R.id.spinner_workcontinnouswave);
        //spinner_workcontinnouswave.setOnItemSelectedListener(this);
        spinner_workmoden_mixer=(Spinner)findViewById(R.id.spinner_workmoden_mixer);
        //spinner_workmoden_mixer.setOnItemSelectedListener(this);
        spinner_workmoden_ifamp=(Spinner)findViewById(R.id.spinner_workmoden_ifamp);
        //spinner_workmoden_ifamp.setOnItemSelectedListener(this);

        lst_workareas = new ArrayList<SpinnerItem>();
        SpinnerItem item = new SpinnerItem(1,"中国900MHz");
        lst_workareas.add(item);
        item = new SpinnerItem(4,"中国800MHz");
        lst_workareas.add(item);
        item = new SpinnerItem(2,"美国");
        lst_workareas.add(item);
        item = new SpinnerItem(3,"欧洲");
        lst_workareas.add(item);
        item = new SpinnerItem(6,"韩国");
        lst_workareas.add(item);
        DiySpinnerAdapter myaAdapter = new DiySpinnerAdapter(this, lst_workareas);
        spinner_workareas.setAdapter(myaAdapter);

        lst_workautofh = new ArrayList<SpinnerItem>();
        item = new SpinnerItem(255,"设置自动跳频");
        lst_workautofh.add(item);
        item = new SpinnerItem(0,"取消自动跳频");
        lst_workautofh.add(item);
        myaAdapter = new DiySpinnerAdapter(this, lst_workautofh);
        spinner_workautofh.setAdapter(myaAdapter);

        lst_workpower = new ArrayList<SpinnerItem>();
        for(int i=26;i>-10;i--) {
            int db=i;
            if(db<10)
            {
                db=db*1000;
            }else if(db<100){
                db=db*100;
            }
            //Integer.toHexString(200);

            byte[] bt=UHF.intToBytes(db);
            Log.d(TAG, "initView:i="+i+",hex:"+StringUtility.ByteArrayToString(bt,bt.length)+",dB:"+db);

            item = new SpinnerItem(db, i+"dB");
            lst_workpower.add(item);
        }

        myaAdapter = new DiySpinnerAdapter(this, lst_workpower);
        spinner_workpower.setAdapter(myaAdapter);

        lst_workcontinnouswave = new ArrayList<SpinnerItem>();
        item = new SpinnerItem(255,"设置连续载波");
        lst_workcontinnouswave.add(item);
        item = new SpinnerItem(0,"取消连续载波");
        lst_workcontinnouswave.add(item);
        myaAdapter = new DiySpinnerAdapter(this, lst_workcontinnouswave);
        spinner_workcontinnouswave.setAdapter(myaAdapter);

        lst_workmoden_mixer = new ArrayList<SpinnerItem>();
        item = new SpinnerItem(2,"6dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(0,"0dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(1,"3dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(3,"9dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(4,"12dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(5,"15dB");
        lst_workmoden_mixer.add(item);
        item = new SpinnerItem(6,"16dB");
        lst_workmoden_mixer.add(item);
        myaAdapter = new DiySpinnerAdapter(this, lst_workmoden_mixer);
        spinner_workmoden_mixer.setAdapter(myaAdapter);


        lst_workmoden_ifamp = new ArrayList<SpinnerItem>();
        item = new SpinnerItem(7,"40dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(0,"12dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(1,"18dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(2,"21dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(3,"24dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(4,"27dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(5,"30dB");
        lst_workmoden_ifamp.add(item);
        item = new SpinnerItem(6,"36dB");
        lst_workmoden_ifamp.add(item);
        myaAdapter = new DiySpinnerAdapter(this, lst_workmoden_ifamp);
        spinner_workmoden_ifamp.setAdapter(myaAdapter);
    }

    /**
     * 构造SimpleAdapter的第二个参数，类型为List<Map<?,?>>
     * @param cmdStr
     * @return
     */
    private List<Map<String, String>> getData(List<String> cmdStr) {
        List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
        Resources res =getResources();
        for(int i=0;i<cmdStr.size();i++)
        {
            String [] cmdArray=cmdStr.get(i).split(",");
            if(cmdArray.length==2)
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title",cmdArray[0]);
                map.put("description", cmdArray[1]);
                listData.add(map);
            }
        }
        return listData;
    }

    public List<String> ReadTxtFile(String strFilePath) {
        List<String> list = new ArrayList<>();
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (!file.isFile()) {
            Log.d("TestFile", "The File doesn't not exist.");
            copyFile("cmd.txt", strFilePath);
            if (!new File(strFilePath).exists()) {
                Toast.makeText(this, "The File doesn't not exist.", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        try {
            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream,
                        "UTF-8");
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                // 分行读取
                while ((line = buffreader.readLine()) != null) {
                    // content += line + "\n";
                    list.add(line.trim());
                }
                instream.close();
            }
        } catch (java.io.FileNotFoundException e) {
            Log.d("TestFile", "The File doesn't not exist.");
            Toast.makeText(this,"The File doesn't not exist.", Toast.LENGTH_SHORT)
                    .show();
        } catch (IOException e) {
            Log.d("TestFile", e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return list;
    }

    private void copyFile(String filename, String newFileName) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }


    private List<Double> getChannelList(int index){
        List<Double> list=new ArrayList<Double>();
        switch (index)
        {
            case 0 : // China 2
                for (int i = 0; i < 20; i++)
                {
                    Double para=920.125 + i * 0.25;
                    list.add(para);
                }
                break;
            case 1: // China 1
                for (int i = 0; i < 20; i++)
                {
                    Double para=840.125 + i * 0.25;
                    list.add(para);
                }
                break;
            case 2: // US
                for (int i = 0; i < 52; i++)
                {
                    Double para=902.25 + i * 0.5;
                    list.add(para);
                }
                break;
            case 3: // Europe
                for (int i = 0; i < 15; i++)
                {
                    Double para=865.1 + i * 0.2;
                    list.add(para);
                }
                break;
            case 4:  // Korea
                for (int i = 0; i < 32; i++)
                {
                    Double para=917.1 + i * 0.2;
                    list.add(para);
                }
                break;
            default :
                break;
        }
        return list;
    }
//    public class SingleShotLocationProvider {
//
//        public static interface LocationCallback {
//            public void onNewLocationAvailable(GPSCoordinates location);
//        }
//
//        // calls back to calling thread, note this is for low grain: if you want higher precision, swap the
//        // contents of the else and if. Also be sure to check gps permission/settings are allowed.
//        // call usually takes <10ms
//        public static void requestSingleUpdate(final Context context, final LocationCallback callback) {
//            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//            if (isNetworkEnabled) {
//                Criteria criteria = new Criteria();
//                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//
//
//                locationManager.requestSingleUpdate(criteria, new LocationListener() {
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude()));
//                    }
//
//                    @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
//                    @Override public void onProviderEnabled(String provider) { }
//                    @Override public void onProviderDisabled(String provider) { }
//                }, null);
//            } else {
//                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                if (isGPSEnabled) {
//                    Criteria criteria = new Criteria();
//                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//                    locationManager.requestSingleUpdate(criteria, new LocationListener() {
//                        @Override
//                        public void onLocationChanged(Location location) {
//                            callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude()));
//                        }
//
//                        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
//                        @Override public void onProviderEnabled(String provider) { }
//                        @Override public void onProviderDisabled(String provider) { }
//                    }, null);
//                }
//            }
//        }
//
//
//        // consider returning Location instead of this dummy wrapper class
//        public static class GPSCoordinates {
//            public float longitude = -1;
//            public float latitude = -1;
//
//            public GPSCoordinates(float theLatitude, float theLongitude) {
//                longitude = theLongitude;
//                latitude = theLatitude;
//            }
//
//            public GPSCoordinates(double theLatitude, double theLongitude) {
//                longitude = (float) theLongitude;
//                latitude = (float) theLatitude;
//            }
//        }
//    }

}
