function init(evt) {
    if (window.svgDocument == null) {
        svgDocument = evt.target.ownerDocument;
    }

    tooltip = svgDocument.getElementById('tooltip');
    tooltip_bg = svgDocument.getElementById('tooltip_bg');
    tooltip_svg_component = svgDocument.getElementById('tooltip_component');
}

function ShowTooltip(evt, mouseovertext, x, y) {
    tooltip.setAttributeNS(null, "x", 18);
    tooltip.setAttributeNS(null, "y", 25);
    tooltip.firstChild.data = mouseovertext;

    length = tooltip.getComputedTextLength() + 25;
    tooltip_bg.setAttributeNS(null, "points", "10,0 20,10 " + length + ",10 " + length + ",40 10,40");

    tooltip_svg_component.setAttributeNS(null, "x", x);
    tooltip_svg_component.setAttributeNS(null, "y", y);
    tooltip_svg_component.setAttributeNS(null, "visibility", "visibile");
}

function HideTooltip(evt) {
    tooltip_svg_component.setAttributeNS(null, "visibility", "hidden");
}
