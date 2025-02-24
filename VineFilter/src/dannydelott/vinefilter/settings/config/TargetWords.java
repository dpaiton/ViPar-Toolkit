package dannydelott.vinefilter.settings.config;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.Setup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class TargetWords {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// filepath given to target words file on instantiation
	private String targetWordsFilePath;

	// json objects given in the target words file
	private List<JsonObject> objects;

	// contains the category as they key, and a hashset of hyponyms as the value
	private Hashtable<String, HashSet<String>> categorizedHyponyms;
	private Hashtable<String, HashSet<String>> categorizedHyponymsNoSpaces;
	private boolean flagCategorizedHyponyms;

	// contains all the categories and hyponyms in an unordered hashset
	private List<String> targetWords;
	private List<String> targetWordsNoSpaces;
	private boolean flagTargetWords;

	// //////////////////////
	// FACTORY CONSTRUCTOR //
	// //////////////////////

	public static TargetWords newInstance(String f) {

		// creates new object using private constructor
		TargetWords t = new TargetWords(f);

		// returns null if TargetWords object can't build Hashtable or HashSet
		if (t.getFlagCategorizedHyponyms() || t.getFlagTargetWords()) {
			return null;
		}

		return t;

	}

	// ///////////////
	// CONSTRUCTOR //
	// ///////////////

	private TargetWords(String f) {

		targetWordsFilePath = f;

		// loads the json objects from the file
		objects = Setup.getJsonObjectsFromFile(targetWordsFilePath);

		if (objects == null) {
			flagCategorizedHyponyms = true;
			flagTargetWords = true;

		} else {

			// loads the objects into Hashtables
			// sets flagCategorizedHyponyms to true on error
			buildCategorizedHyponyms();

			// loads the objects into HashSets
			// sets flagTargetWords to true on error
			buildTargetWords();
		}

	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private void buildCategorizedHyponyms() {
		JsonArray array;
		String category;
		String categoryNoSpaces;

		// Recycled when building hashset
		String hyponym;
		String hyponymNoSpaces;

		HashSet<String> hyponymsList = new HashSet<String>();
		HashSet<String> hyponymsListNoSpaces = new HashSet<String>();

		// empties the Hashtables
		categorizedHyponyms = new Hashtable<String, HashSet<String>>();
		categorizedHyponymsNoSpaces = new Hashtable<String, HashSet<String>>();

		for (JsonObject object : objects) {

			// gets category value from json object
			category = object.get("category").asString();
			if (category == null) {
				System.out.println(Messages.TargetWords_errorBadCategoryValue);
				flagCategorizedHyponyms = true;
				return;
			}
			categoryNoSpaces = category.replace(" ", "");

			// gets hyponyms values from json object
			array = object.get("hyponyms").asArray();
			if (array == null) {
				System.out.println(Messages.TargetWords_errorBadHyponymsValue);
				flagCategorizedHyponyms = true;
				return;
			}

			// loops over hyponyms array and stores elements in local Hashsets
			for (int i = 0; i < array.size(); i++) {

				// stores hyponyms
				hyponym = array.get(i).asString();
				hyponymNoSpaces = hyponym.replace(" ", "");

				// adds hyponyms to lists
				hyponymsList.add(hyponym);
				hyponymsListNoSpaces.add(hyponymNoSpaces);

			}

			// adds key and values to global Hashtables
			categorizedHyponyms.put(category, hyponymsList);
			categorizedHyponymsNoSpaces.put(categoryNoSpaces,
					hyponymsListNoSpaces);

		}

		// sets flag to false if successful
		flagCategorizedHyponyms = false;

	}

	private void buildTargetWords() {

		JsonArray array;
		String category;
		String categoryNoSpaces;
		String hyponym;
		String hyponymNoSpaces;

		// empties the HashSets
		targetWords = new ArrayList<String>();
		targetWordsNoSpaces = new ArrayList<String>();

		for (JsonObject object : objects) {

			// gets category
			category = object.get("category").asString();
			if (category == null) {
				System.out.println(Messages.TargetWords_errorBadCategoryValue);
				flagTargetWords = true;
				return;
			}
			categoryNoSpaces = category.replace(" ", "");

			// adds category to HashSets
			targetWords.add(category);
			targetWordsNoSpaces.add(categoryNoSpaces);

			// gets hyponyms
			array = object.get("hyponyms").asArray();
			if (array == null) {
				System.out.println(Messages.TargetWords_errorBadHyponymsValue);
				flagTargetWords = true;
				return;
			}
			for (int i = 0; i < array.size(); i++) {

				hyponym = array.get(i).asString();
				hyponymNoSpaces = hyponym.replace(" ", "");

				// adds hyponyms to HashSets
				targetWords.add(hyponym);
				targetWordsNoSpaces.add(hyponymNoSpaces);

			}

		}

		// sets flag to false if successful
		flagTargetWords = false;
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public Hashtable<String, HashSet<String>> getCategorizedHyponyms() {
		return categorizedHyponyms;
	}

	public Hashtable<String, HashSet<String>> getCategorizedHyponymsNoSpaces() {
		return categorizedHyponymsNoSpaces;
	}

	public boolean getFlagCategorizedHyponyms() {
		return flagCategorizedHyponyms;
	}

	public List<String> getTargetWords() {
		return targetWords;

	}

	public List<String> getTargetWordsNoSpaces() {
		return targetWordsNoSpaces;
	}

	public boolean getFlagTargetWords() {
		return flagTargetWords;
	}

	public List<JsonObject> getObjects() {
		return objects;
	}

	public String getFilePath() {
		return targetWordsFilePath;
	}
}
