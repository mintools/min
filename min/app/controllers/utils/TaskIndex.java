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
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import play.Play;
import play.db.jpa.JPABase;

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
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
            writer = new IndexWriter(FSDirectory.open(new File(TaskIndex.INDEX_PATH)), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

            List<Task> tasks = Task.findAll();
            for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext();) {
                Task task = iterator.next();

                TaskIndex.addTaskToIndex(task, writer);
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

        Document document = new Document();
        document.add(new Field("id", task.id.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("title", task.title, Field.Store.YES, Field.Index.ANALYZED));

        if (!StringUtils.isEmpty(task.content)) {
            document.add(new Field("content", task.content, Field.Store.YES, Field.Index.ANALYZED));
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


//        if (!StringUtils.isEmpty(task.tagsString)) {
//            document.add(new Field("tags", task.tagsString, Field.Store.YES, Field.Index.ANALYZED));
//        }

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
}
