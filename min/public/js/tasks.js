function task(taskElement) {

	var cTask = taskElement;
	var current = this;
	var dialog = new dialogObj();
	var isNew = false;

	this.init = function() {
		$(".editButton", cTask).click(function() {
			current.edit();
			return false;
		});

		$(".deleteButton", cTask).click(function() {
			console.log("delete button presssed");
			current.deleteButton();
			return false;
		});

		$(".saveButton", cTask).click(function() {
			current.save();
			return false;
		});

		$(".cancelButton", cTask).click(function() {
			current.cancel();
			return false;
		});

		/*
		 * Initialise tag editing
		 */
		$("ul.tagContainer", cTask).tagit({
			availableTags : []
		});

		/*
		 * Variables
		 */

		current.refreshVars();

		/*
		 * Save their original state
		 */
		current.originalTitle = $(".title h3", cTask).html();
		current.originalDesc = $(".description", cTask).html();
		current.originalTags = $("ul.tagContainer", cTask).html();

	};

	this.refreshVars = function() {

		current.actionUrl = $("form", cTask).attr("action");
		current.authenticityToken = $("input[name='authenticityToken']", cTask).val();

		current.id = $("input[name='id']", cTask).val();
		current.title = $(".title h3", cTask).html();
		current.content = $(".description", cTask).html();
		current.tags = new Array();
		$(".tagit-choice input", cTask).each(function() {
			current.tags.push($(this).val());
		});

	};

	this.getDataForPost = function() {

		current.refreshVars();

		var rtnData = {};
		rtnData.task = {};

		rtnData.authenticityToken = current.authenticityToken;
		rtnData["task.id"] = current.id;
		rtnData["task.title"] = current.title;
		rtnData["task.content"] = current.content;
		rtnData.selectedTags = current.tags;

		return rtnData;
	};

	this.edit = function() {
		$(cTask).addClass('editMode');
		current.showSaveButtons();
		current.showLongSummary();
		current.makeEditable();
	};

	this.deleteButton = function() {

		dialog.makeConfirmation({
			"Yes" : function() {
				current.deleteTask();
				dialog.close();
			},
			"No" : function() {
				dialog.close();
			}
		});

		dialog.setTitle("Confirm Delete");
		dialog.setContents(current.title + "<br/><b>Are you sure you want to delete the Task?</b>");
		dialog.open();
	};

	this.deleteTask = function() {

		// console.log("Deleted");

		$.get("/tasks/delete", {
			taskId : current.id
		}, function(data) {
			$(cTask).fadeOut(600, function() {
				$(this).remove();
			});
		});
	};

	this.save = function() {

		$(cTask).removeClass('editMode');

		current.makeUnEditable();
		current.showEditButtons();

		var data = current.getDataForPost();
		data.action = "save";

		$.post(current.actionUrl, data, function(data) {

			$('.contents', cTask).html(data);
			current.init();
		}, "html");
	};

	this.cancel = function() {

		$(cTask).removeClass('editMode');
		current.makeUnEditable();
		current.showShortSummary();

		$(".title h3", cTask).html(current.originalTitle);
		$(".description", cTask).html(current.originalDesc);
		$("ul.tagContainer", cTask).html(current.originalTags);

		$("ul.tagContainer", cTask).tagit({
			availableTags : []
		});

		current.showEditButtons();
	};

	this.showEditButtons = function() {
		$('.saveButton, .cancelButton', cTask).hide();
		$('.editButton, .deleteButton', cTask).show();
	};

	this.showSaveButtons = function() {
		$('.editButton, .deleteButton', cTask).hide();
		$('.saveButton, .cancelButton', cTask).show();
	};

	this.showShortSummary = function() {
		$('.description ', cTask).addClass("descriptionSummary");
	};

	this.showLongSummary = function() {
		$('.description ', cTask).removeClass("descriptionSummary");
	};

	this.makeUnEditable = function() {

		$('.title h3', cTask).attr('contentEditable', false);

		$('.description', cTask).attr('contentEditable', false);

		$('li.tagit-new', cTask).addClass('hide');
	};

	this.makeEditable = function() {

		$('.title h3', cTask).attr('contentEditable', true).keypress(function(event) {
			return event.which != 13;
		});

		$('li.tagit-new', cTask).removeClass('hide');

		$('.description', cTask).attr('contentEditable', true);

	};

};

function tasks() {

	var taskOrder = new Array();
	
	this.init = function() {
		/*
		 * Initialise tasks
		 */
		$('.task').each(function() {
			var aTask = new task(this);

			aTask.init();
		});
		
		$(".task .properties").each(function() {
			taskOrder.push($("input[name='id']", this).val());
		});
		
		$("a.newTask").click(function(){
			
			
			return false;
		});

	};
	
	this.loadSort = function() {
		$('.tasks').sortable({
			items : ".task",
			placeholder : 'taskPlaceholder',
			handle : $(".move"),
			update : function(event, ui) {
				var current = 0;
				var toSwap = new Array();
				$(".task .properties").each(function() {

					var tempTask = new task($(this));
					tempTask.init();

					if (taskOrder[current] !== tempTask.id) {
						toSwap.push(tempTask.id);
						taskOrder[current] = tempTask.id;
					}
					current++;
				});

				$.post('/tasks/sort', {
					order : toSwap
				}, function() {

					// todo: perhaps stop a spinning progress indicator or
					// something
					$("#notifications").text("saved");
					$("#notifications").show();
					$("#notifications").fadeOut(2000);
				});

				// console.log(newOrder);
			}
		});
	};
	
	this.addNew = function() {

	};

};

$(document).ready(function() {

	theTasks = new tasks();
	theTasks.init();



	/*
	 * Make the tasks sortable. Only submit the reordered tasks.
	 */
	theTasks.loadSort();

	// $(".thumbnails img.thumb[rel]").overlay({
	// mask : {
	// color : '#ebecff',
	// loadSpeed : 200,
	// opacity : 0.9
	// },
	// closeOnClick : true
	// });

});