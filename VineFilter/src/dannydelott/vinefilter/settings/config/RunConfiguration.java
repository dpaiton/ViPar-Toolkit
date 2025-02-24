package dannydelott.vinefilter.settings.config;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.config.dataset.Dataset;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class RunConfiguration {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// run configuration as JSON object
	private JsonObject jsonObject;

	// run configuration
	private int startAt;
	private int numToCollect;
	private int validationQuota;
	private String targetWordsFilePath;
	private TargetWords targetWords;
	private String datasetDirectory;
	private Dataset dataset;

	private HashSet<String> posTags;
	private boolean hasPosTagsFile;
	private String posTagsFilePath;

	private HashSet<String> relationTags;
	private boolean hasRelationTagsFile;
	private String relationTagsFilePath;

	// error flags
	private boolean flagRunConfiguration;
	private boolean flagTargetWords;
	private boolean flagDataSet;
	private boolean flagPosAndRelationTags;

	// //////////////////////
	// FACTORY CONSTRUCTOR //
	// //////////////////////

	public static RunConfiguration newInstance(JsonObject j) {

		// creates new object using private constructor
		RunConfiguration r = new RunConfiguration(j);

		// checks if all config elements were parsed correctly
		if (r.getFlagRunConfiguration() || r.getFlagTargetWords()
				|| r.getFlagDataset() || r.getFlagPosAndRelationTags()) {
			return null;
		}

		return r;
	}

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	private RunConfiguration(JsonObject j) {

		// --------------------------------------
		// 0.
		// ASSIGNS JSON OBJECT TO GLOBAL VARIABLE
		// --------------------------------------

		// Assigns JsonObject to global variable.
		//
		// The JsonObject j passed in by the constructor contains the Run
		// Configuration settings values necessary for a customized run. It is
		// first parsed (see next step), then the TargetWords and Dataset
		// objects are constructed from the given filepaths.

		jsonObject = j;

		// -------------------------------------
		// 1.
		// PARSES OUT RUN CONFIGURATION ELEMENTS
		// -------------------------------------

		// Parses out the run configuration values from jsonObject.
		//
		// Once the global jsonObject has been assigned, this method will parse
		// out the individual JSON elements and store them in their respective
		// global variables. This can be accessed outside of the
		// RunConfiguration object by calling the getVariable() methods.

		System.out.print(Messages.RunConfig_loadingRunConfiguration);
		parseRunConfiguration();
		if (flagRunConfiguration) {
			return;
		}
		System.out.println("success");

		// --------------------------
		// 2.
		// BUILDS TARGET WORDS OBJECT
		// --------------------------

		// Builds the TargetWords object.
		//
		// Using the run configuration elements that have been parsed out in the
		// previous step, create a TargetWords object from targetWordsFilePath
		// and store it in the global variable targetWords.

		System.out.print(Messages.RunConfig_loadingTargetWords);
		buildTargetWords();
		if (flagTargetWords) {
			return;
		}
		System.out.println("success");

		// -------------------------
		// 3.
		// BUILDS THE DATASET OBJECT
		// -------------------------

		// Builds the Dataset object.
		//
		// Using the run configuration elements that have been parsed out in
		// Step 1, create a Dataset object from datasetDirectory and store it in
		// the global variable dataset.

		System.out.print(Messages.RunConfig_loadingDataset);
		buildDataset();
		if (flagDataSet) {
			return;
		}
		System.out.println("success");

		// -----------------------------------------------
		// 4.
		// BUILDS PART-OF-SPEECH AND RELATION TAG HASHSETS
		// -----------------------------------------------

		// Builds the part-of-speech and relation tag hashsets.
		//
		// Using the run configuration elements that have been parsed out in
		// Step 1, create the hashsets for the part-of-speech tags and the
		// grammar relation tags. These are useful when verifying that a Filter
		// object's filter expression is using actual grammar tags that exist in
		// the dataset.
		System.out.print(Messages.RunConfig_loadingPosAndRelationTagsFile);
		parseTagsFromFile(true, true);
		if (flagPosAndRelationTags) {
			return;
		}
		System.out.println("success");

	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagRunConfiguration() {
		return flagRunConfiguration;
	}

	public boolean getFlagTargetWords() {
		return flagTargetWords;
	}

	public boolean getFlagDataset() {
		return flagDataSet;
	}

	public boolean getFlagPosAndRelationTags() {
		return flagPosAndRelationTags;
	}

	public boolean getHasPosTagsFile() {
		return hasPosTagsFile;
	}

	public boolean getHasRelationTagsFile() {
		return hasRelationTagsFile;
	}

	public JsonObject getJsonObject() {
		return jsonObject;
	}

	public int getStartAt() {
		return startAt;
	}

	public int getNumToCollect() {
		return numToCollect;
	}

	public int getValidationQuota() {
		return validationQuota;
	}

	public String getTargetWordsFilePath() {
		return targetWordsFilePath;
	}

	public TargetWords getTargetWords() {
		return targetWords;
	}

	public String getDatasetDirectory() {
		return datasetDirectory;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public HashSet<String> getPosTags() {
		return posTags;
	}

	public HashSet<String> getRelationTags() {
		return relationTags;
	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	/**
	 * Checks if the given part-of-speech tag exists in the posTags hashset.
	 * 
	 * @param tag
	 *            the part-of-speech tag to check
	 * @return true if the tag exists in the hashset.
	 * @return false if the tag does not exist in the hashset.
	 */
	public boolean containsPosTag(String tag) {
		if (posTags.contains(tag.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private void parseRunConfiguration() {

		// holds the current json value
		JsonValue temp;

		// used for checking targetWordsFile and datasetDirectory
		File f;

		// resets flag to false
		flagRunConfiguration = false;

		// "startAt"
		temp = jsonObject.get("startAt");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorStartAt);
			flagRunConfiguration = true;
			return;
		} else if (temp.isNumber()) {
			startAt = temp.asInt();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorStartAt);
			flagRunConfiguration = true;
			return;
		}
		if (startAt < 1) {
			System.out.println(Messages.RunConfig_errorStartAt);
			flagRunConfiguration = true;
			return;
		}

		// "numToCollect"
		temp = jsonObject.get("numToCollect");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorNumToCollect);
			flagRunConfiguration = true;
			return;
		} else if (temp.isNumber()) {
			numToCollect = temp.asInt();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorNumToCollect);
			flagRunConfiguration = true;
			return;
		}
		if (numToCollect == 0 || numToCollect < -1) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorNumToCollect);
			flagRunConfiguration = true;
			return;
		}

		// "validationQuota"
		temp = jsonObject.get("validationQuota");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorValidationQuota);
			flagRunConfiguration = true;
			return;
		} else if (temp.isNumber()) {
			validationQuota = temp.asInt();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorValidationQuota);
			flagRunConfiguration = true;
			return;
		}
		if (validationQuota < 1) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorValidationQuota);
			flagRunConfiguration = true;
			return;
		}

		// "targetWordsFile"
		temp = jsonObject.get("targetWordsFile");
		if (temp == null) { // missing
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorTargetWordsFilePath);
			flagRunConfiguration = true;
			return;
		} else if (temp.isString()) {
			targetWordsFilePath = temp.asString();
		} else { // invalid
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorTargetWordsFilePath);
			flagRunConfiguration = true;
			return;
		}
		f = new File(targetWordsFilePath);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorTargetWordsFilePath);
			flagRunConfiguration = true;
			return;
		}

		// "datasetDirectory"
		temp = jsonObject.get("datasetDirectory");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorDatasetDirectory);
			flagRunConfiguration = true;
			return;
		} else if (temp.isString()) {
			datasetDirectory = temp.asString();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorDatasetDirectory);
			flagRunConfiguration = true;
			return;
		}
		f = new File(datasetDirectory);
		if (!f.exists() || !f.isDirectory()) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorDatasetDirectory);
			flagRunConfiguration = true;
			return;
		}

		// "posTagsFile"
		temp = jsonObject.get("posTagsFile");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorPosTagsFilePath);
			flagRunConfiguration = true;
			return;
		} else if (temp.isString()) {
			posTagsFilePath = temp.asString();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorPosTagsFilePath);
			flagRunConfiguration = true;
			return;
		}
		f = new File(posTagsFilePath);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorPosTagsFilePath);
			flagRunConfiguration = true;
			return;
		}

		// "relationTagsFile"
		temp = jsonObject.get("relationTagsFile");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorRelationTagsFilePath);
			flagRunConfiguration = true;
			return;
		} else if (temp.isString()) {
			relationTagsFilePath = temp.asString();
		} else {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorRelationTagsFilePath);
			flagRunConfiguration = true;
			return;
		}
		f = new File(relationTagsFilePath);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("failed");
			System.out.println(Messages.RunConfig_errorRelationTagsFilePath);
			flagRunConfiguration = true;
			return;
		}

	}

	private void buildTargetWords() {

		// creates object for accessing the target words given in the
		// RunConfiguration object
		targetWords = TargetWords.newInstance(targetWordsFilePath);

		if (targetWords == null) {
			flagTargetWords = true;
		}
		flagTargetWords = false;
	}

	private void buildDataset() {

		// resets the error flag
		flagDataSet = false;

		// sets Dataset object
		dataset = Dataset.newInstance(datasetDirectory);
		if (dataset == null) {
			flagDataSet = true;
		}
	}

	/**
	 * Parses the tags from the "posTags" file and the "relationsTags" file.
	 * Future functionality will include the ability to parse these tag hashsets
	 * from the dataset itself. For now, you must provide these files in the
	 * "config" entry.
	 * 
	 * @param doPosTags
	 *            must be set to true
	 * @param doRelationTags
	 *            must be set to true
	 */
	private void parseTagsFromFile(boolean doPosTags, boolean doRelationTags) {

		if (doPosTags) {

			// resets flag
			flagPosAndRelationTags = false;

			// initializes set
			posTags = new HashSet<String>();

			// opens file stream
			File file = new File(posTagsFilePath);
			String line = "";

			// adds tags to set
			try {
				LineIterator it = FileUtils.lineIterator(file, "UTF-8");
				while (it.hasNext()) {
					line = it.nextLine();
					posTags.add(line.toLowerCase());
				}

			} catch (IOException e) {
				flagPosAndRelationTags = true;
				e.printStackTrace();
			}

		}

		if (doRelationTags) {

			// splits up broad/specific relation tags
			String[] s;

			// resets flag
			flagPosAndRelationTags = false;

			// initializes set
			relationTags = new HashSet<String>();

			// opens file stream
			File file = new File(relationTagsFilePath);
			String line;

			// adds tags to set
			try {
				LineIterator it = FileUtils.lineIterator(file, "UTF-8");
				while (it.hasNext()) {

					line = it.nextLine();

					// specific
					relationTags.add(line.toLowerCase());
				}

			} catch (IOException e) {
				flagPosAndRelationTags = true;
				e.printStackTrace();
			}

		}

	}

}
