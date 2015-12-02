// Model representing the text, the selected regions
// and the parameters associated to each region
function TypicalEntry() {

	var self = this;

	// typical text that needs to be parsed
	self.text = "";


	// setting the typical entry
	self.setEntry = function (entry) {

		self.text = entry;
		self.variableRegions = [];
		//self.updateLetters();

	};

	// regions in the entry that can change (selected by the used
	self.variableRegions = [];

	// add a variable region to the list
	self.addVariableRegion = function (begin, end, flags) {

		//console.log("Adding region from " + begin + " to " + end);


		// create a new object with the default policy
		var variableRegion = new VariableRegion(self.text, begin, end, flags);

		// pushing it to the list
		self.variableRegions.push(variableRegion);

		// returns it
		return variableRegion;
	};
	self.result = "";

	// get the list of variable regions
	self.getVariableRegions = function () {
		return self.variableRegions;
	};

	self.removeRegion = function (region) {
		//console.log("removing region");
		var index = self.variableRegions.indexOf(region);
		//console.log("indexOf(region)", index);
		//self.variableRegions[index] = undefined;
		if (self.selectedRegion === region) self.selectedRegion = undefined;
		self.variableRegions.remove(index);
		self.updateLetters();


	};

	self.currentRegion = undefined;

	self.selectedRegion = undefined;

	self.selectRegion = function (region) {
		self.selectedRegion = region;
	}


	self.setText = function (text) {

		self.text = text;
		console.log(text.length);
		for (var i = 0; i != text.length; i++) {
			if (self.letters[i] == undefined) {
				self.letters[i] = new Letter(text[i], i);
			}
			self.letters[i].char = text[i]
		}

		if (self.letters.length > text.length) {
			self.variableRegions.forEach(function (region) {
				if (region.end > text.length) {
					self.removeRegion(region);
				}
			});
		}

		self.letters.length = text.length;
	};

	/*
		Selection Model
	*/

	function Letter(char, index) {

		var self = this;
		self.char = char;
		self.index = index;
		self.region = undefined;

		self.isBegin = function () {
			if (self.region === undefined) return false;
			return self.region.begin = index;
		}

		self.class = "";

		self.getClass = function () {
			if (self.region == undefined) return "";
			return "selected";
		};

		self.unselect = function () {
			self.region = undefined;
			self.class = "";
		};

		self.select = function (region) {
			//console.log("selecting", self.char, region);
			self.region = region;
			self.class = region.class;
		};

		self.belongs = function (region) {
			//console.log(region.begin, region.end, self.index);
			if (self.index >= region.begin && self.index <= region.end) {
				self.select(region);
				return true;
			} else return false;
		};

	};


	self.letters = [];

	self.isSelectionStarted = false;


	self.blockSelection = false;

	self.startSelection = function (index) {
		self.isSelectionStarted = true;
		self.currentRegion = self.addVariableRegion(index, index);
	};

	self.elongateRegion = function (index) {

		var letterRegion = self.letters[index].region;

		if (letterRegion == undefined) {
			if (self.currentRegion == undefined) self.startSelection(index);

			if (index < self.currentRegion.begin) {
				self.currentRegion.begin = index;
			} else {
				self.currentRegion.end = index;
			}

			self.letters[index].select(self.currentRegion);

		} else if (letterRegion != self.currentRegion) {

			self.blockSelection = true;
			self.stopSelection();

		}
	};

	self.stopSelection = function () {
		if (self.currentRegion == undefined) return;
		self.isSelectionStarted = false;
		self.blockSelection = false;
		self.selectRegion(self.currentRegion);
		self.currentRegion = undefined;
		self.updateLetters();

	};

	self.updateLetters = function () {

		self.letters.forEach(function (letter) {
			letter.unselect();
		});

		self.variableRegions.forEach(function (region) {
			self.letters.forEach(function (letter) {
				letter.belongs(region);
			});
		});



	};
	/*
		Entry compilation
	*/


	// compile the regions into a regular expression
	self.compile = function () {
		var entry = self.text;
		var result = "";
		var regions = self.getVariableRegions();
		var currentRegion = undefined;

		for (var i = 0; i < entry.length; i++) {

			// cheching if the letter belongs to a region
			for (var j in regions) {
				var region = regions[j];
				if (i == region.begin) {

					currentRegion = region;
				}
			}
			if (currentRegion != undefined) {
				if (currentRegion.begin == i) result += currentRegion.compile();
				else if (currentRegion.end == i) {
					currentRegion = undefined;

				}

			} else result += self.escapeChar(entry[i]);
		}
		result = self.postProcess(result);
		self.result = result;
		self.re = new RegExp(result);
		return result;
	};

	self.decomposition = undefined;
	
	// decompose the entry into parts usable for pattern display.
	// since a typical is usually an alterance of text and
	// variable region, two functions are used to translate
	// the region and the text into similar objects so the view
	// can display them easily.
	self.decompose = function (transformRegion, transformText) {
		
		if(self.decomposition != undefined) return self.decomposition;
		
		// default transformation functions
		if (transformRegion == undefined) {
			transformRegion = function (region) {
				return region.compile(new RegionToTextCompiler(region));
			};
		}
		if (transformText == undefined) {
			transformText = function (text) {
				return {
					text: text
				};
			};
		}

		// entry of the typical region
		var entry = self.text;
	
		// list of object representing each part of the view
		var result = [];
	
		// the list of variable regions
		var regions = self.getVariableRegions();
	
		// sorting the regions by index of beginning in the string
		regions = regions.sort(function (r1, r2) {
			return r1.begin - r2.begin
		});
	
		// variable used to add the text not belonging to a region
		var currentText = "";
	
		// for each letter of typical entry
		for (var i = 0; i < entry.length; i++) {
			
			// checking if this index is the beginning of a region :
			// for each region
			for (var j in regions) {
				
				// the current region
				var region = regions[j];
				
				// if it is
				if (region.begin == i) {
					
					// the currentText is transform into a view object
					if (currentText != "") {
						result.push(transformText(currentText));
						
						// and erased
						currentText = "";
					}

					// transforming into a view object
					var decomposedRegion = transformRegion(region);

					// the transform function can return an array
					// of view object, if it's the case, they
					// added one by one to the result
					if (decomposedRegion.constructor == Array) {
						decomposedRegion.forEach(function (part) {
							result.push(part);
						});
					}
					// if not, we just push the view object
					else {
						result.push(decomposedRegion);
					}
					
					// advancing the index to the end of the region
					i = region.end + 1;

					continue;

				}
			}
			// in case we go to far
			if (i < entry.length)
				currentText += entry[i];

		}
		// if the loop reach an end without encounting region
		if (currentText != "") {
			result.push(transformText(currentText));
		}
		
		self.decomposition = result;
	
		// returning the result
		return result;
	};

	self.clearDecomposition = function() {
		self.decomposition = undefined;
	};


	// search through text and return
	// an array of objects representing
	// the identified parts (and non identified)
	// of the input string
	self.search = function (text) {

		var result = {};
		var regionNameList = [];
		var regionList = self.variableRegions.sort(function (r1, r2) {
			return r1.begin - r2.begin;
		});
		regionList.forEach(function (region, index) {
			var regionName = region.name;

			if (regionName == undefined || regionName == "") regionName = "no name " + index;
			result[regionName] = "(*.*)";
			regionNameList.push(regionName);
		});

		if (self.re == undefined) return result;

		var matches = self.re.exec(text);
		if (matches == null) return result;
		else {
			matches.shift();

			matches.forEach(function (match, index) {
				result[regionNameList[index]] = match;

			});


		}
		return result;
	}

	/*
		Compilation configuration and post process
	*/


	self.ADD_BEGIN = false;
	self.ADD_END = false;


	self.reSpecialChar = /[\.\\?+!\/\\]/;

	self.escapeChar = function (char) {
		if (self.reSpecialChar.test(char)) {
			return "\\" + char;
		} else return char;
	};

	self.postProcess = function (result) {
		var escapeSpecialCharacters = function (result) {

			var specialCharacters = ["\\", ".", "?", "+", "/"];

			specialCharacters.forEach(function (char) {
				result = result.replace(char, "\\" + char);
			});

			return result;
		};
		var prepareForJava = function (result) {
			return result;
		};
		var addBegin = function (result) {
			if (self.ADD_BEGIN) {
				return "^" + result;
			}
			return result;
		};
		var addEnd = function (result) {
			if (self.ADD_END) {
				result += "$";
			}
			return result;
		};


		var postProcessess = [prepareForJava, addBegin, addEnd];

		postProcessess.forEach(function (process) {
			result = process(result);
		});
		return result;
	}
}



