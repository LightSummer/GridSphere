package org.gridsphere.portlets.core.news;

import org.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridsphere.provider.event.jsr.FormEvent;
import org.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridsphere.services.core.jcr.ContentDocument;
import org.gridsphere.services.core.jcr.ContentException;
import org.gridsphere.services.core.jcr.JCRService;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import java.util.List;

/*
* @author <a href="mailto:wehrens@gridsphere.org">Oliver Wehrens</a>
* @version $Id$
*/
public class NewsPortlet extends ActionPortlet {

    private JCRService jcrService = null;
    private String document = "MessageOfTheDay";
    //private String 

    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        DEFAULT_VIEW_PAGE = "doView";
        DEFAULT_EDIT_PAGE = "doConfigure";
        jcrService = (JCRService) createPortletService(JCRService.class);
    }


    public void doView(RenderFormEvent event) throws PortletException {
        String content = jcrService.getContent(document);
        event.getRenderRequest().setAttribute("document", content);
        setNextState(event.getRenderRequest(), "news/view.jsp");
    }

    public void doMyConfigure(FormEvent event) throws PortletException {
        ListBoxBean docList = event.getListBoxBean("document");
        List<ContentDocument> allDocs = null;
        try {
            allDocs = jcrService.listChildContentDocuments("");
            for (int i = 0; i < allDocs.size(); i++) {
                ListBoxItemBean item = new ListBoxItemBean();
                item.setValue(allDocs.get(i).getTitle());
                item.setName(allDocs.get(i).getTitle());
                if (allDocs.get(i).getTitle().equals(document)) item.setSelected(true);
                docList.addBean(item);
            }
        } catch (ContentException e) {
            e.printStackTrace();
            createErrorMessage(event, "Could not get list of documents.");
        }
    }

    public void doConfigure(ActionFormEvent event) throws PortletException {
        doMyConfigure(event);
        setNextState(event.getActionRequest(), "news/admin.jsp");
    }

    public void doConfigure(RenderFormEvent event) throws PortletException {
        doMyConfigure(event);
        setNextState(event.getRenderRequest(), "news/admin.jsp");
    }

    public void doSave(ActionFormEvent event) throws PortletException {
        ListBoxBean cmsDocument = event.getListBoxBean("document");
        document = cmsDocument.getSelectedName();
        event.getActionResponse().setPortletMode(PortletMode.VIEW);
        setNextState(event.getActionRequest(), "doView");
    }

}