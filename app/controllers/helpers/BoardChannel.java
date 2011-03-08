package controllers.helpers;

import java.util.ArrayList;
import java.util.List;

import models.Tag;
import models.Task;

/**
 * A utility class that helps model the Task Board. 
 * 
 * @author Dan
 */
public class BoardChannel {	
	public Tag tag;
	public List<Task> tasks = new ArrayList<Task>();
}
