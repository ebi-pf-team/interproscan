/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id$
 */

// Chevrons
var SHOW_ICON = "\u00BB"; // »
var HIDE_ICON = "\u00AB"; // «

$(document).ready(function() {
    
    createAllEntriesShowHideButton();

    // Read colour preference from cookie (requires http://plugins.jquery.com/project/Cookie)
//    if($.cookie("css")) {
//        var id = $.cookie("colour-by-database");
//        ...
//    }

    // CSS switching
    var checkbox = $("input[type=checkbox]");
    configureStylesheets(checkbox.checked); // initialise
    checkbox.click(function() {
        configureStylesheets(this.checked);
        // Save in cookie
        //$.cookie("colour-by-database", this.checked, {expires: 365, path: '/'});
    });
    
});

// Disables or enables stylesheets for database colouring
function configureStylesheets(enable) {
    $("link.database").each(function(i) {
        this.disabled = (!enable);
    });
}

// Add button to show and hide all signatures
function createAllEntriesShowHideButton(){

    // TODO: This messes up the state of individual toggling, so may need to loop through each element and toggle?

    var showText = "Show all signatures " + SHOW_ICON;
    var hideText = "Hide all signatures " + HIDE_ICON;
    var buttonId = "all-signatures-toggle";
    var allSignaturesClass = ".entry-signatures";

    // Add button to page
    $(getButtonHtml(buttonId)).prependTo("#section-domains-sites");
    createShowHideButton(buttonId, allSignaturesClass, showText, showText, hideText);

    // Hide all signatures after 0 milliseconds -- leaves visible if JavaScript off or not available
    $(allSignaturesClass).slideToggle(0);
}

// Add button to show and hide signatures for an individual entry
function createSingleEntryShowHideButton(targetId){
    var showText = "Show signatures " + SHOW_ICON;
    var hideText = "Hide signatures " + HIDE_ICON;
    var buttonId = targetId + "-toggle";
    // Add button to page
    document.write(getButtonHtml(buttonId));
    createShowHideButton(buttonId, "#" + targetId, showText, showText, hideText);
}

function getButtonHtml(buttonId) {
    return "<button id='" + buttonId + "'></button>";
}

function createShowHideButton(buttonId, target, initialText, showText, hideText) {

    // console.log("createShowHideButton: " + buttonId);

    // Set initial text
    var button = $("#" + buttonId);
    button.text(initialText);
    
    // Add show/hide behaviour
    button.click(function () {
        //console.log("clicked");
        var delay = 400; // milliseconds
        $(target).slideToggle(delay);
        // Toggle
        setTimeout(function() {
            var s = (button.text() == showText ? hideText : showText);
            button.text(s);
        }, delay / 2);
    });

    return button;
}
