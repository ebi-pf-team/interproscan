<#macro supermatchLocationPopup superMatchPopupId supermatch colourClass>
    <#assign title=supermatch.type>
    <#assign locationObj=supermatch.location>

    <#--Show detailed hierarchy information in a popup using hidden DIV-->
    <#if standalone>
        <div id="${superMatchPopupId}" style="display: none;">
    <#else>
        <div id="${superMatchPopupId}">
    </#if>
        <div class="popup_topl">
            <span class="${colourClass} caption_puce"></span>${locationObj.start} - ${locationObj.end}
        </div>
        <div class="rel_tree">${supermatch.entryHierarchyForPopup}</div>
    </div>

</#macro>
