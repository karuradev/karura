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

import java.util.HashMap;

import com.karura.autogen.utils.SourceBuffer;
import com.karura.framework.FrameworkConfig;
import com.karura.framework.PluginManager;
import com.karura.framework.plugins.WebViewPlugin;

public class FrameworkGenerator extends PluginGenerator {

	HashMap<String, Class<? extends WebViewPlugin>> pluginMap;

	public FrameworkGenerator(HashMap<String, Class<? extends WebViewPlugin>> pluginMap) {
		super(PluginManager.class);
		this.pluginMap = pluginMap;
	}

	@Override
	public void applyToBuffer(SourceBuffer gen) {

		startAnonymousSelfExecutingFunction(gen, true);
		defineVariable(gen, "exports = window.exports", "globals = window.globals", "debug = window.debug");

		SourceBuffer pluginGen = new SourceBuffer();
		for (Class<? extends WebViewPlugin> clazz : pluginMap.values()) {

			new PluginGenerator(clazz).applyToBuffer(pluginGen);
			gen.addLine(pluginGen.toString());
			pluginGen.reset();
		}

		// --- start ---
		// definition of the FrameworkFacade Wrapper javascript function

		startFunction(gen, false, FrameworkConfig.FRAMEWORK_JS_CLASS_NAME, "objectMap", "callMap");

		gen.sB("if (!(this instanceof " + FrameworkConfig.FRAMEWORK_JS_CLASS_NAME + ")) ");
		gen.addLine("return new " + FrameworkConfig.FRAMEWORK_JS_CLASS_NAME + "(objectMap, callMap);");
		gen.eB();

		gen.sB("if (objectMap == null)");
		gen.addLine("objectMap = new window.exports.ObjectMap();");
		gen.eB();
		
		gen.sB("if (callMap == null)");
		gen.addLine("callMap = new window.exports.ObjectMap();");
		gen.eB();
		
		defineVariable(gen, "dispatcher = new window.exports.Dispatcher(this)");
		
		defineVariable(gen, "nextCallId = 1");

		defineProperty(gen, "objectMap", "return objectMap;", null);
		defineProperty(gen, "callMap", "return callMap;", null);
		defineProperty(gen, "nextCallId", "nextCallId = nextCallId+1;return nextCallId;", null);
		defineProperty(gen, "dispatcher", "return dispatcher;", null);

		endFunction(gen, false, false);

		// --- end ---
		// definition of the Karura Wrapper javascript function

		// --- start ---
		// definition of the Karura Wrapper javascript prototype

		startPrototype(gen, FrameworkConfig.FRAMEWORK_JS_CLASS_NAME);

		for (String pluginName : pluginMap.keySet()) {

			startMemberFunction(gen, true, pluginName);
			defineVariable(gen, "instance = new " + PluginGenerator.jsWrapperNameForPlugin(pluginName) + "(this)");
			startMemberFunction(gen, false, FrameworkConfig.FRAMEWORK_JS_CLASS_NAME + ".prototype." + pluginName);
			gen.addLine("return instance;");
			endFunction(gen, true, false);
			gen.addLine("return instance;");
			endFunction(gen, false, true);

		}
		
		addExportedMethodsForClass(gen, "window." + FrameworkConfig.FRAMEWORK_JS_HANDLE, FrameworkConfig.FRAMEWORK_JS_CLASS_NAME, FrameworkConfig.FRAMEWORK_JS_CLASS_NAME);

		endPrototype(gen);

		// -- end of prototype for the facade function

		gen.addLine("exports." + FrameworkConfig.FRAMEWORK_JS_CLASS_NAME + " = " + FrameworkConfig.FRAMEWORK_JS_CLASS_NAME + ";");

		endAnonymousSelfExecutingFunction(gen);

	}

}
