function unlinkOrDeleteDialog(message,fragment,showUnlinkBtn, unlinkFunction, deepDeleteFunction) {

    var body = '<p>' + message + '</p>' + fragment;

    $("#message-dialog .content").html(body);


    var buttons = {
        "Cancel": function() {
            $(this).dialog("close");
        }
    };

    if(showUnlinkBtn){

        buttons["Just Unlink"] =  function() {
            unlinkFunction();
            $(this).dialog("close");

        };

        buttons["Deep Delete"] = function() {
            deepDeleteFunction();
            $(this).dialog("close");
        }

    }else{
        buttons["Delete"] = function() {
            deepDeleteFunction();
            $(this).dialog("close");
        }
    }


    $('#message-dialog').dialog('option', 'buttons',buttons);
    $('#message-dialog').dialog('open');

}


function message(message, onConfirm) {
    $("#message-dialog span.content").html(message);
    $('#message-dialog').dialog('option', 'buttons', { "Cancel": function() {
        $(this).dialog("close");
    }, "Ok": function() {
        onConfirm();
        $(this).dialog("close");
    } });
    $('#message-dialog').dialog('open');
}

function bindReturn(field, toClick, event) {
    if (event.keyCode == 13) {
        $(toClick).click();
        return false;
    }
}