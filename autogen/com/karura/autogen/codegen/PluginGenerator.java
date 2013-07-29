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

package com.karura.autogen.codegen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import android.annotation.SuppressLint;

import com.karura.autogen.BuildConfig;
import com.karura.autogen.FunctionType;
import com.karura.autogen.utils.SourceBuffer;
import com.karura.framework.Constants;
import com.karura.framework.FrameworkConfig;
import com.karura.framework.annotations.Asynchronous;
import com.karura.framework.annotations.Description;
import com.karura.framework.annotations.Event;
import com.karura.framework.annotations.Events;
import com.karura.framework.annotations.ExportToJs;
import com.karura.framework.annotations.JavascriptModule;
import com.karura.framework.annotations.Param;
import com.karura.framework.annotations.Params;
import com.karura.framework.annotations.SupportJavascriptInterface;
import com.karura.framework.annotations.Synchronous;
import com.karura.framework.plugins.WebViewPlugin;

/**
 * This component generates the wrapper javascript for a specific plugin
 * 
 */
public class PluginGenerator extends AbsCodeGenerator implements Constants {
	private static final String TAG = "PluginJavascriptGenerator";

	/*
	 * Reference for the plugin class for which the javascript is to be generated. For the most part you can pass any
	 * class to function. If that class does not have required annotations nothing will be generated.
	 */
	private Class<? extends WebViewPlugin> clazz;

