package dannydelott.vinescrape.bufferthread;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

import cmu.arktweetnlp.Tagger;

import com.eclipsesource.json.JsonObject;

import dannydelott.vinescrape.ExportFile;
import dannydelott.vinescrape.HtmlParser;
import dannydelott.vinescrape.Timer;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import twitter4j.Status;
import twitter4j.URLEntity;

public class ProcessingThread implements Runnable {

	// list of raw tweets to process
	private HashSet<Status> tweets;

	private HashSet<String> largeWordList;

	// buffer being sent
	private BufferType bufferType;

	// list of vine urls to check duplicates against
	private HashSet<String> urls;

	// save location
	private String outputDirectory;

	// current save file
	private String outputFile;

	// quantity already in the save location
	private int numVinesInFile;

	// number of vines to store per file
	private int numVinesPerFile;

	// quantity of total vines scraped
	private int numVinesScraped;

	// quantity of status objects collected
	private long numTweetsScraped;

	// Timer object to update run.json
	private JsonObject runJson;
	private Timer runTimer;

	// Timer object to time processing thread
	private Timer threadTimer;

	// list of results
	private HashSet<String> results;

	// pos tagger and dependency parser
	private Tagger tagger;
	private HashSet<String> posTags;

	private LexicalizedParser lexicalizedParser;
	private HashSet<String> relationTags;

	// listeners
	private ProcessingFinishedListener finishedListener;

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	public ProcessingThread(ProcessingBundle bundle_A, HashSet<String> urls,
			HashSet<String> largeWordList,
			ProcessingFinishedListener finishedListener) {

		// ------------
		// LOADS BUNDLE
		// ------------

		this.tweets = new HashSet<Status>(bundle_A.getTweets());
		this.bufferType = bundle_A.getBufferType();

		this.outputDirectory = bundle_A.getOutputDirectory();
		this.outputFile = bundle_A.getOutputFile();
		this.numVinesInFile = bundle_A.getNumVinesInFile();
		this.numVinesPerFile = bundle_A.getNumVinesPerFile();

		this.numVinesScraped = bundle_A.getNumVinesScraped();
		this.numTweetsScraped = bundle_A.getNumTweetsScraped();

		this.runTimer = bundle_A.getRunTimer();
		this.runJson = bundle_A.getRunJson();

		this.tagger = bundle_A.getTagger();
		this.lexicalizedParser = bundle_A.getLexicalizedParser();

		// ----------------------
		// INSTANTIATES HASH SETS
		// ----------------------
		posTags = new HashSet<String>();
		relationTags = new HashSet<String>();

		// ------------------------
		// LOADS URLS AND WORD LIST
		// ------------------------

		this.urls = urls;
		this.largeWordList = largeWordList;

		// ---------------
		// LOADS LISTENERS
		// ---------------

		this.finishedListener = finishedListener;

		// -------------------------
		// INSTANTIATES THREAD TIMER
		// -------------------------

		threadTimer = new Timer();

	}

	// /////////////
	// RUN METHOD //
	// /////////////

	@Override
	public void run() {

		threadTimer.begin();

		// -----------------------------
		// PRINTS PROCESSING INFORMATION
		// -----------------------------

		switch (bufferType) {
		case A:
			System.out.println("\nPROCESS BUFFER:\tA (size: "
					+ NumberFormat.getInstance().format(tweets.size()) + ")");
			break;
		case B:
			System.out.println("\nPROCESS BUFFER:\tB (size: "
					+ NumberFormat.getInstance().format(tweets.size()) + ")");
			break;
		}

		// ---------------------
		// CURRENT STATUS VALUES
		// ---------------------

		Vine vine;
		String url;
		String text;
		String downloadUrl;
		String html = null;

		// -----------------------
		// BEGIN PROCESSING BUFFER
		// -----------------------

		for (Status status : tweets) {

			// -------------
			// GETS VINE URL
			// -------------

			// gets the url, if any
			url = getVineUrl(status);
			if (url == null) {
				continue;
			}

			// checks if vine url is duplicate
			if (isDuplicateVineUrl(url)) {
				continue;
			}

			// adds url to list for future duplicate checking
			synchronized (this) {
				urls.add(url);
			}
			// ---------
			// GETS HTML
			// ---------

			try {
				html = HtmlParser.sendGet(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (html == null) {
				continue;
			}

			// --------------
			// GETS VINE TEXT
			// --------------

			// gets the vine text by navigating to the url
			// and parsing out the html output
			text = HtmlParser.parseText(html);

			// -----------------
			// GETS DOWNLOAD URL
			// -----------------

			downloadUrl = HtmlParser.parseVideoUrl(html);

			// -------------------
			// CREATES VINE OBJECT
			// -------------------

			// instantiates a new Vine object
			vine = new Vine(largeWordList, tagger, lexicalizedParser);
			vine.setUrl(url); // sets url in vine object
			vine.setText(text);
			vine.setDownloadUrl(downloadUrl);
			vine.setId(status.getId());

			// scrubs vine and gets pos tags and grammar dependencies
			if (!vine.parseVine()) {
				continue;
			}

			// ------------------------------------------------
			// GETS POS TAGS AND RELATION TAGS FROM VINE OBJECT
			// ------------------------------------------------

			posTags.addAll(vine.getPosTags());
			relationTags.addAll(vine.getRelationTags());

			synchronized (this) {

				// creates new file when current file is full vines
				if (numVinesInFile == numVinesPerFile) {
					outputFile = outputDirectory + "/" + "vines."
							+ numVinesScraped + ".json";
					numVinesInFile = 1;
				} else {
					numVinesInFile++;

				}

				// increases total quantity of vines collected
				numVinesScraped++;

				// exports vine to file
				ExportFile.appendJsonObjectToFile(vine.getVineJsonObject(),
						outputFile);

				// saves run.json time
				saveRunJsonFile();
			}

		}

		threadTimer.end();

		System.out.println("EXECUTION TIME:\t" + threadTimer.getExecutionTime()
				+ " ms");

		System.out.println("TOTAL TWEETS:\t"
				+ NumberFormat.getInstance().format(numTweetsScraped - 1));

		// finished. Send total scraped to Main.java
		finishedListener.onProcessFinished(numVinesScraped, numVinesInFile,
				outputFile, bufferType, urls, posTags, relationTags);
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private String getVineUrl(Status status) {

		String vineUrl;

		for (URLEntity url : status.getURLEntities()) {
			if (url.getExpandedURL().contains("vine.co/v/")) {
				vineUrl = url.getExpandedURL();
				return vineUrl;
			}
		}

		return null;

	}

	private boolean isDuplicateVineUrl(String url) {

		// checks if vine already exists
		if (urls.contains(url)) {
			return true;
		}

		return false;

	}

	private void saveRunJsonFile() {

		runJson.remove("end_date");
		runJson.remove("run_time");
		runJson.remove("vine_count");
		runJson.remove("total_tweets_searched");

		runJson.add("end_date", runTimer.getDate());
		runTimer.end();
		runJson.add("run_time", runTimer.getFormattedExecutionTime());
		runJson.add("vine_count", numVinesScraped);
		runJson.add("total_tweets_searched", numTweetsScraped);
		try {
			FileUtils.writeStringToFile(
					new File(outputDirectory + "/run.json"),
					runJson.toString(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public int getNumResults() {
		return results.size();
	}

	public String getOutputFile() {
		return outputFile;
	}

	public int getTotalQuantityScraped() {
		return numVinesScraped;
	}

	public HashSet<String> getUrls() {
		return urls;
	}
}
