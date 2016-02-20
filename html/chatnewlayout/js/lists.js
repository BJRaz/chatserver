/**
 *	Write something...
 */
Object.prototype.equals = function (obj) {
    if (typeof obj == "object") {
        if (this == obj)
            return true;
    }
    return false;
}


/*
 *
 */
function ListIterator(list) {
    var list = list;
    var index = 0;
    var currentItem = null;

    this.first = function () {
        currentItem = list.elementAt(0);
    }

    this.next = function () {
        if (!this.isDone()) {
            currentItem = list.elementAt(++index);
        }
    }
    this.isDone = function () {
        if (index == list.count())
            return true;
        return false;
    }
    this.item = function () {
        return currentItem;
    }
    this.reset = function () {
        currentItem = null;
        index = 0;
    }
}


/*
 *	LIST
 */
function List() {
    this.listItems = new Array();
    this.listeners = new Array();

    this.sort = function () {
        this.listItems.sort();
    }

    this.count = function () {
        return this.listItems.length;
    }

    this.clear = function () {
        this.listItems = new Array();
        this.notifyObservers();
    }

    this.add = function (obj) {
        this.listItems[this.listItems.length] = obj;
        this.notifyObservers();
    }

    this.remove = function (obj) {
        for (var i = 0; i < this.listItems.length; i++) {
            if (obj.equals(this.listItems[i])) {
                this.listItems.splice(i, 1);	// removes obj from array
                this.notifyObservers();
                return true;
            }
        }
        return false;
    }

    this.elementAt = function (index) {
        try {
            return this.listItems[index];
        } catch (e) {
            throw e;
        }
    }

    this.find = function (obj) {
        for (var i = 0; i < this.listItems.length; i++) {
            if (obj.equals(this.listItems[i]))
                return obj;
        }
    }

    this.getIterator = function () {
        return new ListIterator(this);
    }
}

List.prototype = new Observable;


function User(id, name) {
    this.id = id;
    this.name = name.toString();
}

User.prototype.equals = function (obj) {
    if (typeof obj == "object") {
        if (obj.constructor == User) {
            // Notice - parseInt and '+ ""' to make mozilla/firefox return true on comparison 
            return (parseInt(obj["id"]) == parseInt(this["id"]) && ((obj["name"] + "") == (this["name"] + ""))) ? true : false;
        }
    }
}


function UserList() {		// Extends List

    function sortUser(a, b) {
        // localeCompare JS version 1.5			
        return (a["name"]).localeCompare((b["name"]));
    }

    this.sort = function () {
        this.listItems.sort(sortUser);
    }

}

UserList.prototype = new List;


function UserView(model, elm) {
    var model = model;
    model.addObserver(this);

    var me = this;
    var element = elm;


    this.html;

    function render() {
        var table = document.createElement("TABLE");

        table.style.width = "100%";
        table.cellPadding = 0;
        table.cellSpacing = 0;
        table.className = "userTable";

        var tbody = document.createElement("TBODY");

        var iter = model.getIterator();

        /**
         *	Iterate through Users.
         */
        for (iter.first(); !iter.isDone(); iter.next()) {
            var tr = document.createElement("TR");
            var td = document.createElement("td");

            var user = iter.item();
            td.id = user["id"];
            td.handle = user["name"];

            if (window.user_id != user["id"]) {
                td.onclick = function () {
                    setAsPrivate(this.id, this.handle);
                }
            } else if (window.user_id == user["id"]) {
                td.onclick = function () {
                    alert("Du kan ikke snakke privat med dig selv .. ");
                }
            }


            var imgtable = document.createElement("table");
            //imgtable.id = user["id"];
            //imgtable.border = 0;
            imgtable.style.borderBottom = "1px solid black";

            var imgtbody = document.createElement("tbody");
            var imgtr = document.createElement("tr");
            var imgtd = document.createElement("td");
            imgtd.style.width = "45px";

            var imgtdtext = document.createElement("td");

            /**
             *	PRECACHING
             */
            //var img = window[user["name"]];


            if (window[user["id"]] == null) {
                window[user["id"]] = new Image();


                window[user["id"]].onerror = function () {

                    this.src = "pics/bonde1.gif";
                    this.onerror = null;

                }
                window[user["id"]].onload = function () {
                    //
                    //document.getElementById("messageBox_1").innerHTML += "Image loaded(" + user["name"] + ", " + this.src + ")<br/>";

                }

                window[user["id"]].src = "pics/" + user["name"] + ".jpg";
                window[user["id"]].style.height = "30px";
                window[user["id"]].style.width = "40px";
            }




            imgtdtext.innerHTML = user["name"] + " " + user["id"];
            imgtdtext.style.width = "100%";
            imgtdtext.style.textAlign = "left";

            //imgtd.innerHTML = "<img src='" + window[user["id"]].src + "'>";
            imgtd.appendChild(window[user["id"]]);
            (imgtd.firstChild.style.border = "1px solid black");

            imgtd.style.width = "40px";

            imgtr.appendChild(imgtd);
            imgtr.appendChild(imgtdtext);

            imgtbody.appendChild(imgtr);
            imgtable.appendChild(imgtbody);

            /*td1.className = "userImage";
             td1.innerHTML = "<img onerror='this.src=\"pics/bonde1.gif\";this.onerror = null' src='pics/" + iter.item()["name"] + ".jpg' style='height:30px'>";
             
             td2 = document.createElement("td");
             
             td2.id = iter.item()["id"];
             td2.name = iter.item()["name"];
             
             td2.onclick = function() {
             setAsPrivate(this.id, this.name);	
             }
             
             td2.appendChild(document.createTextNode(iter.item()["name"]));
             tr.appendChild(td1);						*/
            td.appendChild(imgtable);
            tr.appendChild(td);
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);

        if (element.firstChild)
            element.replaceChild(table, element.firstChild);
        else
            element.appendChild(table);
    }

    this.setElement = function (elm) {
        element = elm;
    }

    this.update = function (obj) {
        model.sort();
        render();
    }

    render();

}



