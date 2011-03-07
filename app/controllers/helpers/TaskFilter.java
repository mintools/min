package controllers.helpers;

import java.io.Serializable;

public class TaskFilter implements Serializable {
	public String[] checkedTags;
	public String searchText;
	public String[] noTag;
	public String[] raisedBy;
	public String[] assignedTo;
	public String[] workingOn;
}
