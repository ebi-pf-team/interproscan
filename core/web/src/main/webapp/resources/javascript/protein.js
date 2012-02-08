/**
 * Behaviour for protein.jsp
 *
 * @author  Antony Quinn
 * @version $Id$
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
        content: {
            text: $('#'.concat(popupId)),
            title: {
                text: 'Location data',
                button: true // Close button
            }
        },
        position: {
            my: 'top center',
            at: 'bottom center',
            viewport: $(window), // Keep the tooltip on-screen at all times
            effect: true // Positioning animation
        },
        show: {
            event: 'click',
            solo: false // Show one tooltip at a time?
        },
        hide: 'close',
        style: {
            classes: 'ui-tooltip-wiki ui-tooltip-light ui-tooltip-shadow'
        }
    });
}


// Disables or enables stylesheets for database colouring
function configureStylesheets(enable) {
    $("link.database").each(function(i) {
        this.disabled = (!enable);
    });
}
