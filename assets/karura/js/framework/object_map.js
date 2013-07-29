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

;(function(window, document, undefined) {
	'use strict';

	var exports = window.exports;
	var debug = window.debug;

	function ObjectMap() {
		// Make sure some one is not able to apply this function to any arbitary closure
		if (!(this instanceof ObjectMap)) {
			return new ObjectMap();
		}

		/*
		 * Singleton, we are redefining the function in the following code so that all
		 * subsequent calls to this function get is the current intance, but there is
		 * no-reinitialization of the map and the getter setter properties
		 */
		var instance = this;
		ObjectMap = function() {
			debug.info('[ObjectMap] singleton instance being returned');
			return instance;
		};

		debug.info('[ObjectMap] constructor called');

		var map = {};

		/*
		 * Getters/Setters
		 */
		Object.defineProperties(this, {
			map : {
				get : function() {
					return map;
				},
				set : function(val) {
					map = val;
				}
			}
		});
	}

	/*
	 * Public APIs
	 */
	ObjectMap.prototype = {
		constructor : ObjectMap,

		VERSION : '1.0',

		/**
		 * Call this function to cache an object
		 * @param objOrId The object or Id of the object which needs to be cached. If two arguments 
		 * passed to this method then this is the id, else this is an object which must have a getId
		 * member for us to retrieve the object id
		 *
		 * @param obj If this argument is present then this is the reference to the object to be cached
		 */
		add : function(objOrId) {
			var id, _obj;

			switch (arguments.length) {
				case 1:
					_obj = objOrId;
					id = objOrId.getId();
					break;
				case 2:
					id = arguments[0];
					_obj = arguments[1];
					break;
				default:
					debug.error('[ObjectMap] add was called incorrectly. Ignored.');
					return;
			}

			debug.log('[ObjectMap] add called with ObjectId ' + id);

			if (id) {
				return !!(this.map[id] = _obj);
			}
		},

		/**
		 * Remove the object from object cache
		 * @param objOrId The object or Id of the object which needs to be removed. If two arguments 
		 * passed to this method then this is the id, else this is an object which must have a getId
		 * member for us to retrieve the object id
		 *
		 * @param obj If this argument is present then this is the reference to the object to be removed
		 */
		remove : function(objOrId) {
			var id, _obj;
			
			switch (arguments.length) {
				case 1:
					_obj = objOrId;
					id = objOrId.getId();
					break;
				case 2:
					id = arguments[0];
					_obj = arguments[1];
					break;
				default:
					debug.error('[ObjectMap] remove was called incorrectly. Ignored.');
					return;
			}

			if (id) {
				this.map[id] = null;
				delete this.map[id];
				return true;
			}

			return false;
		},

		/**
		 * Retrieve a cached object 
		 * @param id The id of the object which needs to be fetched
		 */
		getById : function(id) {
			debug.log('[ObjectMap] getById called with ObjectId ' + id);
			return this.map[id];
		},

		toString : function() {
			return '[ObjectMap]';
		}
	};

	/*
	 * Make this class final
	 */
	(Object.freeze||Object)(ObjectMap.prototype);

	/*
	 * Make this class accessible in the exports namespace
	 */
	exports.add('ObjectMap', ObjectMap);
}(window, document));
