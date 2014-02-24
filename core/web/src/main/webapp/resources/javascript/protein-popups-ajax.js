// Prepare protein match/structural match popups
// E.g. Tie "match-location-1" span to "match-popup-1" hidden div content.
function preparePopup(spanId) {
    var searchString = 'location-';
    var prefix = spanId.substring(0, spanId.indexOf(searchString));
    var postfix = spanId.substring(prefix.length + searchString.length, spanId.length);
    var popupId = prefix.concat('popup-', postfix);

    //alert('spanId: '.concat(spanId, ', popupId: ', popupId));

    $('#'.concat(spanId)).click(function(event) {
        event.preventDefault();
        return false;
    });

    $('#'.concat(spanId)).qtip({
        content: {
            text: function(event, api) {
                $.ajax({
                    url: api.elements.target.attr('href') // Use href attribute as URL
                })
                .then(function(content) {
                    // Set the tooltip content upon successful retrieval
                    api.set('content.text', content);
                }, function(xhr, status, error) {
                    // Upon failure... set the tooltip content to error
                    api.set('content.text', status + ': ' + error);
                });

                return 'Loading...'; // Set some initial text
            },
            title: {
                text: ' ', // Popup text in the title bar
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
            delay: 150 // Avoid a mass of popups when moving mouse across the screen!
        },
        hide: {
            fixed: true, // If the user mouses out of the span to the popup then keep the popup open
            delay: 500
        },
        style: {
            classes: 'ui-tooltip-light ui-tooltip-shadow'
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
