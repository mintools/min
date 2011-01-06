package controllers;

import models.Tag;
import models.TagGroup;
import play.mvc.Controller;

import java.util.List;

/**
 * User: soyoung
 * Date: Jan 5, 2011
 */
public class Tags extends Controller {
    public static void index() {
        List<TagGroup> tagGroups = TagGroup.findAll();

        List<Tag> tags = Tag.findAll();

        List<Tag> orphanTags = Tag.find("select t from models.Tag as t where t.group = null").fetch();

        render(tagGroups, tags, orphanTags);
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
