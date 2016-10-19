/**
 * Behaviour for protein page
 *
 * @author  Antony Quinn
 * @version $Id: protein.js,v 1.6 2012/02/20 12:09:41 matthew Exp $
 */

// Disables or enables stylesheets for database colouring
function configureStylesheets(disable) {
    $.cookie("colour-by-domain", disable, { path: '/' });
    // References the html <link/> element with class 'database' and enables / disables it.
    $("link.database").each(function(i) {
        this.disabled = disable;
    });
}

function displayType(checkbox) {
    var checked = checkbox.checked;
    var classes = checkbox.value;
    $.cookie('#' + checkbox.id, checked, { path: '/' });
    // May be more than one class controlled by this checkbox, white space separated.
    var cssClasses = classes.split(" ");
    for (i = 0; i < cssClasses.length; i++) {
        var typeSelector = '.' + cssClasses[i];
        if (checked) {
            $(typeSelector).show("blind", { direction: "vertical" }, 300);
        }
        else {
            $(typeSelector).hide("blind", { direction: "vertical" }, 300);
        }
    }
}

function displayUnintegrated(checkbox) {
    var checked = checkbox.checked;
    $.cookie('#' + checkbox.id, checked, {path: '/'});
    if (checked) {
        $('#uni_sign').show("blind", {direction: "vertical"}, 300);
    }
    else {
        $('#uni_sign').hide("blind", {direction: "vertical"}, 300);
    }
}

function displaySites(checkbox) {
    var checked = checkbox.checked;
    $.cookie('#' + checkbox.id, checked, { path: '/' });
    if (checked) {
        $('#sites').show("blind", { direction: "vertical" }, 300);
    }
    else {
        $('#sites').hide("blind", { direction: "vertical" }, 300);
    }
}

