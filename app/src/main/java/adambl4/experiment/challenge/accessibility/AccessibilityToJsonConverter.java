package adambl4.experiment.challenge.accessibility;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;

import com.google.gson.JsonObject;

/**
 * Created by Адамыч on 16.10.2015.
 */
public class AccessibilityToJsonConverter {
    public String getJsonString(AccessibilityEvent event) {
        return getJsonObjectFromEvent(event).toString();
    }

    public String getJsonString(AccessibilityNodeInfo node) {
        return getJsonObjectFromNodeInfo(node).toString();
    }

    private JsonObject getJsonObjectFromEvent(AccessibilityEvent event) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("getEventType", AccessibilityEvent.eventTypeToString(event.getEventType()));
        jsonObject.addProperty("getClassName", String.valueOf(event.getClassName()));
        jsonObject.addProperty("getAction", String.valueOf(event.getAction()));
        jsonObject.addProperty("getContentChangeTypes", String.valueOf(event.getContentChangeTypes()));
        //jsonObject.addProperty("getMovementGranularity", String.valueOf(event.getMovementGranularity()));
        jsonObject.addProperty("getRecordCount", String.valueOf(event.getRecordCount()));
        //jsonObject.addProperty("getMovementGranularity", String.valueOf(event.getMovementGranularity()));
        //jsonObject.addProperty("getParcelableData", String.valueOf(event.getParcelableData()));
        //jsonObject.addProperty("getAddedCount", String.valueOf(event.getAddedCount()));
        //jsonObject.addProperty("getBeforeText", String.valueOf(event.getBeforeText()));
        jsonObject.addProperty("getCurrentItemIndex", String.valueOf(event.getCurrentItemIndex()));
        jsonObject.addProperty("getFromIndex", String.valueOf(event.getFromIndex()));
        jsonObject.addProperty("getToIndex", String.valueOf(event.getToIndex()));
        jsonObject.addProperty("getItemCount", String.valueOf(event.getItemCount()));
        //jsonObject.addProperty("getMaxScrollX", String.valueOf(event.getMaxScrollX()));
        //jsonObject.addProperty("getMaxScrollY", String.valueOf(event.getMaxScrollY()));
        //jsonObject.addProperty("getRemovedCount", String.valueOf(event.getRemovedCount()));
        //jsonObject.addProperty("getScrollX", String.valueOf(event.getScrollX()));
        //jsonObject.addProperty("getScrollY", String.valueOf(event.getScrollY()));
        //jsonObject.addProperty("getWindowId", String.valueOf(event.getWindowId()));
        //jsonObject.addProperty("getText", event.getText().toString());

