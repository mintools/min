
function enhancedDomReady(func){
	function bodyReady(){
		if(document.body){
			clearInterval(checkBody);
			if(testUserDevice()){
				enhanceDocument();
				//forward functions to domReady event with available library
				if(jQuery){$(function(){func();});}
				else indepDomReady(function(){func();});
			}
		}
	}
	var checkBody = setInterval(bodyReady, 10);
}

//this gets called after test is passed
function enhanceDocument(){
	//add class to body "enhanced" for css scoping (makes sure it's not already there)
	if (!/\benhanced\b/.exec(document.body.className)){
		document.body.className += ' enhanced';
	}
	//function to enable enhanced stylesheets (enables alt stylesheet links with class name "enhanced)
	var allLinks = document.getElementsByTagName('link');
	for(i=0; i<allLinks.length; i++){
		//if the link has a class of "basicNoCascade", disable it
		if (/\bbasicNoCascade\b/.exec(allLinks[i].className)){
			allLinks[i].disabled = true;
		}
		//if the link has a class of "enhanced", enable it
		if (/\benhanced\b/.exec(allLinks[i].className)){
			allLinks[i].disabled = true; //opera likes to have it toggled
			allLinks[i].disabled = false;
		}
	}
}

//test function - runs a variety of tests to weed out any unqualified browsers from enhanced functionality
var testUserDevice = function (){	
	if(readCookie('enhanced')){
		enhanceDocument();
		return true;
	}
	if(document.getElementById && document.createElement){
		var newDiv = document.createElement('div');
		document.body.appendChild(newDiv);
		newDiv.style.visibility = 'hidden';
		newDiv.style.width = '20px';
		newDiv.style.padding = '10px';
		var divWidth = newDiv.offsetWidth;
		if(divWidth != 40) {document.body.removeChild(newDiv); return false;}
		newDiv.style.position = 'absolute';
		newDiv.style.left = '10px';
		var leftVal = newDiv.offsetLeft;
		if(leftVal != 10) { document.body.removeChild(newDiv); return false;}
		var newInnerDiv = document.createElement('div');
		newInnerDiv.style.width = '5px';
		newInnerDiv.style.cssFloat = 'left';
		newInnerDiv.style.styleFloat = 'left';
		newDiv.appendChild(newInnerDiv);
		var secondInnerDiv = newInnerDiv.cloneNode(true); 
		newDiv.appendChild(secondInnerDiv);
		var newInnerDivTop = newInnerDiv.offsetTop;
		var secondInnerDivTop = secondInnerDiv.offsetTop;
		if(newInnerDivTop != secondInnerDivTop) { document.body.removeChild(newDiv); return false;}
		newDiv.innerHTML = '<ul><li style="width: 5px; float: left;">test</li><li style="width: 5px; float: left;clear: left;">test</li></ul>';
		var top1 = newDiv.getElementsByTagName('li')[0].offsetTop;
		var top2 = newDiv.getElementsByTagName('li')[1].offsetTop;
		if(top1 == top2){return false;}
		newDiv.innerHTML = '<div style="height: 20px;"></div>';
		newDiv.style.padding = '0';
		newDiv.style.height = '10px';
		newDiv.style.overflow = 'auto';
		var newDivHeight = newDiv.offsetHeight;
		if(newDivHeight != 10){document.body.removeChild(newDiv); return false;}
		newDiv.innerHTML = '<div style="line-height: 2; font-size: 10px;">Te<br />st</div>';
		newDiv.style.padding = '0';
		newDiv.style.height = 'auto';
		newDiv.style.overflow = '';
		var newDivHeight = newDiv.offsetHeight;
		if(newDivHeight > 40){document.body.removeChild(newDiv); return false;}
		if(window.onresize == false){document.body.removeChild(newDiv); return false;}
		if(!window.print){ document.body.removeChild(newDiv); return false;}
		if(window.clientInformation && window.opera){document.body.removeChild(newDiv); return false;}
		document.body.removeChild(newDiv);
		enhanceDocument();
		createCookie('enhanced', 'true');
		return true;
	}
	else{return false; }
}

/*Utilities for this script*/


/* library-independent domReady script (used if jQuery is not available) Note: can't find script source.. tba */
function indepDomReady(func){
	if (!window.__load_events){
		var init = function (){
				if (arguments.callee.done) return;
				arguments.callee.done = true;
				if (window.__load_timer){
					clearInterval(window.__load_timer);
					window.__load_timer = null;
				}
				for (var i=0;i < window.__load_events.length;i++){
					window.__load_events[i]();
				}
				window.__load_events = null;
		};
		if (document.addEventListener){
			document.addEventListener("DOMContentLoaded", init, false);
		}
		if (/WebKit/i.test(navigator.userAgent)){
			window.__load_timer = setInterval(function(){
				if (/loaded|complete/.test(document.readyState)){
					init();
				}
			}
			, 10);
		}
	window.onload = init;
	window.__load_events = [];
	}
	window.__load_events.push(func);
}

//cookie functions from quirksmode
function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}
function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}