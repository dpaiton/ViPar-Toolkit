package dannydelott.vinefilter.settings.config.dataset;

import dannydelott.vinefilter.settings.Setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.eclipsesource.json.JsonObject;

public class Dataset {

	// ///////////////////
	// GLOBAL VARIABLES //
	// ///////////////////

	// file path to the dataset directory
	private String datasetDirectory;

	// list of dataset files in the directory
	private List<String> datasetFilesList;

	private DatasetFile currentDatasetFile;

	// error flag
	private boolean flagDataset;

	// //////////////
	// CONSTRUCTOR //
	// //////////////

	public static Dataset newInstance(String d) {

		Dataset dataset = new Dataset(d);

		if (dataset.getFlagDataset()) {
			return null;
		}

		return dataset;
	}

	private Dataset(String d) {
		datasetDirectory = d;
		buildDataFilesList();
	}

	// /////////////////
	// PUBLIC METHODS //
	// /////////////////

	public boolean setCurrentDatasetFile(String fp) {
		currentDatasetFile = DatasetFile.newInstance(fp);
		if (currentDatasetFile == null) {
			return false;
		}
		return true;
	}

	public List<Vine> getVinesFromDatasetFile(int i) {

		List<JsonObject> objects = Setup
				.getJsonObjectsFromFile(datasetFilesList.get(i));

		List<Vine> list = new ArrayList<Vine>();

		Vine tempVine;

		for (JsonObject j : objects) {
			tempVine = Vine.newInstance(j);
			if (tempVine == null) {
				System.out.println(datasetFilesList.get(i));
				return null;
			} else {
				list.add(tempVine);
			}
		}

		return list;

	}

	// /////////////////
	// GLOBAL GETTERS //
	// /////////////////

	public boolean getFlagDataset() {
		return flagDataset;
	}

	public String getDirectory() {
		return datasetDirectory;
	}

	public List<String> getDatasetFilesList() {
		return datasetFilesList;
	}

	public DatasetFile getCurrentDatasetFile() {
		return currentDatasetFile;
	}

	// //////////////////
	// PRIVATE METHODS //
	// //////////////////

	private void buildDataFilesList() {

		// holds the file extension
		// recycled for each file in for loop
		String ext;

		// loads the contents of the directory into a file array
		File dir = new File(datasetDirectory);
		File[] f = dir.listFiles();

		// resets error flag
		flagDataset = false;

		datasetFilesList = new ArrayList<String>();

		for (int i = 0; i < f.length; i++) {

			// gets the extension
			ext = FilenameUtils.getExtension(f[i].getAbsolutePath());

			// checks that the file is not a directory
			// and that the file type is json.
			if (f[i].isFile() && ext.contentEquals("json")) {
				datasetFilesList.add(f[i].getAbsolutePath());
			}
		}

		// no json files found
		if (datasetFilesList.size() == 0) {
			flagDataset = true;
		}

	}
}
