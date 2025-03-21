package pojo;

public class Join 
{
	private String tableName;
	private String fieldName;
	private String referenceTable;
	private String referenceField;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getReferenceTable() {
		return referenceTable;
	}
	public void setReferenceTable(String referenceTable) {
		this.referenceTable = referenceTable;
	}
	public String getReferenceField() {
		return referenceField;
	}
	public void setReferenceField(String referenceField) {
		this.referenceField = referenceField;
	}
	
	@Override
	public String toString() {
		return "Join [tableName=" + tableName + ", fieldName=" + fieldName + ", referenceTable=" + referenceTable
				+ ", referenceField=" + referenceField + "]";
	}
}
