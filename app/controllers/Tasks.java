package controllers;

import com.mortennobel.imagescaling.ResampleOp;
import controllers.utils.TaskIndex;
import models.Attachment;
import models.Comment;
import models.Member;
import models.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.With;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@With(Secure.class)
public class Tasks extends BaseController {
    private static final String FILES_DIR = Play.configuration.getProperty("fileStorage.location");

    public static void index(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn) throws Exception {
        flash.clear();
        
        List<Task> tasks = TaskIndex.filterTasks(checkedTags, searchText, noTag, raisedBy, assignedTo, workingOn);

        params.flash();
        render(tasks);
    }

    public static void filter(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn) throws Exception {

        List<Task> tasks = TaskIndex.filterTasks(checkedTags, searchText, noTag, raisedBy, assignedTo, workingOn);

        params.flash();
        renderTemplate("Tasks/_tasks.html", tasks);
    }

    public static void show(Long id, boolean editMode) {
        Task task = Task.findById(id);
        renderTemplate("Tasks/_task.html", task, editMode);
    }

    public static void save(@Valid Task task, File[] attachments, String newComment, Long[] workerIds) throws Exception {

        Member loggedInMember = getLoggedInMember();

        notFoundIfNull(loggedInMember);

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
                    Attachment attachment = createAttachment(file);

                    attachment.task = task;
                    task.attachments.add(attachment);
                }
            }

            // add comment
            if (!StringUtils.isEmpty(newComment)) {
                task.addComment(loggedInMember, newComment);
            }

            // add workers
            if (workerIds != null) {
                List<Member> workers = new ArrayList<Member>();
                for (int i = 0; i < workerIds.length; i++) {
                    Long workerId = workerIds[i];
                    if (workerId != null) {
                        Member member = Member.findById(workerId);
                        workers.add(member);
                    }
                }
                task.setWorkers(workers);
            }
//            if (workers != null) {
//                task.setWorkers(workers);
//            }

            task.isActive = true;
            task.save();

            // todo: nasty hack: need to set sortOrder to id for newly created tasks

            if (task.sortOrder == null) {
                task.sortOrder = task.id;
                task.save();
            }

            TaskIndex.addTaskToIndex(task);

            boolean editMode = false;
            renderTemplate("Tasks/_task.html", task, editMode);
        }
    }

    public static void addAttachment(Long taskId, File file) throws Exception {
        Task task = Task.findById(taskId);

        Attachment attachment = createAttachment(file);

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

    private static Attachment createAttachment(File file) throws IOException {
        // Destination directory
        File dir = new File(FILES_DIR);

        String filename = file.getName();
        String extension = filename.substring(filename.lastIndexOf('.') + 1);

        // generate a filename
        String uuid = UUID.randomUUID().toString();

        // attach it to task
        Attachment attachment = new Attachment();

        attachment.title = filename;
        attachment.name = uuid;
        attachment.createdDate = new Date();
        attachment.filename = attachment.name + "." + extension;

        // check if the file is an image
        if ("png".equalsIgnoreCase(extension) ||
                "gif".equalsIgnoreCase(extension) ||
                "jpg".equalsIgnoreCase(extension)) {

            attachment.type = "image";

            // read the image
            BufferedImage srcImage = ImageIO.read(file);

            // scale to thumbnail
            ResampleOp resampleOp = new ResampleOp(100, 100);
            BufferedImage thumbnail = resampleOp.filter(srcImage, null);

            // write the thumbnail
            ImageIO.write(thumbnail, "png", new File(dir, "thumbnail_" + uuid + ".png"));
        }

        // todo: is there a better way?
        FileInputStream in = new FileInputStream(file);
        File outFile = new File(dir, uuid + "." + extension);

        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return attachment;
    }
}
