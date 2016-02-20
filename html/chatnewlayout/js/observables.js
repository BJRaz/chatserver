// Observer & Observable

function Observable() { 		/// Object

    this.listeners = null; 	// Needs override...

    this.addObserver = addObserver;
    this.notifyObservers = notifyObservers;
}


function addObserver(obj) {
    this.listeners[this.listeners.length] = obj;
}

function notifyObservers() {
    for (i = 0; i < this.listeners.length; i++) {
        this.listeners[i].update(this);
    }
}	