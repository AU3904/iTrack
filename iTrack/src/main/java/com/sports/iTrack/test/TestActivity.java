package com.sports.iTrack.test;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;
import com.sports.iTrack.R;
import com.sports.iTrack.base.BaseActivity;

import java.util.ArrayList;

/**
 * Created by aaron_lu on 2/11/15.
 */
public class TestActivity extends BaseActivity {

    LineChart[] mCharts = new LineChart[1]; // 4条数据
    //    Typeface mTf; // 自定义显示字体
    int[] mColors = new int[] { Color.rgb(137, 230, 81), Color.rgb(240, 240, 30),//
            Color.rgb(89, 199, 250), Color.rgb(250, 104, 104) }; // 自定义颜色

    String[] mMonths = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.test_layout);


        mCharts[0] = (LineChart) findViewById(R.id.chart);
        // 生产数据
        LineData data = getData(36, 100);

//        for (int i = 0; i < mCharts.length; i++) {
//            // add some transparency to the color with "& 0x90FFFFFF"
//            setupChart(mCharts[i], data, mColors[i % mColors.length]);
//        }

    }


    // 设置显示的样式
    void setupChart(LineChart chart, LineData data, int color) {
        // if enabled, the chart will always start at zero on the y-axis
//        chart.setStartAtZero(true);
        //节点不显示具体值
//        chart.setDrawYValues(false);
//        chart.setDrawBorder(false);

        chart.setDescription("海拔图");//不是X,Y轴的描述
        chart.setNoDataTextDescription("You need to provide data for the chart.");

//        chart.setDrawVerticalGrid(false); //grid lines
        //chart.setDrawHorizontalGrid(false);
        chart.setDrawGridBackground(false);
        chart.setGridColor(Color.WHITE & 0x70FFFFFF);
        chart.setGridWidth(1.25f);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);// if disabled, scaling can be done on x- and y-axis separately

//        chart.setBackgroundColor(color);// 设置背景

        chart.setData(data); // 设置数据

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);// 样式
        l.setFormSize(6f);// 字体
        l.setTextColor(Color.WHITE);// 颜色

        YLabels y = chart.getYLabels();
        y.setTextColor(Color.WHITE);
        y.setLabelCount(10); // y轴上的标签的显示的个数

        XLabels x = chart.getXLabels();
        x.setTextColor(Color.WHITE);
        chart.animateX(2500);
    }


    // 生成一个数据，
    LineData getData(int count, float range) {
        /**
         * 设置数据
         *
         * x 时间
         * y1 速度
         * y2 海拔
         */
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            // x轴显示的数据，这里默认使用数字下标显示
            xVals.add(mMonths[i % 12]);
        }

        // y轴的数据
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) + 3;
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        // y轴的数据集合
        LineDataSet set1 = new LineDataSet(yVals, "时速");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f); // 线宽
        set1.setCircleSize(3f);// 显示的圆形大小
        set1.setColor(Color.WHITE);// 显示颜色
        set1.setCircleColor(Color.WHITE);// 圆形的颜色
        set1.setHighLightColor(Color.WHITE); // 高亮的线的颜色



        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        return data;
    }
}
