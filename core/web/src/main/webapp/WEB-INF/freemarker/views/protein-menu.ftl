<#--
  Created by IntelliJ IDEA.
  User: pjones
  Date: 08/02/12
  Time: 14:58
  To change this template use File | Settings | File Templates.
-->
<div id="menu" style="display: none;">
    <div class="menu-filter">

        <h1>Filter view on</h1>

        <div class="menu-filter-type"><h1>Entry type</h1>

            <fieldset>
                <div ${protein.disabledStyleIfNoMatches("FAMILY")}>
                    <input type="checkbox" class="type" id="check-2"
                           value="Family-row" ${protein.disableIfNoMatches("FAMILY")}/>
                    <label for="check-2" class="family">Family</label>
                </div>
                <div ${protein.disabledStyleIfNoMatches("DOMAIN")}>
                    <input type="checkbox" class="type" id="check-3" value="Domain-row"
                           checked="checked" ${protein.disableIfNoMatches("DOMAIN")}/>
                    <label for="check-3" class="domains">Domains</label>
                </div>
                <div ${protein.disabledStyleIfNoMatches("REPEAT")}>
                    <input type="checkbox" class="type" id="check-4" value="Repeat-row"
                           checked="checked" ${protein.disableIfNoMatches("REPEAT")}/>
                    <label for="check-4" class="repeats">Repeats</label>
                </div>
                <div ${protein.disabledStyleIfNoMatches("SITE")}>
                    <input type="checkbox" class="type" id="check-5"
                           value="SITE-row Conserved_site-row PTM-row Binding_site-row Active_site-row"
                           checked="checked" ${protein.disableIfNoMatches("SITE")}/>
                    <label for="check-5" class="site">Site</label>
                </div>
            </fieldset>
        </div>


        <div class="menu-filter-status"><h1>Status</h1>

            <fieldset>
                <div ${protein.disabledStyleIfNoMatches("UNKNOWN")}>
                    <input type="checkbox" name="type" id="check-6"
                           value="unintegrated" ${protein.disableIfNoMatches("UNKNOWN")}/>
                    <label for="check-6" class="uni">Unintegrated</label>
                </div>
            </fieldset>
        </div>

        <div class="menu-filter-colour">
            <div style="border:1px pink solid; overflow:auto; background-color: #f2f6f8; border: 1px solid #9eb6ce; padding: 4px 10px;margin: 0;">
                <div style=" float:left;color: #2e5882; font-size: 110%;  font-weight: bold;margin-bottom: 0px;">
                    Colour by
                </div>

                <#if ! standalone>
                    <div style=" float:right;margin-bottom: 0px;"><a href="http://wwwdev.ebi.ac.uk/interpro/user_manual.html#25"
                                                                 target="_blank">help</a></div>
                </#if>
            </div>
            <fieldset>
                <div style="background-color:white;">
                    <div>
                        <input type="radio" name="blobColour" id="domainColour" value="domainColour" checked="checked"/>
                        <label for="domainColour">domain relationship</label>
                    </div>
                    <div>
                        <input type="radio" name="blobColour" id="databaseColour" value="databaseColour"/>
                        <label for="databaseColour">source database</label>
                    </div>
                </div>
            </fieldset>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function() {
// Read colour preference from cookie (requires http://plugins.jquery.com/project/Cookie)

            // Retrieve existing cookies and set checkbox states accordingly
            var checkBoxIDs = ["#check-2", "#check-3", "#check-4", "#check-5", "#check-6"];
            for (i = 0; i < checkBoxIDs.length; i++) {
                var checkBoxId = checkBoxIDs[i];
                var cookieVal = $.cookie(checkBoxId);
                if (cookieVal != null) {
                    $(checkBoxId).prop("checked", cookieVal == "true");
                }
            }

            // and the radio button group
            var radioCookieVal = $.cookie("colour-by-domain");
            if (radioCookieVal != null) {
                if (radioCookieVal == "true") {
                    $('#domainColour').prop("checked", true);
                }
                else {
                    $('#databaseColour').prop("checked", true);
                }
            }

            // Keep the filter menu in view
            $("#menu").jScroll();
            // CSS switching
            configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour'); // initialise

            $('input[name="blobColour"]').change(function() {
                configureStylesheets($('input[name="blobColour"]:checked').attr('id') == 'domainColour');
            });

            // Change event for type checkboxes (Family, Domain etc.)
            $(".type").change(function() {
                displayType(this);
            });

            // Initialise types
            $(".type").each(function() {
                displayType(this);
            });

            // Change event for un-integrated sig matches checkbox
            $("#check-6").change(function() {
                displayUnintegrated(this);
            });

            // Initialise un-integrated.
            // TODO Work out how to do this without each! Only one thing with an ID of "check-6"!
            $("#check-6").each(function() {
                displayUnintegrated(this);
            });

        // Make the menu visible - Javascript is enabled.
        $("#menu").css('display', 'block');
    });
</script>
