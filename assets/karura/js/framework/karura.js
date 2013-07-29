//---- framework javascript begins
//undefined is passed to restore the definition of undefined incase it has been
//overridden
; (
function (window,document,undefined) {
    'use strict';
    var exports = window.exports,globals = window.globals,debug = window.debug;
    //Plugin can be used to query device specific parameters
    function DevicePlugin(Karura) {
        debug.info("[DevicePlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof DevicePlugin)) {
            return new DevicePlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        //Field which will contain device UUID
        Object.defineProperty(this, 'UUID_FIELD',{
            get : function(){
                return 'uuid';
            },
    
        }
        );
        //Field for returning the platform version
        Object.defineProperty(this, 'PLATFORM_VERSION_FIELD',{
            get : function(){
                return 'version';
            },
    
        }
        );
        //Field for returning platform name
        Object.defineProperty(this, 'PLATFORM_FIELD',{
            get : function(){
                return 'platform';
            },
    
        }
        );
        //Field for return device name
        Object.defineProperty(this, 'DEVICE_NAME_FIELD',{
            get : function(){
                return 'name';
            },
    
        }
        );
        //Field for returning device model
        Object.defineProperty(this, 'DEVICE_MODEL_FIELD',{
            get : function(){
                return 'model';
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("Device");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    DevicePlugin.prototype = {
        constructor : DevicePlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Fetch device specific details
        //returns Returns the stringified json object containing device params 
        getDeviceInfo : function() {
            debug.log('[DevicePlugin] calling native function :getDeviceInfo');
            return this.nativeObj.getDeviceInfo();
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[DevicePlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    //This plugin allows users to read media file meta data, and capture audio, video,
    //and photographs using native components 
    function CapturePlugin(Karura) {
        debug.info("[CapturePlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof CapturePlugin)) {
            return new CapturePlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        //MIME type for video 3gpp files
        Object.defineProperty(this, 'VIDEO_3GPP',{
            get : function(){
                return 'video/3gpp';
            },
    
        }
        );
        //MIME type for mp4 files
        Object.defineProperty(this, 'VIDEO_MP4',{
            get : function(){
                return 'video/mp4';
            },
    
        }
        );
        //MIME type for audio 3gpp files
        Object.defineProperty(this, 'AUDIO_3GPP',{
            get : function(){
                return 'audio/3gpp';
            },
    
        }
        );
        //MIME type for jpeg image files
        Object.defineProperty(this, 'IMAGE_JPEG',{
            get : function(){
                return 'image/jpeg';
            },
    
        }
        );
        //Height of the media element whose metadata is being retrieved.
        Object.defineProperty(this, 'HEIGHT_FIELD',{
            get : function(){
                return 'height';
            },
    
        }
        );
        //Width of the media element whose metadata is being retrieved.
        Object.defineProperty(this, 'WIDTH_FIELD',{
            get : function(){
                return 'width';
            },
    
        }
        );
        //Bitrate of the media element whose metadata is being retrieved.
        Object.defineProperty(this, 'BITRATE_FIELD',{
            get : function(){
                return 'bitrate';
            },
    
        }
        );
        //Duration (in miliseconds) of the media element whose metadata is being
        //retrieved.
        Object.defineProperty(this, 'DURATION_FIELD',{
            get : function(){
                return 'duration';
            },
    
        }
        );
        //Codecs information for the media element whose metadata is being retrieved.
        Object.defineProperty(this, 'CODECS_FIELD',{
            get : function(){
                return 'codecs';
            },
    
        }
        );
        //Name of the media file (image/audio/video) which was just recorded
        Object.defineProperty(this, 'MEDIA_FILE_NAME_FIELD',{
            get : function(){
                return 'name';
            },
    
        }
        );
        //File system path for the media component just recorded.
        Object.defineProperty(this, 'FILE_PATH_FIELD',{
            get : function(){
                return 'fullPath';
            },
    
        }
        );
        //Mime type for the file just recorded.
        Object.defineProperty(this, 'FILE_TYPE_FIELD',{
            get : function(){
                return 'type';
            },
    
        }
        );
        //Date when the file was last modified
        Object.defineProperty(this, 'FILE_MODIFIED_FIELD',{
            get : function(){
                return 'lastModifiedDate';
            },
    
        }
        );
        //Size of the file in bytes
        Object.defineProperty(this, 'FILE_SIZE_FIELD',{
            get : function(){
                return 'fileSize';
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("Capture");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    CapturePlugin.prototype = {
        constructor : CapturePlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Try and read the metadata associated with the specified media file
        //callId (String) The method correlator between javascript and java.
        //filePath (String) The file from which the metadata is to be loaded
        //userSuggestedMime (String) User hint for the mimetype of the file which needs to
        //be processed in this API
        //returns A JSON Object which contains the height, width, bitrate, duration and
        //codec information, if available in media object. Any field which is not
        //available is returned as 0
        getMetadataForMedia : function(filePath, userSuggestedMime, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[CapturePlugin] calling native function :getMetadataForMedia');
            this.nativeObj.getMetadataForMedia(callId, filePath, userSuggestedMime);
            this.Karura.callMap.add(callId, options);
        },
    
        //Sets up an intent to capture audio and then starts the native system component
        //to record audio. If there are more than one activity which can do the needful
        //then a chooser dialog is displayed to the user to select the component which
        //they will like to use to record the audio clip.
        //callId (String) The method correlator between javascript and java.
        //returns Returns a json object which contains the file name, type, modified date,
        //size and path if successful.
        captureAudio : function(options) {
            var callId = this. Karura.nextCallId;
            debug.log('[CapturePlugin] calling native function :captureAudio');
            this.nativeObj.captureAudio(callId);
            this.Karura.callMap.add(callId, options);
        },
    
        //Sets up an intent to capture images, and then starts the component.
        //callId (String) The method correlator between javascript and java.
        //returns Returns a json object which contains the file name, type, modified date,
        //size and path if successful.
        captureImage : function(options) {
            var callId = this. Karura.nextCallId;
            debug.log('[CapturePlugin] calling native function :captureImage');
            this.nativeObj.captureImage(callId);
            this.Karura.callMap.add(callId, options);
        },
    
        //Sets up an intent to capture video and starts the system component to handle
        //recording of video
        //callId (String) The method correlator between javascript and java.
        //returns Returns a json object which contains the file name, type, modified date,
        //size and path if successful.
        captureVideo : function(arg1, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[CapturePlugin] calling native function :captureVideo');
            this.nativeObj.captureVideo(callId, arg1);
            this.Karura.callMap.add(callId, options);
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[CapturePlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    //Application lifecycle components
    function UIPlugin(Karura) {
        debug.info("[UIPlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof UIPlugin)) {
            return new UIPlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        //One of the param keys used in the loadUrl API, specifies the delay after which
        //the webpage should be loaded.
        Object.defineProperty(this, 'WAIT_KEY',{
            get : function(){
                return 'wait';
            },
    
        }
        );
        //One of the param keys used in loadUrl API, specifies whether the url should be
        //openned in external viewer.
        Object.defineProperty(this, 'OPEN_EXTR_KEY',{
            get : function(){
                return 'openexternal';
            },
    
        }
        );
        //One of the param keys used in loadUrl API, specifies whether the browser history
        //should be cleaned before loading the url
        Object.defineProperty(this, 'CLEAR_HISTORY_KEY',{
            get : function(){
                return 'clearhistory';
            },
    
        }
        );
        //One of the params used in overrideButton API, used to specify the volume up
        //button
        Object.defineProperty(this, 'VOLUME_UP_KEY',{
            get : function(){
                return 'volumeup';
            },
    
        }
        );
        //One of the params used in overrideButton API, used to specify the volume down
        //button
        Object.defineProperty(this, 'VOLUME_DOWN_KEY',{
            get : function(){
                return 'volumedown';
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("UI");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    UIPlugin.prototype = {
        constructor : UIPlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Clear the resource cache.
        //returns none
        clearCache : function() {
            debug.log('[UIPlugin] calling native function :clearCache');
            this.nativeObj.clearCache();
        },
    
        //Go to previous page displayed. This is the same as pressing the back button on
        //Android device.
        //returns none
        backHistory : function() {
            debug.log('[UIPlugin] calling native function :backHistory');
            this.nativeObj.backHistory();
        },
    
        //Load the url into the webview.
        //callId (String) The method correlator between javascript and java.
        //url (String) URL to be loaded in the web browser
        //props (JSONObject) Specifies the parameters for customizing the loadUrl
        //experience. Look at WAIT_KEY, OPEN_EXTR_KEY and CLEAR_HISTORY_KEY. If the
        //OPEN_EXTR_KEY is specified then this object can also contain additional
        //parameters which need to be passed to the external viewer in the intent. The
        //other keys can only be integer, boolean or string
        //returns none, will load the specified URL in the webview
        loadUrl : function(url, props, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[UIPlugin] calling native function :loadUrl');
            this.nativeObj.loadUrl(callId, url, props);
            this.Karura.callMap.add(callId, options);
        },
    
        //Clear page history for the app.
        //returns none
        clearHistory : function() {
            debug.log('[UIPlugin] calling native function :clearHistory');
            this.nativeObj.clearHistory();
        },
    
        //Override the default behavior of the Android back button. If overridden, when
        //the back button is pressed, the "backKeyDown" JavaScript event will be fired.
        //override (boolean)  T=override, F=cancel override
        //returns none
        overrideBackbutton : function(override) {
            debug.log('[UIPlugin] calling native function :overrideBackbutton');
            this.nativeObj.overrideBackbutton(override);
        },
    
        //Override the default behavior of the Android volume buttons. If overridden, when
        //the volume button is pressed, the "volume[up|down]button" JavaScript event will
        //be fired.
        //button (String) Specifies the button to be bound, available options
        //VOLUME_UP_KEY, VOLUME_DOWN_KEY
        //override (boolean)  T=override, F=cancel override
        //returns none
        overrideButton : function(button, override) {
            debug.log('[UIPlugin] calling native function :overrideButton');
            this.nativeObj.overrideButton(button, override);
        },
    
        //Return whether the Android back button is overridden by the user.
        //returns boolean
        isBackbuttonOverridden : function() {
            debug.log('[UIPlugin] calling native function :isBackbuttonOverridden');
            return this.nativeObj.isBackbuttonOverridden();
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[UIPlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    //A plugin to access, search and modify native contacts
    function ContactsPlugin(Karura) {
        debug.info("[ContactsPlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof ContactsPlugin)) {
            return new ContactsPlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        Object.defineProperty(this, 'SUCCESS',{
            get : function(){
                return '0';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_UNKNOWN',{
            get : function(){
                return '1';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_INVALID_ARGUMENT',{
            get : function(){
                return '2';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_TIMEOUT',{
            get : function(){
                return '3';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_IO',{
            get : function(){
                return '4';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_NOT_SUPPORTED',{
            get : function(){
                return '5';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_INVALID_ARG',{
            get : function(){
                return '6';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_EMPTY_RESULT',{
            get : function(){
                return '7';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_INVALID_JSON',{
            get : function(){
                return '8';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_RECORD_NOT_FOUND',{
            get : function(){
                return '9';
            },
    
        }
        );
        Object.defineProperty(this, 'ERR_PERMISSION_DENIED',{
            get : function(){
                return '20';
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("Contacts");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    ContactsPlugin.prototype = {
        constructor : ContactsPlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Resolve the content URI to a base64 image.
        //callId (String) The method correlator between javascript and java.
        //contactContentUri (String) Content URI of the photograph which was returned by
        //the plugin in the call to getContact API
        //returns Returns the base64 encoded photograph of the specified contact
        getPhoto : function(contactContentUri, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[ContactsPlugin] calling native function :getPhoto');
            this.nativeObj.getPhoto(callId, contactContentUri);
            this.Karura.callMap.add(callId, options);
        },
    
        //Search contacts in the native address book based upon mentioned criteria
        //callId (String) The method correlator between javascript and java.
        //projection (String) Comma separated list of fields to be retrieved from the
        //contacts db. For selecting all fields, use *.
        //selection (String) Criteria for selection. This value will be used in where
        //clause.
        //startIndex (int) Specifies the start index for the page which needs to be
        //fetched
        //limit (int) Specifies the number of entries in the current page to be fetched.
        //returns JSON array of Contacts. The fields of each contact are as specified in
        //the projection criteria.
        getContact : function(projection, selection, startIndex, limit, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[ContactsPlugin] calling native function :getContact');
            this.nativeObj.getContact(callId, projection, selection, startIndex, limit);
            this.Karura.callMap.add(callId, options);
        },
    
        //Delete a contact from the native address book
        //callId (String) The method correlator between javascript and java.
        //contactId (String) Id of the contact to be deleted.
        //returns integer specifying success or failure, depending upon whether the api
        //was executed successfully or not.
        removeContact : function(contactId, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[ContactsPlugin] calling native function :removeContact');
            this.nativeObj.removeContact(callId, contactId);
            this.Karura.callMap.add(callId, options);
        },
    
        //Update the specified contact in the native address book.
        //callId (String) The method correlator between javascript and java.
        //jsonEncodedContact (String) Stringified json object representing the contact to
        //be updated. Look at the field constants to see what parameters can be updated.
        //returns Returns the new contact record (a json object) on success, or the error
        //code on failure.
        saveContact : function(jsonEncodedContact, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[ContactsPlugin] calling native function :saveContact');
            this.nativeObj.saveContact(callId, jsonEncodedContact);
            this.Karura.callMap.add(callId, options);
        },
    
        //Find the number of contacts in the native database which match the given
        //criteria
        //callId (String) The method correlator between javascript and java.
        //projection (String) Comma separated list of fields to be retrieved from the
        //contacts db. For selecting all fields, use *.
        //selection (String) Criteria for selection. This value will be used in where
        //clause.
        //returns Returns the number of contacts which match the given criteria
        getCount : function(projection, selection, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[ContactsPlugin] calling native function :getCount');
            this.nativeObj.getCount(callId, projection, selection);
            this.Karura.callMap.add(callId, options);
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[ContactsPlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    //This plugin allows you to display native dialogs from javascript.
    function NotificationPlugin(Karura) {
        debug.info("[NotificationPlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof NotificationPlugin)) {
            return new NotificationPlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        //Used to identify the positive button for the dialogs.
        Object.defineProperty(this, 'POSITIVE_BTN',{
            get : function(){
                return '1';
            },
    
        }
        );
        //Used to identify the negative button for the dialogs.
        Object.defineProperty(this, 'NEGATIVE_BTN',{
            get : function(){
                return '2';
            },
    
        }
        );
        //Used to signal that a dialog was cancelled.
        Object.defineProperty(this, 'CANCELED',{
            get : function(){
                return '3';
            },
    
        }
        );
        //Used to identify the optional third button on dialogs.
        Object.defineProperty(this, 'NEUTRAL_BTN',{
            get : function(){
                return '4';
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("Notification");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    NotificationPlugin.prototype = {
        constructor : NotificationPlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Builds and shows a native Android alert with given Strings
        //callId (String) The method correlator between javascript and java.
        //title (String) The title of the alert
        //message (String) The message the alert should display
        //buttonLabel (String) The label of the button
        //returns Will send a the button code to javascript based on user action.
        //Currently POSITIVE_BTN and CANCELLED are sent.
        alert : function(title, message, buttonLabel, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[NotificationPlugin] calling native function :alert');
            this.nativeObj.alert(callId, title, message, buttonLabel);
            this.Karura.callMap.add(callId, options);
        },
    
        //Play a small vibrator alert
        //time (long) duration in miliseconds for which the vibrator needs to be played
        //returns none
        vibrate : function(time) {
            debug.log('[NotificationPlugin] calling native function :vibrate');
            this.nativeObj.vibrate(time);
        },
    
        //Builds and shows a native Android confirm dialog with given title, message,
        //buttons. This dialog only shows up to 3 buttons. Anylabels after that will be
        //ignored. The index of the button pressed will be returned to the JavaScript
        //callback identified by callId.
        //callId (String) The method correlator between javascript and java.
        //title (String) The title of the alert
        //message (String) The message the alert should display
        //buttonLabels (String)  A comma separated list of button labels (Up to 3
        //buttons). The first button is identified by NEGATIVE_BTN, second by NEUTRAL_BTN
        //and third by POSITIVE_BTN. You dont have to give all three buttons 
        //returns Index of the button selected by the user or cancelled in case the dialog
        //was cancelled
        confirm : function(title, message, buttonLabels, options) {
            var callId = this. Karura.nextCallId;
            debug.log('[NotificationPlugin] calling native function :confirm');
            this.nativeObj.confirm(callId, title, message, buttonLabels);
            this.Karura.callMap.add(callId, options);
        },
    
        //Play a small alert tone
        //count (long) duration in miliseconds for which the beep sound needs to be played
        //returns none
        beep : function(count) {
            debug.log('[NotificationPlugin] calling native function :beep');
            this.nativeObj.beep(count);
        },
    
        //Show the spinner.
        //title (String) The title of the spinner dialog
        //message (String) The message to be displayed on the spinner
        //returns none
        showSpinner : function(title, message) {
            debug.log('[NotificationPlugin] calling native function :showSpinner');
            this.nativeObj.showSpinner(title, message);
        },
    
        //Stop spinner.
        //returns none
        hideSpinner : function() {
            debug.log('[NotificationPlugin] calling native function :hideSpinner');
            this.nativeObj.hideSpinner();
        },
    
        //Set value of progress bar.
        //value (int) value of the progress bar between 0 and 100.
        //returns none
        progressValue : function(value) {
            debug.log('[NotificationPlugin] calling native function :progressValue');
            this.nativeObj.progressValue(value);
        },
    
        //Stop progress dialog.
        //returns none
        progressStop : function() {
            debug.log('[NotificationPlugin] calling native function :progressStop');
            this.nativeObj.progressStop();
        },
    
        //Display a toast to the user
        //message (String) Message to be displayed in the toast
        //returns none
        showToast : function(message) {
            debug.log('[NotificationPlugin] calling native function :showToast');
            this.nativeObj.showToast(message);
        },
    
        //Show the progress dialog.
        //title (String) Title of the dialog
        //message (String) The message of the dialog
        //returns none
        progressStart : function(title, message) {
            debug.log('[NotificationPlugin] calling native function :progressStart');
            this.nativeObj.progressStart(title, message);
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[NotificationPlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    //Use android logging facility
    function LoggerPlugin(Karura) {
        debug.info("[LoggerPlugin] constructor with 'Karura': " + typeof Karura);
        if (!(this instanceof LoggerPlugin)) {
            return new LoggerPlugin(Karura);
        }
        Object.defineProperty(this, 'Karura',{
            get : function(){
                return Karura;
            },
    
        }
        );
        var nativeObj = window._karura_plugins.instanceFor("Logger");
        Object.defineProperty(this, 'nativeObj',{
            get : function(){
                return nativeObj;
            },
    
        }
        );
        Karura.objectMap.add(this);
    }
    LoggerPlugin.prototype = {
        constructor : LoggerPlugin,
        version : '1.0',
        release : function () {
            this.nativeObj.release();
            this.Karura.objectMap.remove(this);
        },
    
        //Log an error log in the android syslogger
        //Tag (String) Tag to be used for logging in the android log system
        //msg (String) The message to be logged.
        //returns none
        e : function(Tag, msg) {
            debug.log('[LoggerPlugin] calling native function :e');
            this.nativeObj.e(Tag, msg);
        },
    
        //Log a debug log in the android syslogger
        //Tag (String) Tag to be used for logging in the android log system
        //msg (String) The message to be logged.
        //returns none
        d : function(Tag, msg) {
            debug.log('[LoggerPlugin] calling native function :d');
            this.nativeObj.d(Tag, msg);
        },
    
        //Log an info log in the android syslogger
        //Tag (String) Tag to be used for logging in the android log system
        //msg (String) The message to be logged.
        //returns none
        i : function(Tag, msg) {
            debug.log('[LoggerPlugin] calling native function :i');
            this.nativeObj.i(Tag, msg);
        },
    
        //Log a verbose log in the android syslogger
        //Tag (String) Tag to be used for logging in the android log system
        //msg (String) The message to be logged.
        //returns none
        v : function(Tag, msg) {
            debug.log('[LoggerPlugin] calling native function :v');
            this.nativeObj.v(Tag, msg);
        },
    
        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[LoggerPlugin] calling native function :getId');
            return this.nativeObj.getId();
        },
    
    };
    function Karura(objectMap,callMap) {
        if (!(this instanceof Karura)) {
            return new Karura(objectMap, callMap);
        }
        if (objectMap == null){
            objectMap = new window.exports.ObjectMap();
        }
        if (callMap == null){
            callMap = new window.exports.ObjectMap();
        }
        var dispatcher = new window.exports.Dispatcher(this);
        var nextCallId = 1;
        Object.defineProperty(this, 'objectMap',{
            get : function(){
                return objectMap;
            },

        }
        );
        Object.defineProperty(this, 'callMap',{
            get : function(){
                return callMap;
            },

        }
        );
        Object.defineProperty(this, 'nextCallId',{
            get : function(){
                nextCallId = nextCallId+1;return nextCallId;
            },

        }
        );
        Object.defineProperty(this, 'dispatcher',{
            get : function(){
                return dispatcher;
            },

        }
        );
    }
    Karura.prototype = {
        constructor : Karura,
        version : '1.0',
        Device : function () {
            var instance = new DevicePlugin(this);
            Karura.prototype.Device = function () {
                return instance;
            };
            return instance;
        },

        Capture : function () {
            var instance = new CapturePlugin(this);
            Karura.prototype.Capture = function () {
                return instance;
            };
            return instance;
        },

        UI : function () {
            var instance = new UIPlugin(this);
            Karura.prototype.UI = function () {
                return instance;
            };
            return instance;
        },

        Contacts : function () {
            var instance = new ContactsPlugin(this);
            Karura.prototype.Contacts = function () {
                return instance;
            };
            return instance;
        },

        Notification : function () {
            var instance = new NotificationPlugin(this);
            Karura.prototype.Notification = function () {
                return instance;
            };
            return instance;
        },

        Logger : function () {
            var instance = new LoggerPlugin(this);
            Karura.prototype.Logger = function () {
                return instance;
            };
            return instance;
        },

        //Get the number of webview plugins
        //returns Integer representing the total number of webview plugins available to
        //the runtime
        count : function() {
            debug.log('[Karura] calling native function :count');
            return window._karura_plugins.count();
        },

        //Retrieve the list of all webview plugins which are available in the current
        //runtime
        //returns A JSON array containing names of all webview plugins available to plugin
        //manager
        names : function() {
            debug.log('[Karura] calling native function :names');
            return window._karura_plugins.names();
        },

        //Creates an instance of the plugin and caches it for future invocations. This
        //component is also automatically bound to the webview instance.Under normal
        //circumstances this method will not be used by developers and is meant for the
        //framework wrapper class which is used for accessing native components and
        //plugins
        //clazz (String) Class of the plugin which needs to be allocated.
        //returns A native webview plugin components
        instanceFor : function(clazz) {
            debug.log('[Karura] calling native function :instanceFor');
            return window._karura_plugins.instanceFor(clazz);
        },

        //Sets up the dispatcher handle, for java to reach the framework on the javascript
        //side. This cannot be done automatically becausethe developers may choose to
        //setup the scripts as per their own liking.
        //dispatchHandle (String) Fully qualified reference to the karura framework in the
        //javascript address space.
        //returns none
        setDispatchHandle : function(dispatchHandle) {
            debug.log('[Karura] calling native function :setDispatchHandle');
            window._karura_plugins.setDispatchHandle(dispatchHandle);
        },

        //Returns the javascript from the cache for the specified script id
        //scriptId (int) Id of the script to be fetched.
        //returns String representing the javascript
        getJsWithId : function(scriptId) {
            debug.log('[Karura] calling native function :getJsWithId');
            return window._karura_plugins.getJsWithId(scriptId);
        },

        //returns Object identifier of the this javascript object
        getId : function() {
            debug.log('[Karura] calling native function :getId');
            return window._karura_plugins.getId();
        },

    };
    exports.Karura = Karura;
}
(window, document));
