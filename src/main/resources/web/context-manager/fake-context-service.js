angular.module("AngularMercury", ["contextServiceApp"])
    .factory("ContextService", function () {
        return new FakeContextService();
    });

function FakeContextService() {
    var self = this;

    self.contextList = [
        {
            id: "main",
            incompatibleWith: "intro"
  }
  , {
            id: "intro",
            incompatibleWith: "id"
  }
  , {
            id: "image-open"
  }
  , {
            id: "image-multichannel"
  }
 ];

    self.widgetList = [
        {
            id: "openButton",
            name: "Open Button",
            description: "When clicked, an image is opened"
  }
  , {
            id: "closeButton",
            name: "Close Button",
            description: "When clicked, the current image is closed"
  }
  , {
            id: "channelSlide",
            name: "Channel slide",
            description: "Slider used to change channel of an image"
  }
 ];

    self.linkList = [
        {
            widget: "openButton",
            context: "intro",
            linkType: "show"
  }
  , {
            widget: "openButton",
            context: "intro",
            linkType: "enable"
  }
  , {
            widget: "closeButton",
            context: "image-open",
            linkType: "show"
  }
  , {
            widget: "closeButton",
            context: "image-open",
            linkType: "enable"
  }
  , {
            widget: "channelSlide",
            context: "image-multichannel",
            linkType: "enable"
  }
 ];

    // enters a context (non needed)
    self.enter = function (contextList) {
        console.log("Entering context " + contextList);
    }

    // leaves a context (non needed)
    self.leave = function (contextList) {
        console.log("leaving context" + contextList);
    };

    // returns the context list as you see above
    self.getContextList = function () {
        return self.contextList;
    };

    // returns the widget list as you see above
    self.getWidgetList = function () {
        return self.widgetList;
    };

    // return the link list as you above
    self.getLinkList = function () {
        return self.linkList;
    };


    // link a widget and a context
    self.link = function (widget, context, linkType) {

        var link = {
            widget: widget,
            context: context,
            linkType: linkType
        };
        console.log("link was added:", link);
        self.linkList.push(link);
    };

    // unlink a widget and a context
    self.unlink = function (widget, context, linkType) {

        var count = self.linkList.length;
        self.linkList = self.linkList.filter(function (link) {
            return !(link.context == context && link.widget == widget && link.linkType == linkType);
        });

        if (self.linkList.length == count - 1) {
            console.log("Link removed successfully");
        } else {
            console.log("Coulnd't find the link. No link removed");
        }

    };
}