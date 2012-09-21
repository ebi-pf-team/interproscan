<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?xml-stylesheet type="text/css" href="resources/css/mouse_over_tooltip.css"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">

<!--All widths and heights are specified in pixel -->
<!--Copied style colours for rectangle, blobs and borders from the InterProWeb Beta site-->
<svg xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns="http://www.w3.org/2000/svg"
     id="svgProteinView"
     width="1280px"
     height="1024px"
     viewBox="0 0 1280 1024"
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
      x="30px" y="30px" width="1233px" height="700px"
      style="fill:white; stroke:#E4E4E4; stroke-width:1"/>

<#--protein info component-->
<#include "component/protein-info-component.ftl"/>

<#--Protein family membership component-->
<#include "component/protein-family-component.tfl"/>

<#--Protein summary view component-->
<#include "component/protein-summary-view-component.ftl"/>

<!--Sequence features component-->
<svg id="sequenceFeatures" x="40px" y="400px">
    <text y="13px"
          style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
        Sequence features
    </text>
    <!--First sequence feature component-->
    <svg y="30px" width="1200" height="100" viewBox="0 0 1200 100">
        <svg>
            <rect x="1px" y="1px" width="110px" height="18px"
                  style="stroke:#D8D8D8;fill:transparent;stroke-width:0.5"/>
            <image x="3px" y="4px" width="13px" height="13px"
                   xlink:href="resources/icons/ico_type_domain_small.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR014756" target="_top">
                <text x="25px" y="15px" text-decoration="underline" style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:11px;stroke:none;fill:#0072FE;">IPR014756
                </text>
            </a>
        </svg>
        <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR014756" target="_top">
            <text x="123px" y="14px" text-decoration="underline"
                  style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#525252;">Immunoglobulin E-set
            </text>
        </a>
        <use x="123px" y="19px" xlink:href="#greyBoxTwoBlobLines"/>
        <!--Arrow component-->
        <svg x="1058px"
             y="20px">
            <use xlink:href="#blackArrowComponent"/>
            <a xlink:href="http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/scop.cgi?ipid=SSF53633" target="_top">
                <text x="15px" y="10.5px" text-decoration="underline"
                      style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:75%;stroke:none;fill:#525252;">SSF53633 (Aa_kinase)
                </text>
            </a>
        </svg>
        <svg x="1058px"
             y="37px">
            <use xlink:href="#blackArrowComponent"/>
            <a xlink:href="http://www.cathdb.info/cathnode/G3DSA:3.40.1160.10" target="_top">
                <text x="15px" y="10.5px" text-decoration="underline"
                      style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:75%;stroke:none;fill:#525252;">G3DSA:3.40.11... (A...)
                </text>
            </a>
        </svg>
        <svg x="122.5px" y="23px">
            <rect id="sequenceFeatureBlob_1"
                  width="640px"
                  height="7px"
                  x="1px"
                  y="1px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:#E6C4A8;stroke:black;stroke-width:0.5"/>
            <rect id="sequenceFeatureBlob_2"
                  width="440px"
                  height="7px"
                  x="25.5px"
                  y="18px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:#E6C4A8;stroke:black;stroke-width:0.5"/>
        </svg>
    </svg>
    <!--Second sequence feature component-->
    <svg y="107px" width="1200" height="100" viewBox="0 0 1200 100">
        <svg>
            <rect x="1px" y="1px" width="110px" height="18px"
                  style="stroke:#D8D8D8;fill:transparent;stroke-width:0.5"/>
            <image x="3px" y="4px" width="13px" height="13px"
                   xlink:href="resources/icons/ico_type_domain_small.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR004193" target="_top">
                <text x="25px" y="15px" text-decoration="underline" style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:11px;stroke:none;fill:#0072FE;">IPR004193
                </text>
            </a>
        </svg>
        <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR004193" target="_top">
            <text x="123px" y="14px" text-decoration="underline"
                  style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#525252;">Glycoside hydrolase, family 13, N-terminal
            </text>
        </a>
        <use x="123px" y="19px" xlink:href="#greyBoxThreeBlobLines"/>
        <!--Arrow component-->
        <svg x="1058px"
             y="20px">
            <use xlink:href="#blackArrowComponent"/>
            <a xlink:href="http://supfam.cs.bris.ac.uk/SUPERFAMILY/cgi-bin/scop.cgi?ipid=SSF53633" target="_top">
                <text x="15px" y="10.5px" text-decoration="underline"
                      style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:75%;stroke:none;fill:#525252;">SSF53633 (Aa_kinase)
                </text>
            </a>
        </svg>
        <svg x="1058px"
             y="37px">
            <use xlink:href="#blackArrowComponent"/>
            <a xlink:href="http://www.cathdb.info/cathnode/G3DSA:3.40.1160.10" target="_top">
                <text x="15px" y="10.5px" text-decoration="underline"
                      style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:75%;stroke:none;fill:#525252;">G3DSA:3.40.11... (A...)
                </text>
            </a>
        </svg>
        <svg x="1058px"
             y="54px">
            <use xlink:href="#blackArrowComponent"/>
            <a xlink:href="http://www.cathdb.info/cathnode/G3DSA:3.40.1160.10" target="_top">
                <text x="15px" y="10.5px" text-decoration="underline"
                      style="font-family:Verdana,Helvetica,sans-serif;
                                 font-size:75%;stroke:none;fill:#525252;">G3DSA:3.40.11... (B...)
                </text>
            </a>
        </svg>
        <svg x="122.5px">
            <rect width="40px"
                  height="7px"
                  x="700px"
                  y="25px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:yellow;stroke:black;stroke-width:0.5"/>
            <rect width="123px"
                  height="7px"
                  x="740px"
                  y="42px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:yellow;stroke:black;stroke-width:0.5"/>
            <rect width="20px"
                  height="7px"
                  x="863px"
                  y="59px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:yellow;stroke:black;stroke-width:0.5"/>
        </svg>
    </svg>
