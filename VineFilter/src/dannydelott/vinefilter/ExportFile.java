package dannydelott.vinefilter;

import dannydelott.vinefilter.settings.config.dataset.Vine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonObject;

public class ExportFile {

	public static final void externalizeFetch(String directory)
			throws IOException {

		// idioms list
		File file = new File(directory + "fetch.jar");

		if (!file.exists()) {
			InputStream stream = Main.class.getClass().getResourceAsStream(
					"/fetch.jar");

			if (stream == null) {
				// send your exception or warning
			}
			OutputStream resStreamOut = null;
			int readBytes;
			byte[] buffer = new byte[4096];
			try {
				resStreamOut = new FileOutputStream(new File(directory
						+ "fetch.jar"));
				while ((readBytes = stream.read(buffer)) > 0) {
					resStreamOut.write(buffer, 0, readBytes);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				stream.close();
				resStreamOut.close();
			}
		}
	}

	public static final void externalizeAnalyze(String directory)
			throws IOException {
		// idioms1 list
		File file = new File(directory + "analyze.jar");

		if (!file.exists()) {
			InputStream stream = Main.class.getClass().getResourceAsStream(
					"/analyze.jar");

			if (stream == null) {
				// send your exception or warning
			}
			OutputStream resStreamOut = null;
			int readBytes;
			byte[] buffer = new byte[4096];
			try {
				resStreamOut = new FileOutputStream(new File(directory
						+ "analyze.jar"));
				while ((readBytes = stream.read(buffer)) > 0) {
					resStreamOut.write(buffer, 0, readBytes);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				stream.close();
				resStreamOut.close();
			}
		}
	}

	public static void printJsonObjectToFile(JsonObject j, String filepath) {
		// builds export string
		String exportString = j.toString();

		try {
			FileUtils.writeStringToFile(new File(filepath),
					exportString + "\n", true);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void appendVineToFile(Vine v, String filepath) {

		// builds export string
		String exportString = v.getJsonObject()
				.add("good_filters", v.getGoodFilters()).toString();

		try {
			FileUtils.writeStringToFile(new File(filepath),
					exportString + "\n", true);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createDirectory(String path) {
		File f = new File(path);
		if (!f.isDirectory()) {
			f.mkdir();
		}
	}
}
