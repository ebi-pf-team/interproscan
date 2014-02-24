<#macro supermatchLocationPopup supermatchPopupId supermatch colourClass>
    <#assign title=supermatch.type>
    <#assign locationObj=supermatch.location>

    <#--Show detailed hierarchy information in a popup using hidden DIV-->
    <div id="${supermatchPopupId}" style="display: none;">
        <div class="popup_topl">
            <span class="${colourClass} caption_puce"></span>${locationObj.start} - ${locationObj.end}
        </div>
        <div class="rel_tree">${supermatch.entryHierarchyForPopup}</div>
    </div>

</#macro>
