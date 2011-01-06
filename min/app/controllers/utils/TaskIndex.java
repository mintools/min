package controllers.utils;

import models.Tag;
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

        if (task.tags != null) {
            StringBuffer tagsString = new StringBuffer();
            for (Iterator<Tag> i = task.tags.iterator(); i.hasNext();) {
                Tag tag = i.next();
                tagsString.append(tag.name);
                tagsString.append(" ");
            }

            document.add(new Field("tags", tagsString.toString(), Field.Store.YES, Field.Index.ANALYZED));
        }

//        if (!StringUtils.isEmpty(task.tagsString)) {
//            document.add(new Field("tags", task.tagsString, Field.Store.YES, Field.Index.ANALYZED));
//        }

        writer.addDocument(document);
    }

    public static List<Task> searchTasks(String field, String queryString, boolean inflateTasks) throws Exception {
        List<Task> tasks = new ArrayList<Task>();

        Searcher searcher = new IndexSearcher(FSDirectory.open(new File(INDEX_PATH)), true);

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        QueryParser parser = new QueryParser(Version.LUCENE_30, (field!=null?field:"content"), analyzer);
        Query query = parser.parse(queryString);


        TopDocs docs = searcher.search(query, 10);

        for (int i = 0; i < docs.scoreDocs.length; i++) {
            Document result = searcher.doc(docs.scoreDocs[i].doc);

            Task task = new Task();

            task.id = Long.parseLong(result.getField("id").stringValue());
            task.title = result.getField("title").stringValue();
            task.content = result.getField("content") == null ? null : result.getField("content").stringValue();

            tasks.add(task);
        }


//        Collector streamingHitCollector = new Collector() {
//            private Scorer scorer;
//            private int docBase;
//
//            // simply print docId and score of every matching document
//            public void collect(int doc) throws IOException {
//                System.out.println("doc=" + doc + docBase + " score=" + scorer.score());
//            }
//
//            public boolean acceptsDocsOutOfOrder() {
//                return true;
//            }
//
//            public void setNextReader(IndexReader reader, int docBase) throws IOException {
//                this.docBase = docBase;
//            }
//
//            public void setScorer(Scorer scorer) throws IOException {
//                this.scorer = scorer;
//            }
//        };
//        searcher.search(query, streamingHitCollector);

        analyzer.close();
        searcher.close();

        if (inflateTasks) {
            if (!tasks.isEmpty()) {
                // collect task ids from the search results
                Long[] taskIds = new Long[tasks.size()];
                for (int i = 0, l = tasks.size(); i < l; i++) {
                    Task taskResult = tasks.get(i);
                    taskIds[i] = taskResult.id;
                }

                // grab results from db and overwrite tasks
                tasks = Task.find("select t from models.Task as t where t.id in (:ids)").bind("ids", taskIds).fetch();
            }
        }

        return tasks;
    }
}
