package com.blackMonster.webkiosk.ui.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackMonster.webkiosk.SharedPrefs.RefreshDBPrefs;
import com.blackMonster.webkiosk.controller.Timetable.TimetableUtils;
import com.blackMonster.webkiosk.controller.model.SingleClass;
import com.blackMonster.webkiosk.ui.Dialog.ModifyTimetableDialog;
import com.blackMonster.webkiosk.ui.TimeLTP;
import com.blackMonster.webkiosk.ui.TimetableListFragment;
import com.blackMonster.webkiosk.ui.UIUtils;
import com.blackMonster.webkioskApp.R;

import java.util.Calendar;
import java.util.List;

/**
 * Created by akshansh on 26/07/15.
 */
public class SingleDayTimetableAdapter extends ArrayAdapter<SingleClass> {
    BroadcastModifyDialog broadcastModifyTimetableDialog;
    int currentDay;                                //Day whose data is to be displayed(Mon, Tue etc.)
    Context context;
    FragmentManager fragmentManager;
    List<SingleClass> values;                      //Data to be displayed.

    public SingleDayTimetableAdapter(int currentDay, List<SingleClass> objects, Context context, FragmentManager fm) {
        super(context, R.layout.activity_timetable_row, objects);
        this.currentDay = currentDay;
        this.context = context;
        fragmentManager=fm;
        values = objects;
    }

    public void updateDataSet(List<SingleClass> list) {
        values = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_timetable_row,
                parent, false);

        final SingleClass singleClass = values.get(position);

        //TODO
       /* rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, singleClass.getSubjectName()+" "+singleClass.getSubjectCode(), Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(context,DetailedAtndActivity.class);
                intent.putExtra(DetailedAtndActivity.SUB_NAME,singleClass.getSubjectName());
                intent.putExtra(DetailedAtndActivity.SUB_CODE,singleClass.getSubjectCode());
                context.startActivity(intent);
            }
        });*/
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createDialog(position,singleClass); //Modify timetable dialog
                notifyDataSetChanged();
                registerReceiver();     //To receive timetable modification status.
                return true;
            }
            private void createDialog(int position, SingleClass singleClass) {
                DialogFragment dialogFragment = new ModifyTimetableDialog();

                Bundle args = new Bundle();
                args.putInt(TimetableListFragment.ARG_DAY, currentDay);
                args.putInt(ModifyTimetableDialog.ARG_CURRENT_TIME, singleClass.getTime());
                args.putString(ModifyTimetableDialog.ARG_CURRENT_VENUE,
                        singleClass.getVenue());
                dialogFragment.setArguments(args);
                dialogFragment.show(fragmentManager, "timetable");
            }
            private void registerReceiver() {
                broadcastModifyTimetableDialog = new BroadcastModifyDialog();
                LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(
                                broadcastModifyTimetableDialog,
                                new IntentFilter(
                                        ModifyTimetableDialog.BROADCAST_MODIFY_TIMETABLE_RESULT));
            }
        });
        setProgressCircle(singleClass, rowView);
        ((TextView) rowView.findViewById(R.id.timetable_Sub_name))
                .setText(singleClass.getSubjectName());
        ((TextView) rowView.findViewById(R.id.timetable_venue))
                .setText(singleClass.getVenue());
        ((TextView) rowView.findViewById(R.id.timetable_class_time))
                .setText(TimetableUtils.getFormattedTime(singleClass
                        .getTime()));


        //Shows recently updated tag.
        if (!(RefreshDBPrefs.getRecentlyUpdatedTagVisibility(context) && singleClass.isAtndModified() == 1))
            ((TextView) rowView.findViewById(R.id.timetable_updated_tag))
                    .setVisibility(View.GONE);

        highlightCurrentClass(singleClass, rowView);


        ProgressBar pb = ((ProgressBar) rowView
                .findViewById(R.id.timetable_attendence_progressBar));
        UIUtils.setProgressBarColor(pb,
                singleClass.getOverallAttendence(), context);
        if (singleClass.getOverallAttendence() == -1) {
            ((TextView) rowView.findViewById(R.id.timetable_attendence))
                    .setText(UIUtils.ATND_NA);
            pb.setProgress(0);
        } else {
            ((TextView) rowView.findViewById(R.id.timetable_attendence))
                    .setText(singleClass.getOverallAttendence().toString()
                            + "%");
            pb.setProgress(singleClass.getOverallAttendence());
        }

        return rowView;

    }



    //Sets time and type("L","T","P") of clock on left of every class.
    private void setProgressCircle(SingleClass singleClass, View view) {
        int t2;
        if (TimetableUtils.isOfTwoHr(singleClass.getClassType(),
                singleClass.getSubjectCode()))
            t2 = singleClass.getTime() + 2;
        else
            t2 = singleClass.getTime() + 1;

        ((TimeLTP) view.findViewById(R.id.timetable_TimeLTP)).setParams(
                singleClass.getTime(), t2, singleClass.getClassType());
    }
    private class BroadcastModifyDialog extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                notifyDataSetChanged();
                LocalBroadcastManager.getInstance(context)
                        .unregisterReceiver(broadcastModifyTimetableDialog); //modification done, better to unregister it.
                broadcastModifyTimetableDialog = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //Highlight ongoing class.
    private void highlightCurrentClass(SingleClass singleClass, View rowView) {
        Calendar calender = Calendar.getInstance();
        boolean isCurrentClass = (calender.get(Calendar.HOUR_OF_DAY) == singleClass
                .getTime())
                || (TimetableUtils.isOfTwoHr(singleClass.getClassType(),
                singleClass.getSubjectCode()) && calender
                .get(Calendar.HOUR_OF_DAY) == singleClass.getTime() + 1);

        if (calender.get(Calendar.DAY_OF_WEEK) == currentDay
                && isCurrentClass) {

            ((RelativeLayout) rowView.findViewById(R.id.timetable_row))
                    .setBackgroundColor(Color.rgb(216, 216, 216));
        }
    }

}