        event.getMovementGranularity();
        event.getBeforeText();
        AccessibilityNodeInfo source = event.getSource();
        if (source != null) {
            jsonObject.add("EVENT_SOURCE", getJsonObjectFromNodeInfo(source));
        }
        return jsonObject;
    }

    private JsonObject getJsonObjectFromNodeInfo(AccessibilityNodeInfo source) {
        if (source == null) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("hashCode", Integer.toHexString(source.hashCode()));
        jsonObject.addProperty("getPackageName", String.valueOf(source.getPackageName()));
        jsonObject.addProperty("getClassName", String.valueOf(source.getClassName()));
        jsonObject.addProperty("getViewIdResourceName", source.getViewIdResourceName());
        //jsonObject.addProperty("getInputType", source.getInputType());
        //jsonObject.addProperty("getLiveRegion", source.getLiveRegion());
        //jsonObject.addProperty("getMaxTextLength", source.getMaxTextLength());
        //jsonObject.addProperty("getMovementGranularities", source.getMovementGranularities());
        //jsonObject.addProperty("getTextSelectionStart", source.getTextSelectionStart());
        //jsonObject.addProperty("getTextSelectionEnd", source.getTextSelectionEnd());
        //jsonObject.addProperty("getError", String.valueOf(source.getError()));
        jsonObject.addProperty("getExtras", source.getExtras().toString());

        AccessibilityNodeInfo.RangeInfo rangeInfo = source.getRangeInfo();
        if(rangeInfo != null){
            jsonObject.addProperty("rangeInfo.getCurrent()", rangeInfo.getCurrent());
            jsonObject.addProperty("rangeInfo.getMax()", rangeInfo.getMax());
            jsonObject.addProperty("rangeInfo.getMin()", rangeInfo.getMin());
            jsonObject.addProperty("rangeInfo.getType()", rangeInfo.getType());
        }

        AccessibilityNodeInfo.CollectionInfo collectionInfo = source.getCollectionInfo();
        if(collectionInfo != null){
            jsonObject.addProperty("collectionInfo.getColumnCount()", collectionInfo.getColumnCount());
            jsonObject.addProperty(" collectionInfo.getRowCount()", collectionInfo.getRowCount());
            jsonObject.addProperty("collectionInfo.getSelectionMode()", collectionInfo.getSelectionMode());
            jsonObject.addProperty("collectionInfo.isHierarchical()", collectionInfo.isHierarchical());

        }

        AccessibilityNodeInfo.CollectionItemInfo collectionItemInfo = source.getCollectionItemInfo();

        if(collectionItemInfo != null){
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.getColumnIndex());
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.getColumnSpan());
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.getRowIndex());
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.getRowSpan());
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.isHeading());
            jsonObject.addProperty("collectionItemInfo.getSelectionMode()", collectionItemInfo.isSelected());
        }


        CharSequence text = source.getText();
        CharSequence description = source.getContentDescription();

        jsonObject.addProperty("text", text != null ? text.toString() : "null");
        jsonObject.addProperty("description", description != null ? description.toString() : "null");


        Rect rectInParent = new Rect();
        Rect rectInScreen = new Rect();
        source.getBoundsInParent(rectInParent);
        source.getBoundsInScreen(rectInScreen);

        jsonObject.addProperty("getBoundsInParent", rectInParent.flattenToString());
        jsonObject.addProperty("getBoundsInScreen", rectInScreen.flattenToString());


        jsonObject.addProperty("isFocusable", source.isFocusable());
        jsonObject.addProperty("isFocused", source.isFocused());
        jsonObject.addProperty("isAccessibilityFocused", source.isAccessibilityFocused());
        jsonObject.addProperty("isCheckable", source.isCheckable());
        jsonObject.addProperty("isChecked", source.isChecked());
        jsonObject.addProperty("isClickable", source.isClickable());
        jsonObject.addProperty("isContentInvalid", source.isContentInvalid());
        //jsonObject.addProperty("isContextClickable", source.isContextClickable());
        jsonObject.addProperty("isDismissable", source.isDismissable());
        jsonObject.addProperty("isEditable", source.isEditable());
        jsonObject.addProperty("isEnabled", source.isEnabled());
        jsonObject.addProperty("isLongClickable", source.isLongClickable());
        jsonObject.addProperty("isMultiLine", source.isMultiLine());
        jsonObject.addProperty("isPassword", source.isPassword());
        jsonObject.addProperty("isScrollable", source.isScrollable());
        jsonObject.addProperty("isSelected", source.isSelected());
        jsonObject.addProperty("isVisibleToUser", source.isVisibleToUser());

        StringBuilder stringBuilder = new StringBuilder();
        for (AccessibilityNodeInfo.AccessibilityAction accessibilityAction : source.getActionList()) {
            stringBuilder.append(",").append(accessibilityAction.toString());
        }
        jsonObject.addProperty("getActionList", stringBuilder.toString());


        // jsonObject.add("getLabeledBy", getJsonObjectFromNodeInfo(source.getLabeledBy()));
        //jsonObject.add("getLabelFor", getJsonObjectFromNodeInfo(source.getLabelFor()));
        //jsonObject.add("getTraversalAfter", getJsonObjectFromNodeInfo(source.getTraversalAfter()));
        //jsonObject.add("getTraversalBefore", getJsonObjectFromNodeInfo(source.getTraversalBefore()));


        jsonObject.addProperty("getChildCount", source.getChildCount());
        if (source.getChildCount() > 0) {
            JsonObject childs = new JsonObject();
            for (int i = 0; i < source.getChildCount(); i++) {
                AccessibilityNodeInfo child = source.getChild(i);
                if (child != null) {
                    childs.add("child " + i, getJsonObjectFromNodeInfo(child));
                }
            }
            jsonObject.add("childs", childs);
        }

        return jsonObject;
    }

    private JsonObject getJsonObjectFromRecord(AccessibilityRecord record) {
        if (record == null) return null;
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("getClassName", String.valueOf(record.getClassName()));
        jsonObject.addProperty("getParcelableData", String.valueOf(record.getParcelableData()));
        jsonObject.addProperty("getAddedCount", String.valueOf(record.getAddedCount()));
        jsonObject.addProperty("getBeforeText", String.valueOf(record.getBeforeText()));
        jsonObject.addProperty("getCurrentItemIndex", String.valueOf(record.getCurrentItemIndex()));
        jsonObject.addProperty("getFromIndex", String.valueOf(record.getFromIndex()));
        jsonObject.addProperty("getToIndex", String.valueOf(record.getToIndex()));
        jsonObject.addProperty("getItemCount", String.valueOf(record.getItemCount()));
        jsonObject.addProperty("getMaxScrollX", String.valueOf(record.getMaxScrollX()));
        jsonObject.addProperty("getMaxScrollY", String.valueOf(record.getMaxScrollY()));
        jsonObject.addProperty("getRemovedCount", String.valueOf(record.getRemovedCount()));
        jsonObject.addProperty("getScrollX", String.valueOf(record.getScrollX()));
        jsonObject.addProperty("getScrollY", String.valueOf(record.getScrollY()));
        jsonObject.addProperty("getWindowId", String.valueOf(record.getWindowId()));


        jsonObject.addProperty("getClassName", String.valueOf(record.getClassName()));

        String text = record.getText() != null ? record.getText().toString() : "null";
        String description = record.getContentDescription() != null ? record.getContentDescription().toString() : "null";

        jsonObject.addProperty("text", text);
        jsonObject.addProperty("description", description);

        return jsonObject;
    }
}
