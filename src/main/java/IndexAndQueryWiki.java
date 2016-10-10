import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexAndQueryWiki {

	public static void indexDocs(String pathIndex, File docFile)
			throws IOException {
		// Setting up the index
		Path indexPath = Paths.get(pathIndex);
		Directory fsdir = FSDirectory.open(indexPath);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(fsdir, config);

		// reading the input files
		BufferedReader in = new BufferedReader(new FileReader(docFile));
		String line = null;
		int cnt = 0;
		while ((line = in.readLine()) != null) {
			String[] fields = line.split("\t", 3);
			if (fields.length == 3) {
				cnt++;
				String title = fields[1];
				String text = fields[2];

				Document d = new Document();

				d.add(new StringField("title", title,
						Field.Store.YES));
				d.add(new TextField("text", text,
						Field.Store.NO));

				writer.addDocument(d);
				if (cnt % 10000 == 0)
					System.out.println("indexted documents: " + cnt);
			}
		}

		in.close();
		writer.close();
	}

	public static void mapper(String pathIndex) throws IOException,
			ParseException {

	}

	public static void main(String[] args) throws IOException, ParseException {
		String indexPath = "de-wiki-20161001-index";
		
		File articleData = new File(
				"/home/kleineanfragen-wp/ka2wiki/data/de-20161001-1-tabbed-article-per-line");
		File dir = new File(indexPath);
		if (!dir.exists()) {
			dir.mkdirs();
			IndexAndQueryWiki.indexDocs(indexPath, articleData);
		}
		System.out.println("loaded or created index");
		IndexAndQueryWiki.mapper(indexPath);
		System.out.println("mapping to done");
	}
}