package controllers;

import com.mortennobel.imagescaling.ResampleOp;
import controllers.utils.TaskIndex;
import models.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.imageio.ImageIO;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * User: soyoung
 * Date: Dec 21, 2010
 */
@With(Secure.class)
public class Tasks extends Controller {
    private static final String FILES_DIR = Play.configuration.getProperty("fileStorage.location");


    public static void index(Long taskId) {
        List<Task> tasks;

        if (taskId != null) {
            tasks = new ArrayList<Task>();
            Task task = Task.findById(taskId);
            tasks.add(task);
        } else {
            tasks = Task.findActive();
        }

        // a placeholder for a new task
        Task task = new Task();

        render(tasks, task);
    }

    public static void show(Long taskId) {
        Task task = Task.findById(taskId);
        boolean editing = Boolean.parseBoolean(params.get("editing"));
        renderTemplate("Tasks/task.html", task, editing);
    }

    public static void save(@Valid Task task, File[] attachments, String selectedTags) throws Exception {

        Member loggedInUser = Member.connected();

        notFoundIfNull(loggedInUser);

        if (Validation.hasErrors()) {
            boolean editing = true;
            renderTemplate("Tasks/_show.html", task, editing);
        } else {
            if (task.createdDate == null) {
                task.createdDate = new Date();
            }

            if (task.owner == null) {
                task.owner = loggedInUser;
            }

            // overwrite tags todo: find a better way of doing this
            if (task.tags != null) task.tags.clear();

            // todo: tags are stored in BOTH db and lucene index. <-- is this ok?
            // todo: we need to enforce the mutex rules for tagGroups
            // todo: remove hardcoding

            // get selected tags
            if (!StringUtils.isEmpty(selectedTags)) {
                StringTokenizer tokens = new StringTokenizer(selectedTags, " ", false);
                while (tokens.hasMoreTokens()) {
                    String tag = tokens.nextToken();                    
                    task.tagItWith(tag);
                }
            }

            // add attachments
            if (attachments != null) {
                for (File file : attachments) {
                    Attachment attachment = createAttachment(file);

                    attachment.task = task;
                    task.attachments.add(attachment);
                }
            }

            task.isActive = true;
            task.save();

            // todo: nasty hack: need to set sortOrder to id for newly created tasks

            if (task.sortOrder == null) {
                task.sortOrder = task.id;
                task.save();
            }

            TaskIndex.addTaskToIndex(task);

            renderTemplate("Tasks/task.html", task);
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

    public static void addInterest(Long id) {
        Task task = Task.findById(id);
        Member member = Member.connected();

        member.addInterest(task);

        renderTemplate("Members/taskInterest.html", member, task);
    }

    public static void removeInterest(Long id) {
        Task task = Task.findById(id);
        Member member = Member.connected();

        member.removeInterest(task);

        renderTemplate("Members/avatar.html", member);
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

        // a placeholder for a new task
        Task task = new Task();

        renderTemplate("Tasks/index.html", tag, tasks, task);
    }

    public static void trash() {
        List<Task> tasks = Task.findInactive();

        // a placeholder for a new task
        Task task = new Task();

        render(tasks);
    }

    public static void filter(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn) throws Exception {

        List<Task> tasks = filterTasks(checkedTags, searchText, noTag, raisedBy, assignedTo, workingOn);

        // a placeholder for a new task
        Task task = new Task();

        renderTemplate("Tasks/index.html", checkedTags, assignedTo, raisedBy, noTag, workingOn, searchText, tasks, task);
    }

    // todo: add "workingOn" to filter
    public static List<Task> filterTasks(String[] checkedTags, String searchText, String[] noTag, String[] raisedBy, String[] assignedTo, String[] workingOn) throws Exception {
        CriteriaBuilder builder = JPA.em().getCriteriaBuilder();

        CriteriaQuery<Task> query = builder.createQuery(Task.class);

        Root<Task> taskRoot = query.from(Task.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        // task must be active
        predicates.add(builder.equal(taskRoot.get("isActive"), true));

        // add raisedBy predicate
        if (raisedBy != null) {
            ArrayList<Long> memberIds = new ArrayList<Long>();
            for (int i = 0, l = raisedBy.length; i < l; i++) {
                String s = raisedBy[i];
                memberIds.add(Long.parseLong(s));
            }

            if (memberIds.size() > 0) {
                predicates.add(taskRoot.get("owner").get("id").in(memberIds));
            }
        }

        // add assignedTo predicate
        List<Predicate> assignedToPredicates = new ArrayList<Predicate>();
        if (assignedTo != null) {
            ArrayList<Long> memberIds = new ArrayList<Long>();
            for (int i = 0, l = assignedTo.length; i < l; i++) {
                String s = assignedTo[i];

                if ("unassigned".equalsIgnoreCase(s)) {
                    // treat unassigned differently
                    assignedToPredicates.add(builder.isNull(taskRoot.get("assignedTo")));
                }
                else {
                    memberIds.add(Long.parseLong(s));
                }
            }

            if (memberIds.size() > 0) {
                assignedToPredicates.add(taskRoot.get("assignedTo").get("id").in(memberIds));
            }

            // OR the unassigned and asignedTo predicates
            if (assignedToPredicates.size() > 0) {
               Iterator i = assignedToPredicates.iterator();
                Predicate finalPredicate = (Predicate) i.next();

                while (i.hasNext()) {
                    finalPredicate = builder.or(finalPredicate, (Predicate) i.next());
                }

                predicates.add(finalPredicate);
            }
        }

//        // add "no tags in group" predictate (must be done in DB since Lucene does not work well with negation queries
//        if (noTag != null) {
//            for (int i = 0; i < noTag.length; i++) {
//                String tagGroupIdString = noTag[i];
//                TagGroup group = TagGroup.findById(Long.parseLong(tagGroupIdString));
//
//                Join<Task, Tag> join = taskRoot.join("tags", JoinType.LEFT);
//                Predicate joinPredicate = join.in(group.tags);
//
//                predicates.add(builder.not(joinPredicate));
//            }
//        }

        // add lucene-related predicate
        BooleanQuery luceneQuery = new BooleanQuery();

//        luceneQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);

        // add search string
        if (!StringUtils.isEmpty(searchText)) {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            QueryParser parser = new QueryParser(Version.LUCENE_30, "content", analyzer);

            Query textSearchQuery = parser.parse(searchText);

            luceneQuery.add(textSearchQuery, BooleanClause.Occur.MUST);
            analyzer.close();
        }

        // iterate through all tagGroups and all tags, checking if they have been checked by the user
        List<Tag> selectedTags = new ArrayList();

        if (checkedTags != null) {
            for (int i = 0; i < checkedTags.length; i++) {
                String tagIdString = checkedTags[i];
                Tag tag = Tag.findById(Long.parseLong(tagIdString));
                selectedTags.add(tag);
            }
        }

        List<TagGroup> selectedNoTagGroup = new ArrayList();
        if (noTag != null) {
            for (int i = 0; i < noTag.length; i++) {
                String tagGroupIdString = noTag[i];
                TagGroup group = TagGroup.findById(Long.parseLong(tagGroupIdString));
                selectedNoTagGroup.add(group);
            }
        }

        List<TagGroup> groups = TagGroup.findAll();
        for (Iterator<TagGroup> iterator = groups.iterator(); iterator.hasNext();) {
            TagGroup tagGroup = iterator.next();

            BooleanQuery groupQuery = new BooleanQuery();

            for (Iterator<Tag> tagIterator = tagGroup.tags.iterator(); tagIterator.hasNext();) {
                Tag tag = tagIterator.next();
                // check if tag has been selected
                if (selectedTags.contains(tag)) {
                    TermQuery tagQuery = new TermQuery(new Term("tags", tag.id.toString()));
                    groupQuery.add(tagQuery, BooleanClause.Occur.SHOULD);
                    // tag has been used, remove it
                    selectedTags.remove(tag);
                }
            }

            // check if "no group" has been selected
            if (selectedNoTagGroup.contains(tagGroup)) {
                TermQuery noTagsQuery = new TermQuery(new Term("noTagsIn_" + tagGroup.id, "true"));
                groupQuery.add(noTagsQuery, BooleanClause.Occur.SHOULD);
            }

//                check if "no group" has been selected
//            if (selectedNoTagGroup.contains(tagGroup)) {
//                BooleanQuery noGroupTagQuery = new BooleanQuery();
//                for (Iterator<Tag> tagIterator = tagGroup.tags.iterator(); tagIterator.hasNext();) {
//                    Tag tag = tagIterator.next();
//                    TermQuery tagQuery = new TermQuery(new Term("tags", tag.name));
//                    noGroupTagQuery.add(tagQuery, BooleanClause.Occur.SHOULD);
//                }
//                groupQuery.add(noGroupTagQuery, BooleanClause.Occur.MUST_NOT);
//            }

            if (!groupQuery.clauses().isEmpty()) luceneQuery.add(groupQuery, BooleanClause.Occur.MUST);
        }

        // go through remaining tags in selectedTags list (ie. those that are not in a group)
        BooleanQuery ungroupedTagQuery = new BooleanQuery();
        for (Iterator<Tag> iterator = selectedTags.iterator(); iterator.hasNext();) {
            Tag tag = iterator.next();

            TermQuery tagQuery = new TermQuery(new Term("tags", tag.id.toString()));
            ungroupedTagQuery.add(tagQuery, BooleanClause.Occur.SHOULD);
        }
        if (!ungroupedTagQuery.clauses().isEmpty()) luceneQuery.add(ungroupedTagQuery, BooleanClause.Occur.MUST);

        boolean noResults = false;

        // create lucene predicate
        if (!luceneQuery.clauses().isEmpty()) {
            Long[] ids = TaskIndex.searchTaskIds(luceneQuery, 10);

            if (ids != null && ids.length > 0) {
                predicates.add(taskRoot.get("id").in(ids));
            }
            else {
                // lucene hasn't returned anything, shortcut the rest of the search
                noResults = true;
            }
        }

        List<Task> tasks;

        if (noResults) {
            tasks = new ArrayList<Task>();
        }
        else {
            // aggregate all predicates
            if (predicates.size() > 0) {
                Iterator i = predicates.iterator();
                Predicate finalPredicate = (Predicate) i.next();

                while (i.hasNext()) {
                    finalPredicate = builder.and(finalPredicate, (Predicate) i.next());
                }

                query.where(finalPredicate);
            }

            // execute query
            TypedQuery<Task> q = JPA.em().createQuery(query);
            tasks = q.getResultList();
        }
        return tasks;
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
            ResampleOp resampleOp = new ResampleOp(50, 50);
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
