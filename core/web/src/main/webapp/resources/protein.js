/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id$
 */

$(document).ready(function() {

    // Set up the button to show and hide all signatures
    var toggleAllId = "all-signatures-toggle";
    var toggleAllClass = ".entry-signatures";
    var showAllText = "Show all signatures \u00BB"; // »
    var hideAllText = "Hide all signatures \u00AB"; // «

    // Add button to show and hide signatures
    $("<button id='" + toggleAllId + "'></button>").prependTo("#section-domains-sites");
    var toggleAllButton = $("#" + toggleAllId);
    toggleAllButton.text(showAllText);

    // Hide signatures after 0 milliseconds -- leaves visible if JavaScript off or not available
    $(toggleAllClass).slideToggle(0);

    // Show or hide all signature matches
    toggleAllButton.click(function () {
        //console.log("clicked");
        var delay = 400; // milliseconds
        $(toggleAllClass).slideToggle(delay);
        // Togglw
        setTimeout(function() {
            var s = (toggleAllButton.text() == showAllText ? hideAllText : showAllText);
            toggleAllButton.text(s);
        }, delay / 2);
    });
    
    // TODO: Toggle individual sections based on entry ac
    //            $("#toggle-entry-signatures").click(function () {
    //                $(".entry-signatures").slideToggle("slow", function () {
    //                    alert("1");
    //                    var button = $("#toggle-entry-signatures");
    //                    $(this).is(":visible") ? button.html("Hide signatures «") : button.html("Show signatures »");
    //                });
    //            });

});
