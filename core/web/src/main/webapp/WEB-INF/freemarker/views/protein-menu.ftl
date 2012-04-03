<#--
  Created by IntelliJ IDEA.
  User: pjones
  Date: 08/02/12
  Time: 14:58
  To change this template use File | Settings | File Templates.
-->
<div id="menu">
    <div class="menu-filter">

        <h1>Filter view on</h1>

        <div class="menu-filter-type"><h1>Entry type</h1>

            <fieldset>
                <div>
                    <input type="checkbox" class="type" id="check-2" value="Family-row"/>
                    <label for="check-2" class="family">Family</label>
                </div>
                <div>
                    <input type="checkbox" class="type" id="check-3" value="Domain-row" checked="checked"/>
                    <label for="check-3" class="domains">Domains</label>
                </div>
                <div>
                    <input type="checkbox" class="type" id="check-4" value="Repeat-row" checked="checked"/>
                    <label for="check-4" class="repeats">Repeats</label>
                </div>
                <div>
                    <input type="checkbox" class="type" id="check-5"
                           value="Conserved_site-row PTM-row Binding_site-row Active_site-row" checked="checked"/>
                    <label for="check-5" class="site">Site</label>
                </div>
            </fieldset>
        </div>


        <div class="menu-filter-status"><h1>Status</h1>

            <fieldset>
                <div>
                    <input type="checkbox" name="type" id="check-6" value="unintegrated"/>
                    <label for="check-6" class="uni">Unintegrated</label>
                </div>
            </fieldset>
        </div>

        <div class="menu-filter-colour">
            <div style="border:1px pink solid; overflow:auto; background-color: #f2f6f8; border: 1px solid #9eb6ce; padding: 4px 10px;margin: 0;">
            <div style=" float:left;color: #2e5882; font-size: 110%;  font-weight: bold;margin-bottom: 0px;">Colours</div>
            <div style=" float:right;margin-bottom: 0px;"><a href="../../resources/images/colour_key_MD.png" target="_blank">view key</a></div></div>
             <fieldset>
            <div style="background-color:white;">
            <div >
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