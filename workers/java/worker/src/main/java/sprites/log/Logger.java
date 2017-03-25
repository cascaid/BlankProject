package sprites.log;

import java.io.BufferedWriter;
import java.io.IOException;

public class Logger{

	public static Logger instance;

	BufferedWriter writer;

	public Logger(BufferedWriter writer){
		this.writer = writer;
	}

	public static void init(BufferedWriter writer){
		instance = new Logger(writer);
	}

	public static void log(String message){
		instance._log(message);
	}

	public void _log(String message){
		try {
			writer.write(message);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
		}
	}
}