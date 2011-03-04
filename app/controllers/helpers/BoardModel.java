package controllers.helpers;

import java.util.ArrayList;
import java.util.List;

import models.Tag;
import models.TagGroup;
import models.Task;
import models.WorkingOn;

/**
 * A utility class that helps model the Task Board.
 * 
 * @author Dan
 */
public class BoardModel {
	public TagGroup tagGroup;
	public List<BoardChannel> channels = new ArrayList<BoardChannel>();
}