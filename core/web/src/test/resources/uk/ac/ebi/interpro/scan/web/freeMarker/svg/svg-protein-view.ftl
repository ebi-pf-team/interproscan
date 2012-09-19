<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?xml-stylesheet type="text/css" href="css/mouse_over_tooltip.css"?>
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
    <script xlink:href="js/mouse_over_tooltip.js" type="text/ecmascript"/>

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
           xlink:href="icons/ico_type_protein.png"/>
    <text x="55px" y="20px" style="fill:#1897E9;font-family:Verdana,Helvetica,sans-serif;
                             font-size:20">Protein
    </text>
    <!--Main box around the protein view-->
    <rect id="mainBoxBorder"
          x="30px" y="30px" width="1233px" height="700px"
          style="fill:white; stroke:#E4E4E4; stroke-width:1"/>
    <!--Protein info component-->
    <svg x="30px" y="30px">
        <text x="10px" y="25px">
            <tspan x="10px" dy="10px" font-size="22" fill="#284ADB" font-family="Verdana, Helvetica, sans-serif"
                   font-weight="normal">
                Malto-oligosyltrehalose trehalohydrolase
            </tspan>
            <tspan fill="#284ADB" font-size="15" font-family="Verdana, Helvetica, sans-serif" font-weight="normal">
                (${protein.ac})
            </tspan>
        </text>
    <text y="75px" font-size="13" fill="#393939" font-family="Verdana, Helvetica, sans-serif">
            <tspan font-weight="bold" x="10px">
                Accession
            </tspan>
	    <a xlink:href="http://www.uniprot.org/uniprot/Q44316" target="_top">
            	<tspan x="10px" dx="100px" fill="#0072FE" text-decoration="underline"
			onmouseover="ShowTooltip(evt, 'Malto-oligosyltrehalose trehalohydrolase (Q44316)', 140, 77)"
                        onmouseout="HideTooltip(evt)">${protein.ac}</tspan>
	    </a>
            <tspan x="10px" dx="152px" fill="#838383">(TREZ_ARTSQ)</tspan>
            <tspan font-weight="bold" x="10px" dy="30px">
                Species
            </tspan>
            <tspan x="10px" dx="100px" fill="#838383">Arthrobacter sp.
                (strain Q36)
            </tspan>
            <tspan font-weight="bold" x="10px" dy="30px">
                Length
            </tspan>
            <tspan x="10px" dx="100px" fill="#838383">598 AA (complete)
            </tspan>
        </text>
        <text x="1084px" y="145px" font-size="10">
            <tspan fill="#525252" font-family="Verdana, Helvetica, sans-serif">Source:</tspan>
            <tspan fill="#838383" font-family="Verdana, Helvetica, sans-serif">UniProtKB/Swiss-Prot</tspan>
        </text>

        <line x1="10px" y1="150px" x2="1220px" y2="150px" stroke="#CDCDCD" stroke-width="1"/>

        <!--Simple tooltip -->
        <svg id="tooltip_component" baseProfile="tiny" visibility="hidden">
            <polygon class="tooltip_bg" id="tooltip_bg" points="0,0"/>
            <text class="tooltip" id="tooltip" font-size="18" font-family="arial" fill="#000">SVG
                Javascript Tooltip
            </text>
        </svg>

    </svg>
    <!--Protein family membership component-->
    <svg id="proteinFamilyTree" x="40px" y="210px" width="1200" height="100" viewBox="0 0 1200 100">
        <text y="13px"
              style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
            Protein family membership
        </text>
        <svg x="14px" y="24px">
            <image x="1px" y="1px" width="22px" height="14px"
                   xlink:href="icons/ico_tree_family.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR001057" target="_top">
                <text x="29px" y="12px" text-decoration="underline" style="fill:#0072FE">
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#0072FE;">
                        Glutamate/acetylglutamate kinase
                    </tspan>
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#393939;">
                        (IPR001057)
                    </tspan>
                </text>
            </a>
        </svg>
        <svg x="42px" y="42px">
            <image x="1px" y="1px" width="22px" height="14px"
                   xlink:href="icons/ico_tree_family.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR005715" target="_top">
                <text x="29px" y="12px" text-decoration="underline" style="fill:#0072FE">
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#0072FE;">
                        Glutamate 5-kinase/delta-1-pyrroline-5-carboxylate synthase
                    </tspan>
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#393939;">
                        (IPR005715)
                    </tspan>
                </text>
            </a>
        </svg>
        <svg x="70px" y="60px">
            <image x="1px" y="1px" width="22px" height="14px"
                   xlink:href="icons/ico_tree_family.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR011529" target="_top">
                <text x="29px" y="12px" text-decoration="underline" style="fill:#0072FE">
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#0072FE;">
                        Glutamate 5-kinase
                    </tspan>
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#393939;">
                        (IPR011529)
                    </tspan>
                </text>
            </a>
        </svg>
        <svg x="42px" y="78px">
            <image x="1px" y="1px" width="22px" height="14px"
                   xlink:href="icons/ico_tree_family.png"/>
            <a xlink:href="http://wwwdev.ebi.ac.uk/interpro/IEntry?ac=IPR011529" target="_top">
                <text x="29px" y="12px" text-decoration="underline" style="fill:#0072FE">
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#0072FE;">
                        Glutamate 5-kinase/delta-2-pyrroline-5-carboxylate synthase
                    </tspan>
                    <tspan style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:13px;stroke:none;fill:#393939;">
                        (IPR011529)
                    </tspan>
                </text>
            </a>
        </svg>
    </svg>
    <!--Summary view component-->
    <!--Nesting SVG elements can be useful to group SVG shapes together, and position them as a collection.-->
    <svg id="summaryView" x="40px" y="340px" width="1200" height="100" viewBox="0 0 1200 100">
        <text y="13px"
              style="fill:#393939;font-family:Verdana,Helvetica,sans-serif;font-size:13px;font-weight:bold">
            Sequence features summary
        </text>
        <use x="123px" y="19px" xlink:href="#greyBoxOneBlobLine"/>
        <!--Summary view scale annotation-->
        <svg>
            <line x1="123px" y1="19px" x2="123px" y2="40px"
                  style="stroke:#B8B8B8;stroke-width:0.8;stroke-dasharray:3.0"/>
            <text x="120px" y="55px" font-size="12"
                  style="fill:#B8B8B8;font-family:Verdana, Helvetica, sans-serif;font-weight:normal">0
            </text>
            <line x1="560px" y1="19px" x2="560px" y2="40px"
                  style="stroke:#B8B8B8;stroke-width:0.8;stroke-dasharray:3.0"/>
            <text x="550px" y="55px" font-size="12"
                  style="fill:#B8B8B8;font-family:Verdana, Helvetica, sans-serif;font-weight:normal">300
            </text>
            <line x1="1053px" y1="19px" x2="1053px" y2="40px"
                  style="stroke:#B8B8B8;stroke-width:0.8;stroke-dasharray:3.0"/>
            <text x="1042px" y="55px" font-size="12"
                  style="fill:#B8B8B8;font-family:Verdana, Helvetica, sans-serif;font-weight:normal">598
            </text>
        </svg>
        <!--Domain annotation right hand site-->
        <svg x="1058px"
             y="19px">
            <!--Arrow component-->
            <use xlink:href="#blackArrowComponent"/>
            <text x="15px" y="10.5px"
                  style="font-family:Verdana,Helvetica,sans-serif;
                             font-size:75%;stroke:none;fill:#525252;">Domain
            </text>
        </svg>
        <!--Domain blobs-->
        <svg x="122.5px" y="19px">
            <rect id="summaryViewBlob_1"
                  x="0.5px"
                  y="5px"
                  width="640px"
                  height="7px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:#E6C4A8;stroke:black;stroke-width:0.5"
                       onmouseover="ShowTooltip(evt, '0 - 402', 760, 345)"
                       onmouseout="HideTooltip(evt)"/>
            <rect id="summaryViewBlob_2"
                  x="700px"
                  y="5px"
                  width="183px"
                  height="7px"
                  rx="3.984848"
                  ry="5.6705141"
                  style="fill:yellow;stroke:black;stroke-width:0.5"/>
        </svg>
    </svg>
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
                       xlink:href="icons/ico_type_domain_small.png"/>
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
                       xlink:href="icons/ico_type_domain_small.png"/>
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