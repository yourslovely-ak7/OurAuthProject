package pojo;

import java.util.ArrayList;
import java.util.List;

public class Order 
{
	private Object tableObject;
	private List<String> orderBy;
	private boolean desc;
	private int limit;
	private int offset;
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public Order()
	{
		orderBy= new ArrayList<String>();
		desc= false;
		limit=0;
	}
	
	public Object getTableObject() {
		return tableObject;
	}
	
	public void setTableObject(Object tableObject) {
		this.tableObject = tableObject;
	}

	public List<String> getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(List<String> orderBy) {
		this.orderBy = orderBy;
	}
	public boolean isDesc() {
		return desc;
	}
	public void setDesc(boolean desc) {
		this.desc = desc;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
}
