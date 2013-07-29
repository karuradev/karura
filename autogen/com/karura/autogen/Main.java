package com.karura.autogen;

/**

============== GPL License ==============
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


============== Commercial License==============
https://github.com/karuradev/licenses/blob/master/toc.txt
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.karura.autogen.codegen.FrameworkGenerator;
import com.karura.autogen.utils.SourceBuffer;
import com.karura.framework.FrameworkConfig;
import com.karura.framework.PluginManager;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.config.LoaderFactory;
import com.karura.framework.plugins.WebViewPlugin;
import com.karura.framework.config.ConfigLoader;
import com.karura.framework.config.ConfigStore;

public class Main {
	public static void printUsage(String... message) {
		System.err.println("Karura script autogenerator");
		System.err.println("---------------------------------------------");
		System.err.println("java -jar autogen.jar [karura_jar] [config_file_path] [output_folder] [generate_comments]");
		System.err.println("karura_jar : Path to karura jar file");
		System.err.println("config_file_path : path to the config file which contains framework config");
		System.err.println("output_folder : Path to directory where " + FrameworkConfig.FRAMEWORK_JS_FILENAME + " needs to be generated");
		System.err.println("generate_comments : Whether karura.js should include comments");
		System.err.println("---------------------------------------------");
		if (message.length > 0) {
			System.err.println("Error : " + message[0]);
			System.err.println("---------------------------------------------");
		}
	}

	public static void main(String[] args) {
		
		if (args.length != 4) {
			printUsage();
			return;
		}

		if (!new File(args[0]).isFile()) {
			printUsage("Please specify a valid path to karura.jar file.");
			return;
		}
		
		if (!new File(args[1]).isFile()){
			printUsage("Please specify the framework config path. This is the config.xml file in the res folder.");
		}

		if (!new File(args[2]).isDirectory()) {
			printUsage("Please specify a valid output directory.");
			return;
		}

		BuildConfig.DEBUG = Boolean.valueOf(args[3]);
		
		
		String jarName = args[0];
		
		ConfigStore cs = LoaderFactory.getLoader(false, args[1]).loadConfig();
		
		File javascriptFile = new File(args[2], FrameworkConfig.FRAMEWORK_JS_FILENAME);

		FileOutputStream fos = null;
		System.out.println(String.format("Processing %s for plugins ", jarName));
		try {
			HashMap<String, Class<? extends WebViewPlugin>> result = new HashMap<String, Class<? extends WebViewPlugin>>();
			URL[] urls = {
				new URL("jar:file:" + jarName + "!/")
			};

			JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
			JarEntry jarEntry;

			while (true) {
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry == null) {
					break;
				}

				if (jarEntry.getName().endsWith(".class")) {
					String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
					className = className.replace('/', '.');

					if (className.startsWith("com.karura.framework.plugins")) {
						Class<?> clazz = null;
						try {

							clazz = (Class<?>) Class.forName(className);
							if (!WebViewPlugin.class.isAssignableFrom(clazz)) {
								continue;
							}
							if (clazz.getAnnotation(JavascriptModule.class) == null) {
								continue;
							}
							@SuppressWarnings("unchecked")
							Class<? extends WebViewPlugin> webViewPlugin = (Class<? extends WebViewPlugin>) clazz;
							result.put(clazz.getSimpleName(), webViewPlugin);
						} catch (Exception e) {
							if (BuildConfig.DEBUG) {
								e.printStackTrace();
							}
							if (clazz != null) {
								System.err.println(clazz.getSimpleName() + " was ignored due to an error");
							} else {
								System.err.println(clazz.getSimpleName() + "Internal but recoverable error");
							}
						}
					}

				}
			}

			SourceBuffer gen = new SourceBuffer();
			new FrameworkGenerator(result).applyToBuffer(gen);
			
			System.out.println(String.format("Writing javascript to %s",javascriptFile.getAbsolutePath()));
			fos = new FileOutputStream(javascriptFile);
			fos.write(gen.toString().getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
