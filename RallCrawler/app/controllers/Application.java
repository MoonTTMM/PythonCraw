package controllers;

import play.*;
import play.mvc.*;
import helper.CrawlerHelper;

import java.text.ParseException;
import java.util.*;

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
    	Hashtable dateUsToTodo = new Hashtable<String, Integer>();
    	Hashtable dateUsToActual = new Hashtable<String, Integer>();	    	
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
    			Date storedTime = (Date)dateUsToTime.get(identifier);
    			if(storedTime == null || time.after(storedTime)){
    				dateUsToTime.put(identifier, time);
    				if(actual != null){
    					dateUsToActual.put(identifier, actual.getAsInt());
    				}else if(todo != null){
    					dateUsToTodo.put(identifier, todo.getAsInt());
    				}
    			}
    			count++;
    		}
    		// Sum all user stories' todo and actual hour in each day.
        	Hashtable dateToTodo = new Hashtable<String, Integer>();
        	Hashtable dateToActual = new Hashtable<String, Integer>();  
    		for(Object dateAndUS : dateUsToTime.keySet().toArray()){
    			String key = (String)dateAndUS;
    			String date = key.split("/")[0];
    			if(dateUsToTodo.containsKey(key)){
    				int todo = (int)dateUsToTodo.get(key);
    				if(dateToTodo.containsKey(date)){
    					int hours = todo + (int)dateToTodo.get(date);
    					dateToTodo.put(date, hours);
    				}else{
    					dateToTodo.put(date, todo);
    				}
    			}
    			if(dateUsToActual.containsKey(key)){
    				int actual = (int)dateUsToActual.get(key);
    				if(dateToActual.containsKey(date)){
    					dateToActual.put(date, actual + (int)dateToActual.get(date));
    				}else{
    					dateToActual.put(date, actual);
    				}
    			}
    		}
    		// Create series data for chart
    		JsonArray burndownJson = new JsonArray();
    		List<String> days = CrawlerHelper.GetDaysBetween(startDate, endDate);   		
    		List<Integer> todos = new ArrayList<Integer>();
    		List<Integer> actuals = new ArrayList<Integer>();
    		int dayCount = 0;
    		for(String day : days){
    			if(!dateToTodo.containsKey(day)){
        			if(dayCount == 0){
        				todos.add(0);
        			}else{
        				todos.add(todos.get(dayCount-1));
        			}
    			}else{
    				todos.add((int)dateToTodo.get(day));
    			}
    			if(!dateToActual.containsKey(day)){
        			if(dayCount == 0){
        				actuals.add(0);
        			}else{
        				actuals.add(actuals.get(dayCount-1));
        			}
    			}else{
    				actuals.add((int)dateToActual.get(day));
    			}
    			dayCount++;
    		}
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
    		chart.xAxis = CrawlerHelper.ConvertStringListToString(days);
    	}
    	
		chart.title = "Burndown Chart";
		chart.renderToId = "burndown_chart";
		chart.yTitle = "Hours";
    	
        render(chart);
    }
}