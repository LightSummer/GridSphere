/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
package org.gridlab.gridsphere.provider.portletui.tags;

import org.gridlab.gridsphere.provider.portletui.beans.FrameBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextBean;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A <code>FrameTag</code> is a stylized <code>TableTag</code> that can also be used to set text messages
 */
public class FrameTag extends TableTag {

    protected String style = "info";
    protected String key = null;
    protected String value = null;

    /**
     * Sets the key used to locate localized text
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the key used to locate localized text
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the style of the text: Available styles are
     * <ul>
     * <li>error</li>
     * <li>info</li>
     * <li>status</li>
     * <li>alert</li>
     * <li>success</li>
     *
     * @param style the text style
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Returns the style of the text: Available styles are
     * <ul>
     * <li>error</li>
     * <li>info</li>
     * <li>status</li>
     * <li>alert</li>
     * <li>success</li>
     *
     * @return the text style
     */
    public String getStyle() {
        return style;
    }

    public int doStartTag() throws JspException {
        if (style.equalsIgnoreCase("error") || (style.equalsIgnoreCase("err"))) {
            this.cssClass = TextBean.MSG_ERROR;
        } else if (style.equalsIgnoreCase("status")) {
            this.cssClass = TextBean.MSG_STATUS;
        } else if (style.equalsIgnoreCase("info")) {
            this.cssClass = TextBean.MSG_INFO;
        } else if (style.equalsIgnoreCase("alert")) {
            this.cssClass = TextBean.MSG_ALERT;
        } else if (style.equalsIgnoreCase("success")) {
            this.cssClass = TextBean.MSG_SUCCESS;
        }
        if (!beanId.equals("")) {
            tableBean = (FrameBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (tableBean == null) {
                tableBean = new FrameBean(beanId);
                this.setBaseComponentBean(tableBean);
                return SKIP_BODY;
            } else {
                String key = tableBean.getKey();
                if (key != null) {
                    tableBean.setValue(getLocalizedText(key));
                    tableBean.setCssClass(cssClass);
                    tableBean.setCssStyle(cssStyle);
                }

            }
        } else {
            tableBean = new FrameBean();
            tableBean.setWidth(width);
            if (cellSpacing != null) tableBean.setCellSpacing(cellSpacing);
            if (cellPadding != null) tableBean.setCellPadding(cellPadding);
            if (border != null) tableBean.setBorder(border);
            tableBean.setCssClass(cssClass);
            tableBean.setCssStyle(cssStyle);
            this.setBaseComponentBean(tableBean);
            if (key != null) {
                tableBean.setKey(key);
                Locale locale = pageContext.getRequest().getLocale();
                ResourceBundle bundle = ResourceBundle.getBundle("Portlet", locale);
                tableBean.setValue(bundle.getString(tableBean.getKey()));
            }
        }

        PanelTag panelTag = (PanelTag) findAncestorWithClass(this, PanelTag.class);
        if (panelTag != null) {

            int numCols = panelTag.getNumCols();

            int thiscol = panelTag.getColumnCounter();
            try {
                JspWriter out = pageContext.getOut();
                if ((thiscol % numCols) == 0) {
                    out.println("<tr>");
                }
                out.println("<td width=\"" + "50%" + "\">");
            } catch (Exception e) {
                throw new JspException(e.getMessage());
            }
        }


        try {
            JspWriter out = pageContext.getOut();
            out.print(tableBean.toStartString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }


        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {

        if (!beanId.equals("")) {
            tableBean = (FrameBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
            if (tableBean == null) {
                return EVAL_PAGE;
            }
        }

        try {
            JspWriter out = pageContext.getOut();
            out.print(tableBean.toEndString());
        } catch (Exception e) {
            throw new JspException(e.getMessage());
        }

        PanelTag panelTag = (PanelTag) findAncestorWithClass(this, PanelTag.class);
        if (panelTag != null) {
            int numCols = panelTag.getNumCols();
            int thiscol = panelTag.getColumnCounter();
            thiscol++;
            panelTag.setColumnCounter(thiscol);
            try {
                JspWriter out = pageContext.getOut();
                out.println("</td>");
                if ((thiscol % numCols) == 0) {
                    out.println("</tr>");
                }
            } catch (Exception e) {
                throw new JspException(e.getMessage());
            }
        }

        return EVAL_PAGE;
    }
}
