package org.techtown.alarm;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TimePicker picker=(TimePicker)findViewById(R.id.timePicker);
        picker.setIs24HourView(true);

        CalendarView calendarView=(CalendarView)findViewById(R.id.cView);
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);

        Date nextDate = nextNotifyTime.getTime();
        //String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
        //Toast.makeText(getApplicationContext(),"[처음 실행시] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

        // 이전 설정값으로 TimePicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EE",Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM",Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        /*String weekday = weekdayFormat.format(currentTime);
        String year = yearFormat.format(currentTime);
        String month = monthFormat.format(currentTime);
        String day = dayFormat.format(currentTime);*/
        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));

        if (Build.VERSION.SDK_INT >= 23 ){
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        }
        else{
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "날짜를 먼저 선택하세요", Toast.LENGTH_LONG).show();
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {

                String ymd_text = String.format("%d년 %d월 %d일",year,month+1,dayOfMonth);
                switch (calendarView.getId())
                {
                    case R.id.cView:

                        button.setOnClickListener(new View.OnClickListener()

                        {
                            @Override
                            public void onClick (View arg0){
                                int hour, hour_24, minute;
                                String am_pm;
                                if (Build.VERSION.SDK_INT >= 23) {
                                    hour_24 = picker.getHour();
                                    minute = picker.getMinute();
                                } else {
                                    hour_24 = picker.getCurrentHour();
                                    minute = picker.getCurrentMinute();
                                }
                                if (hour_24 > 12) {
                                    am_pm = "PM";
                                    hour = hour_24 - 12;
                                } else {
                                    hour = hour_24;
                                    am_pm = "AM";
                                }
                                // 현재 지정된 시간으로 알람 시간 설정
                                Calendar calendar = Calendar.getInstance();
                                Date date = new Date(calendarView.getDate());
                                calendar.setTime(date);
                                calendar.setTimeInMillis(System.currentTimeMillis());
                                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                /*if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }*/

                                Date currentDateTime = calendar.getTime();
                                String date_text = new SimpleDateFormat(" a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                                Toast.makeText(getApplicationContext(), ymd_text + date_text +"으로 알람이 설정되었습니다!", Toast.LENGTH_LONG).show();

                                //  Preference에 설정한 값 저장
                                SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                                editor.putLong("nextNotifyTime", (long) calendar.getTimeInMillis());
                                editor.apply();


                                diaryNotification(calendar);
                            }
                        });
                        break;
                }
            }
        });


    }

    void diaryNotification(Calendar calendar)
    {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {


            if (alarmManager != null) {

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }
    }
}