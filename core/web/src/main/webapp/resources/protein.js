/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id$
 */


$(document).ready(function() {

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
