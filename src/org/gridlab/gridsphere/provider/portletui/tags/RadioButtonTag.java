/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.tags;

import org.gridlab.gridsphere.provider.portletui.beans.RadioButtonBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.List;
import java.util.Iterator;

/**
 * A <code>RadioButtonTag</code> represents a radio button element
 */
public class RadioButtonTag extends BaseComponentTag {

    protected RadioButtonBean radiobutton = null;
    protected boolean selected = false;

    /**
     * Sets the selected status of the bean
     *
     * @param flag status of the bean
     */
    public void setSelected(boolean flag) {
        this.selected = flag;
    }

    /**
     * Returns the selected status of the bean
     *
     * @return selected status
     */
    public boolean isSelected() {
        return selected;
    }

    public int doStartTag() throws JspException {

        if (!beanId.equals("")) {
            radiobutton = (RadioButtonBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (radiobutton == null) {
                radiobutton = new RadioButtonBean((HttpServletRequest) pageContext.getRequest(), beanId);
                this.setBaseComponentBean(radiobutton);
            } else {
                //this.updateBaseComponentBean(radiobutton);
                this.overrideBaseComponentBean(radiobutton);
            }
            List vals = radiobutton.getSelectedValues();
            if (vals.contains(value)) {
                radiobutton.setSelected(true);
            } else {
                //if (!selected) selected = radiobutton.isSelected();
                if (vals.isEmpty()) radiobutton.setSelected(selected);
            }

        } else {
            radiobutton = new RadioButtonBean();
            radiobutton.setSelected(selected);
            this.setBaseComponentBean(radiobutton);
        }

        //debug();


        try {
            JspWriter out = pageContext.getOut();
            out.print(radiobutton.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        return SKIP_BODY;
    }

}
