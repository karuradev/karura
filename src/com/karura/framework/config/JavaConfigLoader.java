package com.karura.framework.config;

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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JavaConfigLoader extends ConfigLoader {

	String configFile;

	JavaConfigLoader(String configFile) {
		this.configFile = configFile;
	}

	public ConfigStore loadConfig() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				public void startElement(String uri, final String localName,
						String qName, final Attributes attributes)
						throws SAXException {
					elementStart(new XMLNode() {

						@Override
						public String getAttributeValue(String attr) {
							return attributes.getValue(null, attr);
						}

						@Override
						public String getName() {
							return localName;
						}

						@Override
						public boolean getAttributeBooleanValue(String attr,
								boolean defaultVal) {
							String result = getAttributeValue(attr);
							if (result == null) {
								return defaultVal;
							}
							return Boolean.valueOf(result);
						}

					});

				}

				public void endElement(String uri, final String localName,
						String qName) throws SAXException {
					elementStart(new XMLNode() {

						@Override
						public String getAttributeValue(String attr) {
							return null;
						}

						@Override
						public String getName() {
							return localName;
						}

						@Override
						public boolean getAttributeBooleanValue(String attr,
								boolean defaultVal) {
							return false;
						}

					});
				}
			};

			saxParser.parse(configFile, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configStore;
	}
}
