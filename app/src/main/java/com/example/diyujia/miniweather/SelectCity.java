package com.example.diyujia.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
    private ArrayAdapter<String> madapter;
    private List citynameList;
    private EditText SearchEditText;
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
        mList.setTextFilterEnabled(true);   //开启listview的过滤功能
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
        madapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,citynameList);
        mList.setAdapter(madapter);
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

        //为搜索城市框设置监听事件
        SearchEditText = (EditText)findViewById(R.id.search_edit);
        SearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());  //过滤数据
                mList.setAdapter(madapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        });
    }

    /*
    * 根据输入框中的值过滤数据并刷新ListView
    * @param filterStr
    * */
    private void filterData(String filterStr){
        filterDateList = new ArrayList<City>();
        if(TextUtils.isEmpty(filterStr)){
            for(City city:cityList){
                filterDateList.add(city);
                citynameList.add(city.getCity());
            }
        }else{
            filterDateList.clear();
            citynameList.clear();
            for(City city:cityList){
                if(city.getCity().indexOf(filterStr.toString())!= -1){
                    filterDateList.add(city);
                    citynameList.add(city.getCity());
                }
            }
        }
        //更新适配器中的内容
        madapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,citynameList);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                /*Intent i = new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK,i);*/
                finish();
                break;
            default:
                break;
        }
    }
}
