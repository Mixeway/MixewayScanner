/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.model;

public class DTrackConfigProperty {
    String groupName;
    String propertyName;
    Object propertyValue;

    public DTrackConfigProperty(String groupName, String propertyName, Object propertyValue){
        this.groupName = groupName;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }
}
