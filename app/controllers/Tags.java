package controllers;

import java.util.List;

import models.Tag;
import models.TagGroup;
import models.Task;
import play.db.jpa.Blob;

/**
 * User: soyoung Date: Jan 5, 2011
 */
public class Tags extends BaseController {
	public static void index() {
		List<Tag> orphanTags = Tag.find("select t from models.Tag as t where t.group = null").fetch();

		render(orphanTags);
	}

	public static void create() {
		render();
	}

	public static void save(String name) {
		Tag.findOrCreateByName(name);
		index();
	}

    public static void edit(Long id) {
        Tag tag = Tag.findById(id);
        notFoundIfNull(tag);

        render(tag);
    }

    public static void update(Tag tag) throws Exception {
        String name = tag.name;

        // check if another tag already has this name
        Tag testTag = Tag.find("byName", name).first();

        if (testTag != null) {
            flash.error("This name is already taken by another tag");
            edit(tag.id);
        }
        else {
            tag.save();
            index();
        }
    }

	public static void setColor(Long id, String color) {
		Tag tag = Tag.findById(id);
		tag.color = color;
		tag.save();
	}

	public static void delete(Long id) {
		Tag tag = Tag.findById(id);

		if (!Task.findTaggedWith(tag.name).isEmpty()) {
			List<Task> tasks = Task.findTaggedWith(tag.name);

			renderTemplate("Tags/deleteTagError.html", tasks);
		} else {
			// if tag is currently a default, remove the tagGroup's default
			if (tag.isDefault()) {
				tag.group.setDefault(null);
			}

			tag.delete();
		}
		index();
	}

	public static void assignGroup(Long tagId, Long groupId) {
		Tag tag = Tag.findById(tagId);

		// if tag is currently a default, remove the tagGroup's default
		if (tag.isDefault()) {
			tag.group.setDefault(null);
			tag.group.save();
		}

		if (groupId == null) {
			tag.group = null;
		} else {
			tag.group = TagGroup.findById(groupId);
		}
		tag.save();
	}

	public static void changeMutex(Long groupId, Boolean isMutex) {
		TagGroup tagGroup = TagGroup.findById(groupId);

		tagGroup.mutex = isMutex;

		tagGroup.save();
	}

	public static void sort(List<Long> tags) {
		for (int i = 0; i < tags.size(); i++) {
			Long tagId = tags.get(i);
			Tag t = Tag.findById(tagId);
			t.sortOrder = tags.size() - i;
			t.save();
		}
	}

	public static void sortGroup(List<Long> tagGroups) {
		for (int i = 0; i < tagGroups.size(); i++) {
			Long tagGroupId = tagGroups.get(i);
			TagGroup group = TagGroup.findById(tagGroupId);
			group.sortOrder = tagGroups.size() - i;
			group.save();
		}
	}

	public static void getTags() {
		List<Tag> tags = Tag.findAll();
		render(tags);
	}
	
	public static void changeVisibleOnBoard(Long groupId, Boolean visibleOnBoard) {
		TagGroup tagGroup = TagGroup.findById(groupId);
		
		tagGroup.visibleOnBoard= visibleOnBoard;

		tagGroup.save();
	}

	public static void addAvatar(Long id, Blob avatar) {
		Tag tag = Tag.findById(id);
		if (tag != null && avatar != null) {
			tag.avatar = avatar;
			tag.save();
		}				
		index();
	}

	public static void getAvatar(Long id) {
		Tag tag = Tag.findById(id);
		if(tag.avatar != null && tag.avatar.exists()) {
			renderBinary(tag.avatar.get());
		}
	}
	
	public static void removeAvatar(Long id) {
		Tag tag = Tag.findById(id);
		if (tag != null ) {
			tag.avatar = null;
			tag.save();
		}				
		index();

	}

}
