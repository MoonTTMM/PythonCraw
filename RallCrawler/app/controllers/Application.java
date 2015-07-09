package controllers;

import play.*;
import play.libs.F.Tuple;
import play.mvc.*;
import helper.CrawlerHelper;

import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import data.PersonalBurndownChart;
import models.*;

public class Application extends Controller {

    public static void index() throws ParseException {
    	PersonalBurndownChart chart = new PersonalBurndownChart();
    	
    	String content = CrawlerHelper.ReadFromJsonFile("D:\\Git\\PythonCraw\\userstory_data.json");
    	Date startDate = null;
    	Date endDate = null;
    	Hashtable dateUsToTime = new Hashtable<String, Date>();    	
    	Hashtable usToDateToTodo = new Hashtable<String, Hashtable<String, Integer>>();
    	Hashtable usToDateToActual = new Hashtable<String, Hashtable<String, Integer>>();
    	Hashtable usToTimespan = new Hashtable<String, Tuple<Date, Date>>();
    	    	
    	JsonParser parser = new JsonParser();
    	JsonArray jsonContent = (JsonArray)parser.parse(content);
    	if(jsonContent != null){
    		int count = 0;
    		// Get todo and actual hour for each UserStory in each day. Each day only has the last modified one.
    		for(JsonElement element : jsonContent){
    			JsonObject jsonObject = element.getAsJsonObject();
    			if(count == 0){
    				startDate = CrawlerHelper.ConvertStringToDate(jsonObject.get("startDate").getAsString());
    				endDate = CrawlerHelper.ConvertStringToDate(jsonObject.get("endDate").getAsString());
    				count++;
    				continue;
    			}
    			
    			String timeString = jsonObject.get("time").getAsString();
    			String date = timeString.split("T")[0];
    			String userstory = jsonObject.get("userstory").getAsString();
    			String identifier = date + "/" + userstory;
    			JsonElement actual = jsonObject.get("actual");
    			JsonElement todo = jsonObject.get("todo");
    			Date time = CrawlerHelper.ConvertStringToDate(timeString);
    			
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
                
                Date storedTime = (Date)dateUsToTime.get(identifier);
    			if(storedTime == null || time.after(storedTime)){
    				dateUsToTime.put(identifier, time);
    			    if(actual != null){
    			    	Hashtable actualDict = new Hashtable<String, Integer>();
    			    	if(usToDateToActual.containsKey(userstory)){
    			    		actualDict = (Hashtable<String, Integer>)usToDateToActual.get(userstory);
    			    	}
                        actualDict.put(date, actual.getAsInt());
                        usToDateToActual.put(userstory, actualDict);
                    }else if(todo != null){
                        Hashtable todoDict = new Hashtable<String, Integer>();
                        if(usToDateToTodo.containsKey(userstory)){
                        	todoDict = (Hashtable<String, Integer>)usToDateToTodo.get(userstory);
                        }
                        todoDict.put(date, todo.getAsInt());
                        usToDateToTodo.put(userstory, todoDict);                    	
    				}
    			}
    			count++;
    		}   		
    		
    		List<String> iterationDays = CrawlerHelper.GetDaysBetween(startDate, endDate);  
    		FulfillUserstoryBurndownTodo(usToDateToTodo, usToTimespan, endDate);
    		FulfillUserstoryBurndownActual(usToDateToActual, usToTimespan, startDate, endDate);   		 		
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
    	    chart.burndownData = burndownData;
    		chart.xAxis = CrawlerHelper.ConvertStringListToString(iterationDays);
    	}
    	
		chart.title = "Burndown Chart";
		chart.renderToId = "burndown_chart";
		chart.yTitle = "Hours";
    	
        render(chart);
    }
    
    // Compensate to have the whole burndown of each us.
    private static void FulfillUserstoryBurndownTodo(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, 
    		Hashtable<String, Tuple<Date, Date>> usToTimespan,
    		Date endDate){        
        for(Object map : usToDateToHour.entrySet()){
        	int previous = 0;
        	Entry<String, Hashtable<String, Integer>> entry = (Entry<String, Hashtable<String, Integer>>)map;
        	String userstory = entry.getKey();
        	Hashtable<String, Integer> dict = entry.getValue();
        	Tuple<Date, Date> timespan = usToTimespan.get(userstory);
        	endDate = new Date().before(endDate) ? new Date() : endDate;
        	List<String> days = CrawlerHelper.GetDaysBetween(timespan._1, endDate);
        	for(String day : days){
                if(!dict.containsKey(day)){
                    dict.put(day, previous);
                }else{
                    previous = (int)dict.get(day);
                }
        	}
        }
    }
    
    private static void FulfillUserstoryBurndownActual(Hashtable<String, Hashtable<String, Integer>> usToDateToHour, 
    		Hashtable<String, Tuple<Date, Date>> usToTimespan, 
    		Date startDate,
    		Date endDate){        
        for(Object map : usToDateToHour.entrySet()){
        	int previous = 0;
        	Entry<String, Hashtable<String, Integer>> entry = (Entry<String, Hashtable<String, Integer>>)map;
        	String userstory = entry.getKey();
        	Hashtable<String, Integer> dict = entry.getValue();
        	Tuple<Date, Date> timespan = usToTimespan.get(userstory);
        	endDate = new Date().before(endDate) ? new Date() : endDate;
        	List<String> days = CrawlerHelper.GetDaysBetween(startDate, endDate);
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