package dannydelott.vinefilter.settings.stringsobject;

import dannydelott.vinefilter.Messages;
import dannydelott.vinefilter.settings.SettingsFile;
import dannydelott.vinefilter.settings.Setup;

import java.util.List;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class StringsObject {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// holds the settings file
	private SettingsFile settings;

	// holds the json object
	private JsonObject jsonObject;

	// / holds the StringsObject values
	private String name;
	private String description;
	private List<String> values;

	// holds error flags
	private boolean flagStringsObject;

	// //////////////////////
	// FACTORY CONSTRUCTOR //
	// //////////////////////

	public static StringsObject newInstance(JsonObject j, SettingsFile s) {

		// creates new StringsObject using private constructor
		StringsObject so = new StringsObject(j, s);

		// checks if all the elements were parsed correctly
		if (so.getFlagStringsObject()) {
			return null;
		}

		return so;

	}

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	private StringsObject(JsonObject j, SettingsFile s) {

		// -----------------------------------------------
		// 0.
		// ASSIGNS SETTINGS FILE OBJECT TO GLOBAL VARIABLE
		// ASSIGNS JSON OBJECT TO GLOBAL VARIABLE
		// -----------------------------------------------

		// Assigns SettingsFile object to global variable.
		//
		// The SettingsFile s passed in by the constructor contains the
		// RunConfiguration object that holds the hashset of part-of-speech
		// tags. This is useful when we go to check that the "assignToPosTag"
		// field contains an actual pos tag from the dataset.

		settings = s;

		// Assigns JsonObject to global variable.
		//
		// The JsonObject j passed in by the constructor contains the values
		// necessary for a customized StringsObject.

		jsonObject = j;

		// ----------------------------------
		// 1.
		// PARSES OUT STRINGS OBJECT ELEMENTS
		// ----------------------------------

		// Parses out the values from jsonObject.
		//
		// Once the global jsonObject has been assigned, this method will parse
		// out the individual JSON elements and store them in their respective
		// global variables. These can be accessed outside of the
		// StringsObject object by calling the getVariable() methods.

		parseStringsObject();
		if (flagStringsObject) {
			return;
		}

	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private void parseStringsObject() {

		// holds the current json value
		JsonValue temp;

		// resets flag to false
		flagStringsObject = false;

		// ----
		// NAME
		// ----

		temp = jsonObject.get("name");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorName);
			flagStringsObject = true;
			return;
		} else if (temp.isString()) {
			name = temp.asString();
		} else {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorName);
			flagStringsObject = true;
			return;
		}

		// -----------
		// DESCRIPTION
		// -----------

		temp = jsonObject.get("description");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorDescription);
			flagStringsObject = true;
			return;
		} else if (temp.isString()) {
			description = temp.asString();
		} else {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorDescription);
			flagStringsObject = true;
			return;
		}

		// ------
		// VALUES
		// ------

		temp = jsonObject.get("values");
		if (temp == null) {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorValues);
			flagStringsObject = true;
			return;
		} else if (temp.isArray()) {

			// turns JsonArray into List<String>
			values = Setup.convertJsonArrayToStringList(temp.asArray());
			if (values == null) { // empty array
				System.out.println("failed");
				System.out.println(Messages.StringsObject_errorValues);
				flagStringsObject = true;
				return;
			}

		} else {
			System.out.println("failed");
			System.out.println(Messages.StringsObject_errorValues);
			flagStringsObject = true;
			return;
		}
	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagStringsObject() {
		return flagStringsObject;
	}

	public JsonObject getJsonObject() {
		return jsonObject;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getValues() {
		return values;
	}

}
