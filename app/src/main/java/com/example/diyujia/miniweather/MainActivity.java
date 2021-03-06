package com.example.diyujia.miniweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.diyujia.bean.TodayWeather;
import com.example.diyujia.util.NetUtil;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Created by diyujia on 2017/9/27.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private  static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv,city_name_Tv,wenduTv;
    private ImageView weatherImg,pmImg;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    /**
    * 重写onCreate方法
    * @param savedInstanceState 保存activity状态
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //把布局加载到MainActivity窗口上
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        //用于测试网络连接是否正常
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){  //如果当前状态不是NONE则显示网络OK
            Log.d("myWeather","网络OK");
            Toast.makeText(MainActivity.this,"网络OK！",Toast.LENGTH_LONG).show();
        }else{
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initView();
    }

    void initView(){
        //利用SharedPreferences获取数据
        SharedPreferences preferences = getSharedPreferences("weather", Context.MODE_PRIVATE);
        String citycodeSave = preferences.getString("citycodeSave","101010100");
        queryWeatherCode(citycodeSave);


        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        wenduTv = (TextView)  findViewById(R.id.temperature_add);
        /*wenduTv.setText("N/A");
        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");*/
    }
    /**
     *
     * @param cityCode
     * */
    private void queryWeatherCode(final String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable(){
            @Override
            public void run(){
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);
                    todayWeather = parseXML(responseStr).get(0);
                    todayWeather.setCitycodeS(cityCode); //手动设置城市ID
                    if(todayWeather != null){
                        Log.d("myWeather",todayWeather.toString());
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally{
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    void  updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        wenduTv.setText("温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        int IntPm25 = 0;
        if(todayWeather.getPm25() != null){  //有些地区没有pm25数值，所以会为null。增加一个判断如果pm25不为空赋值，如果pm25为空就为0
            IntPm25 = Integer.parseInt(todayWeather.getPm25());
        }
        int Pm25Value = 0;
        if(IntPm25 > 50 && IntPm25 <= 100){
            Pm25Value = 1;
        }else if(IntPm25 >100 && IntPm25 <= 150){
            Pm25Value = 2;
        }else if(IntPm25 >150 && IntPm25 <= 200){
            Pm25Value = 3;
        }else if(IntPm25 >200 && IntPm25 <= 300){
            Pm25Value = 4;
        }else if(IntPm25 > 300){
            Pm25Value = 5;
        }
        switch (Pm25Value){
            case 0:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
                break;
            case 1:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
                break;
            case 2:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
                break;
            case 3:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
                break;
            case 4:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
                break;
            case 5:
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
                break;
        }
        if(todayWeather.getType() != null){
            switch (todayWeather.getType()){
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨冰雹":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "雨加雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
            }
        }

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
        //用SharePreferences保存最后一次更新的城市代码
        SharedPreferences preferences = getSharedPreferences("weather",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorWeather = preferences.edit();
        String citycodeSave = todayWeather.getCitycodeS();
        editorWeather.putString("citycodeSave",citycodeSave);
        editorWeather.commit();
    }

    private List<TodayWeather> parseXML(String xmldata){
        List<TodayWeather> returnList = new ArrayList<TodayWeather>();
        TodayWeather todayWeather = null;
        TodayWeather todayWeather1 = null;
        TodayWeather todayWeather2 = null;
        TodayWeather todayWeather3 = null;
        TodayWeather todayWeather4 = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                            todayWeather1 = new TodayWeather();
                            todayWeather2 = new TodayWeather();
                            todayWeather3 = new TodayWeather();
                            todayWeather4 = new TodayWeather();
                        }
                        if(xmlPullParser.getName().equals("city")){
                            eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                            Log.d("myWeather","city:    " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            todayWeather.setUpdatetime(xmlPullParser.getText());
                            Log.d("myWeather","updatetime:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("shidu")){
                            eventType = xmlPullParser.next();
                            todayWeather.setShidu(xmlPullParser.getText());
                            Log.d("myWeather","shidu:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("wendu")){
                            eventType = xmlPullParser.next();
                            todayWeather.setWendu(xmlPullParser.getText());
                            Log.d("myWeather","wendu:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("pm25")){
                            eventType = xmlPullParser.next();
                            todayWeather.setPm25(xmlPullParser.getText());
                            Log.d("myWeather","pm25:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("quality")){
                            eventType = xmlPullParser.next();
                            todayWeather.setQuality(xmlPullParser.getText());
                            Log.d("myWeather","quality:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("fengxiang")){
                            switch (fengxiangCount){
                                case 1:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setFengxiang(xmlPullParser.getText());
                                    Log.d("myWeather","fengxiang:  " + xmlPullParser.getText());
                                    fengxiangCount++;
                                    break;
                                case 3:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setFengxiang(xmlPullParser.getText());
                                    Log.d("myWeather","fengxiang1:  " + xmlPullParser.getText());
                                    fengxiangCount++;
                                    break;
                                case 5:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setFengxiang(xmlPullParser.getText());
                                    Log.d("myWeather","fengxiang2:  " + xmlPullParser.getText());
                                    fengxiangCount++;
                                    break;
                                case 7:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setFengxiang(xmlPullParser.getText());
                                    Log.d("myWeather","fengxiang3:  " + xmlPullParser.getText());
                                    fengxiangCount++;
                                    break;
                                case 9:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setFengxiang(xmlPullParser.getText());
                                    Log.d("myWeather","fengxiang4:  " + xmlPullParser.getText());
                                    fengxiangCount++;
                                    break;
                                default:
                                    eventType = xmlPullParser.next();
                                    fengxiangCount++;
                                    break;
                            }

                        }else if(xmlPullParser.getName().equals("fengli")){
                            switch (fengliCount){
                                case 1:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setFengli(xmlPullParser.getText());
                                    Log.d("myWeather","fengli:  " + xmlPullParser.getText());
                                    fengliCount++;
                                    break;
                                case 3:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setFengli(xmlPullParser.getText());
                                    Log.d("myWeather","fengli1:  " + xmlPullParser.getText());
                                    fengliCount++;
                                    break;
                                case 5:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setFengli(xmlPullParser.getText());
                                    Log.d("myWeather","fengli2:  " + xmlPullParser.getText());
                                    fengliCount++;
                                    break;
                                case 7:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setFengli(xmlPullParser.getText());
                                    Log.d("myWeather","fengli3:  " + xmlPullParser.getText());
                                    fengliCount++;
                                    break;
                                case 9:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setFengli(xmlPullParser.getText());
                                    Log.d("myWeather","fengli4:  " + xmlPullParser.getText());
                                    fengliCount++;
                                    break;
                                default:
                                    eventType = xmlPullParser.next();
                                    fengliCount++;
                                    break;
                            }

                        }else if(xmlPullParser.getName().equals("date")){
                            switch (dateCount){
                                case 0:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setDate(xmlPullParser.getText());
                                    Log.d("myWeather","date:  " + xmlPullParser.getText());
                                    dateCount++;
                                    break;
                                case 1:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setDate(xmlPullParser.getText());
                                    Log.d("myWeather","date1:  " + xmlPullParser.getText());
                                    dateCount++;
                                    break;
                                case 2:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setDate(xmlPullParser.getText());
                                    Log.d("myWeather","date2:  " + xmlPullParser.getText());
                                    dateCount++;
                                    break;
                                case 3:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setDate(xmlPullParser.getText());
                                    Log.d("myWeather","date3:  " + xmlPullParser.getText());
                                    dateCount++;
                                    break;
                                case 4:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setDate(xmlPullParser.getText());
                                    Log.d("myWeather","date4:  " + xmlPullParser.getText());
                                    dateCount++;
                                    break;
                            }

                        }else if(xmlPullParser.getName().equals("high")){
                            switch (highCount){
                                case 0:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setHigh(xmlPullParser.getText());
                                    Log.d("myWeather","high:  " + xmlPullParser.getText());
                                    highCount++;
                                    break;
                                case 1:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setHigh(xmlPullParser.getText());
                                    Log.d("myWeather","high1:  " + xmlPullParser.getText());
                                    highCount++;
                                    break;
                                case 2:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setHigh(xmlPullParser.getText());
                                    Log.d("myWeather","high2:  " + xmlPullParser.getText());
                                    highCount++;
                                    break;
                                case 3:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setHigh(xmlPullParser.getText());
                                    Log.d("myWeather","high3:  " + xmlPullParser.getText());
                                    highCount++;
                                    break;
                                case 4:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setHigh(xmlPullParser.getText());
                                    Log.d("myWeather","high4:  " + xmlPullParser.getText());
                                    highCount++;
                                    break;
                            }

                        }else if(xmlPullParser.getName().equals("low")){
                            switch (lowCount){
                                case 0:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setLow(xmlPullParser.getText());
                                    Log.d("myWeather","low:  " + xmlPullParser.getText());
                                    lowCount++;
                                    break;
                                case 1:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setLow(xmlPullParser.getText());
                                    Log.d("myWeather","low1:  " + xmlPullParser.getText());
                                    lowCount++;
                                    break;
                                case 2:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setLow(xmlPullParser.getText());
                                    Log.d("myWeather","low2:  " + xmlPullParser.getText());
                                    lowCount++;
                                    break;
                                case 3:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setLow(xmlPullParser.getText());
                                    Log.d("myWeather","low3:  " + xmlPullParser.getText());
                                    lowCount++;
                                    break;
                                case 4:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setLow(xmlPullParser.getText());
                                    Log.d("myWeather","low4:  " + xmlPullParser.getText());
                                    lowCount++;
                                    break;
                            }

                        }else if(xmlPullParser.getName().equals("type")){
                            switch (typeCount){
                                case 0:
                                    eventType = xmlPullParser.next();
                                    todayWeather.setType(xmlPullParser.getText());
                                    Log.d("myWeather","type:  " + xmlPullParser.getText());
                                    typeCount++;
                                    break;
                                case 2:
                                    eventType = xmlPullParser.next();
                                    todayWeather1.setType(xmlPullParser.getText());
                                    Log.d("myWeather","type1:  " + xmlPullParser.getText());
                                    typeCount++;
                                    break;
                                case 4:
                                    eventType = xmlPullParser.next();
                                    todayWeather2.setType(xmlPullParser.getText());
                                    Log.d("myWeather","type2:  " + xmlPullParser.getText());
                                    typeCount++;
                                    break;
                                case 6:
                                    eventType = xmlPullParser.next();
                                    todayWeather3.setType(xmlPullParser.getText());
                                    Log.d("myWeather","type3:  " + xmlPullParser.getText());
                                    typeCount++;
                                    break;
                                case 8:
                                    eventType = xmlPullParser.next();
                                    todayWeather4.setType(xmlPullParser.getText());
                                    Log.d("myWeather","type4:  " + xmlPullParser.getText());
                                    typeCount++;
                                    break;
                                default:
                                    eventType = xmlPullParser.next();
                                    typeCount++;
                            }

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        returnList.add(todayWeather);
        returnList.add(todayWeather1);
        returnList.add(todayWeather2);
        returnList.add(todayWeather3);
        returnList.add(todayWeather4);
        return returnList;
    }

    /*private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){

                        }
                        if(xmlPullParser.getName().equals("city")){
                            eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                            Log.d("myWeather","city:    " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            todayWeather.setUpdatetime(xmlPullParser.getText());
                            Log.d("myWeather","updatetime:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("shidu")){
                            eventType = xmlPullParser.next();
                            todayWeather.setShidu(xmlPullParser.getText());
                            Log.d("myWeather","shidu:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("wendu")){
                            eventType = xmlPullParser.next();
                            todayWeather.setWendu(xmlPullParser.getText());
                            Log.d("myWeather","wendu:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("pm25")){
                            eventType = xmlPullParser.next();
                            todayWeather.setPm25(xmlPullParser.getText());
                            Log.d("myWeather","pm25:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("quality")){
                            eventType = xmlPullParser.next();
                            todayWeather.setQuality(xmlPullParser.getText());
                            Log.d("myWeather","quality:  " + xmlPullParser.getText());
                        }else if(xmlPullParser.getName().equals("fengxiang")&&fengxiangCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setFengxiang(xmlPullParser.getText());
                            Log.d("myWeather","fengxiang:  " + xmlPullParser.getText());
                            fengxiangCount++;
                        }else if(xmlPullParser.getName().equals("fengli")&&fengliCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setFengli(xmlPullParser.getText());
                            Log.d("myWeather","fengli:  " + xmlPullParser.getText());
                            fengliCount++;
                        }else if(xmlPullParser.getName().equals("date")&&dateCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setDate(xmlPullParser.getText());
                            Log.d("myWeather","date:  " + xmlPullParser.getText());
                            dateCount++;
                        }else if(xmlPullParser.getName().equals("high")&&highCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setHigh(xmlPullParser.getText());
                            Log.d("myWeather","high:  " + xmlPullParser.getText());
                            highCount++;
                        }else if(xmlPullParser.getName().equals("low")&&lowCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setLow(xmlPullParser.getText());
                            Log.d("myWeather","low:  " + xmlPullParser.getText());
                            lowCount++;
                        }else if(xmlPullParser.getName().equals("type")&&typeCount == 0){
                            eventType = xmlPullParser.next();
                            todayWeather.setType(xmlPullParser.getText());
                            Log.d("myWeather","type:  " + xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d("myWeather","现在进入了结束标签");
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }*/

    /**
     * 实现OnClickListener中的onClick方法
     * @param view
     */
    @Override
    public void onClick(View view){
        //实现选择城市按钮的操作
        if(view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }

        //实现更新按钮的操作
        if(view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            //cityCode 从sharedPreference中的main_city_code字段中取
            //如果该字段没有定义或者没有值得话，缺省值为101010100
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("myWeather",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(cityCode);
            }else{
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码为"+newCityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络OK");
                queryWeatherCode(newCityCode);
            }else{
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }
    }
}
