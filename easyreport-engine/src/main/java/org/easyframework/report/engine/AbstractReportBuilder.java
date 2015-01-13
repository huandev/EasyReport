package org.easyframework.report.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.easyframework.report.engine.data.ColumnTree;
import org.easyframework.report.engine.data.ColumnTreeNode;
import org.easyframework.report.engine.data.ReportDataColumn;
import org.easyframework.report.engine.data.ReportDataSet;
import org.easyframework.report.engine.data.ReportParameter;
import org.easyframework.report.engine.data.ReportTable;

public abstract class AbstractReportBuilder {
	protected final ReportDataSet reportDataSet;
	protected final ReportParameter reportParameter;
	protected final StringBuilder tableRows = new StringBuilder();

	protected AbstractReportBuilder(ReportDataSet reportDataSet, ReportParameter reportParameter) {
		this.reportDataSet = reportDataSet;
		this.reportParameter = reportParameter;
	}

	public ReportTable getTable() {
		StringBuilder table = new StringBuilder();
		table.append("<table id=\"easyreport\" class=\"easyreport\">");
		table.append(this.tableRows.toString());
		table.append("</table>");
		return new ReportTable(table.toString(),
				this.reportParameter.getSqlText(),
				reportDataSet.getMetaData().getRows().size());
	}

	public void drawTableHeaderRows() {
		List<ReportDataColumn> leftFixedColumns = this.reportDataSet.getHeaderLeftFixedColumns();
		ColumnTree rightColumnTree = this.reportDataSet.getHeaderRightColumnTree();
		int rowCount = rightColumnTree.getDepth();
		String rowSpan = rowCount > 1 ? String.format(" rowspan=\"%s\"", rowCount) : "";

		this.tableRows.append("<thead>");
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			this.tableRows.append("<tr class=\"easyreport-header\">");
			if (rowIndex == 0) {
				for (ReportDataColumn leftColumn : leftFixedColumns) {
					this.tableRows.append(String.format("<th%s>%s</th>", rowSpan, leftColumn.getText()));
				}
			}
			for (ColumnTreeNode rightColumn : rightColumnTree.getNodesByLevel(rowIndex)) {
				String colSpan = rightColumn.getSpans() > 1 ? String.format(" colspan=\"%s\"", rightColumn.getSpans()) : "";
				this.tableRows.append(String.format("<th%s>%s</th>", colSpan, rightColumn.getValue()));
			}
			this.tableRows.append("</tr>");
		}
		this.tableRows.append("</thead>");
	}

	/**
	 * 生成表体左边每一行的单元格
	 * 
	 * @param pathTreeNodeMap
	 *            树中每个节点的path属性为key,treeNode属性为value的map对象
	 * @param lastNodePaths
	 *            上一个跨行结点的树路径
	 * @param rowNode
	 *            当前行结点
	 * @param isRowSpan
	 *            是否跨行(rowspan)
	 * @return
	 */
	protected String[] drawLeftFixedColumn(Map<String, ColumnTreeNode> pathTreeNodeMap,
			String[] lastNodePaths, ColumnTreeNode rowNode, boolean isRowSpan) {
		if (isRowSpan) {
			return this.drawLeftRowSpanColumn(pathTreeNodeMap, lastNodePaths, rowNode);
		}

		String[] paths = StringUtils.splitPreserveAllTokens(rowNode.getPath(), this.reportDataSet.getPathSeparator());
		if (paths == null || paths.length == 0) {
			return null;
		}

		int level = paths.length > 1 ? paths.length - 1 : 1;
		for (int i = 0; i < level; i++) {
			this.tableRows.append(String.format("<td class=\"easyreport-fixed-column\">%s</td>", paths[i]));
		}
		return null;
	}

	/**
	 * 按层次遍历报表列树中每个结点，然后以结点path为key,treeNode属性为value，生成一个Map对象
	 * 
	 * @param columnTree
	 *            报表列树对象
	 * @return 树中每个节点的path属性为key,treeNode属性为value的map对象
	 */
	protected Map<String, ColumnTreeNode> getPathTreeNodeMap(ColumnTree columnTree) {
		Map<String, ColumnTreeNode> pathTreeNodeMap = new HashMap<String, ColumnTreeNode>();
		for (int level = 0; level < columnTree.getDepth(); level++) {
			for (ColumnTreeNode treeNode : columnTree.getNodesByLevel(level)) {
				pathTreeNodeMap.put(treeNode.getPath(), treeNode);
			}
		}
		return pathTreeNodeMap;
	}

	/**
	 * 生成表体左边每一行的跨行(rowspan)单元格
	 * 
	 * @param pathTreeNodeMap
	 *            树中每个节点的path属性为key,treeNode属性为value的map对象
	 * @param lastNodePaths
	 *            上一个跨行结点的树路径
	 * @param rowNode
	 *            当前行结点
	 * @return 当前跨行结点的树路径
	 */
	protected abstract String[] drawLeftRowSpanColumn(Map<String, ColumnTreeNode> pathTreeNodeMap, String[] lastNodePaths, ColumnTreeNode rowNode);
}