/*

	Region Model

*/


VariableRegion.CHAR_POL_MIXED = "mixed";
VariableRegion.CHAR_POL_SEQUENCE = "sequence";
VariableRegion.CHAR_POL_ANYTHING = "anything";
VariableRegion.SIZE_CONSTANT = "constant";
VariableRegion.SIZE_VARIABLE = "variable";
VariableRegion.RE_LETTER = /\w/;
VariableRegion.RE_NUMBER = /\d/;
VariableRegion.RE_SPACE = /\s/;
VariableRegion.TYPE_LETTER = "char";
VariableRegion.TYPE_NUMBER = "number";
VariableRegion.TYPE_SPACE = "space";

VariableRegion.SYMBOL_TABLE = [
	{
		symbol: "\\w",
		re: VariableRegion.RE_LETTER,
		type: VariableRegion.TYPE_LETTER,
		name: "Letter"
	}
	, {
		symbol: "\\d",
		re: VariableRegion.RE_NUMBER,
		type: VariableRegion.TYPE_NUMBER,
		name: "Number"
	}
	, {
		symbol: "\\s",
		re: VariableRegion.RE_SPACE,
		type: VariableRegion.TYPE_SPACE,
		name: "Space"
	}

];

VariableRegion.COLORS = ["red", "yellow", "blue", "green", "purple", "cyan"];
VariableRegion.COLOR_COUNT = 0;