	public PluginGenerator(Class<? extends WebViewPlugin> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Helper method to get the name of the javascript object
	 * 
	 * @param pluginName
	 * @return
	 */
	public static String jsWrapperNameForPlugin(String pluginName) {
		return pluginName + "Plugin";
	}

	public void applyToBuffer(SourceBuffer gen) {

		if (clazz == null || !clazz.isAnnotationPresent(JavascriptModule.class)) {
			return;
		}

		int count = signatureCount(clazz);

		if (count == 0)
			return;

		if (clazz.isAnnotationPresent(Description.class) && BuildConfig.DEBUG) {
			addComment(gen, clazz.getAnnotation(Description.class).value());

		}

		String wrapperName = jsWrapperNameForPlugin(clazz.getSimpleName());
		String frameworkClsName = FrameworkConfig.FRAMEWORK_JS_CLASS_NAME;
		
		defineConstructor(gen, frameworkClsName, wrapperName);
		
		// --- start ---
		// definition of a Plugin javascript prototype

		startPrototype(gen, wrapperName);

		startMemberFunction(gen, true, "release");
		gen.addLine("this.nativeObj.release();");
		gen.addLine("this." + frameworkClsName + ".objectMap.remove(this);");
		endFunction(gen, false, true);

		addExportedMethodsForClass(gen, "this.nativeObj", frameworkClsName, wrapperName);

		endPrototype(gen);
	}
	
	protected void defineConstructor(SourceBuffer gen, String frameworkClsName, String wrapperName){
		// --- start ---
		// definition of a Plugin javascript function
		
		
		startFunction(gen, false, wrapperName, frameworkClsName);

		gen.addLine("debug.info(\"[" + wrapperName
				+ "] constructor with '" + frameworkClsName + "': \" + typeof " + frameworkClsName + ");");

		gen.sB("if (!(this instanceof " + wrapperName + ")) ");
		gen.addLine("return new " + wrapperName + "(" + frameworkClsName + ");");
		gen.eB();

		defineProperty(gen, frameworkClsName, "return " + frameworkClsName + ";", null);

		ExportToJs classAnnotations = clazz.getAnnotation(ExportToJs.class);
		if (classAnnotations != null) {
			for (Class<?> refClazz : classAnnotations.classes()) {
				addExportedFieldsFromClass(gen, refClazz);
			}
		}

		addExportedFieldsFromClass(gen, clazz);

		defineVariable(gen,
				"nativeObj = window." + FrameworkConfig.FRAMEWORK_JS_HANDLE + ".instanceFor(\"" + clazz.getSimpleName()
						+ "\")");

		defineProperty(gen, "nativeObj", "return nativeObj;", null);

		gen.addLine(frameworkClsName + ".objectMap.add(this);");

		endFunction(gen, false, false);

		// --- end ---
		// definition of a Plugin javascript function
	}

	protected void addExportedMethodsForClass(SourceBuffer gen, String nativeObjHandle, String frameworkClsName, String wrapperName) {
		HashSet<String> funcMap = new HashSet<String>();
		Class<?> currentClazz = clazz;
		do {
			for (Method method : currentClazz.getDeclaredMethods()) {

				if (!method.isAnnotationPresent(SupportJavascriptInterface.class)) {
					continue;
				}

				if (funcMap.contains(method.getName()))
					continue;
				funcMap.add(method.getName());
				StringBuffer sig = new StringBuffer();

				String apiName = method.getName();

				int argCount = method.getParameterTypes().length;

				FunctionType fnType;

				if (method.isAnnotationPresent(Synchronous.class)) {
					fnType = FunctionType.SYNC_FN;
				} else if (method.isAnnotationPresent(Asynchronous.class)) {
					fnType = FunctionType.ASYNC_FN;
				} else {
					continue;
				}

				boolean isVoid = method.getReturnType() == void.class || method.getReturnType() == Void.class;

				if (method.isAnnotationPresent(Description.class) && BuildConfig.DEBUG) {
					addComment(gen, method.getAnnotation(Description.class).value());
				}

				if (method.isAnnotationPresent(Params.class) && BuildConfig.DEBUG) {
					int k = 0;
					Class<?>[] parameterTypes = method.getParameterTypes();
					for (Param p : method.getAnnotation(Params.class).value()) {
						addComment(gen, p.name() + " (" + parameterTypes[k].getSimpleName() + ") " + p.description());
						k++;
					}
				}

				if (method.isAnnotationPresent(Synchronous.class) && BuildConfig.DEBUG) {
					addComment(gen, "returns " + method.getAnnotation(Synchronous.class).retVal());
				} else if (method.isAnnotationPresent(Asynchronous.class) && BuildConfig.DEBUG) {
					addComment(gen, "returns " + method.getAnnotation(Asynchronous.class).retVal());
				}

				sig.append(apiName + " : function(");
				int k = 0;
				if (fnType == FunctionType.ASYNC_FN) {
					k = 1;
				}

				Param[] params = null;
				if (method.isAnnotationPresent(Params.class)) {
					params = method.getAnnotation(Params.class).value();
				}
				for (; k < argCount; k++) {
					if (params != null && params.length > k) {
						sig.append(params[k].name());
					} else {
						sig.append("arg" + k);
					}
					if (k < argCount - 1) {
						sig.append(", ");
					}
				}

				if (fnType == FunctionType.ASYNC_FN) {
					if (argCount > 1) {
						sig.append(", ");
					}
					sig.append("options");
				}
				sig.append(") ");

				gen.sB(sig.toString());
				if (fnType == FunctionType.ASYNC_FN) {
					gen.addLine("var callId = this. " + frameworkClsName + ".nextCallId;");
				}

				gen.addLine("debug.log('[" + wrapperName + "] calling native function :" + method.getName() + "');");
				sig = new StringBuffer();

				if (fnType == FunctionType.SYNC_FN && !isVoid) {
					sig.append("return " + nativeObjHandle + "." + apiName + "(");
				} else {
					sig.append( nativeObjHandle + "." + apiName + "(");
				}
				if (fnType == FunctionType.ASYNC_FN) {
					sig.append("callId");
				}

				k = 0;
				if (fnType == FunctionType.ASYNC_FN) {
					k = 1;
				}
				for (; k < argCount; k++) {
					if (fnType == FunctionType.ASYNC_FN || k > 0) {
						sig.append(", ");
					}
					if (params != null && params.length > k) {
						sig.append(params[k].name());
					} else {
						sig.append("arg" + k);
					}
				}
				sig.append(");");

				gen.addLine(sig.toString());

				if (fnType == FunctionType.ASYNC_FN) {
					gen.addLine("this." + frameworkClsName + ".callMap.add(callId, options);");
				}

				gen.eBWithComma();
			}
			currentClazz = currentClazz.getSuperclass();
		} while (!currentClazz.equals(Object.class));

		if (clazz.isAnnotationPresent(Events.class)) {
			Events events = clazz.getAnnotation(Events.class);
			for (Event event : events.value()) {
				gen.sB("set" + event.name() + " : function(arg0) ");
				gen.addLine("this." + event.name() + "Listener = arg0;");
				gen.eBWithComma();
			}
		}
	}

	private void addExportedFieldsFromClass(SourceBuffer gen, Class<?> refClazz) {
		Field[] classFields = refClazz.getDeclaredFields();
		for (Field f : classFields) {
			if (f.isAnnotationPresent(ExportToJs.class)) {
				String returnLine = "";
				try {
					// Try and fetch the value of the exported field
					boolean origAccessibility = f.isAccessible();
					f.setAccessible(true);
					returnLine = String.valueOf(f.get(refClazz));
					f.setAccessible(origAccessibility);
				} catch (Exception e) {
					continue;
				}
				// If we are running in debug mode then we will additionally dump the description
				// associated with the field being exported
				if (f.isAnnotationPresent(Description.class) && BuildConfig.DEBUG) {
					addComment(gen, f.getAnnotation(Description.class).value());
				}

				defineProperty(gen, f.getName(), "return '" + returnLine + "';", null);
			}
		}
	}

	@SuppressLint("NewApi")
	private int signatureCount(Class<?> clazz) {
		int count = 0;
		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(SupportJavascriptInterface.class)) {
				continue;
			}
			count++;
		}
		return count;
	}
}
