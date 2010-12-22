package controllers;

import com.mortennobel.imagescaling.ResampleOp;
import models.Attachment;
import models.Tag;
import models.Task;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
public class Tasks extends Controller {
    private static final String HOME_DIR = "/soy/play-1.1/min";
    private static final String FILES_DIR = HOME_DIR + "/public/files";

    public static void index() {
        List<Task> tasks = Task.find("from Task t where t.isActive = true order by createdDate desc").fetch();

        // a placeholder for a new task 
        Task task = new Task();

        render(tasks, task);
    }

    public static void show(Long taskId) {
        Task task = Task.findById(taskId);
        boolean editing = Boolean.parseBoolean(params.get("editing"));
        renderTemplate("Tasks/_show.html", task, editing);
    }

    public static void save(@Valid Task task, File[] attachments) throws Exception {
        if (Validation.hasErrors()) {
            boolean editing = true;
            renderTemplate("Tasks/_show.html", task, editing);
        } else {
            // overwrite tags todo: find a better way of doing this
            if (task.tags != null) task.tags.clear();

            // get selected tags
            String[] selectedTags = params.getAll("selectedTags");
            for (String selectedTagName : selectedTags) {
                if (!StringUtils.isEmpty(selectedTagName)) {
                    task.tagItWith(selectedTagName);
                }
            }

            if (task.createdDate == null) {
                task.createdDate = new Date();
            }

            // add attachments
            if (attachments != null) {
                for (File file : attachments) {
                    // stick file into filesystem

                    // Destination directory
                    File dir = new File(FILES_DIR);

                    String filename = file.getName();
                    String extension = filename.substring(filename.lastIndexOf('.') + 1);

                    // generate a filename
                    String uuid = UUID.randomUUID().toString();

                    // attach it to task
                    Attachment attachment = new Attachment();

                    attachment.filename = file.getName();
                    attachment.name = uuid;
                    attachment.createdDate = new Date();
                    attachment.task = task;

                    // check if the file is an image
                    if ("png".equalsIgnoreCase(extension) ||
                            "gif".equalsIgnoreCase(extension) ||
                            "jpg".equalsIgnoreCase(extension)) {

                        attachment.type = "image";

                        // read the image
                        BufferedImage srcImage = ImageIO.read(file);

                        // scale to thumbnail
                        ResampleOp resampleOp = new ResampleOp(50, 50);
                        BufferedImage thumbnail = resampleOp.filter(srcImage, null);

                        // write the thumbnail
                        ImageIO.write(thumbnail, "png", new File(dir, "thumbnail_" + uuid + ".png"));
                    }

                    // todo: is there a better way?
                    FileInputStream in = new FileInputStream(file);
                    File outFile = new File(dir, file.getName());

                    FileOutputStream out = new FileOutputStream(outFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    task.attachments.add(attachment);
                }
            }

            task.isActive = true;
            task.save();
            renderTemplate("Tasks/_show.html", task);
        }
    }

    public static void delete(Long taskId) {
//        Task.deleteById(taskId);
        Task task = Task.findById(taskId);
        task.deactivate();
    }

    public static void listTagged(String tag) {
        List<Task> tasks = Task.findTaggedWith(tag);

        // a placeholder for a new task
        Task task = new Task();

        renderTemplate("Tasks/index.html", tag, tasks, task);
    }

    public static void trash() {
        List<Task> tasks = Task.find("from Task t where t.isActive = false order by createdDate desc").fetch();

        // a placeholder for a new task
        Task task = new Task();

        renderTemplate("Tasks/index.html", tasks, task);
    }
}
