package controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.HighChartData;
import models.Iteration;
import play.mvc.Controller;

public class TdmsBurndown extends Controller {
	private static String filePath = "D:\\Git\\PythonCraw\\burndown-tdms.json";
	
	public static void burndownChart(String iterationQuery, String ownerQuery, String projectQuery) throws ParseException{    	
    	List<String> owners = new ArrayList<String>();  
    	owners.add("");
    	List<String> projects = new ArrayList<String>();
    	projects.add("");
		
    	Iteration currentIteration = null;
    	if(iterationQuery != null){
    		currentIteration = Application.iterationMap.get(iterationQuery);
    	}
    	
		List<Iteration> allIterations = new ArrayList<Iteration>();
		for(Iteration i : Application.iterationMap.values()){
			allIterations.add(i);
		}
		Collections.sort(allIterations);
    	
    	HighChartData chart = HighChartData.generateChart(currentIteration, ownerQuery, projectQuery, filePath, projects, owners);
				
		render(chart, owners, allIterations, projects, ownerQuery, projectQuery, currentIteration);
	}
}
