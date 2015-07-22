package models;

import helper.CrawlerHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Iteration implements Comparable<Iteration> {
	public String iterationName;
	public Date startDate;
	public Date endDate;
	public int estimate;
	public int todo;
	public int actual;
	
	public Iteration(String iterationName, Date startDate, Date endDate){
		this.iterationName = iterationName;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public Iteration(String iterationName, Date startDate, Date endDate,
			int estimate, int todo, int actual){
		this.iterationName = iterationName;
		this.todo = todo;
		this.estimate = estimate;
		this.actual = actual;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public boolean before(Iteration target){
		return endDate.before(target.startDate);
	}
	
	@Override
	public int compareTo(Iteration arg0) {
		if(this.endDate == null || this.startDate == null || this.before(arg0)){
			return -1;
		}else if(this.startDate.equals(arg0.startDate) && this.endDate.equals(arg0.endDate)){
			return 0;
		}else{
			return 1;
		}
	}
	
	public List<String> getIterationDays(){
		return CrawlerHelper.GetDaysBetween(startDate, endDate, false);
	}
	
	public static boolean contained(List<Iteration> iterations, String iterationName){
		boolean flag = false;
		for(Iteration it : iterations){
			if(it.iterationName.equals(iterationName)){
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public static List<Integer> getEstimatesSeries(List<Iteration> iterations){
		List<Integer> estimates = new ArrayList<Integer>();
		for(Iteration i : iterations){
			estimates.add(i.estimate);
		}
		return estimates;
	}
	
	public static List<Integer> getActualsSeries(List<Iteration> iterations){
		List<Integer> actuals = new ArrayList<Integer>();
		for(Iteration i : iterations){
			actuals.add(i.actual);
		}
		return actuals;
	}
	
	public static List<Integer> getTodosSeries(List<Iteration> iterations){
		List<Integer> todos = new ArrayList<Integer>();
		for(Iteration i : iterations){
			todos.add(i.todo);
		}
		return todos;
	}
	
	public static List<String> getIterationNames(List<Iteration> iterations){
		List<String> names = new ArrayList<String>();
		for(Iteration i : iterations){
			names.add(i.iterationName);
		}
		return names;
	}
}
