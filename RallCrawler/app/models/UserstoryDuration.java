package models;

import helper.CrawlerHelper;

import java.util.Date;
import java.util.List;

public class UserstoryDuration implements Comparable<UserstoryDuration> {
	public Iteration iteration;
	public Date startTime;
	public Date endTime;
	
	public Date getActualStart(){
		return startTime != null && iteration.startDate.before(startTime) ? startTime : iteration.startDate;
	}
	
	public Date getActualEnd(){
		Date end =  endTime != null && iteration.endDate.after(endTime) ? endTime : iteration.endDate;
		return end.after(new Date()) ? new Date() : end;
	}
	
	public UserstoryDuration(Iteration i){
		iteration = i;
	}
	
	public boolean before(UserstoryDuration target){
		return getActualEnd().before(target.getActualStart());
	}
	
	@Override
	public int compareTo(UserstoryDuration arg0) {
		if(this.before(arg0)){
			return -1;
		}else if(getActualEnd().equals(arg0.getActualEnd()) && this.getActualStart().equals(arg0.getActualStart())){
			return 0;
		}else{
			return 1;
		}
	}
	
	public List<String> getDurationDays(){
		return CrawlerHelper.GetDaysBetween(getActualStart(), getActualEnd(), false);
	}
}
