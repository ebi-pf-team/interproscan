<#--Protein summary view component-->
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