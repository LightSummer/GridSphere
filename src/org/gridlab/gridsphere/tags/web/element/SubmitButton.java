/**
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */

package org.gridlab.gridsphere.tags.web.element;

public class SubmitButton extends BaseButton {

    public String toString() {
        this.type = "submit";
        return super.toString();
    }
}
