package controllers;

import controllers.utils.TaskIndex;
import models.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.With;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@With(Secure.class)
public class Tasks extends BaseController {


    public static void index(Long id, String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn, String start_date, String end_date) throws Exception {
        flash.clear();

        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        if (!StringUtils.isEmpty(start_date) && !StringUtils.isEmpty(end_date)) {
            startDate = format.parse(start_date);
            endDate = format.parse(end_date);
        }
        else if (!StringUtils.isEmpty(start_date)) {
            startDate = format.parse(start_date);
        }

        List<Task> tasks = new ArrayList<Task>();

        if (id != null) {
            tasks.add(Task.<Task>findById(id));
        }
        else {
            tasks = TaskIndex.filterTasks(checkedTags, searchText, noTag, raisedBy, assignedTo, workingOn, startDate, endDate);
        }

        params.flash();
        render(tasks);
    }

    public static void filter(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn, String start_date, String end_date) throws Exception {
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        if (!StringUtils.isEmpty(start_date) && !StringUtils.isEmpty(end_date)) {
            startDate = format.parse(start_date);
            endDate = format.parse(end_date);
        }
        else if (!StringUtils.isEmpty(start_date)) {
            startDate = format.parse(start_date);
        }

        List<Task> tasks = TaskIndex.filterTasks(checkedTags, searchText, noTag, raisedBy, assignedTo, workingOn, startDate, endDate);

        params.flash();
        renderTemplate("Tasks/_tasks.html", tasks);
    }

    public static void show(Long id, boolean editMode) {
        Task task = Task.findById(id);
        renderTemplate("Tasks/_task.html", task, editMode);
    }

    public static void save(@Valid Task task, File[] attachments, String newComment, List<Long> interests, List<Long> workerIds, List<Long> selectedTagIds) throws Exception {

        Member loggedInMember = getLoggedInMember();

        notFoundIfNull(loggedInMember);

        boolean isNew = (task.id == null);

        if (Validation.hasErrors()) {
            boolean editMode = true;
            renderTemplate("Tasks/_task.html", task, editMode);
        } else {
            if (task.createdDate == null) {
                task.createdDate = new Date();
            }

            if (task.owner == null) {
                task.owner = loggedInMember;
            }

            // add attachments
            if (attachments != null) {
                for (File file : attachments) {
                    Attachment attachment = Attachment.createAttachment(file);

                    attachment.task = task;
                    task.attachments.add(attachment);
                }
            }

            // set tags
            if (selectedTagIds != null) {
                task.setTags(selectedTagIds);
            }


            // add comment
            if (!StringUtils.isEmpty(newComment)) {
                task.addComment(loggedInMember, newComment);
            }

            // add workers
            if (workerIds != null) {
                task.setWorkers(workerIds);
            }

            // add interests
            if (interests != null) {
                task.setInteresteds(interests);
            }

            task.isActive = true;
            task.save();

            // todo: nasty hack: need to set sortOrder to id for newly created tasks

            if (task.sortOrder == null) {
                task.sortOrder = task.id;
                task.save();
            }

            TaskIndex.addTaskToIndex(task);

            // send email
            if (isNew) {
//            Mails.newTask(task);                
            }
            else {
                // delay 10 secs to allow transaction to finish (so mail can pick up the latest change log)
                EmailSpec email = new EmailSpec(task.id);
                email.save();
            }

            boolean editMode = false;
            renderTemplate("Tasks/_task.html", task, editMode);
        }
    }

    public static void addAttachment(Long taskId, File file) throws Exception {
        Task task = Task.findById(taskId);

        Attachment attachment = Attachment.createAttachment(file);

        attachment.task = task;
        task.attachments.add(attachment);
        attachment.save();

        renderTemplate("Tasks/addAttachment.xml", attachment);
    }

    public static void deleteAttachment(Long id) {
        Attachment attachment = Attachment.findById(id);
        attachment.delete();
    }

    public static void addComment(Long id, Long memberId, String content) {
        Task task = Task.findById(id);
        Member member = Member.findById(memberId);
        Comment comment = task.addComment(member, content);

        renderTemplate("Tasks/comment.html", comment);
    }

    public static void deleteComment(Long id) {
        Comment comment = Comment.findById(id);

        comment.delete();
    }

    public static void delete(Long taskId) {
//        Task.deleteById(taskId);
        Task task = Task.findById(taskId);
        task.deactivate();
    }


    public static void undelete(Long taskId) {
//        Task.deleteById(taskId);
        Task task = Task.findById(taskId);
        task.activate();
    }

    public static void listTagged(String tag) throws Exception {
        List<Task> tasks = null;

        if (!StringUtils.isEmpty(tag)) {
            TermQuery query = new TermQuery(new Term("tags", tag));

            Long[] ids = TaskIndex.searchTaskIds(query, 10);
            if (ids != null && ids.length > 0) {
                tasks = Task.find("form Task t where t.isActive = true and t.id in (:ids)").bind("ids", ids).fetch();
            }
        }

        renderTemplate("Tasks/index.html", tag, tasks);
    }

    public static void trash() {
        List<Task> tasks = Task.findInactive();

        render(tasks);
    }

    public static void sort(Long[] order) {

        // work out ordering
        List<Task> tasks = Task.find("select t from models.Task t where t.id in (:taskIds) order by t.sortOrder desc").bind("taskIds", order).fetch();

        ArrayList<Long> ordering = new ArrayList<Long>();
        for (Task task : tasks) {
            ordering.add(task.sortOrder);
        }

        // assign ordering
        for (int i = 0; i < order.length; i++) {
            Task task = Task.findById(order[i]);
            task.sortOrder = ordering.get(i);
            task.save();
        }
    }

    public static void getRevisions(Long taskId) throws Exception {
        Task task = Task.findById(taskId);

        Map changeMap = task.retrieveRevisions();

        render(changeMap, task);
    }
}
