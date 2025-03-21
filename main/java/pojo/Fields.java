package pojo;

import java.util.List;
import java.util.Map;

public class Fields<T>
{
	private String tableName;
	private List<String> fieldNames;
	private Map<String, Object> values;
	private List<T> records;
	
	public List<T> getRecords() {
		return records;
	}
	public void setRecords(List<T> records) {
		this.records = records;
	}
	public Map<String, Object> getValues() {
		return values;
	}
	public void setValues(Map<String, Object> newValues) {
		this.values = newValues;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<String> getFieldNames() {
		return fieldNames;
	}
	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}
}
