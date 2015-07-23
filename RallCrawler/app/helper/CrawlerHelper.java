package helper;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import controllers.Application;
import models.Iteration;

public class CrawlerHelper {
	public static String ReadFromJsonFile(String path){
		BufferedReader reader = null;
		String laststr = "";
		try{
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while((tempString = reader.readLine()) != null){
					laststr += tempString;
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return laststr;
	}
	
	public static Date ConvertStringToDate(String dateString) throws ParseException{
		dateString = dateString.replace("T", " ");
		dateString = dateString.replace("Z", "");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(dateString);
	}
	
	public static List<String> GetDaysBetween(Date startDate, Date endDate, boolean removeWeekend){
		long startTime = startDate.getTime();
		long endTime = endDate.getTime();
		long oneDay = 1000*60*60*24;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List days = new ArrayList<String>();;
		while(startTime < endTime){
			Date date = new Date(startTime);
			Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    // 去掉双休日
		    if((cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) || (!removeWeekend)){
		    	days.add(sdf.format(new Date(startTime)));
		    }
			startTime += oneDay;
		}
		return days;
	}
	
	public static Iteration getContainingIteration(Date date){
		for(Iteration i: Application.iterationMap.values()){
			if(i.startDate.before(date) && i.endDate.after(date)){
				return i;
			}
		}
		return null;
	}
	
	public static List<String> GetContainingIterationDays(Collection<String> dates) throws ParseException{
		List<Iteration> iterations = new ArrayList<Iteration>();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
		for(String dateString : dates){
			Date date = sdf.parse(dateString);
			Iteration i = CrawlerHelper.getContainingIteration(date);
			if(i != null && !Iteration.contained(iterations, i.iterationName)){
				iterations.add(i);
			}
		}
		return CrawlerHelper.getAllIterationDays(iterations);
	}
	
	public static List<String> getAllIterationDays(List<Iteration> iterations){
		List<String> days = new ArrayList<String>();
		Collections.sort(iterations);
		for(Iteration i : iterations){
			days.addAll(i.getIterationDays(false));
		}
		return days;
	}
	
	public static String ConvertIntListToString(List<Integer> list){
		String str = "[";
		int count = 1;
		for(int item : list){
			if(count < list.size()){
				str += (item + ",");
			}else{
				str += (item + "]");
			}
			count ++;
		}
		return str;
	}
	
	public static String ConvertStringListToString(List<String> list){
		String str = "[";
		int count = 1;
		for(String item : list){
			if(count < list.size()){
				str += ("'" + item + "',");
			}else{
				str += ("'" + item + "']");
			}
			count ++;
		}
		return str;
	}
}
