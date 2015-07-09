import org.junit.*;

import helper.CrawlerHelper;

import java.util.*;
import java.text.ParseException;

import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
    }
    
    @Test
    public void ReadJsonFileTest(){
    	String content = CrawlerHelper.ReadFromJsonFile("D:\\Git\\PythonCraw\\userstory_data.json");
    	assertFalse(content.isEmpty());
    	assertTrue(content.startsWith("[{\"startDate\":"));
    }
    
    @Test
    public void ConvertStringToDateTest(){
    	try{
    		Date date = CrawlerHelper.ConvertStringToDate("2015-06-23T08:49:43.287Z");
    		assertNotNull(date);
    	}catch(ParseException e){
    		fail();
    	}
    }
    
    @Test
    public void ConvertListToStringTest(){
    	List<Integer> list = new ArrayList<Integer>();
    	list.add(1);
    	list.add(2);
    	list.add(3);
    	String str = CrawlerHelper.ConvertIntListToString(list);
    	assertEquals("[1,2,3]", str);
    }
}
