package controllers;

import models.Tag;
import models.TagGroup;
import models.Task;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 5, 2011
 */
public class Tags extends Controller {
    public static void index() {
        List<Tag> orphanTags = Tag.find("select t from models.Tag as t where t.group = null").fetch();

        render(orphanTags);
    }

    public static void create() {
        Tag tag = new Tag();        
        render(tag);
    }

    public static void save(@Valid Tag tag) {
        if (Validation.hasErrors()) {
            boolean editing = true;
            renderTemplate("Tags/create.html", tag);
        } else {
            tag.save();
            index();
        }
    }

    public static void delete(Long id) {
        Tag tag = Tag.findById(id);

        if (!Task.findTaggedWith(tag.name).isEmpty()) {
            flash.error("Cannot delete tag due to existing tasks");
        }
        else {
            tag.delete();
        }
        index();
    }

    public static void assignGroup(Long tagId, Long groupId) {
        Tag tag = Tag.findById(tagId);

        if (groupId == null) {
            tag.group = null;
        }
        else {     
            tag.group = TagGroup.findById(groupId);
        }
        tag.save();        
    }
}
