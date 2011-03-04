package controllers.utils;

import models.Tag;
import models.TagGroup;
import models.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: soyoung
 * Date: Jan 6, 2011
 */
public class TaskIndex {
    public static final String INDEX_PATH = Play.configuration.getProperty("index.path");

    public static void rebuildIndex() throws Exception {
        IndexWriter writer = null;
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            writer = new IndexWriter(FSDirectory.open(new File(TaskIndex.INDEX_PATH)), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

            List<Task> tasks = Task.findAll();
            for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
                Task task = iterator.next();

                addTaskToIndex(task, writer);
            }
        } finally {
            if (writer != null) {
                writer.optimize();
                writer.close();
            }
        }
    }

    public static void addTaskToIndex(Task task) throws Exception {
        IndexWriter writer = null;
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            writer = new IndexWriter(FSDirectory.open(new File(INDEX_PATH)), analyzer, IndexWriter.MaxFieldLength.UNLIMITED);

            addTaskToIndex(task, writer);
        } finally {
            if (writer != null) {
                writer.optimize();
                writer.close();
            }
        }
    }

    public static void addTaskToIndex(Task task, IndexWriter writer) throws Exception {
        // try remove document from index in case it has been indexed already
        QueryParser parser = new QueryParser(Version.LUCENE_30, "content", writer.getAnalyzer());
        Query query = parser.parse("id:" + task.id);
        writer.deleteDocuments(query);

        // this is an aggregate of all the text content in the task - used for searching
        StringBuilder allTextContentString = new StringBuilder();

        Document document = new Document();
        document.add(new Field("id", task.id.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("title", task.title, Field.Store.YES, Field.Index.ANALYZED));

        allTextContentString.append(task.title);
        allTextContentString.append(" ");

        if (!StringUtils.isEmpty(task.content)) {
            document.add(new Field("content", task.content, Field.Store.YES, Field.Index.ANALYZED));
            allTextContentString.append(task.content);
            allTextContentString.append(" ");
        }

        List<TagGroup> groups = TagGroup.findAll();

        // add tags to index
        if (task.tags != null) {
            StringBuffer tagsString = new StringBuffer();
            for (Iterator<Tag> i = task.tags.iterator(); i.hasNext();) {
                Tag tag = i.next();
                tagsString.append(tag.id);
                tagsString.append(" ");
            }

            document.add(new Field("tags", tagsString.toString(), Field.Store.YES, Field.Index.ANALYZED));
        }

        // add special field to indicate if document has NO tags of a TagGroup
        for (Iterator<TagGroup> iterator = groups.iterator(); iterator.hasNext();) {
            TagGroup group = iterator.next();
            if (!group.hasTags(task.tags)) {
                document.add(new Field("noTagsIn_" + group.id, "true", Field.Store.YES, Field.Index.NOT_ANALYZED));        
            }
        }

        document.add(new Field("allText", allTextContentString.toString(), Field.Store.NO, Field.Index.ANALYZED));

        writer.addDocument(document);
    }

    public static Long[] searchTaskIds(Query query, int numResults) throws Exception {
        Searcher searcher = new IndexSearcher(FSDirectory.open(new File(INDEX_PATH)), true);

        TopDocs docs = searcher.search(query, numResults);

        Long[] taskIds = new Long[docs.scoreDocs.length];

        for (int i = 0; i < docs.scoreDocs.length; i++) {
            Document result = searcher.doc(docs.scoreDocs[i].doc);

            taskIds[i] = Long.parseLong(result.getField("id").stringValue());
        }

        searcher.close();

        return taskIds;
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
        addMembersPredicate(assignedTo, builder, taskRoot.get("assignedTo"), predicates);

        // add workingOn predicate
        //    addMembersPredicate(workingOn, builder, taskRoot.get("workingOn").get("member"), predicates);

        // add lucene-related predicate
        BooleanQuery luceneQuery = new BooleanQuery();

        // add search string
        if (!StringUtils.isEmpty(searchText)) {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            QueryParser parser = new QueryParser(Version.LUCENE_30, "allText", analyzer);

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

            if (!groupQuery.clauses().isEmpty()) luceneQuery.add(groupQuery, BooleanClause.Occur.MUST);
        }

        // todo: the following shouldn't happen anymore as everything should now be in the "Other" group 
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
            Long[] ids = TaskIndex.searchTaskIds(luceneQuery, 50);

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
                query.orderBy(builder.desc(taskRoot.get("sortOrder")));
            }

            // execute query
            TypedQuery<Task> q = JPA.em().createQuery(query);
            tasks = q.getResultList();
        }
        return tasks;
    }

    private static void addMembersPredicate(String[] members, CriteriaBuilder builder, Path path, List<Predicate> predicates) {
        if (members != null) {
            List<Predicate> tempPredicates = new ArrayList<Predicate>();
            ArrayList<Long> memberIds = new ArrayList<Long>();
            for (int i = 0, l = members.length; i < l; i++) {
                String s = members[i];

                if ("unassigned".equalsIgnoreCase(s)) {
                    // treat unassigned differently
                    tempPredicates.add(builder.isNull(path));
                }
                else {
                    memberIds.add(Long.parseLong(s));
                }
            }

            if (memberIds.size() > 0) {
                tempPredicates.add(path.get("id").in(memberIds));
            }

            // OR the unassigned and asignedTo predicates
            if (tempPredicates.size() > 0) {
               Iterator i = tempPredicates.iterator();
                Predicate finalPredicate = (Predicate) i.next();

                while (i.hasNext()) {
                    finalPredicate = builder.or(finalPredicate, (Predicate) i.next());
                }

                predicates.add(finalPredicate);
            }
        }
    }
}
