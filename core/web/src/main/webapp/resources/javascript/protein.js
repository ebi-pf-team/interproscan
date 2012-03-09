/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id: protein.js,v 1.6 2012/02/20 12:09:41 matthew Exp $
 */

// Prepare protein match popup
// E.g. Tie "match-location-1" span to "match-popup-1" hidden div content.
function preparePopup(spanId) {
    var searchString = 'location-';
    var prefix = spanId.substring(0, spanId.indexOf(searchString));
    var postfix = spanId.substring(prefix.length + searchString.length, spanId.length);
    var popupId = prefix.concat('popup-', postfix);
    //alert('spanId: '.concat(spanId, ', prefix: ', prefix, ', postfix: ', postfix, ', popupId: ', popupId));

    $('#'.concat(spanId)).qtip({
        prerender: true, // Render all popups at page load time?
        content: {
            text: $('#'.concat(popupId)),
            title: {
                text: 'Location data', // Popup text in the title bar
                button: true // Close button
            }
        },
        position: {
            my: 'bottom center',
            at: 'top center',
            viewport: $(window), // Keep the tooltip on-screen at all times
            effect: true // Positioning animation
        },
        show: {
            event: 'mouseenter',
            solo: false, // Show one tooltip at a time?
            delay: 250 // Avoid a mass of popups when moving mouse across the screen!
        },
        hide: {
            fixed: true, // If the user mouses out of the span to the popup then keep the popup open
            delay: 500
        },
        style: {
            classes: 'ui-tooltip-wiki ui-tooltip-light ui-tooltip-shadow'
        },
        events: {
            render: function(event, api) {
                api.elements.target.bind('click', function() {
                    api.set('hide.event', false);
                });
            },
            hide: function(event, api) {
                api.set('hide.event', 'mouseout');
            }
        }
    });
}

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
