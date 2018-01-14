package com.blackMonster.webkiosk.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackMonster.webkiosk.SharedPrefs.RefreshDBPrefs;
import com.blackMonster.webkiosk.databases.AttendanceUtils;
import com.blackMonster.webkiosk.databases.Tables.AttendenceOverviewTable;
import com.blackMonster.webkiosk.databases.Tables.DetailedAttendanceTable;
import com.blackMonster.webkiosk.ui.UIUtils;
import com.blackMonster.webkioskApp.R;

/**
 * Created by akshansh on 26/07/15.
 */
public class AtndOverviewAdapter extends CursorAdapter {

    Context context;
    public AtndOverviewAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.attendence_overview_row,
                parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor == null) return;
        String subName = cursor.getString(cursor
                .getColumnIndex(AttendenceOverviewTable.C_NAME));
        String subCode = cursor.getString(cursor
                .getColumnIndex(AttendenceOverviewTable.C_CODE));
        setTextView(R.id.atndo_Sub_name, subName, view);
        Log.d("TAGGER", "bindView: "+new DetailedAttendanceTable(subCode,0,context).getPresentCount());
        Log.d("TAGGER", "bindView: Total Classes "+subName+" "+new DetailedAttendanceTable(subCode,0,context).getTotalClasses());
        int pbProgress;
        float presentTotal=new DetailedAttendanceTable(subCode,0,context).getPresentCount();
        float totalClasses=new DetailedAttendanceTable(subCode,0,context).getTotalClasses();
        int ifPresent=0;
        int ifAbsent=0;
        if(totalClasses==0){
            ifPresent=100;
            ifAbsent=0;
        }
        else{
            ifPresent= (int) Math.ceil(((presentTotal+1)/(totalClasses+1))*100);
            ifAbsent= (int) Math.ceil((presentTotal/(totalClasses+1))*100);
        }
        Log.d("PRESENT", "bindView: "+subName+" "+ifPresent);
        Log.d("ABSENT", "bindView: "+subName+" "+ifAbsent);
        if (AttendanceUtils.isLab(subCode, context)) {
            pbProgress = cursor.getInt(cursor
                    .getColumnIndex(AttendenceOverviewTable.C_PRACTICAL));
            ((TextView) view.findViewById(R.id.atndo_lect)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.atndo_tute)).setVisibility(View.GONE);
        } else {
            pbProgress = cursor.getInt(cursor
                    .getColumnIndex(AttendenceOverviewTable.C_OVERALL));
            setTextView(R.id.atndo_lect, "L : " + atndToString(cursor.getInt(cursor
                    .getColumnIndex(AttendenceOverviewTable.C_LECTURE))), view);
            setTextView(R.id.atndo_tute, "T : " + atndToString(cursor.getInt(cursor
                    .getColumnIndex(AttendenceOverviewTable.C_TUTORIAL))), view);
        }
        setTextView(R.id.atndo_absent_attendence,atndToString(ifPresent),view);
        setTextView(R.id.atndo_present_attendence,atndToString(ifAbsent),view);
        setTextView(R.id.atndo_overall_attendence, atndToString(pbProgress), view);


       //Setting progress bar showing attendance with color.
        if (pbProgress == -1)   //In case attendance is not available.
            pbProgress = 0;
        /*TextView progressBarPresent= (TextView) view.findViewById(R.id.atndo_present_attendence);
        TextView progressBarAbsent= (TextView) view.findViewById(R.id.atndo_absent_attendence);*/
        ProgressBar pbar = ((ProgressBar) view.findViewById(R.id.atndo_progressBar));
       /* Rect presentBounds = progressBarPresent.getProgressDrawable().getBounds();
        Rect absentBounds = progressBarAbsent.getProgressDrawable().getBounds();*/
        Rect bounds = pbar.getProgressDrawable().getBounds(); //Save the drawable bound
        /*UIUtils.setProgressBarColor(progressBarPresent, ifPresent, context);
        UIUtils.setProgressBarColor(progressBarAbsent, ifAbsent, context);*/
        UIUtils.setProgressBarColor(pbar, pbProgress, context);
        /*progressBarPresent.setProgress(ifPresent);
        progressBarAbsent.setProgress(ifAbsent);*/
        pbar.setProgress(pbProgress);
        /*progressBarPresent.getProgressDrawable().setBounds(presentBounds);
        progressBarAbsent.getProgressDrawable().setBounds(absentBounds);*/
        pbar.getProgressDrawable().setBounds(bounds);


        //Shows recently updated tag.
        if (RefreshDBPrefs.getRecentlyUpdatedTagVisibility(context) &&
                cursor.getInt(cursor.getColumnIndex(AttendenceOverviewTable.C_IS_MODIFIED)) == 1)
            ((TextView) view.findViewById(R.id.atndo_updated_tag)).setVisibility(View.VISIBLE);
        else
            ((TextView) view.findViewById(R.id.atndo_updated_tag)).setVisibility(View.INVISIBLE);
    }


    private String atndToString(int x) {
        if (x == -1)
            return UIUtils.ATND_NA;
        else
            return x + "%";
    }

    private void setTextView(int id, String text, View view) {
        if (text == null) text = UIUtils.ATND_NA;
        TextView tview = ((TextView) view.findViewById(id));
        tview.setText(text);
        tview.setVisibility(View.VISIBLE);
    }

}
