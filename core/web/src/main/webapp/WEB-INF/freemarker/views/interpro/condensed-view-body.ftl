<#import "../../macros/condensedView.ftl" as condensedViewMacro/>

<#if condensedView?has_content>

    <@condensedViewMacro.condensedView condensedView=condensedView scale=scale entryColours=entryColours idPrefix="dr" />

<script type="text/javascript">
    $(document).ready(function () {
        <#if standalone>
            // Use hidden DIVs to display popups
            $('span[id*="span-"]').each(
                    function(i) {
                        <#if condensedView??>
                            preparePopup(this.id, ${condensedView.numSuperMatchBlobs});
                        <#else>
                            // No supermatches for this protein, but there are structural matches (e.g. B7ZMM2 as of InterPro release 37.0)
                            preparePopup(this.id, 0);
                        </#if>
                    }
            );
        <#else>
            // Use AJAX call to display popups
            $('a[id*="location-"]').each(
                    function(i) {
                        preparePopup(this.id);
                    }
            );
        </#if>
    });
</script>

<#else>
<#-- We have no information for the specified protein accession at all - not found -->
<p>
    No InterPro domains.
</p>
</#if>
