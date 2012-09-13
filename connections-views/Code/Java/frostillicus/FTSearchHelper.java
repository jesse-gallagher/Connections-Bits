package frostillicus;

import com.ibm.xsp.component.xp.*;
import com.ibm.xsp.extlib.builder.ControlBuilder;
import com.ibm.xsp.extlib.builder.ControlBuilder.ControlImpl;

import java.util.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class FTSearchHelper {
	private FTSearchHelper() { }

	public static void addColumns(XspViewPanel panel, List<String> columns) {
		ControlImpl<XspViewPanel> viewControl = new ControlImpl<XspViewPanel>(panel);
		for(String columnInfo : columns) {
			String columnName, columnHeader;
			int index = columnInfo.indexOf("|");
			if(index > -1) {
				columnName = columnInfo.substring(index+1);
				columnHeader = columnInfo.substring(0, index);
			} else {
				columnName = columnInfo;
				columnHeader = columnInfo;
			}

			// Create the column
			XspViewColumn column = new XspViewColumn();
			column.setColumnName(columnName);
			column.setId("col" + columnName);
			ControlImpl<UIComponent> cCol = new ControlImpl<UIComponent>(column);

			// Create the header
			XspViewColumnHeader header = new XspViewColumnHeader();
			header.setSortable(true);
			header.setValue(columnHeader);
			cCol.addChild(new ControlImpl<UIComponent>(header));

			viewControl.addChild(cCol);
		}
		ControlBuilder.buildControl(FacesContext.getCurrentInstance(), viewControl,true);
	}
}
