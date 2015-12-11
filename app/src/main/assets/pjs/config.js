console.log("hello from pjs");




//load pjs file in V8 manager
var PjsList = function(){
    this.list = [];
};
PjsList.prototype = {
    _addPjs : function(pjs){
        this.list[pjs.key] = pjs;
    },

    loadPjs : function(path){
        pjs.load(path);
    },

    callFun : function(key,funName,args){
        this.loadPjs(key);
        return this.list[key].callFun(funName,args);
    },

    _isPjsExists : function(key){
        var js = this.list[key];
        if(typeof js === 'undefined') {
            return false;
        }
        else {
            return true;
        }
    }

};

//for one pjs file object
var Pjs = function(key,script){
    this.key = key;
    this.script = new script();
};
Pjs.prototype = {
    callFun : function(name,args){
        if(this.isValidFun(name)){
            return this.script[name].apply(this.script,args);
        }else{
            console.log("func not valid");
        }
    },
    isValidFun : function(name){
        return this.script[name] !== undefined;
    }
};





//connected webscoket list manager
var WsList = function(){
    this.list = {};
};
WsList.prototype = {
    add : function(){
        var key = this.getFreeKey();
        var ws = new Ws();
        ws.key = key;
        this.list[key] = ws;
        return ws;
    },

    delete : function(ws){
        var dl = delete this.list[ws.key];
    },

    getFreeKey : function(){
        for(var i = 0; i <= this.getLength(); i++){
            if(this.list[i+""] === undefined){
                return i + "";
            }
        }
    },

    isWsExits : function(ws){
        return !(this.list[ws.key] === undefined);
    },

    getLength : function(){
        return Object.keys(this.list).length;
    }
};





var wsList = new WsList();
var pjsList = new PjsList();

pjsList.loadPjs("pathik.pjs");


//for one webscoket
var Ws = function(){
    this.onopen = function(){
        console.log("WS-OPEN:"+this.key);
        //w.onpjsList.callFun("test.pjs","hello",["pathik","devani"]);
    };

    this.onpong = function(){
        console.log("WS-PONG:"+this.key);
    };

    this.onclose = function(){
        console.log("WS-CLOSE:"+this.key);
        wsList.delete(this);
    };

    this.onmessage = function(data){
        console.log("WS-MESSAGE:"+this.key + ":" + JSON.stringify(data));
        var ans = pjsList.callFun(data.file,data.fun,data.args);

        var sendData = {
            "count" : data.count,
            "data" : ans
        };


        console.log("WS-MESSAGE-SEND:"+this.key + ":" + JSON.stringify(sendData));
        this.send(JSON.stringify(sendData));
    };
};

