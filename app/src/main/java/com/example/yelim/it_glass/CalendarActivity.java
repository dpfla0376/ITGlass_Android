package com.example.yelim.it_glass;

/**
 * Created by sehyeon on 2017-05-05.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity {

    @BindView(R.id.yearAndMonth)
    TextView yearAndMonth;
    @BindView(R.id.calendarBody)
    GridView calendarView;
    @BindView(R.id.previousMonth)
    TextView previousMonth;
    @BindView(R.id.nextMonth)
    TextView nextMonth;

    private GridAdapter gridAdapter;
    private ArrayList<String> dayList;
    private ArrayList<String> contentList;
    private ArrayList<Recode> recodeList;
    private Calendar calendar;
    private int year;
    private int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);

        long now = System.currentTimeMillis();
        final Date date = new Date(now);

        final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        recodeList = new ArrayList<Recode>();

        // yyyy년 mm월
        yearAndMonth.setText(curYearFormat.format(date) + "년 " + curMonthFormat.format(date) + "월");
        year = Integer.parseInt(curYearFormat.format(date));
        month = Integer.parseInt(curMonthFormat.format(date));

        calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) - 1, 1);

        int dayNum = calendar.get(Calendar.DAY_OF_WEEK);
        setCalendarHeader(dayNum);
        setCalendarDate(recodeList, calendar.get(Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(getApplicationContext(), recodeList);
        calendarView.setAdapter(gridAdapter);

        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousYear;
                int previousMonth;

                if (month == 1) {
                    previousMonth = 12;
                    previousYear = year - 1;
                } else {
                    previousMonth = month - 1;
                    previousYear = year;
                }
                calendar = Calendar.getInstance();

                calendar.set(previousYear, previousMonth - 1, 1);
                yearAndMonth.setText(previousYear + "년 " + previousMonth + "월");
                year = previousYear;
                month = previousMonth;

                int dayNum = calendar.get(Calendar.DAY_OF_WEEK);
                setCalendarHeader(dayNum);
                setCalendarDate(recodeList, calendar.get(Calendar.MONTH) + 1);
                gridAdapter.notifyDataSetChanged();

            }
        });

        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextYear;
                int nextMonth;

                if (month == 12) {
                    nextMonth = 1;
                    nextYear = year + 1;
                } else {
                    nextMonth = month + 1;
                    nextYear = year;
                }
                calendar = Calendar.getInstance();

                calendar.set(nextYear, nextMonth - 1, 1);
                yearAndMonth.setText(nextYear + "년 " + nextMonth + "월");
                year = nextYear;
                month = nextMonth;

                int dayNum = calendar.get(Calendar.DAY_OF_WEEK);
                setCalendarHeader(dayNum);
                setCalendarDate(recodeList, calendar.get(Calendar.MONTH) + 1);
                gridAdapter.notifyDataSetChanged();
            }
        });

    }

    private void setCalendarHeader(int dayNum) {
        recodeList.clear();
        addRecode(recodeList, "일", "");
        addRecode(recodeList, "월", "");
        addRecode(recodeList, "화", "");
        addRecode(recodeList, "수", "");
        addRecode(recodeList, "목", "");
        addRecode(recodeList, "금", "");
        addRecode(recodeList, "토", "");

        //1일 - 요일 매칭 시키기 위해 공백 add
        for (int i = 1; i < dayNum; i++) {
            addRecode(recodeList, "", "");
        }
    }

    /**
     * (날짜+기록)을 레코드 리스트로 만든다.
     *
     * @param list
     * @param month
     */
    private void setCalendarDate(ArrayList<Recode> list, int month) {
        calendar.set(Calendar.MONTH, month - 1);
        for (int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            addRecode(list, "" + (i + 1), "content");
        }
    }

    /**
     * 날짜+기록
     */
    private class Recode {
        String day;
        String recode;
    }

    /**
     * @param list
     * @param day
     * @param recode
     */
    private void addRecode(ArrayList list, String day, String recode) {
        Recode r = new Recode();
        r.day = day;
        r.recode = recode;
        list.add(r);
    }

    private class GridAdapter extends BaseAdapter {
        private final List<Recode> recodeList;
        private final LayoutInflater inflater;
        Context context;

        /**
         * @param context
         * @param recodeList
         */
        public GridAdapter(Context context, List<Recode> recodeList) {
            this.context = context;
            this.recodeList = recodeList;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return recodeList.size();
        }

        @Override
        public Recode getItem(int position) {
            return recodeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.calendar_item, parent, false);

                holder = new ViewHolder();
                holder.day = (TextView) convertView.findViewById(R.id.day_item);
                holder.recode = (TextView) convertView.findViewById(R.id.day_content);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 내용! 여기서 입력!!
            holder.day.setText("" + getItem(position).day);
            holder.recode.setText("" + getItem(position).recode);

            // 월화수목금토일
            if (position < 7)
                holder.recode.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            return convertView;
        }

        private class ViewHolder {
            TextView day;
            TextView recode;
        }
    }
}

