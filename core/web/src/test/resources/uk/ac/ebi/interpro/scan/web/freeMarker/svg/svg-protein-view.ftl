<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?xml-stylesheet type="text/css" href="resources/css/mouse_over_tooltip.css"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">

<!--All widths and heights are specified in pixel -->
<!--Copied style colours for rectangle, blobs and borders from the InterProWeb Beta site-->
<svg xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns="http://www.w3.org/2000/svg"
     id="svgProteinView"
     width="1280px"
     height="2524px"
     viewBox="0 0 1280 2524"
     onload="init(evt)"
     version="1.1"
     baseProfile="tiny">

    <!--JavaScript references-->
    <script xlink:href="resources/js/mouse_over_tooltip.js" type="text/ecmascript"/>

    <desc>SVG mock up for the new protein view. The goal is to auto-generate SVG files in InterProScan 5.</desc>
    <defs id="defs3220">
        <!--Defines a grey box for a single domain or a single sequence feature-->
        <rect id="greyBoxOneBlobLine"
              width="930px"
              height="17px"
              style="fill:#EFEFEF"/>
        <rect id="greyBoxTwoBlobLines"
              width="930px"
              height="34px"
              style="fill:#EFEFEF"/>
        <rect id="greyBoxThreeBlobLines"
              width="930px"
              height="51px"
              style="fill:#EFEFEF"/>
        <polygon id="blackArrowComponent" points="0,2  8,6  0,10"
                 style="stroke:#660000; fill:black;"/>
        <rect id="blackSquare" x="1px" y="7px" height="6px" width="6px" style="fill:393939"/>
    </defs>
    <image x="30px" y="4px" width="22px" height="22px"
           xlink:href="resources/icons/ico_type_protein.png"/>
    <text x="55px" y="20px" style="fill:#1897E9;font-family:Verdana,Helvetica,sans-serif;
                             font-size:20">Protein
    </text>
<#--Main box around the protein view-->
    <rect id="mainBoxBorder"
          x="30px" y="30px" width="2533px" height="700px"
          style="fill:white; stroke:#E4E4E4; stroke-width:1"/>

<#--protein info component-->
<#include "component/protein-info-component.ftl"/>

<#--Protein family membership component-->
<#include "component/protein-family-component.ftl"/>

<#--Protein summary view component-->
<#include "component/protein-summary-view-component.ftl"/>

<#--Protein features component-->
<#--<#include "component/protein-features-component.ftl"/>-->

<#--Protein GO terms component-->
<#--<#include "component/protein-xref-component.ftl"/>-->
</svg>