function RegExpSymbol(symbol, count) {
	var self = this;
	self.symbol = symbol;
	self.name = symbol.name;
	self.count = count;
	self.increment = function () {
		self.count++
	};
	self.stringSymbol = symbol.symbol;
	self.type = symbol.type;
	self.re = symbol.re;

	self.equals = function (reSymbol) {
		if (reSymbol === undefined) return false;

		if (self.type === reSymbol.type) return true;
		return false;
	};
}



// Region model, determines where the region start and ends and how to compile it to a regular expression symbols
function VariableRegion(entry, begin, end, flags) {
	var self = this;

	self.name = "";

	self.entry = entry; // entry
	self.begin = begin; // beginning
	self.end = end; // end

	self.class = "rrg-" + VariableRegion.COLORS[VariableRegion.COLOR_COUNT++ % VariableRegion.COLORS.length];




	/* Transformation Flags */

	self.TRF_VARIABLE_SIZE = true;

	self.TRF_CUSTOM_SIZE = false;
	self.CUSTOM_SIZE_MIN = undefined;
	self.CUSTOM_SIZE_MAX = undefined;


	self.TRF_MIX_CHARACTERS = true; // if true, instead of having a sequence of symbols, all symbols are mixed between brackets : e.g : A01 -> \w{2}\d -> [\w\d]{3}

	// if true : changes the symbols to "."
	self.TRF_CAN_BE_ANYTHING = false;

	// if true : change the size to *
	self.TRF_CAN_BE_EMPTY = false;

	// forces to add number symbols
	self.TRF_ADD_NUMBERS = false;

	// forces to add word symbols
	self.TRF_ADD_LETTERS = false;

	// forces to add space symbols
	self.TRF_ADD_SPACES = false;


	self.TRF_ADDITIONAL_CHARS = "";



	// apply a flag to the region (flags transform the region)
	self.applyFlag = function (flag) {
		self.setFlag(flag, true);
	};

	self.correctFlag = function (flagName) {

		if (self["TRF_" + flag] != undefined) return "TRF_" + flag;
		if (self[flag] !== undefined) return flag;
		else {
			//console.log("Wrong flag : ", flag);
			return flag;
		}
	};

	self.toggleFlag = function (flag, bool) {
		if (bool === undefined) {
			bool = !self[flag];
		}
		self.setFlag(flag, bool);
		////console.log("Toggling flag " + flag);

	};

	self.getFlag = function (flag) {
		return self[self.correctFlag(flag)];
	};

	self.setFlag = function (flag, bool) {
		self[self.correctFlag(flag)] = bool;
	};

	self.doMix = function () {
		return self.TRF_MIX_CHARACTERS
	};

	// if flags are specified
	if (flags != undefined) {
		if (Array.isArray(flags)) flags.forEach(self.applyFlag);
		else self.applyFlag(flags);
	}


	// returns the text inside the defined region
	self.part = function () {
		return self.entry.substring(self.begin, self.end + 1);
	};

	// get the symbol composition of the region
	// e.g -> A01 return : [{type:"letter",count:1},{type:"number",count:2}]

	self.findSymbol = function (composition, reSymbol) {
		var found;
		composition.forEach(function (reSymbol2) {
			if (reSymbol2.equals(reSymbol)) found = reSymbol2;
		});

		return found;
	};

	self.getComposition = function () {
		var part = self.part();

		var composition = [];
		var lastSymbol = undefined;

		for (var i in part) {
			var char = part[i];
			var symbol = self.getSymbol(char);
			if (symbol.equals(lastSymbol)) {
				lastSymbol.increment();
			} else if (self.doMix() && self.findSymbol(composition) != undefined) {
				self.findSymbol(composition).increment();
			} else {
				lastSymbol = symbol;
				composition.push(symbol);

			}
		}
		return composition;
	}


	self.getSymbolSequence = function () {
		var part = self.part();

		var composition = [];
		var lastSymbol = undefined;

		for (var i in part) {
			var char = part[i];
			var symbol = self.getSymbol(char);
			if (symbol.equals(lastSymbol)) {
				lastSymbol.increment();
			} else {
				lastSymbol = symbol;
				composition.push(symbol);
			}
		}
		return composition;
	};


	self.isCompositionUnique = function () {
		return self.getSymbolSequence().length == 1;
	};

	// returns the symbol object from a character
	self.getSymbol = function (char) {
		var symbol = undefined;
		VariableRegion.SYMBOL_TABLE.forEach(function (entry) {
			if (entry.re.test(char)) {
				symbol = new RegExpSymbol(entry, 1);
			}
		});

		if (symbol == undefined)
			symbol = new RegExpSymbol({
				symbol: "\\" + char,
				name: char,
				type: char
			});

		return symbol;
	};
	self.compile = function (compiler) {
		if (compiler == undefined) compiler = new RegionCompiler(self);
		return compiler.compile();
	};


	self.noNumber = function () {
		////console.log(/\d/.test(self.part()));
		return (!/\d/.test(self.part()));
	};

	self.noSpace = function () {
		return (!/\s/.test(self.part()));
	};

	self.noWord = function () {
		return (!/\w/.test(self.part()));
	}

}