/******************************************************************/



function Message(id, handle, message) {
    this.id = id;
    this.handle = handle;
    this.message = message;
}

function PrivateMessage(id, handle, message) {
    this.id = id;
    this.handle = handle;
    this.message = message;
}

function MessageView(model, elm) {

    var model = model;
    model.addObserver(this);

    var me = this;
    var element = elm;

    this.html;

    function render() {

        var table = document.createElement("TABLE");
        table.border = 0;
        table.cellPadding = 2;
        table.cellSpacing = 2;

        table.className = "messageTableView";

        var tbody = document.createElement("TBODY");

        table.appendChild(tbody);

        if (element.firstChild)
            element.replaceChild(table, element.firstChild);
        else
            element.appendChild(table);

        me.html = table;

    }

    this.update = function (listobj) {
        try {
            var tbody = me.html.getElementsByTagName("TBODY");
            tr = document.createElement("TR");
            td = document.createElement("TD");
            td.vAlign = "bottom";
            td1 = document.createElement("TD");


            var obj = model.elementAt(model.count() - 1);
            if (obj == null) {
                element.innerHTML = "";
                render();
                return;
            }
            var handle = obj["handle"]
            var text = obj["message"];

            /**
             *	PRE CACHING
             */
            //alert(id + " "  + handle + " " + event + " " + msg)
            //var img = window[id];
            var imagesrc = "pics/" + handle + ".jpg"

            if (window[obj["id"]] == null) {
                window[obj["id"]] = new Image();
                //window[id] = img;

                window[obj["id"]].onerror = function () {
                    //document.getElementById("messageBox_1").innerHTML += ("IMAGE LOAD ERROR " + this.src);
                    this.src = "pics/bonde1.gif";
                    this.onload = null;
                }

                window[obj["id"]].onload = function () {
                    //document.getElementById("messageBox_1").innerHTML += ("IMAGE LOADED " + this.src);				
                }
                window[obj["id"]].src = imagesrc;

            }

            var aTable = document.createElement("table");
            aTable.border = 0;
            aTable.className = "handleTable";
            var aTbody = document.createElement("tbody");
            var aTr = document.createElement("tr")
            var aTr2 = document.createElement("tr")
            var aTd = document.createElement("td")
            aTd.align = "center";
            var aImg = document.createElement("img");
            //aImg.src = img.src;
            aTd.className = 'useravatar';
            // PRE CONDITION - window[id] must exist...					
            aTd.innerHTML = "<img src='" + (window[obj["id"]]).src + "'>";


            /*var aTd2 = document.createElement("td")
             aTd2.align = "center";
             aTd2.className = "useravatar";
             aTd2.innerHTML = handle;
             */
            aTr.appendChild(aTd);
            //aTr2.appendChild(aTd2);
            aTbody.appendChild(aTr);
            //aTbody.appendChild(aTr2);
            aTable.appendChild(aTbody);

            /*	
             var html = "<table border='0' class='handletable'>";
             html += "<tr><td align='center'><img class='useravatar' src='" + img.src + "'></td></tr>";
             //html += "<tr><td align='center'><img class='useravatar' src='pics/" + img.src + "'></td></tr>";
             html += "<tr><td align='center'  class='handle'>" + handle + "</td></tr>";
             html += "</table>";
             */

            var className = "publicMsg";
            if (obj.constructor == PrivateMessage)
                className = "privateMsg";
            /*else
             td1.className = "publicMsg";
             */
            var talk = "<table border='0' cellpadding='0' cellspacing='0'>";
            talk += "<tr><td><img style='width:20px;height:20px;' src='pics/left_top.gif'></td><td style='background-color:white;border-top:1px solid black;'>&nbsp;</td><td><img style='width:20px;height:20px;' src='pics/right_top.gif'></td></tr>";
            talk += "<tr><td style='background-color:white;border-left:1px solid black;'>&nbsp;</td><td class='" + className + "' align='left'>[" + handle + "] " + text + "</td><td style='background-color:white;border-right:1px solid black;'>&nbsp;</td></tr>";
            talk += "<tr><td><img style='width:20px;height:20px;' src='pics/left_lower.gif'></td><td style='background-color:white;border-bottom:1px solid black;'>&nbsp;</td><td><img style='width:20px;height:20px;' src='pics/right_lower.gif'></td></tr>";
            talk += "</table>";


            //td.appendChild(document.createTextNode(text));
            td.appendChild(aTable);//= html;


            tr.appendChild(td);
            td1.innerHTML = talk;

            td1.vAlign = "top";
            tr.appendChild(td1);

            tbody.item(0).appendChild(tr);
            tr.scrollIntoView();
            obj = null;
        } catch (e) {
            alert(e);
        }
    }

    render();
}

