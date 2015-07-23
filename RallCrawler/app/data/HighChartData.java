package data;

import helper.CrawlerHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import models.Iteration;
import play.libs.F.Tuple;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.Application;

public class HighChartData{
	public String renderToId;
	public String title;
	public String yTitle;
	public String xAxis;
	public String chartData;
	
	public static HighChartData generateChart(Iteration currentIteration, String ownerQuery, 
			String projectQuery, String filePath, List<String> projects, List<String> owners) throws ParseException{
    	HighChartData chart = new HighChartData();    	
    	filePath = filePath == null ? "D:\\Git\\PythonCraw\\burndown-se.json" : filePath;
    	String burndownContent = CrawlerHelper.ReadFromJsonFile(filePath);       	  
    	Hashtable dateUsToTodoTime = new Hashtable<String, Date>();    
    	Hashtable dateUsToActualTime = new Hashtable<String, Date>();  
    	
    	Hashtable usToDateToTodo = new Hashtable<String, Hashtable<String, Integer>>();
    	Hashtable usToDateToActual = new Hashtable<String, Hashtable<String, Integer>>();
    	Hashtable usToTimespan = new Hashtable<String, Tuple<Date, Date>>();   	    	
    			
		JsonParser parser = new JsonParser();
    	JsonArray jsonBurndownContent = (JsonArray)parser.parse(burndownContent);
    	if(jsonBurndownContent != null){
    		for(JsonElement element : jsonBurndownContent){  			    			
    			JsonObject jsonObject = element.getAsJsonObject();    
    			JsonElement timeObject = jsonObject.get("time");
    			if(timeObject == null){
    				continue;
    			}
    			String timeString = timeObject.getAsString();
    			Date time = CrawlerHelper.ConvertStringToDate(timeString);
    			// -5 hours to make time consistent.
    			time = new Date(time.getTime() - 1000 * 60 * 60 * 5); 
    			
    			String userstory = jsonObject.get("userstory").getAsString();   
    			String owner = jsonObject.get("owner").getAsString();
    			String project = jsonObject.get("project").getAsString();
    			
    			String iteration = jsonObject.get("iteration").getAsString();
    			Iteration itForUs = Application.iterationMap.get(iteration);
    			if(!projects.contains(project)){
    				projects.add(project);
    			}
    			if(!owners.contains(owner)){
    				owners.add(owner);
    			}
    			if(currentIteration == null){
    				continue;
    			}
    			if(!itForUs.iterationName.equals(currentIteration.iterationName) && itForUs.before(currentIteration)){
    				continue;
    			}
    			if(ownerQuery == null || (!ownerQuery.contains(owner) && !ownerQuery.isEmpty())){
    				continue;
    			}
    			if(projectQuery == null || (!projectQuery.contains(project) && !projectQuery.isEmpty())){
    				continue;
    			}
    			JsonElement actual = jsonObject.get("actual");
    			JsonElement todo = jsonObject.get("todo");
   			    			
    			SetUserstoryRevisionDuration(usToTimespan, userstory, time);    			
                if(actual != null){    		
                	InitUsToDataToHourMap(usToDateToActual, dateUsToActualTime, actual.getAsInt(), time, userstory);
                }else if(todo != null){
                	InitUsToDataToHourMap(usToDateToTodo, dateUsToTodoTime, todo.getAsInt(), time, userstory);
                }
    		}   		
    		
			if(currentIteration == null){
				return chart;
			}
    		List<String> iterationDays = CrawlerHelper.GetDaysBetween(currentIteration.startDate, currentIteration.endDate, true);  
    		FulfillUserstoryBurndownTodo(usToDateToTodo, usToTimespan, currentIteration);
    		FulfillUserstoryBurndownActual(usToDateToActual, currentIteration);   		 		
    		List<Integer> todos = GetBurndownHourSeries(usToDateToTodo, iterationDays);
    		List<Integer> actuals = GetBurndownHourSeries(usToDateToActual, iterationDays);

    		// Create series data for chart
    		JsonArray burndownJson = new JsonArray();
    		JsonObject actualChartData = new JsonObject();
    		actualChartData.addProperty("name", "Actual");
    		actualChartData.addProperty("data", CrawlerHelper.ConvertIntListToString(actuals));
    		burndownJson.add(actualChartData);
    		JsonObject todoChartData = new JsonObject();
    		todoChartData.addProperty("name", "Todo");
    		todoChartData.addProperty("data", CrawlerHelper.ConvertIntListToString(todos));
    		burndownJson.add(todoChartData);
    		
    		String burndownData = burndownJson.toString().replaceAll("\"", "").replaceAll("Actual", "'Actual'").replaceAll("Todo", "'Todo'");
    	    chart.chartData = burndownData;
    		chart.xAxis = CrawlerHelper.ConvertStringListToString(iterationDays);
    	}
    	
		chart.title = "Persernal Burndown Chart";
		chart.renderToId = "burndown_chart";
		chart.yTitle = "Hours";  	
		
		return chart;
	}
	
