package sunshine.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static FileWriter fileWriter;
	private static boolean active = false;
	
	public static void activate() {
		active = true;
	}
	
	public static void setLogFile(String filename) {
		try {
			fileWriter = new FileWriter(filename, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void record(String str) {
		if (!active) return;
		DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS] ");
		Date date = new Date();
		str = dateFormat.format(date) + str + "\n";
		System.err.print(str);
		if (fileWriter == null) return;
		try {
			fileWriter.append(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
