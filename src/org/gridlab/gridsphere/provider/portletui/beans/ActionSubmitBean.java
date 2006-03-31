/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.provider.portletui.beans;

/**
 * An <code>ActionSubmitBean</code> is a visual bean that represents an HTML button and
 * has an associated <code>DefaultPortletAction</code>
 */
public class ActionSubmitBean extends ActionBean implements TagBean {

    public static final String SUBMIT_STYLE = "portlet-form-button";
    public static final String NAME = "as";

    /**
     * Constructs a default action submit bean
     */
    public ActionSubmitBean() {
        super(NAME);
        this.cssClass = SUBMIT_STYLE;
    }

    /**
     * Constructs an action submit bean from a supplied portlet request and bean identifier
     *
     * @param beanId the bean identifier
     */
    public ActionSubmitBean(String beanId) {
        super(NAME);
        this.cssClass = SUBMIT_STYLE;
        this.beanId = beanId;
    }

    public String toStartString() {
        return "<input " + getFormattedCss() + " type=\"submit\" " + checkDisabled();
    }

    public String toEndString() {
        String pname = (name == null) ? "" : name;
        String sname = pname;
        StringBuffer sb = new StringBuffer();
        if (action != null) sname = action;
        if (anchor != null) sname += "#" + anchor;
        if (onClick != null) {
            // 'onClick' replaced by 'onclick' for XHTML 1.0 Strict compliance
            sb.append(" onclick='" + onClick + "' ");
        }
        sb.append("name=\"" + sname + "\" value=\"" + value + "\"/>");
        return sb.toString();
    }

}
