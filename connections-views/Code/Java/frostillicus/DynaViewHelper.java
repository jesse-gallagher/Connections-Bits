package frostillicus;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import lotus.domino.*;

public class DynaViewHelper {
	private DynaViewHelper() { }

	public static String getView(String databaseName, String selectionFormula, List<String> columns) throws NotesException, NoSuchAlgorithmException {
		String server = "";
		String filePath = "";
		if(databaseName.isEmpty()) {
			filePath = ExtLibUtil.getCurrentDatabase().getFilePath();
		} else if(databaseName.contains("!!")) {
			String[] pathBits = databaseName.split("!!");
			server = pathBits[0];
			filePath = pathBits[1];
		} else {
			filePath = databaseName;
		}

		Session sessionAsSigner = ExtLibUtil.getCurrentSessionAsSigner();
		Database database = sessionAsSigner.getDatabase(server, filePath);

		StringBuilder signature = new StringBuilder();
		signature.append(selectionFormula);
		for(String columnName : columns) {
			signature.append(columnName);
		}
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte[] digest = md5.digest(signature.toString().getBytes());
		String digestText = new BigInteger(1, digest).toString(16);

		String viewName = "($$DynaView-" + digestText + ")";
		View view = database.getView(viewName);
		if(view == null) {
			view = database.createView(viewName, selectionFormula, null, true);

			// Remove the default first column
			view.removeColumn();

			// Add each specified column in turn
			for(String columnDescription : columns) {
				ColumnInfo info = new ColumnInfo(columnDescription);
				ViewColumn col = view.createColumn();
				col.setTitle(info.getTitle());
				col.setFormula(info.getFormula());

				if(info.isDefaultSortAscending()) {
					col.setSorted(true);
				} else if(info.isDefaultSortDescending()) {
					col.setSortDescending(true);
				}

				col.setResortAscending(info.isSortAscending());
				col.setResortDescending(info.isSortDescending());
			}
		}
		view.recycle();
		database.recycle();

		return viewName;
	}

	public static class ColumnInfo {
		private String title;
		private String formula;
		private boolean defaultSortAscending = false;
		private boolean defaultSortDescending = false;
		private boolean sortAscending = false;
		private boolean sortDescending = false;

		public ColumnInfo(String columnDescription) {
			String[] columnBits = columnDescription.split("--");

			String title = columnBits.length < 2 ? columnBits[0] : columnBits[1];
			String formula = columnBits[0];

			this.setTitle(title);
			this.setFormula(formula);

			if(columnBits.length >= 3) {
				// Then there's a default sort bit
				if(columnBits[2].equals("ascending")) {
					this.setDefaultSortAscending(true);
				} else if(columnBits[2].equals("descending")) {
					this.setDefaultSortDescending(true);
				}
			}

			if(columnBits.length >= 4) {
				// Then there's an optional sort bit
				if(columnBits[3].equals("ascending")) {
					this.setSortAscending(true);
				} else if(columnBits[3].equals("descending")) {
					this.setSortDescending(true);
				} else if(columnBits[3].equals("both")) {
					this.setSortAscending(true);
					this.setSortDescending(true);
				}
			}
		}

		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public String getFormula() { return formula; }
		public void setFormula(String formula) { this.formula = formula; }
		public boolean isDefaultSortAscending() { return defaultSortAscending; }
		public void setDefaultSortAscending(boolean defaultSortAscending) { this.defaultSortAscending = defaultSortAscending; }
		public boolean isDefaultSortDescending() { return defaultSortDescending; }
		public void setDefaultSortDescending(boolean defaultSortDescending) { this.defaultSortDescending = defaultSortDescending; }
		public boolean isSortAscending() { return sortAscending; }
		public void setSortAscending(boolean sortAscending) { this.sortAscending = sortAscending; }
		public boolean isSortDescending() { return sortDescending; }
		public void setSortDescending(boolean sortDescending) { this.sortDescending = sortDescending; }
	}
}
