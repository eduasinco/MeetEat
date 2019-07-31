package com.example.eduardorodriguez.comeaqui.utilities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.eduardorodriguez.comeaqui.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateFragment extends Fragment {
    private static final String DATE = "date";
    private String dateString;

    public DateFragment() {
        // Required empty public constructor
    }
    public static DateFragment newInstance(String param1) {
        System.out.println(param1);
        DateFragment fragment = new DateFragment();
        Bundle args = new Bundle();
        args.putString(DATE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dateString = getArguments().getString(DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date, container, false);
        TextView dateTextView = view.findViewById(R.id.date);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH);
        try {
            Date date = format.parse(dateString);
            dateTextView.setText(getDateTextString(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return view;
    }

    String getDateTextString(Date date){
        String[] week_days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String dateTextString = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long startOfDay = LocalDateTime.now().with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            long differenceToDate = now - date.getTime();
            long differenceToStartOrDay = now - startOfDay;

            if (differenceToStartOrDay >= differenceToDate) {
                String pattern = "HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                dateTextString = df.format(date);
            } else if (differenceToStartOrDay + TimeUnit.DAYS.toMillis(1) >= differenceToDate && differenceToDate > differenceToStartOrDay) {
                dateTextString = "Yesterday";
            } else if (differenceToStartOrDay + TimeUnit.DAYS.toMillis(7) >= differenceToDate && differenceToDate > differenceToStartOrDay + TimeUnit.DAYS.toMillis(1)) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                dateTextString = week_days[c.get(Calendar.DAY_OF_WEEK)];
            } else{
                String pattern = "MM/dd/yyyy";
                DateFormat df = new SimpleDateFormat(pattern);
                dateTextString = df.format(date);
            }
        }
        return dateTextString;
    }
}