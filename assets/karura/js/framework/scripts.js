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

/**
* Script.js
* This javascript module allows you to dynamically load and append javascripts to DOM
* Upon successful loading of the javascript the caller is notified of the result successful 
* or otherwise
*/
;(function(window, document, undefined) {
	'use strict';
	
	var exports = window.exports;
	var debug = window.debug;

	/**
	* The constructor for the module
	*/
	function Scripts() {
		if (!(this instanceof Scripts)) {
			return new Scripts();
		}
		return this;
	}

	/*
	 * Private methods
	 */
	
	/**
	 * This method when called will remove a script node from dom. 
	 * @param id Specifies Id of the script which needs to be removed.
	 */
	function removeScript(id) {
		var scripts = document.getElementsByTagName('script');
		
		for (var i = 0; i < scripts.length; i++) {
			var script = scripts[i];
			if (script.id == id) {
				debug.info('[Scripts] Removing script with id: ' + id);
				script.parentNode.removeChild(script);
				script = null;
			}
		}
	}

	/*
	 * Public APIs
	 */
	Scripts.prototype = {
		constructor : Scripts,

		VERSION : '1.0',

		/**
		 * Use this api to load and append a javascript to DOM
		 * src : url of the script to be appended to DOM
		 * where : name of the element to which the specified script has to be appended
		 * id : unique identifier for the script
		 * callback : An optional callback method to be called once the script has been 
		 *            successfully loaded into memory. Prototype of the function called 
		 *            upon completion : callback(boolean success)
		 */
		load : function(src, where, id, callback) {
			debug.log('[Scripts] load called to request script src: ' + src + ' id: ' + id);

			//remove past instance of the script if any
			removeScript(id);

			var script = document.createElement('script');

			script.src = src;
			script.type = 'text/javascript';
			script.async = true; //load the script asynchronously
			script.charset = 'UTF-8';
			script.setAttribute('id', id);

			//setting up the event handler for when the script is loaded successfully or otherwise
			script.onreadystatechange = script.onload = function() {
				var state = document.readyState;
				if (_.isFunction(callback) && (!state || /loaded|complete/i.test(state))) {
					debug.info('[Scripts] delivering script ready callback for src: ' + src);
					callback(true);
				}
			};

			//append the script at its correct location
			document.querySelector(where || 'head').appendChild(script);
			debug.info('[Scripts] completed script loading');
		},

		toString : function() {
			return '[Scripts]';
		}
	};

	/*
	 * Make this class final
	 */
	(Object.freeze||Object)(Scripts.prototype);

	/*
	 * Make this class accessible in the exports namespace
	 */
	exports.add('Scripts', Scripts);
}(window, document));