    private static void SetUserstoryRevisionDuration(Hashtable<String, Tuple<Date, Date>> usToTimespan, String userstory, Date time){
		if(usToTimespan.containsKey(userstory)){
			Tuple<Date, Date> timespan = (Tuple<Date, Date>)usToTimespan.get(userstory);
            if(time.before(timespan._1)){
                usToTimespan.put(userstory, new Tuple<Date, Date>(time, timespan._2));
            }else if(time.after(timespan._2)){
                usToTimespan.put(userstory, new Tuple<Date, Date>(timespan._1, time));
            }
		}else{
			usToTimespan.put(userstory, new Tuple<Date, Date>(time, time));
		}
    }
    
    private static void InitUsToDataToHourMap(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, 
    		Hashtable<String, Date> dateUsToTime, int hour, Date time, String userstory){
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
		String date = sdf.format(time);
		String identifier = date + "/" + userstory;
	    Date storedTime = (Date)dateUsToTime.get(identifier);
		if(storedTime == null || time.after(storedTime)){
			dateUsToTime.put(identifier, time);
            Hashtable todoDict = new Hashtable<String, Integer>();
            if(usToDateToHour.containsKey(userstory)){
            	todoDict = (Hashtable<String, Integer>)usToDateToHour.get(userstory);
            }
            todoDict.put(date, hour);
            usToDateToHour.put(userstory, todoDict);                    	
		}
    }
    
    // Compensate to have the whole burndown of each us.
    // private static void FulfillUserstoryBurndownTodo(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, 
    // 		Hashtable<String, Tuple<Date, Date>> usToTimespan,
    // 		Iteration iteration) throws ParseException{        
    //     for(Object map : usToDateToHour.entrySet()){
    //     	int previous = 0;
    //     	Entry<String, Hashtable<String, Integer>> entry = (Entry<String, Hashtable<String, Integer>>)map;
    //     	String userstory = entry.getKey();
    //     	Hashtable<String, Integer> dict = entry.getValue();
    //     	List<String> days = Application.userstoryMap.get(userstory).getAllDurationDays();
    //     	for(String day : days){
    //             if(!dict.containsKey(day)){
    //                 dict.put(day, previous);
    //             }else{
    //                 previous = (int)dict.get(day);
    //             }
    //     	}
    //     }
    // }

    private static void FulfillUserstoryBurndownTodo(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, 
            Hashtable<String, Tuple<Date, Date>> usToTimespan,
            Iteration iteration) throws ParseException{        
        for(Object map : usToDateToHour.entrySet()){
            int previous = 0;
            Entry<String, Hashtable<String, Integer>> entry = (Entry<String, Hashtable<String, Integer>>)map;
            String userstory = entry.getKey();
            Hashtable<String, Integer> dict = entry.getValue();
            Userstory us = Application.userstoryMap.get(userstory);
            List<UserstoryDuration> durations = us.getSortedDurations();
            List<String> days = Application.userstoryMap.get(userstory).getAllDurationDays();
            for(String day : days){
                if(!dict.containsKey(day)){
                    dict.put(day, previous);
                }else{
                    previous = (int)dict.get(day);
                }
            }
        }
    }
    
    private static void FulfillUserstoryBurndownActual(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, Iteration iteration){        
        for(Hashtable<String, Integer> dict : usToDateToHour.values()){
        	int previous = 0;
        	Date endDate = new Date().before(iteration.endDate) ? new Date() : iteration.endDate;
        	List<String> days = CrawlerHelper.GetDaysBetween(iteration.startDate, endDate, true);
        	for(String day : days){
                if(!dict.containsKey(day)){
                    dict.put(day, previous);
                }else{
                    previous = (int)dict.get(day);
                }
        	}
        }
    }
    
    // Sum hours for user stories in the same day.
    private static List<Integer> GetBurndownHourSeries(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, List<String> days){
		List<Integer> hours = new ArrayList<Integer>();
        for(String day : days){
            int hour = 0;
            for(Object value : usToDateToHour.values()){
            	Hashtable<String, Integer> dict = (Hashtable<String, Integer>)value;
                if(dict.containsKey(day)){
                	hour += (int)dict.get(day);
                }
            }
            hours.add(hour);
        }
        return hours;
    }
}
