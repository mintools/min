function dialogObj() {

	var dialogSelector = "#dialog";
	var theDialog = $(dialogSelector);
	var current = this;

	this.init = function(params) {

		if (!params) {
			var params = {};
		}

		params.autoOpen = false;
		params.minWidth = 200;
		params.width = 400;

		$(theDialog).dialog("destroy");
		$(theDialog).dialog(params);
	};

	this.makeConfirmation = function(buttons) {

		var params = {};
		params.buttons = buttons;

		current.init(params);
		current.setContents("Are you sure you want to do this?");

	};

	this.setContents = function(contents) {
		$(theDialog).html(contents);
	};

	this.setTitle = function(title) {
		$(theDialog).dialog("option", "title", title);
	};

	this.open = function() {
		$(theDialog).dialog("open");
	};

	this.close = function() {
		$(theDialog).dialog("close");
	};
};

$(document).ready(function() {

	// dialog = new dialogObj();
	//
	// dialog.makeConfirmation({
	//
	// "Yes" : function() {
	// console.log("Hello world");
	// dialog.close();
	// },
	// "No" : function() {
	// console.log("No");
	// dialog.close();
	// }
	// });
	//
	// dialog.setContents("Are you sure?");
	// dialog.setTitle("Confirm");
	// dialog.open();

});