</svg>
<!-- BEGIN: GO terms section -->
<g id="goSection">
    <!-- GO terms section title heading -->
    <svg x="40px" y="600px" width="1200" height="200" viewBox="0 0 1200 200">
        <text font-size="13px" y="13px" fill="#393939" font-family="Verdana, Helvetica, sans-serif"
              font-weight="bold">
            GO Term prediction
        </text>
        <g id="goTable">
            <svg y="30px">
                <!--GO Terms data column 1-->
                <text font-size="11px" x="10px" dy="20px" width="426px" height="111px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="10px" fill="#393939" font-weight="bold">
                        Biological process
                    </tspan>

                    <tspan x="10px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        <a xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0005975" target="_top">
                            GO:0005975
                        </a>
                        carbohydrate metabolic process
                    </tspan>
                    <tspan x="10px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        <a xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0005975" target="_top">
                            GO:0005975
                        </a>
                        carbohydrate metabolic process
                    </tspan>
                </text>
                <!--GO Terms data column 2-->
                <text font-size="11px" x="436px" dy="20px" width="426px" height="111px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="436px" fill="#393939" font-weight="bold">
                        Molecular function
                    </tspan>

                    <tspan x="436px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        <a xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0003824" target="_top">
                            GO:0003824
                        </a>
                        catalytic activity
                    </tspan>
                    <tspan x="436px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        <a xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0004553" target="_top">
                            GO:0004553
                        </a>
                        hydrolase activity, hydrolyzing O-glycosyl compound
                    </tspan>
                    <tspan x="436px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        <a xlink:href="http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0043169" target="_top">
                            GO:0043169
                        </a>
                        cation binding
                    </tspan>
                </text>
                <!--GO Terms data column 3-->
                <text font-size="11px" x="862px" dy="20px" width="426px" height="111px"
                      font-family="Verdana, Helvetica, sans-serif">
                    <tspan x="862px" fill="#393939" font-weight="bold">
                        Cellcular component
                    </tspan>

                    <tspan x="862px" dy="20px" fill="#0072FE" font-family="Verdana, Helvetica, sans-serif"
                           text-decoration="underline"
                           style="font-family:Verdana,Helvetica,sans-serif;font-size:85%;stroke:none;fill:#0072FE;">
                        No cellular component GO terms.
                    </tspan>
                </text>
            </svg>
        </g>
    </svg>
</g>
<!--END: GO Terms section-->
</svg>