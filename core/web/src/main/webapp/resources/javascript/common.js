// This file contains Javascript common to all pages in InterProScan web

$(document).ready(function() {
    // Qtip2 popups (used on all pages of I5 web)
    $('a[title]').qtip({
        position: {
            viewport: $(window) // Keep the tooltip on-screen at all times
        }
    });
    $('img[title]').qtip({
        position: {
            viewport: $(window) // Keep the tooltip on-screen at all times
        }
    });
    $('abbr[title]').qtip({
        position: {
            viewport: $(window) // Keep the tooltip on-screen at all times
        }
    });
    $('span[title]').qtip({
        position: {
            viewport: $(window) // Keep the tooltip on-screen at all times
        }
    });
});
