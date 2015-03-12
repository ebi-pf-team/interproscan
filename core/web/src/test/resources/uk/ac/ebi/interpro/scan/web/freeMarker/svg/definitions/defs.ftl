<defs id="protein_view_defs">
<#--Black arrow (displayed in front of every member databases match link) -->
    <polygon id="blackArrowComponent" points="0,2  8,6  0,10"
             style="stroke:#660000; fill:black;"/>
<#--<rect id="blackSquare" x="1px" y="7px" height="6px" width="6px" style="fill:393939"/>-->
<#--Red protein family icon-->
    <svg id="ico_type_family_small" width="11" height="11" viewBox="0 0 100 106">
        <g>
            <linearGradient id="family_gradient" gradientUnits="userSpaceOnUse" x1="50.0942" y1="102.2583"
                            x2="50.0942" y2="3.5391">
                <stop offset="0" style="stop-color:#D21614"/>
                <stop offset="1" style="stop-color:#FF3603"/>
            </linearGradient>
            <path fill="url(#family_gradient)" d="M16.923,102.258c-8.969,0-16.265-6.863-16.265-15.3V18.841c0-8.438,7.296-15.302,16.265-15.302
			h66.341c8.969,0,16.266,6.865,16.266,15.302v68.118c0,8.437-7.297,15.3-16.266,15.3H16.923z"/>
            <path fill="#B00D0C" d="M83.265,4.039c8.693,0,15.766,6.64,15.766,14.802v68.118c0,8.16-7.072,14.8-15.766,14.8H16.923
			c-8.693,0-15.765-6.64-15.765-14.8V18.841c0-8.162,7.072-14.802,15.765-14.802H83.265 M83.265,3.039H16.923
			c-9.26,0-16.765,7.073-16.765,15.802v68.118c0,8.723,7.505,15.8,16.765,15.8h66.341c9.258,0,16.766-7.077,16.766-15.8V18.841
			C100.03,10.111,92.522,3.039,83.265,3.039L83.265,3.039z"/>
        </g>
        <text transform="matrix(1 0 0 1 23.4287 86.6528)" fill="#FFFFFF" font-family="Verdana, Helvetica, sans-serif"
              font-size="93.9316">F
        </text>
    </svg>
<#--Green protein domain icon-->
    <svg id="ico_type_domain_small" width="11" height="11" viewBox="0 0 100 106">
        <g>
            <linearGradient id="domain_gradient" gradientUnits="userSpaceOnUse" x1="50.0315" y1="3.7998"
                            x2="50.0315" y2="102.729" gradientTransform="matrix(1 0 0 -1 0.0449 106.0391)">
                <stop offset="0" style="stop-color:#54B533"/>
                <stop offset="1" style="stop-color:#4FD624"/>
            </linearGradient>
            <path fill="url(#domain_gradient)" d="M16.837,102.239c-8.987,0-16.3-6.878-16.3-15.333v-68.26c0-8.456,7.313-15.336,16.3-15.336h66.481
			c8.987,0,16.299,6.88,16.299,15.336v68.26c0,8.455-7.312,15.333-16.299,15.333H16.837z"/>
            <path fill="#34A218" d="M83.318,3.81c8.712,0,15.799,6.655,15.799,14.836v68.26c0,8.179-7.087,14.833-15.799,14.833H16.837
				c-8.712,0-15.8-6.654-15.8-14.833v-68.26c0-8.181,7.088-14.836,15.8-14.836H83.318 M83.318,2.81H16.837
				c-9.279,0-16.8,7.087-16.8,15.836v68.26c0,8.742,7.521,15.833,16.8,15.833h66.481c9.274,0,16.799-7.091,16.799-15.833v-68.26
				C100.117,9.897,92.594,2.81,83.318,2.81L83.318,2.81z"/>
        </g>
        <text transform="matrix(1 0 0 1 18.1262 88.5439)" fill="#FFFFFF" font-family="Verdana, Helvetica, sans-serif"
              font-size="94.1286">D
        </text>
    </svg>
<#--Grey un-integrated match icon-->
    <svg id="ico_type_uni_small" width="11" height="11" viewBox="0 0 100 106">
        <g>
            <linearGradient id="uni_gradient" gradientUnits="userSpaceOnUse" x1="50.0273" y1="102.2642" x2="50.0273"
                            y2="3.3105">
                <stop offset="0" style="stop-color:#8E8E8E"/>
                <stop offset="1" style="stop-color:#A0A0A0"/>
            </linearGradient>
            <path fill="url(#uni_gradient)" d="M16.777,102.264c-8.989,0-16.302-6.88-16.302-15.337V18.65c0-8.458,7.313-15.34,16.302-15.34h66.505
		c8.986,0,16.297,6.881,16.297,15.34v68.277c0,8.457-7.311,15.337-16.297,15.337H16.777z"/>
            <g>
                <path fill="#595E63" d="M83.282,3.81c8.71,0,15.797,6.657,15.797,14.84v68.277c0,8.181-7.087,14.837-15.797,14.837H16.777
			c-8.713,0-15.802-6.656-15.802-14.837V18.65c0-8.183,7.089-14.84,15.802-14.84H83.282 M83.282,2.81H16.777
			c-9.276,0-16.802,7.089-16.802,15.84v68.277c0,8.745,7.526,15.837,16.802,15.837h66.505c9.271,0,16.797-7.092,16.797-15.837V18.65
			C100.079,9.899,92.554,2.81,83.282,2.81L83.282,2.81z"/>
            </g>
        </g>
        <text transform="matrix(1 0 0 1 19.7798 89.8755)" fill="#FFFFFF" font-family="Verdana, Helvetica, sans-serif"
              font-size="94.1525">?
        </text>
    </svg>
