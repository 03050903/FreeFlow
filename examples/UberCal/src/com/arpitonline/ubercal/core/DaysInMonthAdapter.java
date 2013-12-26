package com.arpitonline.ubercal.core;

import java.util.ArrayList;

import org.freeflow.core.BaseSectionedAdapter;
import org.freeflow.core.Section;

import com.arpitonline.ubercal.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class DaysInMonthAdapter implements BaseSectionedAdapter {

	private ArrayList<MonthInDaysView> days = new ArrayList<DaysInMonthAdapter.MonthInDaysView>();
	public DaysInMonthAdapter(){
		for(int i=0; i<12; i++){
			MonthInDaysView m = new MonthInDaysView(i);
			days.add(m);
		}
	}
	
	@Override
	public long getItemId(int section, int position) {
		return 0;
	}

	@Override
	public View getViewForSection(int section, int position, View convertView,
			ViewGroup parent) {
		
		
		if(convertView == null){

			Log.d("UberCal", ">>> getViewForSection");
			
			Context c = parent.getContext();
			convertView = new View(c);
			convertView.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.daybox));
		}
	//	MonthInDaysView m = days.get(position);
		
		return convertView;
		
	}

	@Override
	public View getHeaderViewForSection(int section, View convertView,
			ViewGroup parent) {
		if(convertView==null){
			convertView = new View(parent.getContext());
		}
		convertView.setBackgroundColor(Color.YELLOW);
		return null;
	}

	@Override
	public int getNumberOfSections() {
		return days.size();
	}

	@Override
	public Section getSection(int index) {
		return days.get(index);
	}
	
	public class MonthInDaysView extends Section{
		public MonthInDaysView(int month){
			this.sectionTitle = "Month "+month;
			for(int i=0; i< 31; i++){
				Day d = new Day();
				data.add(d);
				
			}
		}
	}
	
	public class Day{
		
	}

}
