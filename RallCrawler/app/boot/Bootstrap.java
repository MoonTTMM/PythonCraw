package boot;

import helper.CrawlerHelper;

import java.text.ParseException;
import java.util.Date;

import models.Iteration;
import models.Userstory;
import models.UserstoryDuration;
import controllers.Application;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() throws ParseException {
    	initIterations();
    	if(Application.userstoryMap.size() == 0){
	    	initUserstories("data\\burndown-tdms.json");
	    	initUserstories("data\\burndown-se.json");
    	}
    }
    
    private void initIterations() throws ParseException{
    	if(Application.iterationMap.size() == 0){
    		JsonParser parser = new JsonParser();
			String iterationsContent = CrawlerHelper.ReadFromJsonFile("data\\iteration.json"); 
			JsonArray jsonIterationContent = (JsonArray)parser.parse(iterationsContent);
	    	if(jsonIterationContent != null){
	    		for(JsonElement element : jsonIterationContent){  	
	    			JsonObject jsonObject = element.getAsJsonObject();
					JsonElement startDateJson = jsonObject.get("startDate");
					JsonElement endDateJson = jsonObject.get("endDate");
					if(startDateJson != null && endDateJson != null){
						String iterationName = jsonObject.get("iteration").getAsString();
						Date startDate = CrawlerHelper.ConvertStringToDate(startDateJson.getAsString());
						Date endDate = CrawlerHelper.ConvertStringToDate(endDateJson.getAsString());
						Iteration iteration = new Iteration(iterationName, startDate, endDate);		
						if(!Application.iterationMap.containsKey(iterationName)){
							Application.iterationMap.put(iterationName, iteration);
						}
					}
	    		}
	    	}
	    	Application.iterationMap.values();
		}
    }
    
    private void initUserstories(String filePath) throws ParseException{
    	
    		JsonParser parser = new JsonParser();
			String iterationsContent = CrawlerHelper.ReadFromJsonFile(filePath); 
			JsonArray jsonIterationContent = (JsonArray)parser.parse(iterationsContent);
	    	if(jsonIterationContent != null){
	    		for(JsonElement element : jsonIterationContent){  	
	    			JsonObject jsonObject = element.getAsJsonObject();
	    			String userstory = jsonObject.get("userstory").getAsString();
	    			String iteration = jsonObject.get("iteration").getAsString();
	    			Userstory us = new Userstory(userstory);
	    			if(Application.userstoryMap.containsKey(userstory)){
	    				us = Application.userstoryMap.get(userstory);
	    			}else{
	    				Application.userstoryMap.put(userstory, us);
	    			}
	    			if(Application.iterationMap.containsKey(iteration)){
	    				Iteration i = Application.iterationMap.get(iteration);
	    				UserstoryDuration duration = new UserstoryDuration(i);
	    				if(us.durationMap.containsKey(iteration)){
	    					duration = us.durationMap.get(iteration);
	    				}else{
	    					us.durationMap.put(iteration, duration);
	    				}
	        			JsonElement startTimeObject = jsonObject.get("startTime");
	        			JsonElement endTimeObject = jsonObject.get("endTime");
	        			if(startTimeObject != null){
	        				duration.startTime = CrawlerHelper.ConvertStringToDate(startTimeObject.getAsString());	  
	        			}
	        			if(endTimeObject != null){
	        				duration.endTime = CrawlerHelper.ConvertStringToDate(endTimeObject.getAsString());
	        			}
	    			}
	    		}
	    	}
    }
}