/* Symbol Transformation */


function RegionCompiler(region) {

	var self = this;
	self.region = region;

	self.result = "";



	/* symbol transformers */
	var addNumber = function (region, reSymbol, result) {
		if (region.TRF_ADD_NUMBERS === true) {
			result += "\\d"
		}
		return result;
	};

	var addSpace = function (region, reSymbol, result) {
		if (region.TRF_ADD_SPACES === true) {
			result += "\\s";
		}
		return result;
	};

	var addLetters = function (region, reSymbol, result) {
		if (region.TRF_ADD_LETTERS) result += "\\w";
		return result;
	};

	var addBrackets = function (region, reSymbol, result) {
		if (result.length > 2) {
			return "[" + result + "]";
		}
		return result;
	}

	var canBeAnything = function (region, reSymbol, result) {
		if (region.TRF_CAN_BE_ANYTHING == true) {
			return ".";
		}
		return result;
	}

	var additionalChars = function (region, reSymbol, result) {
		return result + region.TRF_ADDITIONAL_CHARS;
	};




	self.symbolTransformers = [addNumber, addSpace, addLetters, canBeAnything, addBrackets];


	/* size transformers */

	self.sizeTranformer = function (region, size, result) {



		if (region.TRF_VARIABLE_SIZE && region.TRF_CUSTOM_SIZE) {

			if (region.CUSTOM_SIZE_MIN == undefined || region.TRF_CAN_BE_EMPTY) {
				region.CUSTOM_SIZE_MIN = 0;
			}
			if (region.CUSTOM_SIZE_MAX == undefined) {
				region.CUSTOM_SIZE_MAX = size;
			}

			return result += "{" + region.CUSTOM_SIZE_MIN + "," + region.CUSTOM_SIZE_MAX + "}";
		}

		if (region.TRF_CAN_BE_EMPTY && region.TRF_VARIABLE_SIZE) return result + "*";

		if (region.TRF_VARIABLE_SIZE) {
			return result + "+";
		}
		if (size > 1) {
			result += "{" + size + "}";
		}


		return result;
	}

	/* Wrap transformation */

	self.addParentheses = function (region, result) {
		if (!region.REMOVE_PARENTHESE) return "(" + result + ")";
		return result;
	};





	self.composition = region.getComposition();

	self.isCompisitionUnique = function () {
		return self.composition.length == 1;
	}

	self.applySymbolTransformation = function (result) {

		////console.log("result before", result);
		self.symbolTransformers.forEach(function (transformer) {
			////console.log("result in the process", result);
			result = transformer(self.region, undefined, result);
		});
		////console.log("result after", result);
		return result;
	};

	self.doMixSymbols = function () {
		return self.region.TRF_MIX_CHARACTERS;
	};

	self.mixSymbols = function (composition) {
		var alreadyAddedSymbol = [];
	}

	self.compile = function () {
		var result = "";
		if (self.isCompisitionUnique()) {
			// retrieving the only kind of symbol in the sequence
			var symbol = self.composition[0];

			// retrieving its string symbol
			result = symbol.stringSymbol;

			// adding string symbol if necessary
			result = self.applySymbolTransformation(result);
			result = self.sizeTranformer(self.region, symbol.count, result);
		} else {
			if (self.doMixSymbols()) {



				// putting all the symbols together
				self.composition.forEach(function (reSymbol) {

					result += reSymbol.stringSymbol;
				});
				////console.log(result);
				// adding the size and other symbol transformation;
				result = self.applySymbolTransformation(result);

				// putting the size of the whole thing
				result = self.sizeTranformer(self.region, region.part().length, result);
			} else {
				// for each symbol, we add the size
				self.composition.forEach(function (reSymbol) {
					result += self.sizeTranformer(region, reSymbol.count, reSymbol.stringSymbol);
				});
			}
		}
		result = self.addParentheses(self.region, result);
		self.result = result;
		return result;
	};




}

function RegionToTextCompiler(region) {
	var self = this;


	self.region = region;

	self.compile = function () {

		var parts = new Array();
		var region = self.region;
		if (self.region == undefined) {
			return {}
		};

		if (region.TRF_MIX_CHARACTERS) {

			var r = {
				text: "",
				color: region.class
			};
			var seq = [];
			region.getSymbolSequence().forEach(function (symbol) {
				var symbolName = symbol.name;
				if(symbolName.length > 1)
					symbolName+="s";
				
				seq.push(symbolName);

			});

			r.text = seq.join(" or ");

			return r;
		} else {
			region
				.getSymbolSequence()
				.forEach(function (symbol) {
					parts.push({
						name: symbol.name,
						color: region.class
					});

				});

			return parts;

		}
		return {
			text: "REGION",
			color: self.region.class
		};

	};
}