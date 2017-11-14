package com.example.diyujia.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.diyujia.app.MyApplication;
import com.example.diyujia.bean.City;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by diyujia on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackBtn;
    private ListView mList;
    private List<City> cityList;
    private List<City> filterDateList;
    private BaseAdapter myadapter;
    private List citynameList;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        initViews();

        //mBackBtn = (ImageView) findViewById(R.id.title_back);
        //mBackBtn.setOnClickListener(this);
    }

    private void initViews(){
        //为mBackBtn设置监听事件
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //mClearEditText = (ClearEditText) findViewById(R.id.search_city);

        mList = (ListView) findViewById(R.id.title_list);
        MyApplication myApplication = (MyApplication) getApplication();
        cityList = myApplication.getmCityList();
        filterDateList = new ArrayList<>();
        citynameList = new ArrayList<>();
        for(City city:cityList){
            Log.d(TAG,city.getCity());
            Log.d(TAG,city.getNumber());
            if(city != null){
                filterDateList.add(city);
            }
        }
        for(City city:cityList){
            if(city != null){
                citynameList.add(city.getCity());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,citynameList);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView,View view,int position,long l){
                City city = filterDateList.get(position);
                Intent i = new Intent();
                i.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
    }
}
