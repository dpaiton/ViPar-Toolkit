package dannydelott.vinefilter.settings;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.config.RunConfiguration;
import dannydelott.vinefilter.settings.filter.Filter;
import dannydelott.vinefilter.settings.stringsobject.StringsObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class SettingsFile {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// holds the filepath to the settings file
	private static String filePath;

	/**
	 * holds the unparsed JSON objects from the settings file
	 */
	private List<JsonObject> objects;

	// holds the JSON object with "config" entry
	private JsonObject runConfigurationObject;
	private RunConfiguration runConfiguration;

	// holds the JSON objects with "filter" entries
	private List<JsonObject> filtersAsJsonObjects;
	private List<JsonObject> childrenAsJsonObjects;

	// holds the "name" entry as key, and the Filter object as value
	private Hashtable<String, Filter> filters;

	// holds the "name" element from all JSON filter child objects
	private HashSet<String> childNames;

	// holds the JSON objects with "s" entries
	private Hashtable<String, StringsObject> stringsObjects;
	private Set<String> stringsObjectsNames;

	// error flags
	private boolean flagObjects;
	private boolean flagEntryType;
	private boolean flagRunConfiguration;
	private boolean flagStringsObjects;
	private boolean flagFilters;
	private boolean flagChildNames;

	// //////////////////////
	// FACTORY CONSTRUCTOR //
	// //////////////////////

	public static SettingsFile newInstance(String f) {

		// creates new object using private constructor
		SettingsFile s = new SettingsFile(f);

		// errors if cannot parse settings file json objects
		if (s.getFlagObjects()) {
			return null;
		}

		// errors if missing, duplicate, or incorrect "config" entry
		if (s.getFlagRunConfiguration()) {
			return null;
		}

		// errors if cannot build list of filter names
		if (s.getFlagChildNames()) {
			return null;
		}

		// errors if missing, duplicate, or incorrect "filter" entries
		if (s.getFlagFilters()) {
			return null;
		}

		return s;
	}

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	private SettingsFile(String f) {

		// -----------------------------------------------
		// 0.
		// ASSIGNS SETTINGS FILEPATH TO GLOBAL VARIABLE
		// BUILDS JSON OBJECTS LIST FROM SETTINGS FILEPATH
		// -----------------------------------------------

		// Assigns the filepath to global variable.
		//
		// The settings filepath passed in through the constructor is stored in
		// a global variable and used to construct the list of JsonObjects which
		// holds the Run Configuration, StringObjects, and Filters.

		filePath = f;

		// Parses out the JsonObjects from the settings file path.
		//
		// In order to extract the values stored in the JsonObjects of the
		// settings file, we must first parse out the JsonObjects and store them
		// in a list.

		flagObjects = false;
		objects = Setup.getJsonObjectsFromFile(filePath);

		// throws flag if JSON parsing error
		if (objects == null) {
			System.out.println(Messages.SettingsFile_errorJsonObjects);
			flagObjects = true;
			return;
		}

		// ---------------------------------
		// 1.
		// CHECKS ENTRY TYPE OF JSON OBJECTS
		// ---------------------------------

		// Checks for valid entry type.
		//
		// If the JsonObjects were parsed successfully in the previous step, we
		// now check each object for a field called "entry". This is a mandatory
		// field that declares what kind of Java object to create from the
		// JsonObject. If there is no field called "entry" in any of the
		// JsonObjects, or if the field does not equal either "config",
		// "filter",
		// or "s", a flag is thrown.

		checkEntryTypes();
		if (flagEntryType) {
			System.out.println(Messages.SettingsFile_errorEntryType);
			return;
		}

		// ------------------------
		// 2.
		// BUILDS RUN CONFIGURATION
		// ------------------------

		// Builds RunConfiguration object.
		//
		// Using the list of JsonObjects extracted in Step 0, find the entry
		// marked "config" and create a RunConfiguration object from it. This
		// object is responsible for storing all of the elements found inside
		// the "config" entry. This method will set flagRunConfiguration to TRUE
		// if there are zero or more than one "config" entry.

		buildRunConfiguration();
		if (flagRunConfiguration) {
			return;
		}

		// ---------------------
		// 3.
		// BUILDS STRINGS OBJECTS
		// ---------------------

		// Builds StringsObject objects.
		//
		// Using the list of JsonObjects extracted in Step 0, find the entries
		// marked "s" and create StringsObject objects from them. These
		// StringsObject objects are responsible for storing all of the elements
		// found inside the "s" entry.

		buildStringsObjects();
		if (flagStringsObjects) {
			return;
		}

		// ----------------------------------------------------
		// 4.
		// BUILDS LISTS OF FILTERS AND CHILDREN AS JSON OBJECTS
		// ----------------------------------------------------

		// Builds list of JsonObjects marked "filter".
		//
		// Using the list of JsonObjects extracted in Step 0, find the entries
		// marked "filter" and add them to the filtersAsJsonObjects list. If the
		// JsonObject's "isChild" field is set to TRUE, then add it to the
		// childrenAsJsonObjects list instead.

		buildFilterLists();
		if (flagFilters) {
			return;
		}

		// -----------------------------
		// 5.
		// BUILDS HASHSET OF CHILD NAMES
		// -----------------------------

		// Builds hashset of child filter names.
		//
		// Using the childrenAsJsonObjects list created in the previous step,
		// create a hashset containing just the names of all the Child filters.
		// This is useful when building the Filter objects in the next step.

		buildChildNames();
		if (flagChildNames) {
			return;
		}

		// --------------------------------------
		// 6.
		// BUILDS THE HASHTABLE OF FILTER OBJECTS
		// --------------------------------------

		// Builds the hashtable of Filter objects.
		//
		// Using the filtersAsJsonObjects list, create a Hashtable containing
		// the Filter object's name value as the key and the Filter object
		// itself as the value. This makes finding Filter objects easier.

		buildFilters();
		if (flagFilters) {
			return;
		}

	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	public JsonObject findJsonObjectFilterByName(String n, boolean isChild) {
		JsonValue temp;

		if (isChild) {
			// iterates over all filters as JsonObjects (**not Filter objects**)
			for (JsonObject object : childrenAsJsonObjects) {

				temp = object.get("name");

				if (temp.isString()) {

					if (temp.asString().contentEquals(n)) {
						return object;
					}
				}
			}
		}
		if (!isChild) {
			// iterates over all filters as JsonObjects (**not Filter objects**)
			for (JsonObject object : filtersAsJsonObjects) {

				temp = object.get("name");

				if (temp.isString()) {
					if (temp.asString().contentEquals(n)) {
						return object;
					}
					return object;
				}
			}
		}
		return null;
	}

	public void printSettings() {
		System.out.print(Messages.SettingsFile_settings);
		System.out.println(Messages.SettingsFile_filePath + filePath);
		System.out.println(Messages.SettingsFile_startAt
				+ runConfiguration.getStartAt());
		System.out.println(Messages.SettingsFile_numToCollect
				+ runConfiguration.getNumToCollect());
		System.out.println(Messages.SettingsFile_validationQuota
				+ runConfiguration.getValidationQuota());
	}

	public boolean containsStringObject(String name) {
		if (stringsObjectsNames.contains(name.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagObjects() {
		return flagObjects;
	}

	public boolean getFlagRunConfiguration() {
		return flagRunConfiguration;
	}

	public boolean getFlagStringsObjects() {
		return flagStringsObjects;
	}

	public boolean getFlagFilters() {
		return flagFilters;
	}

	public boolean getFlagChildNames() {
		return flagChildNames;
	}

	public List<JsonObject> getObjects() {
		return objects;
	}

	public JsonObject getRunConfigurationAsJsonObject() {
		return runConfigurationObject;
	}

	public List<JsonObject> getFilterJsonObjectsAsList(boolean children) {
		if (children) {
			return childrenAsJsonObjects;
		} else {
			return filtersAsJsonObjects;
		}
	}

	public RunConfiguration getRunConfiguration() {

		return runConfiguration;
	}

	public Hashtable<String, Filter> getFilters() {
		return filters;
	}

	public HashSet<String> getChildNames() {
		return childNames;
	}

	public Hashtable<String, StringsObject> getStringsObjects() {
		return stringsObjects;
	}

	// /////////////////
	// STATIC METHODS //
	// /////////////////

	public static EntryType getEntryTypeFromJsonObject(JsonObject o) {

		String entry = o.get("entry").asString().toLowerCase();

		if (entry.contentEquals("config")) {
			return EntryType.CONFIG;
		}
		if (entry.contentEquals("filter")) {
			return EntryType.FILTER;
		}
		if (entry.contentEquals("s")) {
			return EntryType.S;
		}

		return null;

	}

	public static boolean getIsChildFromJsonObject(JsonObject j) {
		JsonValue temp;
		temp = j.get("isChild");
		if (temp == null) {
			return false;
		}
		if (temp.isBoolean()) {
			if (temp.asBoolean() == true) {
				return true;
			}
		}

		return false;
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private void checkEntryTypes() {

		// resets error flag
		flagEntryType = false;

		for (JsonObject object : objects) {

			try {

				// gets entry type and null checks
				JsonValue value = object.get("entry");
				if (value == null) {
					flagEntryType = true;
					return;
				}

				// assigns Entry Type to String
				String entry = value.asString().toLowerCase();

				// "config" or "filter" or "s"
				if (!entry.contentEquals("config")
						&& !entry.contentEquals("filter")
						&& !entry.contentEquals("s")) {

					System.out.println(Messages.SettingsFile_errorEntryType);
					flagEntryType = true;
					return;

				}
			} catch (UnsupportedOperationException e) {
				System.out.println(Messages.SettingsFile_errorEntryType);
				flagEntryType = true;
				return;
			} catch (NullPointerException e) {
				e.printStackTrace();
				flagEntryType = true;
				return;
			}

		}

	}

	/**
	 * Using the list of JsonObjects extracted in Step 0, find the entry marked
	 * "config" and create a RunConfiguration object from it. This object is
	 * responsible for storing all of the elements found inside the "config"
	 * entry. This method will set flagRunConfiguration to TRUE if there are
	 * zero or more than one "config" entry.
	 */
	private void buildRunConfiguration() {

		// ---------------------------------------------
		// 1. Prevents multiple "config" entries.
		// Gets index of "config" entry in objects list.
		// ---------------------------------------------

		// number of "config" entries
		int c = 0;

		// index of current "config" entry
		int index = 0;

		// iterate over all settings objects to parse "config" entries
		for (int i = 0; i < objects.size(); i++) {
			try {
				if (objects.get(i).get("entry").asString()
						.contentEquals("config")) {
					c++;
					index = i;
				}
			} catch (UnsupportedOperationException e) {
				System.out.println(Messages.RunConfig_errorParseException);
				flagRunConfiguration = true;
				return;
			}
		}

		// ---------------------------------------------------------
		// 2. Pulls out "config" entry JsonObject from objects list.
		// ---------------------------------------------------------

		// sets runConfiguration or throws flag
		if (c == 1) {
			runConfigurationObject = objects.get(index);
			flagRunConfiguration = false;
		} else {
			System.out.println(Messages.SettingsFile_errorConfigEntry);
			flagRunConfiguration = true;
			return;
		}

		// -------------------------------------------------
		// 3. Builds RunConfiguration object from JsonObject
		// -------------------------------------------------

		runConfiguration = RunConfiguration.newInstance(runConfigurationObject);
		if (runConfiguration == null) {
			flagRunConfiguration = true;
			return;
		}
	}

	private void buildStringsObjects() {

		// prints loading message
		System.out.print(Messages.SettingsFile_loadingStringsObjects);

		// resets error flag
		flagStringsObjects = false;

		// initializes StringsObject list
		stringsObjects = new Hashtable<String, StringsObject>();

		// iterate over entire list of settings file json objects
		for (int i = 0; i < objects.size(); i++) {

			if (objects.get(i).get("entry").asString().contentEquals("s")) {

				// add "s" entries to list of json objects.
				StringsObject s = StringsObject.newInstance(objects.get(i),
						this);
				if (s == null) {
					flagStringsObjects = true;
					return;
				}

				stringsObjects.put(s.getName().toLowerCase(), s);

			}
		}

		// initializes Set<String> stringsObjectNames
		stringsObjectsNames = stringsObjects.keySet();

		// Prints success message.
		// StringsObject will print "failed" if unable to create a new instance.
		System.out.println("success");

	}

	private void buildFilterLists() {

		boolean isChild;

		// set to throw flag automatically until a parent Filter is found.
		flagFilters = true;

		// instantiates filters list
		filtersAsJsonObjects = new ArrayList<JsonObject>();
		childrenAsJsonObjects = new ArrayList<JsonObject>();

		// Parses "filter" JSON objects from settings file.
		for (int i = 0; i < objects.size(); i++) {

			// "filter"
			if (getEntryTypeFromJsonObject(objects.get(i)) == EntryType.FILTER) {

				isChild = getIsChildFromJsonObject(objects.get(i));

				if (isChild) {
					childrenAsJsonObjects.add(objects.get(i));
				}
				if (!isChild) {

					filtersAsJsonObjects.add(objects.get(i));
					flagFilters = false; // parent filter found
				}
			}

		}

		// throws flag if no filters exist
		if (filtersAsJsonObjects.size() == 0) {
			System.out.println(Messages.SettingsFile_errorGrammarFilters);
			return;
		}

	}

	private void buildChildNames() {

		// holds the current value
		JsonValue temp;

		// instantiates set
		childNames = new HashSet<String>();

		// resets error flag
		flagChildNames = false;

		// loops through list of filters
		for (JsonObject filter : childrenAsJsonObjects) {
			temp = filter.get("name");
			if (temp != null && temp.isString()) {
				childNames.add(temp.asString());
			} else {
				System.out.println(Messages.Filter_errorName);
				flagChildNames = true;
				return;
			}
		}
	}

	private void buildFilters() {

		// holds current Filter object being built
		Filter temp;

		// instantiates local filters Hashtable <key, value> (?)
		filters = new Hashtable<String, Filter>();

		// resets error flag
		flagFilters = false;

		// formatting for output
		int tabs = 1;
		System.out.print(Messages.SettingsFile_loadingFilters);

		// Builds Hashtable of Filter objects
		for (JsonObject object : filtersAsJsonObjects) {

			temp = Filter.newInstance(object, this, tabs);
			if (temp == null) {
				System.out.println("flag!");
				flagFilters = true;
				return;
			}
			
			filters.put(temp.getName(), temp);
			tabs = 2;

		}
	}

}
