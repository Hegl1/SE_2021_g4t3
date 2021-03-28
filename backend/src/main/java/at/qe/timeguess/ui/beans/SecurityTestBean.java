package at.qe.timeguess.ui.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Basic request scoped bean to test bean initialization.
 *
 * This class is part of the skeleton project provided for students of the
 * courses "Software Architecture" and "Software Engineering" offered by the
 * University of Innsbruck.
 */
@Component
@Scope("request")
public class SecurityTestBean {

    private boolean showOkDialog = false;
    private String performedAction = "NONE";

    // :TODO: Fix the corresponding vulnerability , cf. http://localhost:8080/secured/test.xhtml
    private String testString = "CHANGE ME";

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public boolean isShowOkDialog() {
        return showOkDialog;
    }

    public String getPerformedAction() {
        return performedAction;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void doAdminAction() {
        performedAction = "ADMIN";
        showOkDialog = true;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    public void doManagerAction() {
        performedAction = "MANAGER";
        showOkDialog = true;
    }

    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public void doEmployeeAction() {
        performedAction = "EMPLOYEE";
        showOkDialog = true;
    }

    public void doHideOkDialog() {
        showOkDialog = false;
    }

}
