package controllers;

import models.Tag;
import models.TagGroup;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * User: soyoung
 * Date: Jan 5, 2011
 */
public class TagGroups extends BaseController {
    public static void create() {
        render();
    }

    public static void save(String name) {
        TagGroup group = TagGroup.find("byName", name).first();

        if (group == null) {
            group = new TagGroup();
            group.name = name;
            group.save();
        }

        Tags.index();
    }

    public static void edit(Long id) {
        TagGroup group = TagGroup.findById(id);
        notFoundIfNull(group);
        render(group);
    }

    public static void update(TagGroup group) throws Exception {
        String name = group.name;
        // check if a taggroup with this name already exists
        TagGroup testGroup = TagGroup.find("byName", name).first();

        if (testGroup != null) {
            flash.error("TagGroup with this name already exists");
            edit(group.id);
        }
        else {
            group.save();
            Tags.index();
        }
    }

    public static void setDefault(Long groupId, Long tagId) {
		TagGroup tagGroup = TagGroup.findById(groupId);
		Tag tag = Tag.findById(tagId);

		tagGroup.setDefault(tag);
	}

    public static void clearDefault(Long groupId) {
        TagGroup tagGroup = TagGroup.findById(groupId);
        tagGroup.clearDefault();

        Tags.index();
    }
}
