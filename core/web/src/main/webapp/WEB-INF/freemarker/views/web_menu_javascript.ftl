<#--
    @author Phil Jones

    This Javascript is used to enable the filter check boxes
    if Javascript is turned on (obviously) and if the type
    for the filter is matched by this protein.
-->
<script type="text/javascript">
    $(document).ready(function() {
        // Make the menu visible - Javascript is enabled.
        $('#menu').css('display', 'block');

    <#if protein.hasMatches("H_SUPERFAMILY")>
        $("#h-superfamily-filter").removeClass("disabled");
        $("#check-1").attr("disabled", false);
    </#if>
    <#if protein.hasMatches("FAMILY")>
        $("#family-filter").removeClass("disabled");
        $("#check-2").attr("disabled", false);
    </#if>
    <#if protein.hasMatches("DOMAIN")>
        $("#domain-filter").removeClass("disabled");
        $("#check-3").attr("disabled", false);
    </#if>
    <#if protein.hasMatches("REPEAT")>
        $("#repeat-filter").removeClass("disabled");
        $("#check-4").attr("disabled", false);
    </#if>
    <#if protein.hasMatches("SITE")>
        $("#site-filter").removeClass("disabled");
        $("#check-5").attr("disabled", false);
    </#if>
    <#if protein.hasMatches("UNKNOWN")>
        $("#unintegrated-filter").removeClass("disabled");
        $("#check-6").attr("disabled", false);
    </#if>
    });
</script>
