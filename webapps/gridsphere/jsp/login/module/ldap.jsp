<ui:frame>
    <ui:tablerow>
        <ui:tablecell width="5%">
            <ui:checkbox beanId="ldapCheck" name="auth_module" value="Hello" selected="<%= active %>"/>
        </ui:tablecell>
        <ui:tablecell width="45%">
            <ui:text key="LOGIN_LDAP_MODULE"/>
        </ui:tablecell>
        <ui:tablecell width="50%">
            <ui:actionlink action="config_ldap" key="LOGIN_MODULE_ACTION"/>
        </ui:tablecell>
    </ui:tablerow>
</ui:frame>