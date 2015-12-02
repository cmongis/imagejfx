Array.prototype.remove = function (from, to) {
	console.log(this,from,to);
	
	var rest = this.slice((to || from) + 1 || this.length);
	this.length = from < 0 ? this.length + from : from;
	return this.push.apply(this, rest);
};


angular.module("RRG", [])
	.factory("TypicalEntry", function () {
		return new TypicalEntry()
	})



function LetterViewModel(char, index) {
	var self = this;

	self.char = char;
	self.region = undefined;
	self.index = index;
	self.isBegin = function () {
		if (self.region === undefined) return false;
		return self.region.begin = index;
	}

	self.class = "";

	self.getClass = function () {
		if (self.region == undefined) return ""
		return "selected";
	};

	self.unselect = function () {
		self.region = undefined;
		self.class = "";
	};

	self.select = function (region) {
		self.region = region;
		self.class = region.class;
	};

	self.belongs = function (region) {
		if (self.index >= region.begin && self.index <= region.end) {
			self.select(region);
			return true;
		} else return false;
	};
}