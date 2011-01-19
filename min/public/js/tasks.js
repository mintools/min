function task(taskElement, taskList) {

	var cTask = taskElement;
	var current = this;
	var dialog = new dialogObj();
	var expanded = false;

	this.taskList = taskList;
	this.isNew = false;
	this.mode = '';
    this.cTask = cTask;

	/**
	 * expand
	 */
	

	this.init = function(isNew) {
		current.isNew = isNew;

		/*
		 * Variables
		 */
		current.refreshVars();

        $(cTask).mouseenter(function(e) {
            if (!current.isNew) $(".hoverAction", cTask).show();
        });

        $(cTask).mouseleave(function(e) {
            if (!current.isNew) $(".hoverAction", cTask).hide();
        });

        $(".insertAboveButton", cTask).click(function() {
            taskList.insertNew(current, true);
        });

        $(".insertBelowButton", cTask).click(function() {
            taskList.insertNew(current, false);
        });

        $(".moveUpButton", cTask).click(function() {
            taskList.moveToTop(current);
        });

        $(".moveDownButton", cTask).click(function() {
            taskList.moveToBottom(current);
        });

		$('.title,.main,.comments',cTask).click(function() {
			
			if (!current.isEditMode()) {
				current.toggle();
			}
			return true;
		});
		
		/*
		 * Buttons
		 */
		$(".editButton", cTask).click(function() {
			current.edit();
			return false;
		});

		$(".deleteButton", cTask).click(function() {
			if (isNew) {
				current.remove();
			} else {
				current.deleteButton();
			}
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

		$(".addInterestButton", cTask).click(function() {
			current.addInterest();
			return false;
		});

		$(".removeInterestButton", cTask).live("click", function() {
			current.removeInterest($(this).closest(".taskInterest"));
			return false;
		});

		$(".thumbnails .attachment img.delete", cTask).click(function() {
			current.deleteAttachmentConfirm($(this).parents('.attachment').first());
			return false;
		});


		if (isNew) {
			current.makeEditable();
			current.showLongSummary();
		}

		$(".thumbnails .attachment .thumb img[rel]", cTask).overlay({
			mask : {
				color : '#ebecff',
				loadSpeed : 200,
				opacity : 0.9
			},
			closeOnClick : true
		});

		/*
		 * Save their original state
		 */
		current.originalTitle = $(".title h3", cTask).html();
		current.originalDesc = $(".description", cTask).html();

		current.createUploader();

	};

	this.refreshVars = function() {

		current.actionUrl = $("form", cTask).attr("action");
		current.authenticityToken = $("input[name='authenticityToken']", cTask).val();

		current.id = $("input[name='id']", cTask).val();
		current.title = $.trim($(".title h3", cTask).html());
		current.content = $.trim($(".description", cTask).html());
        current.selectedTags = $.trim($(".selectedTags", cTask).text());
	};

	this.refresh = function() {

		$.get('/tasks/show', {
			taskId : current.id
		}, function(data) {
			// console.log(data);

			$(".contents", cTask).replaceWith(data);
			current.init();
		});
	};

	this.getDataForPost = function() {

//		current.refreshVars();

		var rtnData = {};
		rtnData.task = {};

		rtnData.authenticityToken = current.authenticityToken;
		rtnData["task.id"] = current.id;
		rtnData["task.title"] = current.title;
		rtnData["task.content"] = current.content;
		rtnData["selectedTags"] = current.selectedTags;        

		return rtnData;
	};

	this.toggle = function() {
		if (current.expanded) {
			$(cTask).removeClass('expandMode');
			current.showShortSummary();
		} else {
			$(cTask).addClass('expandMode');
			current.showLongSummary();

		}
		current.expanded = !current.expanded;
	};

	this.isEditMode = function() {
		return $(cTask).hasClass('editMode');
	};

	this.edit = function() {

		$(cTask).removeClass('expandMode');
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
			current.remove();
		});
	};

	this.deleteAttachmentConfirm = function(attachment) {
		dialog.makeConfirmation({
			"Yes" : function() {
				current.deleteAttachment(attachment);
				dialog.close();
			},
			"No" : function() {
				dialog.close();
			}
		});

		dialog.setTitle("Confirm Delete");
		dialog.setContents("Are you sure you want to delete the Attachment?</b>");
		dialog.open();
	};

	this.deleteAttachment = function(attachment) {
		//console.log(attachment);

		var attId = $('input[name=attachmentId]', attachment).val();

		$.get("/tasks/deleteAttachment", {
			id : attId
		}, function(data) {
			attachment.remove();
		});
	};

	this.remove = function() {
		$(cTask).slideUp(600, function() {
			$(this).remove();
		});
	};

	this.save = function() {
		current.title = $.trim($(".title h3", cTask).html());
		current.content = $.trim($(".description", cTask).html());
        current.selectedTags = $(".tagContainer", cTask).val();

		$(cTask).removeClass('editMode');

		current.makeUnEditable();
		current.showEditButtons();

		var data = current.getDataForPost();
		if (!current.isNew) {
			data.action = "save";
		}

		$.post(current.actionUrl, data, function(data) {

			var tempIsNew = current.isNew;
			$('.contents', cTask).replaceWith(data);

			current.init();

			if (tempIsNew) {
                // if we're inserting
                if (current.beforeTaskId || current.afterTaskId) {
                    current.taskList.insertIntoList(current);
                }
				current.showMoveHandle();
				current.taskList.sortRefresh();
			}

		}, "html");
	};

	this.cancel = function() {

		$(cTask).removeClass('editMode');
		current.makeUnEditable();
		current.showShortSummary();

		$(".title h3", cTask).html(current.originalTitle);
		$(".description", cTask).html(current.originalDesc);
        $(".selectedTags", cTask).html(current.selectedTags);

		current.showEditButtons();
	};

	this.addInterest = function() {
		$.get("/tasks/addInterest", {
			id : current.id
		}, function(data) {
			$('.taskInterests', cTask).append(data);
			$('.addInterestButton', cTask).hide();
		});
	};

	this.removeInterest = function(parent) {
		$.get("/tasks/removeInterest", {
			id : current.id
		}, function(data) {
			parent.remove();
			$('.addInterestButton', cTask).show();
		});
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
	};

	this.makeEditable = function() {
		$(cTask).addClass('editMode');
		$('.title h3', cTask).attr('contentEditable', true).keypress(function(event) {
			return event.which != 13;
		});

		$('.description', cTask).attr('contentEditable', true);

        $('.selectedTags', cTask).html('<input class="tagContainer" type="text" name="selectedTags" value="' + (current.selectedTags ? current.selectedTags: "") + '"/>');
        $('.tagContainer', cTask).tagSuggest({
            url: '/tasks/gettags',
//            tags: ['bugs'],
            delay: 250
        });               
	};

	this.showMoveHandle = function() {
		$('.move', cTask).removeClass('hide');
	};

	this.createUploader = function() {
		$("input[type=file]", cTask).filestyle({
			image : "public/images/newAttachment.png",
			imageheight : 18,
			imagewidth : 25,
			width : 40
		}).change(function() {
			// console.log('upload file');

			var form = $(".uploader form", cTask).ajaxSubmit({
				dataType : "xml",
				success : function(data) {
					current.refresh();
				}
			});
		});
	};

}

