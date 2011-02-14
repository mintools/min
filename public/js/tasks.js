function task(taskElement, taskList, locked) {

	var cTask = taskElement;
	var current = this;
	var dialog = new dialogObj();
	var expanded = false;

	this.taskList = taskList;
	this.isNew = false;
	this.isEditMode = false;
	this.cTask = cTask;

	/**
	 * expand
	 */

	this.init = function() {
		/*
		 * Variables
		 */
		current.id = $(".task", cTask).attr("data-id");
		current.editMode = $(".task", cTask).attr("data-editMode") == 'true';
		current.isNew = $(".task", cTask).attr("data-isNew") == 'true';

		if (current.editMode) {
			$(cTask).addClass("editMode");
		} else {
			$(cTask).removeClass("editMode");
		}

		/*
		 * Events
		 */
        if (!locked) {
            $(cTask).mouseenter(function(e) {
                if (!current.isNew) {
                    $(".hoverAction", cTask).show();
                }
            });

            $(cTask).mouseleave(function(e) {
                if (!current.isNew) {
                    $(".hoverAction", cTask).hide();
                }
            });
        }

		/*
		 * Plugins
		 */
		$('.tagContainer', cTask).tagSuggest({
			delay : 250
		});

		$(".multi", cTask).MultiFile();

        /*
           * Buttons
           */
        $(".showHistoryButton", cTask).click(function(e) {
            current.showHistory(this);
        });

		$('.title,.description,.comments', cTask).click(function() {
			if (!current.editMode) {
				current.toggle();
			}
			return true;
		});

		$(".editButton", cTask).click(function() {
			current.edit();
			return false;
		});

		$(".deleteButton", cTask).click(function() {
			current.confirmDelete();
			return false;
		});

		$(".taskForm", cTask).submit(function() {
			current.save();
			return false;
		});

		$(".saveButton", cTask).click(function() {
			current.save();
			return false;
		});

		$(".cancelButton", cTask).click(function() {
			if (current.isNew) {
				current.remove();
			} else {
				current.cancel();
			}
			return false;
		});

		$(".attachments .attachment img.delete", cTask).click(function() {
			current.confirmDeleteAttachment($(this).parents('.attachment').first());
			return false;
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

		$(".attachments .attachment .thumb img[rel]", cTask).overlay({
			mask : {
				color : '#ebecff',
				loadSpeed : 200,
				opacity : 0.9
			},
			closeOnClick : true
		});
	};

	this.toggle = function() {
		current.expand(!current.expanded);
	};

	this.expand = function(expand) {
		if (expand) {
			$(cTask).addClass('expandMode');
			current.showLongSummary();
			current.expanded = true;
		} else {
			$(cTask).removeClass('expandMode');
			current.showShortSummary();
			current.expanded = false;
		}
	};

	this.edit = function() {

		$.get('/tasks/show', {
			id : current.id,
			editMode : true
		}, function(data) {
			$(".task", cTask).replaceWith(data);
			current.expand(true);
			current.init();
		});
	};

	/**
	 * Remove everything except breaks
	 */
	this.filterHtml = function(element) {

		var html = $(element).html().replace(/<br>/g, '-----!!!!!Break!!!!!-----');
		html = $.trim(html);
		html = $('<div>' + html + '</div>').text();
		html = html.replace(/-----!!!!!Break!!!!!-----/g, '<br/>');

		return html;
	};

	this.save = function() {
		var wasNew = current.isNew;

		var options = {
			success : function(data) {
				$('.task', cTask).replaceWith(data);
				current.init();

				if (wasNew) {
					// if we're inserting
					if (current.beforeTaskId || current.afterTaskId) {
						current.taskList.insertIntoList(current);
					}
					current.taskList.sortRefresh();
				}

				current.expand(false);
				taskList.loadSort();
			},
			type : 'post',
			timeout : 3000
		};

		$(".taskForm", cTask).ajaxSubmit(options);
	};

	this.cancel = function() {
		$.get('/tasks/show', {
			id : current.id
		}, function(data) {
			$(".task", cTask).replaceWith(data);
			current.init();
			current.expand(false);
			taskList.loadSort();
		});
	};

	this.confirmDelete = function() {
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
		$.get("/tasks/delete", {
			taskId : current.id
		}, function(data) {
			current.remove();
		});
	};

	this.confirmDeleteAttachment = function(attachment) {
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
		// console.log(attachment);

		var attId = $('input[name=attachmentId]', attachment).val();

		$.get("/tasks/deleteAttachment", {
			id : attId
		}, function(data) {
			attachment.remove();
		});
	};

	this.remove = function() {
		$(cTask).fadeOut(300, function() {
			$(this).remove();
		});
	};

	this.showShortSummary = function() {
		$('.description ', cTask).addClass("descriptionSummary");
	};

	this.showLongSummary = function() {
		$('.description ', cTask).removeClass("descriptionSummary");
	};

    this.showHistory = function(element) {
        var day = new Date();
        var id = day.getTime();
        var URL = "/tasks/getRevisions?taskId=" + current.id;

        eval("page" + id + " = window.open(URL, '" + id + "', 'toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=0,width=500,height=500,left = 470,top = 200');");
	};
}

function tasks(options) {

    options = $.extend(true, {
        tasksSel: '.tasks',
        locked: false
	}, options);

	var taskOrder = [];
	var current = this;

    var tasksElement = $(options.tasksSel);

	this.init = function() {
		/*
		 * Initialise tasks
		 */
		$('.taskContainer').each(function() {
			var aTask = new task(this, current, options.locked);

			aTask.init();
		});

        // clear previously added listeners first
        $("a.newButton").unbind();
		$("a.newButton").click(function() {
			current.addNew();
			return false;
		});
		current.setOrder();
		current.loadSort();

	};

	this.loadSort = function() {

        if (!options.locked) {
            tasksElement.sortable({
                items : '.taskContainer',
                placeholder : 'taskPlaceholder',
                handle : $(".move"),
                update : function(event, ui) {
                    var current = 0;
                    var toSwap = [];
                    // console.log(taskOrder);
                    $('.taskContainer', tasksElement).each(function() {

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
        }
	};

	this.sortRefresh = function() {

		// console.log("sortable again");
		tasksElement.sortable('destroy');

		current.loadSort();
		current.setOrder();
	};

	this.setOrder = function() {
		taskOrder = [];
		$(".task", tasksElement).each(function() {
			taskOrder.push($(this).attr("data-id"));
		});
	};

	this.addNew = function() {
		var clone = $("#newTask .taskContainer").clone();

		var newTask = new task(clone, current, options.locked);
		newTask.init();

		tasksElement.prepend(clone);
	};

	this.insertNew = function(baseTask, before) {
		var clone = $("#newTask .taskContainer").clone();

		var newTask = new task(clone, current, options.locked);
		newTask.init();

		if (before) {
			$(baseTask.cTask).before(clone);
			newTask.beforeTaskId = baseTask.id;
		} else {
			$(baseTask.cTask).after(clone);
			newTask.afterTaskId = baseTask.id;
		}
	};

	this.moveToTop = function(baseTask) {
		var toSwap = [];
		// push baseTask to top of sort order
		toSwap.push(baseTask.id);
		for ( var i = 0; i < taskOrder.length; i++) {
			if (taskOrder[i] != baseTask.id) {
				toSwap.push(taskOrder[i]);
			}
		}

		$.post('/tasks/sort', {
			order : toSwap
		}, function() {
			$(baseTask.cTask).hide().prependTo(".tasks").fadeIn(1000);
			$.scrollTo($(baseTask.cTask), 1000, {
				offset : -20
			});
			current.setOrder();
		});
	};

	this.moveToBottom = function(baseTask) {
		var toSwap = [];

		var found = false;

		for ( var i = 0; i < taskOrder.length; i++) {
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
			$.scrollTo($(baseTask.cTask), 1000, {
				offset : +20
			});
			current.setOrder();
		});
	};

	this.insertIntoList = function(baseTask) {
		var toSwap = [];

		var before = baseTask.beforeTaskId;

		var anchorTaskId = baseTask.beforeTaskId ? baseTask.beforeTaskId : baseTask.afterTaskId;

		for ( var i = 0; i < taskOrder.length && taskOrder[i] != anchorTaskId; i++) {
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
	};
}