/********************/

function MessageViewSimple(model, elm) {

    var model = model;
    model.addObserver(this);

    var me = this;
    var element = elm;
    var count = 0;


    this.html;

    function render() {

        var table = document.createElement("TABLE");
        table.border = 0;
        table.cellPadding = 0;
        table.cellSpacing = 0;
        with (table) {
            style.width = "100%";
        }
        //table.className = "simpleMessagesView";

        var tbody = document.createElement("TBODY");

        table.appendChild(tbody);

        if (element.firstChild)
            element.replaceChild(table, element.firstChild);
        else
            element.appendChild(table);

        me.html = table;

    }

    this.update = function (listobj) {
        try {
            var tbody = me.html.getElementsByTagName("TBODY");
            tr = document.createElement("TR");
            td = document.createElement("TD");

            td.vAlign = "bottom";

            // get last element from model
            var obj = model.elementAt(model.count() - 1);
            if (obj == null) {									// model is cleared
                //element.innerHTML = "";
                //render();
                // CLEAR TABLE 
                var nodeList = tbody.item(0).childNodes;
                for (var i = nodeList.length - 20; i >= 0; i--) {
                    tbody.item(0).removeChild(nodeList.item(i));
                }
                return;
            }
            var handle = obj["handle"]
            var text = obj["message"];

            /**
             *	PRE CACHING
             */
            //alert(id + " "  + handle + " " + event + " " + msg)
            //var img = window[id];
            var imagesrc = "pics/" + handle + ".jpg"

            /*if(window[obj["id"]] == null) {
             window[obj["id"]] = new Image();
             //window[id] = img;
             
             window[obj["id"]].onerror = function() {
             //document.getElementById("messageBox_1").innerHTML += ("IMAGE LOAD ERROR " + this.src);
             this.src = "pics/bonde1.gif";
             this.onload = null;				
             }
             
             window[obj["id"]].onload = function() {
             //document.getElementById("messageBox_1").innerHTML += ("IMAGE LOADED " + this.src);				
             }
             window[obj["id"]].src = imagesrc;	
             
             }
             */
            var className = "publicMsg";
            if (obj.constructor == PrivateMessage)
                className = "privateMsg";


            var aTable = document.createElement("table");
            aTable.border = 0;

            aTable.cellPadding = 0;
            aTable.cellSpacing = 0;
            aTable.className = "simpleMessagesView";
            var aTbody = document.createElement("tbody");
            var aTr = document.createElement("tr")

            var aTd = document.createElement("td")
            aTd.align = "center";
            //aTd.className ='useravatar';


            // PRE CONDITION - window[id] must exist...					
            //aTd.innerHTML = "<img src='" + imagesrc + "'>";							
            aTd.innerHTML = "<span>[" + handle + "]</span>&nbsp;";

            var aTd2 = document.createElement("td")
            aTd2.align = "left";
            //aTd.className ='useravatar';
            aTd2.style.width = "100%";
            aTd2.className = className;
            aTd2.innerHTML = "<span>" + text + "</span>&nbsp;";

            aTr.appendChild(aTd);
            aTr.appendChild(aTd2);
            aTbody.appendChild(aTr);

            aTable.appendChild(aTbody);

            td.appendChild(aTable);

            tr.appendChild(td);

            tbody.item(0).appendChild(tr);
            if (tr.scrollIntoView)
                tr.scrollIntoView();
            obj = null;
        } catch (e) {
            alert(e);
        }
    }

    render();
}