/*
 * Created by IntelliJ IDEA.
 * User: russell
 * Date: Jan 31, 2003
 * Time: 4:06:44 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.gridlab.gridsphere.services.core.security.acl;

import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.service.PortletService;

import java.util.List;
import java.util.Set;

public interface AccessControlManagerService extends PortletService {

    /*** PORTLET GROUP METHODS ***/

    public List getGroups();

    public void modifyGroupAccess(PortletGroup group, boolean isPublic);

    public void deleteGroup(PortletGroup group);

    public PortletGroup getGroup(String groupId);

    public PortletGroup getGroupByName(String groupName);

    /*** GROUP REQUEST METHODS ***/

    public List getGroupRequests();

    public List getGroupRequests(User user);

    public List getGroupRequests(PortletGroup group);

    public List getGroupRequestsForGroups(List groups);

    public GroupRequest getGroupRequest(String id);

    public GroupRequest createGroupRequest();

    public GroupRequest createGroupRequest(GroupEntry entry);

    public void validateGroupRequest(GroupRequest request)
            throws InvalidGroupRequestException;

    public void submitGroupRequest(GroupRequest request)
            throws InvalidGroupRequestException;


    public void approveGroupRequest(GroupRequest request);

    public void denyGroupRequest(GroupRequest request);

    /*** GROUP ENTRY METHODS ***/

    public List getGroupEntries();

    public List getGroupEntries(User user);

    public List getGroupEntries(PortletGroup group);

    public GroupEntry getGroupEntry(String id);

    public GroupEntry getGroupEntry(User user, PortletGroup group);

    public void addGroupEntry(User user, PortletGroup group, PortletRole role);

    public void deleteGroupEntry(GroupEntry entry);

    public void deleteGroupEntries(User user);

    /*** ACCESS CONTROL LOGIC METHODS ***/

    public List getGroups(User user);

    public List getGroupsNotMemberOf(User user);

    public List getUsers(PortletGroup group);

    public List getUsers(PortletGroup group, PortletRole role);

    public List getUsersNotInGroup(PortletGroup group);

    public boolean isUserInGroup(User user, PortletGroup group);

    public PortletRole getRoleInGroup(User user, PortletGroup group);

    public boolean hasRoleInGroup(User user, PortletGroup group, PortletRole role);

    public boolean hasAdminRoleInGroup(User user, PortletGroup group);

    public boolean hasUserRoleInGroup(User user, PortletGroup group);

    public boolean hasGuestRoleInGroup(User user, PortletGroup group);

    public List getUsersWithSuperRole();

    public void grantSuperRole(User user);

    public boolean hasSuperRole(User user);
}
