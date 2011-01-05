function task(taskElement) {

	var cTask = taskElement;
	var current = this;

	this.id = $("input[name='id']", cTask).val();

	this.init = function() {
		$(".editButton", cTask).click(function() {
			current.edit();
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
		 * Save their original state
		 */
		current.originalTitle = $(".title h3", cTask).html();
		current.originalDesc = $(".description", cTask).html();
		current.originalTags = $("ul.tagContainer", cTask).html();

	};

	this.edit = function() {
		current.showSaveButtons();
		current.makeEditable();
		current.showLongSummary();
	};

	this.save = function() {
		
		
		
		current.showEditButtons();

	};

	this.cancel = function() {

		current.makeUnEditable();

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
		$('.editButton', cTask).show();
	};

	this.showSaveButtons = function() {
		$('.editButton', cTask).hide();
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

$(document).ready(function() {

	/*
	 * Initialise tasks
	 */
	$('.task').each(function() {
		var aTask = new task(this);

		aTask.init();
	});

	/*
	 * Track the initial order
	 */
	var taskOrder = new Array();

	$(".task .properties").each(function() {
		taskOrder.push($("input[name='id']", this).val());
	});

	/*
	 * Make the tasks sortable. Only submit the reordered tasks.
	 */
	$('.tasks').sortable({
		items : ".task",
		placeholder : 'taskPlaceholder',
		handle : $(".move"),
		update : function(event, ui) {
			var current = 0;
			var toSwap = new Array();
			$(".task .properties").each(function() {

				var tempTask = new task($(this));

				if (taskOrder[current] !== tempTask.id) {
					toSwap.push('task-' + tempTask.id);
					taskOrder[current] = tempTask.id;
				}
				current++;
			});

			$.post('/tasks/sort', {
				order : toSwap
			}, function() {

				// todo: perhaps stop a spinning progress indicator or something
				$("#notifications").text("saved");
				$("#notifications").show();
				$("#notifications").fadeOut(2000);
			});

			// console.log(newOrder);
		}
	});

	$(".thumbnails img.thumb[rel]").overlay({
		mask : {
			color : '#ebecff',
			loadSpeed : 200,
			opacity : 0.9
		},
		closeOnClick : true
	});

});