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

import com.karura.autogen.BuildConfig;
import com.karura.autogen.utils.SourceBuffer;
import com.karura.autogen.utils.StringUtils;
import com.karura.framework.FrameworkConfig;

public abstract class AbsCodeGenerator {
	public abstract void applyToBuffer(SourceBuffer gen);

	protected void startFunction(SourceBuffer gen, boolean isStrict, String functionName, String... args) {
		StringBuffer sb = new StringBuffer();

		sb.append("function ");
		if (functionName != null) {
			sb.append(functionName);
		}
		sb.append("(");
		sb.append(StringUtils.join(",", args));
		sb.append(") ");

		gen.sB(sb.toString());
		if (isStrict) {
			gen.addLine("'use strict';");
		}
	}

	protected void startMemberFunction(SourceBuffer gen, boolean jsonNotation, String functionName, String... args) {
		StringBuffer sb = new StringBuffer();

		if (functionName == null) {
			throw new IllegalArgumentException();
		}

		sb.append(functionName);

		if (jsonNotation) {
			sb.append(" : ");
		} else {
			sb.append(" = ");
		}

		sb.append("function ");

		sb.append("(");
		sb.append(StringUtils.join(",", args));
		sb.append(") ");

		gen.sB(sb.toString());
	}

	protected void endFunction(SourceBuffer gen, boolean useSemi, boolean useComma) {
		if (useSemi && useComma) {
			throw new IllegalArgumentException();
		}
		if (useSemi) {
			gen.eBwithSemi();
		} else if (useComma) {
			gen.eBWithComma();
		} else {
			gen.eB();
		}
	}

	protected void startAnonymousSelfExecutingFunction(SourceBuffer gen, boolean isStrict) {

		addComment(gen, "---- framework javascript begins");
		addComment(gen, "undefined is passed to restore the definition of undefined incase it has been overridden");

		gen.addLine("; (");
		startFunction(gen, isStrict, null, "window", "document", "undefined");
	}

	protected void endAnonymousSelfExecutingFunction(SourceBuffer gen) {
		gen.eB();
		gen.addLine("(window, document));");
	}

	protected void defineVariable(SourceBuffer gen, String... variables) {
		StringBuffer sb = new StringBuffer("var ");
		sb.append(StringUtils.join(",", variables));
		sb.append(";");
		gen.addLine(sb.toString());
	}

	protected void defineProperty(SourceBuffer gen, String propertyName, String propBodyGet, String propBodySet) {
		gen.sB("Object.defineProperty(this, '" + propertyName + "',");
		if (propBodyGet != null) {
			gen.sB("get : function()");
			gen.addLine(propBodyGet);
			gen.eBWithComma();
		}

		if (propBodySet != null) {
			gen.sB("set : function()");
			gen.addLine(propBodySet);
			gen.eBWithComma();
		}

		gen.eB();
		gen.addLine(");");
	}

	protected void startPrototype(SourceBuffer gen, String className) {
		gen.sB(className + ".prototype = ");
		gen.addLine("constructor : " + className + ",");
		gen.addLine("version : '" + FrameworkConfig.FRAMEWORK_VERSION + "',");
	}

	protected void endPrototype(SourceBuffer gen) {
		gen.eBwithSemi();
	}

	protected void addComment(SourceBuffer gen, String desc) {
		if (BuildConfig.DEBUG) {
			if (!StringUtils.isEmpty(desc)) {
				String[] comps = StringUtils.wrap(desc, 80, StringUtils.LINE_SEPARATOR, false).split(StringUtils.LINE_SEPARATOR);
				for (String comp : comps) {
					gen.addLine("//" + comp);
				}
			}
		}
	}

	protected void addMultiComment(SourceBuffer gen, String desc, boolean begin, boolean end) {
		if (BuildConfig.DEBUG) {
			if (begin) {
				gen.addLine("/*");
			}
			if (!StringUtils.isEmpty(desc)) {
				String[] comps = StringUtils.wrap(desc, 80, StringUtils.LINE_SEPARATOR, false).split(StringUtils.LINE_SEPARATOR);
				for (String comp : comps) {
					gen.addLine("*" + comp);
				}
			}

			if (end) {
				gen.addLine("*/");
			}
		}
	}
}