function tasks() {

	var taskOrder = new Array();
	var current = this;

	this.init = function() {
		/*
		 * Initialise tasks
		 */
		$('.task').each(function() {
			var aTask = new task(this, current);

			aTask.init();
		});

		$("a.newButton").click(function() {
			current.addNew();
			return false;
		});
		current.setOrder();
		current.loadSort();

	};

	this.loadSort = function() {

		$('.tasks').sortable({
			items : ".task",
			placeholder : 'taskPlaceholder',
			handle : $(".move"),
			update : function(event, ui) {
				var current = 0;
				var toSwap = new Array();
				// console.log(taskOrder);
				$(".tasks .task .properties").each(function() {

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

	this.sortRefresh = function() {

		// console.log("sortable again");
		$('.tasks').sortable('destroy');

		current.loadSort();
		current.setOrder();
	};

	this.setOrder = function() {
		taskOrder = new Array();
		$(".tasks .task .properties").each(function() {
			taskOrder.push($("input[name='id']", this).val());
		});
	};

	this.addNew = function() {
		var clone = $("#newTask .task").clone();

		var newTask = new task(clone, current);
		newTask.init(true);

		$('.tasks').prepend(clone);
	};

    this.insertNew = function(baseTask, before) {
        var clone = $("#newTask .task").clone();

        var newTask = new task(clone, current);
		newTask.init(true);

        if (before) {
            $(baseTask.cTask).before(clone);
            newTask.beforeTaskId = baseTask.id;
        }
        else {
            $(baseTask.cTask).after(clone);
            newTask.afterTaskId = baseTask.id;
        }
    };

    this.moveToTop = function(baseTask) {
        var toSwap = new Array();
        // push baseTask to top of sort order
        toSwap.push(baseTask.id);
        for (var i = 0; i < taskOrder.length; i++) {
            if (taskOrder[i] != baseTask.id) {
                toSwap.push(taskOrder[i]);
            }
        }

        $.post('/tasks/sort', {
            order : toSwap
        }, function() {
            $(baseTask.cTask).hide().prependTo(".tasks").fadeIn(1000);
            $.scrollTo($(baseTask.cTask), 1000, {offset: -20});
            current.setOrder();            
        });
    };

    this.moveToBottom = function(baseTask) {
        var toSwap = new Array();

        var found = false;

        for (var i = 0; i < taskOrder.length; i++) {
            if (found) {
                toSwap.push(taskOrder[i]);
            }

            found |= (taskOrder[i] == baseTask.id);
        }

        // push baseTask to bottom of sort order
        toSwap.push(baseTask.id);

        $.post('/tasks/sort', {
            order : toSwap
        }, function() {
            $(baseTask.cTask).hide().appendTo(".tasks").fadeIn(1000);
            $.scrollTo($(baseTask.cTask), 1000, {offset: +20});
            current.setOrder();
        });
    };

    this.insertIntoList = function(baseTask) {
        var toSwap = new Array();

        var before = baseTask.beforeTaskId;

        var anchorTaskId = baseTask.beforeTaskId ? baseTask.beforeTaskId : baseTask.afterTaskId;

        for (var i = 0; i < taskOrder.length && taskOrder[i] != anchorTaskId; i++) {
            toSwap.push(taskOrder[i]);
        }

        if (!before) {
            toSwap.push(anchorTaskId);
        }

        toSwap.push(baseTask.id);

        if (toSwap.length > 1) {
            $.post('/tasks/sort', {
                order : toSwap
            }, function() {
                current.setOrder();
            });
        }
    }
}

$(document).ready(function() {

	theTasks = new tasks();
	theTasks.init();

	/*
	 * Make the tasks sortable. Only submit the reordered tasks.
	 */

});