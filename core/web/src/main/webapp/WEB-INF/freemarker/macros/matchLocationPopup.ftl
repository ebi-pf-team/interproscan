<#macro matchLocationPopup matchPopupId proteinAc signature location colourClass>
    <#assign title=signature.ac>
<#--Signatures like "G3DSA:3.20.20.80" cause issues, remove special characters -->
<#--TODO? Try http://stackoverflow.com/questions/296264/using-regular-expressions-in-jsp-el-->
    <#assign prefix=signature.ac?replace(":","")?replace(".","")>

    <script type="text/javascript">

    </script>

    <#if standalone>
    <div id="${matchPopupId}" style="display: none;">
    <#else>
    <div id="${matchPopupId}">
    </#if>

        <div class="popup_topl"><span class="${colourClass} caption_puce"></span>${location.start} - ${location.end}</div>
        <div class="popup_botl" style="font-size:88%;">
            <b>${signature.dataSource.sourceName}</b> <abbr class="icon icon-generic" data-icon="i" title="${signature.dataSource.description}"></abbr> <br/>

            <#assign linkUrl=signature.dataSource.getLinkUrl(signature.ac)>
            <#if signature.ac?starts_with("mobidb")>
                <!-- Always link using signature accession except for MobiDB: in InterProScan HTML output show homepage, in web protein page show use protein accession-->
                <#if standalone>
                    <#assign linkUrl=signature.dataSource.getHomeUrl()>
                <#else>
                    <#assign linkUrl=signature.dataSource.getLinkUrl(proteinAc)>
                </#if>
            </#if>
            <a href='${linkUrl}' class="ext">${signature.ac} </a>
            <span>(<#if signature.name??>${signature.name}<#else>${signature.ac}</#if>)</span>
        </div>
    </div>
</#macro>
