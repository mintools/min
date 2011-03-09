package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.Tag;
import models.TagGroup;
import models.Task;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import play.cache.Cache;
import play.mvc.With;
import play.mvc.Scope.Session;
import controllers.helpers.BoardChannel;
import controllers.helpers.BoardModel;
import controllers.helpers.TaskFilter;
import controllers.utils.TaskIndex;

@With(Secure.class)
public class Board extends BaseController {

	/**
	 * Main entry point for the Task Board.
	 * 
	 * @throws Exception
	 */
	public static void index() throws Exception {
		List<TagGroup> tagGroups = TagGroup.getAllMutuallyExclusive();

		String selectedTagGroup = session.get("board.tagGroup.id");
		TagGroup tagGroup = null;

		if (!StringUtils.isBlank(selectedTagGroup)) {
			tagGroup = TagGroup.findById(NumberUtils.toLong(selectedTagGroup));
		}

		if (tagGroup == null && tagGroups.size() > 0) {
			tagGroup = tagGroups.get(0);
			session.put("boardTagGroupId", tagGroup.id);
		}

		if (tagGroups.size() > 0) {
			BoardModel boardModel = createBoardModel(tagGroup);
			render(boardModel, tagGroups);
		} else {
			throw new Exception("There are no mutually exclusive Tag Groups!");
		}
	}

	public static void filter(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn) throws Exception {
		TaskFilter filter = new TaskFilter();
		filter.assignedTo = assignedTo;
		filter.checkedTags = checkedTags;
		filter.noTag = noTag;
		filter.raisedBy = raisedBy;
		filter.searchText = searchText;
		filter.workingOn = workingOn;
		Cache.set(taskFilterCacheKey(), filter);
		refresh();
	}

	/**
	 * Allows us to group essentially two calls to updateBoardForTag(). Task is
	 * dragged 'from' a channel 'to' another.
	 * 
	 * @param taskId
	 * @param toTagId
	 * @param toSortOrder
	 * @param fromTagId
	 * @param fromSortOrder
	 * @throws Exception
	 */
	public static void updateBoard(Long taskId, Long toTagId, Long[] toSortOrder, Long fromTagId, Long[] fromSortOrder) throws Exception {
		updateBoardForTag(taskId, toTagId, toSortOrder);
		updateBoardForTag(taskId, fromTagId, fromSortOrder);
	}

	/**
	 * A task (taskId) is drop to a channel (representing a tagId), and the
	 * order of the channel is specified by order.
	 * 
	 * @param taskId
	 * @param tagId
	 * @param order
	 * @throws Exception
	 */
	public static void updateBoardForTag(Long taskId, Long tagId, Long[] order) throws Exception {
		Task task = Task.findById(taskId);
		Tag tag = Tag.findById(tagId);

		// First job, remove any tags from [tag] tagGroup. And add the new tag.
		task.tags.removeAll(tag.group.tags);
		task.tags.add(tag);

		ArrayList<Long> tagIds = new ArrayList<Long>();
		for (Tag t : task.tags) {
			tagIds.add(t.id);
		}
		task.setTags(tagIds);
		task.save();	
		TaskIndex.addTaskToIndex(task);

		// Then sort.
		if (order != null && order.length > 0) {
			Tasks.sort(order);
		}
	}

	/**
	 * @param id
	 * @throws Exception
	 */
	public static void showBoardforTagGroup(Long id) throws Exception {
		List<TagGroup> tagGroups = TagGroup.getAllMutuallyExclusive();
		TagGroup tagGroup = TagGroup.findById(id);
		session.put("boardTagGroupId", tagGroup.id);

		if (tagGroups.size() == 0) {
			throw new Exception("There are no mutually exclusive Tag Groups!");
		}
		if (tagGroup == null) {
			throw new Exception(String.format("The TagGroup(%s) does not exist", id));
		}

		BoardModel boardModel = createBoardModel(tagGroup);
		render("Board/index.html", boardModel, tagGroups);
	}

	public static void refresh() throws Exception {
		TagGroup tagGroup = TagGroup.findById(NumberUtils.toLong(session.get("boardTagGroupId")));
		BoardModel boardModel = createBoardModel(tagGroup);
		render("Board/_board.html", boardModel);
	}

	/**
	 * @param tagGroup
	 * @return a {@link BoardModel} that represents the supplied tagGroup
	 * @throws Exception
	 */
	public static BoardModel createBoardModel(TagGroup tagGroup) throws Exception {
		tagGroup = TagGroup.findById(tagGroup.id);
		BoardModel boardModel = new BoardModel();
		boardModel.tagGroup = tagGroup;
		for (Tag tag : tagGroup.tags) {
			BoardChannel channel = new BoardChannel();
			channel.tag = tag;

			// Use the current cached filter.
			TaskFilter taskFilter = (TaskFilter) Cache.get(taskFilterCacheKey());
			if (taskFilter != null) {

				// The checked tags list MUST contain the channels tag id.
				String[] checkedTagsArray = null;
				if (taskFilter.checkedTags != null) {					
					checkedTagsArray = taskFilter.checkedTags;
				} else {
					checkedTagsArray = new String[] { String.valueOf(tag.id) };
				}		
							
				// Get the filtered tasks.
				channel.tasks = TaskIndex.filterTasks(checkedTagsArray, taskFilter.searchText, taskFilter.noTag, taskFilter.raisedBy, taskFilter.assignedTo, taskFilter.workingOn, null, null);
				
				// This would be better done in Lucene. TODO - notags?
				for(int i=0;i<channel.tasks.size(); i++) {					
					boolean relevant = false;
					for (Tag t : channel.tasks.get(i).tags) {						
						if(t.id.equals(tag.id)) {						
							relevant = true;
							break;
						}
					}										
					if(!relevant) {					
						channel.tasks.remove(i);
						i--;
					}
				}							
			} else {				
				channel.tasks = TaskIndex.filterTasks(new String[] { "" + tag.id }, null, null, null, null, null, null, null);
			}
			boardModel.channels.add(channel);
		}
		return boardModel;
	}

	public static String taskFilterCacheKey() {
		return taskFilterCacheKey(session);

	}

	public static String taskFilterCacheKey(Session session) {
		return session.getId() + ".taskFilter";

	}
}
