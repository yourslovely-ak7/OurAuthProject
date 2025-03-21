package pojo;

public class Condition 
{
	private String tableName;
	private String fieldName;
	private Object value;
	private String operator;
	private String conjuctiveOpt;
	
	public String getConjuctiveOpt() {
		return conjuctiveOpt;
	}
	public void setConjuctiveOpt(String conjuctiveOpt) {
		this.conjuctiveOpt = conjuctiveOpt;
	}
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
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
}
