<#import "../../macros/condensedView.ftl" as condensedViewMacro/>

<#if condensedView?? && condensedView?has_content>

    <@condensedViewMacro.condensedView condensedView=condensedView scale=scale entryColours=entryColours />

    <#else>
    <#-- We have no information for the specified protein accession at all - not found -->
        <p>
            Protein accession not found.
        </p>
</#if>