<#--Blue protein type icon -->
    <svg id="ico_type_protein" width="20" height="20" viewBox="0 0 100 106">
        <g>
            <linearGradient id="protein_type_gradient" gradientUnits="userSpaceOnUse" x1="50.0315" y1="3.7998"
                            x2="50.0315"
                            y2="102.729" gradientTransform="matrix(1 0 0 -1 0.0449 106.0391)">
                <stop offset="0" style="stop-color:#02AEFF"/>
                <stop offset="1" style="stop-color:#5BC3EA"/>
            </linearGradient>
            <path fill="url(#protein_type_gradient)" d="M16.837,102.239c-8.987,0-16.3-6.878-16.3-15.333v-68.26c0-8.456,7.313-15.336,16.3-15.336h66.481
			c8.987,0,16.299,6.88,16.299,15.336v68.26c0,8.455-7.312,15.333-16.299,15.333H16.837z"/>
            <path fill="#2E8EE5" d="M83.318,3.81c8.712,0,15.799,6.655,15.799,14.836v68.26c0,8.179-7.087,14.833-15.799,14.833H16.837
				c-8.712,0-15.8-6.654-15.8-14.833v-68.26c0-8.181,7.088-14.836,15.8-14.836H83.318 M83.318,2.81H16.837
				c-9.279,0-16.8,7.087-16.8,15.836v68.26c0,8.742,7.521,15.833,16.8,15.833h66.481c9.274,0,16.799-7.091,16.799-15.833v-68.26
				C100.117,9.897,92.594,2.81,83.318,2.81L83.318,2.81z"/>
            <text transform="matrix(1 0 0 1 22.0505 88.5439)" fill="#FFFFFF"
                  font-family="Verdana, Helvetica, sans-serif"
                  font-size="94.1286">P
            </text>
        </g>
    </svg>
<#--Orange protein repeats icon -->
    <svg id="ico_type_repeat_small" width="11" height="11" viewBox="0 0 100 106">
        <g>
            <linearGradient id="repeat_gradient" gradientUnits="userSpaceOnUse" x1="50.0315" y1="3.7998" x2="50.0315"
                            y2="102.729" gradientTransform="matrix(1 0 0 -1 0.0449 106.0391)">
                <stop offset="0" style="stop-color:#F66D1C"/>
                <stop offset="0.9879" style="stop-color:#FFB001"/>
            </linearGradient>
            <path fill="url(#repeat_gradient)" d="M16.837,102.239c-8.987,0-16.3-6.878-16.3-15.333v-68.26c0-8.456,7.313-15.336,16.3-15.336h66.481
			c8.987,0,16.299,6.88,16.299,15.336v68.26c0,8.455-7.312,15.333-16.299,15.333H16.837z"/>
            <path fill="#B27015" d="M83.318,3.81c8.712,0,15.799,6.655,15.799,14.836v68.26c0,8.179-7.087,14.833-15.799,14.833H16.837
				c-8.712,0-15.8-6.654-15.8-14.833v-68.26c0-8.181,7.088-14.836,15.8-14.836H83.318 M83.318,2.81H16.837
				c-9.279,0-16.8,7.087-16.8,15.836v68.26c0,8.742,7.521,15.833,16.8,15.833h66.481c9.274,0,16.799-7.091,16.799-15.833v-68.26
				C100.117,9.897,92.594,2.81,83.318,2.81L83.318,2.81z"/>
            <text transform="matrix(1 0 0 1 16.8215 88.5439)" fill="#FFFFFF"
                  font-family="Verdana, Helvetica, sans-serif"
                  font-size="94.1286">R
            </text>
        </g>
    </svg>
<#--Purple protein site icon -->
    <svg id="ico_type_site_small" width="11" height="11" viewBox="0 0 100 106">
        <g>
            <linearGradient id="site_gradient" gradientUnits="userSpaceOnUse" x1="50.0315" y1="3.7998" x2="50.0315"
                            y2="102.729" gradientTransform="matrix(1 0 0 -1 0.0449 106.0391)">
                <stop offset="0" style="stop-color:#9F31C0"/>
                <stop offset="1" style="stop-color:#CE6FE1"/>
            </linearGradient>
            <path fill="url(#site_gradient)" d="M16.837,102.239c-8.987,0-16.3-6.878-16.3-15.333v-68.26c0-8.456,7.313-15.336,16.3-15.336h66.481
			c8.987,0,16.299,6.88,16.299,15.336v68.26c0,8.455-7.312,15.333-16.299,15.333H16.837z"/>
            <path fill="#6312A1" d="M83.318,3.81c8.712,0,15.799,6.655,15.799,14.836v68.26c0,8.179-7.087,14.833-15.799,14.833H16.837
				c-8.712,0-15.8-6.654-15.8-14.833v-68.26c0-8.181,7.088-14.836,15.8-14.836H83.318 M83.318,2.81H16.837
				c-9.279,0-16.8,7.087-16.8,15.836v68.26c0,8.742,7.521,15.833,16.8,15.833h66.481c9.274,0,16.799-7.091,16.799-15.833v-68.26
				C100.117,9.897,92.594,2.81,83.318,2.81L83.318,2.81z"/>
            <text transform="matrix(1 0 0 1 19.4333 88.5439)" fill="#FFFFFF"
                  font-family="Verdana, Helvetica, sans-serif"
                  font-size="94.1286">S
            </text>
        </g>
    </svg>
</defs>