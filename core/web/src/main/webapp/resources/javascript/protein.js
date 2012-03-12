/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id: protein.js,v 1.6 2012/02/20 12:09:41 matthew Exp $
 */

// Disables or enables stylesheets for database colouring
function configureStylesheets(disable) {
    $.cookie("colour-by-domain", disable, { path: '/' });
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
    $.cookie('#check-6', checkbox.checked, { path: '/' });
    if (checkbox.checked) {
        $('#uni').show("blind", { direction: "vertical" }, 300);
    }
    else {
        $('#uni').hide("blind", { direction: "vertical" }, 300);
    }
}
