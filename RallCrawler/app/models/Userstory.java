package models;

import helper.CrawlerHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class Userstory {
	public String id;
	public Hashtable<String, UserstoryDuration> durationMap;
	
	public Userstory(String id){
		this.id = id;
		this.durationMap = new Hashtable<String, UserstoryDuration>();
	}
	
	public List<String> getAllDurationDays(){
		List<UserstoryDuration> durations = getSortedDurations();
		for(UserstoryDuration d : durations){
			days.addAll(d.getDurationDays());
		}
		return days;
	}

	public List<UserstoryDuration> getSortedDurations(){
		List<UserstoryDuration> durations = new ArrayList<UserstoryDuration>();
		for(UserstoryDuration d : durationMap.values()){
			durations.add(d);
		}
		List<String> days = new ArrayList<String>();
		Collections.sort(durations);
		return durations;
	}
}
