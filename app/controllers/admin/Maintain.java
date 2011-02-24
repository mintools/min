package controllers.admin;

import models.Tag;
import models.TagGroup;
import models.Task;
import play.db.DB;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 12, 2011
 */
public class Maintain extends Controller {
    public static void migrate1() {
        List<Task> tasks = Task.find("from Task t order by t.sortOrder desc").fetch();

        long i = 1;
        for (Task task : tasks) {
            // change newlines to <br>
            task.content = task.content.replace("\n", "<br/>");

            // assign sortOrder

            task.sortOrder = i++;
            task.save();
        }
    }

    public static void migrate2() {
        List<Task> tasks = Task.find("from Task t order by t.sortOrder desc").fetch();

        long i = 1;
        for (Task task : tasks) {
            // change newlines to <br>
            task.title = task.title.replaceAll("<br.{0,}?>", "\n").trim();
            task.content = task.content.replaceAll("<br.{0,}?>", "\n").trim();

            // remove all other html tags
            task.title = task.title.replaceAll("\\<.*?>","");
            task.content = task.content.replaceAll("\\<.*?>","");

            task.save();
        }
    }

    public static void migrate3() {
        // create a tag group called other
        TagGroup group = TagGroup.find("byName", "Other").first();

        if (group == null) {
            group = new TagGroup();
            group.name = "Other";
            group.save();
        }

        // move all ungrouped tags to this group
        List<Tag> tags = Tag.find("from Tag t where t.group is null").fetch();
        for (Iterator<Tag> iterator = tags.iterator(); iterator.hasNext();) {
            Tag tag = iterator.next();
            tag.group = group;
            tag.save();
        }
    }

    public static void migrate4() {
        // touch all entities to create initial version

        Date date = new Date();
        long time = date.getTime();

        DB.execute("INSERT INTO MinRevisionEntity (id, timestamp, username) values (1, " + time + " , \"InitialImport\")");

        DB.execute("INSERT INTO Member_AUD (id,password,username,email,REV, REVTYPE) SELECT id,password,username,email,1,0 FROM Member");

        DB.execute("INSERT INTO TagGroup_AUD (id,name,sortOrder,mutex,defaultTag_id,REV, REVTYPE) SELECT id,name,sortOrder,mutex,defaultTag_id,1,0 FROM TagGroup");
        DB.execute("INSERT INTO Tag_AUD (id,name,group_id,sortOrder,REV, REVTYPE) SELECT id,name,group_id,sortOrder,1,0 FROM Tag");

        DB.execute("INSERT INTO Task_AUD (id,content,createdDate,isActive,sortOrder,title,assignedTo_id, owner_id,REV, REVTYPE) SELECT id,content,createdDate,isActive,sortOrder,title,assignedTo_id, owner_id,1,0 FROM Task");

        DB.execute("INSERT INTO Attachment_AUD (id,createdDate,filename,name,title,type,task_id,REV, REVTYPE) SELECT id,createdDate,filename,name,title,type,task_id,1,0 FROM attachment");
        DB.execute("INSERT INTO Comment_AUD (id,content,createdDate,member_id,task_id,REV, REVTYPE) SELECT id,content,createdDate,member_id,task_id,1,0 FROM comment");
        DB.execute("INSERT INTO Task_Tag_AUD (Task_id, tags_id,REV, REVTYPE) SELECT Task_id, tags_id,1,0 FROM task_tag");
        DB.execute("INSERT INTO WorkingOn_AUD (id, dateAssigned, member_id, task_id ,REV, REVTYPE) SELECT id, dateAssigned, member_id, task_id , 1,0 FROM WorkingOn");
    }
}
