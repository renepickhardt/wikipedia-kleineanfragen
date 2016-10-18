import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

	static final int PORT = 1985;

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

				d.add(new StringField("title", title, Field.Store.YES));
				d.add(new TextField("text", text, Field.Store.NO));

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
		Path indexPath = Paths.get(pathIndex);
		Directory fsdir = FSDirectory.open(indexPath);
		Analyzer analyzer = new StandardAnalyzer();
		IndexReader reader = DirectoryReader.open(fsdir);
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());

		QueryParser parser = new QueryParser("text", analyzer);

		// User UI

		BufferedReader in = new BufferedReader(new FileReader("ka.csv"));
		BufferedWriter out = new BufferedWriter(
				new FileWriter(
						"/Users/rpickhardt/ownCloud/websciencemooc/part2/LESSONS/modellingText/scripts/hrv/ka2wiki.tsv"));

		String line = null;
		int cnt = 0;
		BooleanQuery.setMaxClauseCount(20000);
		while ((line = in.readLine()) != null) {
			cnt++;
			System.out.println();
			String[] values = line.split("\t");
			String url = values[0];
			String queryString = values[1];
			try {
				Query q = parser.parse(queryString);
				TopDocs top10 = searcher.search(q, 10);
				ScoreDoc[] results = top10.scoreDocs;
				String outString = url;
				for (int i = 0; i < results.length; i++) {
					float score = results[i].score;
					Document d = searcher.doc(results[i].doc);
					outString += "\thttps://de.wikipedia.org/wiki/"
							+ d.get("title").replace(' ', '_') + "\t" + score;
				}
				if (cnt % 5 == 0)
					System.out.println(cnt + " anfragen verarbeitet.");
				out.write(outString + "\n");
				out.flush();

			} catch (Exception e) {
				System.out.println(url + " IMPOSSIBLE \t\t" + e.toString());
			}
		}
		out.close();
		reader.close();
	}

	public static void main(String[] args) throws IOException, ParseException {
		String indexPath = "de-wiki-20161001-index";

		File dir = new File(indexPath);
		if (!dir.exists()) {
			System.out.println("need to create index");
			dir.mkdirs();
			File articleData = new File(
					"python/data/de-20161001-1-tabbed-article-per-line");
			IndexAndQueryWiki.indexDocs(indexPath, articleData);
		}

		BufferedReader in = new BufferedReader(new FileReader(
				System.getProperty("user.dir") + "/python/data/ka2wiki.tsv"));
		String line = "";
		HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();
		while ((line = in.readLine()) != null) {
			String[] vals = line.split("\t");
			ArrayList<String> al = new ArrayList<String>(Arrays.asList(vals));
			al = new ArrayList<String>(al.subList(1, vals.length));
			cache.put(vals[0], al);
		}
		System.out
				.println(cache
						.get("https://kleineanfragen.de/schleswig-holstein/18/237-einspeisemanagement"));

		System.out.println("loaded or created index");
		// IndexAndQueryWiki.mapper(indexPath);
		System.out.println("mapping to done");

		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();

		}
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("I/O error: " + e);
			}
			// new threa for a client
			new ResponseThread(socket, cache).start();
		}

		// File indexCache = new File("");
		// if (!indexCache.exists()){
		// System.out.println("no mappings cached so far");
		// }

	}
}