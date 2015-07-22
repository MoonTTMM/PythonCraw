package controllers;

import helper.CrawlerHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import models.Iteration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonNull;

import data.HighChartData;
import play.libs.F.Tuple;
import play.mvc.Controller;

public class IterationController extends Controller {
	public static void iterations(String projectQuery) throws ParseException{
    	HighChartData chart = new HighChartData();    	
    	List<String> projects = new ArrayList<String>();
    	Hashtable<String, List<Iteration>> projectToIterations = new Hashtable<String, List<Iteration>>();
    	String content = CrawlerHelper.ReadFromJsonFile("D:\\Git\\PythonCraw\\iteration.json");    	    	    	    	
    	JsonParser parser = new JsonParser();
    	JsonArray jsonContent = (JsonArray)parser.parse(content);
    	if(jsonContent != null){
    		for(JsonElement element : jsonContent){  			    			
    			JsonObject jsonObject = element.getAsJsonObject();
    			String iterationName = jsonObject.get("iteration").getAsString();
    			String project = jsonObject.get("project").getAsString();
    			JsonElement estimateJson = jsonObject.get("estimateTotal");
    			JsonElement todoJson = jsonObject.get("todoTotal");
    			JsonElement actualJson = jsonObject.get("actualTotal");
    			int estimate = estimateJson == null || estimateJson == JsonNull.INSTANCE ? 0 : (int)(estimateJson.getAsDouble());    			
    			int todo = todoJson == null || todoJson == JsonNull.INSTANCE ? 0 : (int)(todoJson.getAsDouble());
    			int actual = actualJson == null || actualJson == JsonNull.INSTANCE ? 0 : (int)(actualJson.getAsDouble());
				Date startDate = CrawlerHelper.ConvertStringToDate(jsonObject.get("startDate").getAsString());
				Date endDate = CrawlerHelper.ConvertStringToDate(jsonObject.get("endDate").getAsString());
				
				List<Iteration> iterations = new ArrayList<Iteration>();
				if(projectToIterations.containsKey(project)){
					iterations = projectToIterations.get(project);
				}else{
					projectToIterations.put(project, iterations);
				}
				Iteration iteration = new Iteration(iterationName, startDate, endDate, estimate, todo, actual);
				boolean flag = true;
				for(Iteration it : iterations){
					if(it.iterationName.equals(iterationName)){
						flag = false;
						break;
					}
				}
				if(flag){
					iterations.add(iteration);
				}
				if(!projects.contains(project)){
					projects.add(project);
				}
    		}
    	}
    	
    	List<Iteration> iterations = new ArrayList<Iteration>();
    	if(projectQuery == null || projectQuery.isEmpty()){    		
    		render(chart, iterations, projects);
    	}
    	
    	if(projectToIterations.containsKey(projectQuery)){
    		iterations = projectToIterations.get(projectQuery);
    	}
		Collections.sort(iterations);
		JsonArray iterationsJson = new JsonArray();
		JsonObject actualChartData = new JsonObject();
		actualChartData.addProperty("name", "Actual");
		actualChartData.addProperty("data", CrawlerHelper.ConvertIntListToString(Iteration.getActualsSeries(iterations)));
		iterationsJson.add(actualChartData);
		JsonObject todoChartData = new JsonObject();
		todoChartData.addProperty("name", "Todo");
		todoChartData.addProperty("data", CrawlerHelper.ConvertIntListToString(Iteration.getTodosSeries(iterations)));
		iterationsJson.add(todoChartData);
		JsonObject estimateChartData = new JsonObject();
		estimateChartData.addProperty("name", "Estimate");
		estimateChartData.addProperty("data", CrawlerHelper.ConvertIntListToString(Iteration.getEstimatesSeries(iterations)));
		iterationsJson.add(estimateChartData);		
		
		String iterationData = iterationsJson.toString().replaceAll("\"", "")
				.replaceAll("Actual", "'Actual'")
				.replaceAll("Todo", "'Todo'")
				.replaceAll("Estimate", "'Estimate'");
		chart.xAxis = CrawlerHelper.ConvertStringListToString(Iteration.getIterationNames(iterations));
	    chart.chartData = iterationData;
		chart.title = "Iteration Hours Chart";
		chart.renderToId = "iterations_chart";
		chart.yTitle = "Hours";
	    
    	render(chart, iterations, projects, projectQuery);
	}
}
