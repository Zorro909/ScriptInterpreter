package ScriptInterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptInterpreter {

	private static ScriptEngine se;
	private static Compilable c;

	public static void main(String[] args)
			throws ScriptException, IOException, IllegalArgumentException, IllegalAccessException {
		getEngine();
		addClassToScript("console", new Console());
		addClassToScript("System", System.class);
		System.out.println("Print JavaScript to execute it!");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		while (!(line = br.readLine()).equalsIgnoreCase("stop")) {
			System.out.println(se.eval(line));
		}
	}

	private static void getEngine() throws MalformedURLException {
		// "where" on Windows and "whereis" on Linux/Mac
				if (System.getProperty("os.name").contains("win") || System.getProperty("os.name").contains("Win")) {
					String path = getCommandOutput("where java");
					if (path == null || path.isEmpty()) {
						System.err.println("There may have been an error processing the command or ");
						System.out.println("JAVAC may not set up to be used from the command line");
						System.out.println("Unable to determine the location of the JDK using the command line");
					} else {
						// Response will be the path including "javac.exe" so need to
						// Get the two directories above that
						File javacFile = new File(path);
						File jdkInstallationDir = javacFile.getParentFile().getParentFile();
						URL[] nashorn = new URL[]{new URL("file://" + jdkInstallationDir.toString() + "/lib/ext/nashorn.jar")};
						URLClassLoader child = new URLClassLoader (nashorn, ClassLoader.getSystemClassLoader());
						se = new ScriptEngineManager(child).getEngineByName("nashorn");
					} // else: path can be found
				} else {
					String response = getCommandOutput("whereis java");
					if (response == null) {
						System.err.println("There may have been an error processing the command or ");
						System.out.println("JAVAC may not set up to be used from the command line");
						System.out.println("Unable to determine the location of the JDK using the command line");
					} else {
						// The response will be "javac: /usr ... "
						// so parse from the "/" - if no "/" then there was an error
						// with the command
						int pathStartIndex = response.indexOf('/');
						if (pathStartIndex == -1) {
							System.err.println("There may have been an error processing the command or ");
							System.out.println("JAVAC may not set up to be used from the command line");
							System.out.println("Unable to determine the location of the JDK using the command line");
						} else {
							// Else get the directory that is two above the javac.exe
							// file
							String path = response.substring(pathStartIndex, response.length());
							File javacFile = new File(path);
							File jdkInstallationDir = javacFile.getParentFile().getParentFile();
							URL[] nashorn = new URL[]{new URL("file://" + jdkInstallationDir.toString() + "/lib/ext/nashorn.jar")};
							URLClassLoader child = new URLClassLoader (nashorn, ClassLoader.getSystemClassLoader());
							se = new ScriptEngineManager(child).getEngineByName("nashorn");
						} // else: path found
					} // else: response wasn't null
				} // else: OS is not windows
	}

	public static Object executeJavaScript(String script) throws ScriptException {
		return se.eval(script);
	}

	public static void addClassToScript(String reference, Object o)
			throws IllegalArgumentException, IllegalAccessException, ScriptException {
		if (se == null) {
			try {
				getEngine();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!(o instanceof Class)) {
			executeJavaScript("var " + reference + " = Java.type(\"" + o.getClass().getCanonicalName() + "\")");
		} else {
			executeJavaScript("var " + reference + " = Java.type(\"" + ((Class) o).getCanonicalName() + "\")");
		}
	}

	public static void addVariableToScript(String string, Object o) throws Exception {
		if (se == null) {
			getEngine();
		}
		se.put(string, o);
	}

	public static Object executeCompiledJavaScript(CompiledScript script) throws ScriptException {
		return script.eval();
	}

	public static CompiledScript compileJavaScript(String script) throws ScriptException {
		if (c == null) {
			c = (Compilable) se;
		}
		return c.compile(script);
	}

	public static CompiledScript compileJavaScript(Reader reader) throws ScriptException {
		if (c == null) {
			c = (Compilable) se;
		}
		return c.compile(reader);
	}

	public static Object executeJavaScript(Reader reader) throws ScriptException {
		return se.eval(reader);
	}
	
	
	
	
	
	
	
	
	private static String getCommandOutput(String command)  {
	    String output = null;       //the string to return

	    Process process = null;
	    BufferedReader reader = null;
	    InputStreamReader streamReader = null;
	    InputStream stream = null;

	    try {
	        process = Runtime.getRuntime().exec(command);

	        //Get stream of the console running the command
	        stream = process.getInputStream();
	        streamReader = new InputStreamReader(stream);
	        reader = new BufferedReader(streamReader);

	        String currentLine = null;  //store current line of output from the cmd
	        StringBuilder commandOutput = new StringBuilder();  //build up the output from cmd
	        while ((currentLine = reader.readLine()) != null) {
	            commandOutput.append(currentLine);
	        }

	        int returnCode = process.waitFor();
	        if (returnCode == 0) {
	            output = commandOutput.toString();
	        }

	    } catch (IOException e) {
	        System.err.println("Cannot retrieve output of command");
	        System.err.println(e);
	        output = null;
	    } catch (InterruptedException e) {
	        System.err.println("Cannot retrieve output of command");
	        System.err.println(e);
	    } finally {
	        //Close all inputs / readers

	        if (stream != null) {
	            try {
	                stream.close();
	            } catch (IOException e) {
	                System.err.println("Cannot close stream input! " + e);
	            }
	        } 
	        if (streamReader != null) {
	            try {
	                streamReader.close();
	            } catch (IOException e) {
	                System.err.println("Cannot close stream input reader! " + e);
	            }
	        }
	        if (reader != null) {
	            try {
	                streamReader.close();
	            } catch (IOException e) {
	                System.err.println("Cannot close stream input reader! " + e);
	            }
	        }
	    }
	    //Return the output from the command - may be null if an error occured
	    return output;
	}
}
