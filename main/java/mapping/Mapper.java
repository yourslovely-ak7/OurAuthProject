package mapping;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import database.SQLQueryBuilder;
import exception.ConstraintViolationException;
import exception.InvalidException;
import helper.Helper;
import helper.Validator;
import pojo.Condition;
import pojo.Fields;
import pojo.Join;
import pojo.Order;

public class Mapper {
	private static SQLQueryBuilder query = new SQLQueryBuilder();
	private static YamlLoader yaml = new YamlLoader();

	public <T> long create(T object, boolean returnGeneratedKey) throws InvalidException, ConstraintViolationException {
		try {
			Validator.checkForNull(object);

			Class<?> clazz = object.getClass();
			String classSimpleName = clazz.getSimpleName();
			String tableName = yaml.getTableName(classSimpleName);

			Fields<T> newData = new Fields<T>();
			newData.setValues(mapFieldsAndValues(object, clazz, classSimpleName, "INSERT"));

			return query.insert(tableName, newData, returnGeneratedKey, false);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException | ClassNotFoundException error) {
			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}
	
	public <T> long createBatch(T object, List<T> records) throws InvalidException, ConstraintViolationException {
		try {
			Validator.checkForNull(object);
			Validator.checkForNull(records);

			Class<?> clazz = object.getClass();
			String classSimpleName = clazz.getSimpleName();
			String tableName = yaml.getTableName(classSimpleName);

			Fields<T> newData = new Fields<T>();
			newData.setValues(mapFieldsAndValues(object, clazz, classSimpleName, "INSERT"));
			newData.setRecords(records);

			return query.insert(tableName, newData, false, true);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException | ClassNotFoundException error) {
			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}

	public <T> List<List<Object>> read(Map<T, List<String>> objects, Map<Integer, Condition> condition, Order order)
			throws InvalidException {
		try {
			List<Fields<T>> requiredFieldObjects = new ArrayList<>();
			List<String> requiredFieldNames;
			List<String> allTables = objects.keySet().stream()
					.map(obj -> yaml.getTableName(obj.getClass().getSimpleName())).collect(Collectors.toList());

			List<Join> join = new ArrayList<Join>();

			for (Map.Entry<T, List<String>> entry : objects.entrySet()) {
				T object = entry.getKey();
				List<String> fields = entry.getValue();

				Class<?> clazz = object.getClass();
				String classSimpleName = clazz.getSimpleName();
				String tableName = yaml.getTableName(classSimpleName);

				if (fields == null) {
					continue;
				}
				requiredFieldNames = new ArrayList<>();
				boolean superClassField = false;

				for (String str : fields) {
					String mappedField = yaml.getColumnName(classSimpleName, str);
					if (mappedField != null) {
						requiredFieldNames.add(mappedField);
					} else {
						superClassField = true;
					}
				}

				Fields<T> newObj = new Fields<T>();
				newObj.setTableName(tableName);
				newObj.setFieldNames(requiredFieldNames);
				requiredFieldObjects.add(newObj);

				String superClassName = clazz.getSuperclass().getSimpleName();
				if (!(superClassName.equals("Object")) && superClassField) {
					newObj = superClassFields(clazz, fields, object);
					requiredFieldObjects.add(newObj);

					allTables.add(superClassName);
					join = constructJoin(join, classSimpleName, allTables);
					allTables.remove(superClassName);
				} else {
					join = constructJoin(join, classSimpleName, allTables);
				}
			}
			Map<Integer, Condition> queryCond = new HashMap<>();
			if (condition.size() != 0) {
				queryCond = constructCondition(condition);
			}

			List<String> orderFields = order.getOrderBy();
			int len = orderFields.size();
			if (len != 0) {
				String classSimpleName = order.getTableObject().getClass().getSimpleName();
				List<String> orderBy = new ArrayList<String>();
				for (int i = 0; i < len; i++) {
					orderBy.add(yaml.getColumnName(classSimpleName, orderFields.get(i)));
				}
				order.setOrderBy(orderBy);
			}
			List<Map<String, Object>> result = query.select(requiredFieldObjects, join, queryCond, order);

			return objectConversion(objects, result);
		} catch (InvalidException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException error) {

			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}

	public <T> int update(List<T> objects, Map<Integer, Condition> condition) throws InvalidException {
		try {
			int length = objects.size();
			List<String> allTables = objects.stream().map(obj -> yaml.getTableName(obj.getClass().getSimpleName()))
					.collect(Collectors.toList());

			List<Join> join = new ArrayList<>();
			List<Fields<T>> newValues = new ArrayList<>();

			for (T iterObj : objects) {
				Class<?> clazz = iterObj.getClass();
				String classSimpleName = clazz.getSimpleName();
				String tableName = yaml.getTableName(classSimpleName);

				Fields<T> valueEntry = new Fields<T>();
				valueEntry.setTableName(tableName);
				valueEntry.setValues(mapFieldsAndValues(iterObj, clazz, classSimpleName, "UPDATE"));
				newValues.add(valueEntry);

				Class<?> superClass = clazz.getSuperclass();
				String superClassName = superClass.getSimpleName();

				if (!(superClassName.equals("Object"))) {
					tableName = yaml.getTableName(superClassName);

					valueEntry = new Fields<T>();
					valueEntry.setTableName(tableName);
					valueEntry.setValues(mapFieldsAndValues(iterObj, superClass, superClassName, "UPDATE"));
					newValues.add(valueEntry);

					allTables.add(superClassName);
					join = constructJoin(join, classSimpleName, allTables);
					allTables.remove(superClassName);
				}

				System.out.println("Join built : " + join.toString());
				if (length > 1) {
					join = constructJoin(join, classSimpleName, allTables);
				}
			}

			Map<Integer, Condition> queryCond = new HashMap<>();

			queryCond = constructCondition(condition);

			return query.update(newValues, join, queryCond);
		} catch (InvalidException | NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException | ClassNotFoundException error) {
			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}

	public <T> void delete(T object, Map<Integer, Condition> condition) throws InvalidException {
		try {
			String classSimpleName = object.getClass().getSimpleName();
			String tableName = yaml.getTableName(classSimpleName);

			Map<Integer, Condition> queryCond = new HashMap<>();

			if (condition.size() != 0) {
				queryCond = constructCondition(condition);
			} else {
				queryCond = constructPKCondition(object);
			}

			query.delete(tableName, queryCond);
		} catch (InvalidException | NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException | ClassNotFoundException error) {
			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}

	public <T> int getCount(List<T> object, Map<Integer, Condition> condition) throws InvalidException {
		try {
			int len = object.size();
			List<String> allTables = object.stream().map(obj -> yaml.getTableName(obj.getClass().getSimpleName()))
					.collect(Collectors.toList());

			List<Join> join = new ArrayList<Join>();

			for (T currObj : object) {
				Class<?> clazz = currObj.getClass();
				String classSimpleName = clazz.getSimpleName();

				String superClassName = clazz.getSuperclass().getSimpleName();
				if (!(superClassName.equals("Object"))) {
					allTables.add(superClassName);
					join = constructJoin(join, classSimpleName, allTables);
					allTables.remove(superClassName);
					System.out.println("Join built : " + join.toString());
				}

				if (len != 1) {
					join = constructJoin(join, classSimpleName, allTables);
				}
			}

			Map<Integer, Condition> queryCond = new HashMap<>();

			if (condition.size() != 0) {
				queryCond = constructCondition(condition);
			}

			String table = yaml.getTableName(object.get(0).getClass().getSimpleName());
			return query.selectCount(table, join, queryCond);
		} catch (InvalidException | ClassNotFoundException error) {
			System.out.println("Error : " + error.getMessage());
			throw new InvalidException("Request Failed! Check required Data and Try Again!", error);
		}
	}

	private <T> Map<String, Object> mapFieldsAndValues(T object, Class<?> clazz, String classSimpleName, String type)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException,
			InvalidException {
		Map<String, Object> valueSet = new HashMap<String, Object>();

		Map<String, String> fieldSet = yaml.getAllColumnNames(classSimpleName);
		String autoIncrementFieldName = yaml.getAutoIncrementField(classSimpleName);

		Validator.checkForNull(fieldSet);

		for (Map.Entry<String, String> entry : fieldSet.entrySet()) {
			String pojoFieldName = entry.getKey();
			String tableFieldName = entry.getValue();

			String getterMethodName = "get" + Helper.capitalize(pojoFieldName);
			Method getterMethod = clazz.getDeclaredMethod(getterMethodName);

			if (autoIncrementFieldName == null || !autoIncrementFieldName.equals(tableFieldName)) {
				Object value = getterMethod.invoke(object);

				switch(type)
				{
					case "INSERT":
						valueSet.put(tableFieldName, value);
						break;
					case "UPDATE":
						if (value != null && isNonEmptyValue(value)) {
							valueSet.put(tableFieldName, value);
						}
						break;
				}
			}
		}
		return valueSet;
	}

	private Map<Integer, Condition> constructCondition(Map<Integer, Condition> conditions) {
		Condition newCondition;

		for (Map.Entry<Integer, Condition> entry : conditions.entrySet()) {
			newCondition = entry.getValue();

			String conditionTable = newCondition.getTableName();
			String tableName = yaml.getTableName(conditionTable);
			String fieldName = yaml.getColumnName(conditionTable, newCondition.getFieldName());

			newCondition.setTableName(tableName);
			newCondition.setFieldName(fieldName);
		}
		return conditions;
	}

	private <T> Map<Integer, Condition> constructPKCondition(T newObj)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		Map<Integer, Condition> queryCond = new HashMap<>();

		Class<?> clazz = newObj.getClass();
		String classSimpleName = clazz.getSimpleName();
		String tableName = yaml.getTableName(classSimpleName);

		String pk = yaml.getPrimaryKey(clazz.getSimpleName());
		String pkFieldName = yaml.getPrimaryKeyFieldName(classSimpleName, pk);
		Object value = "";
		Field[] declaredFields = clazz.getDeclaredFields();

		for (Field field : declaredFields) {
			String fieldName = field.getName();
			if (fieldName.equals(pkFieldName)) {
				String getterMethodName = "get" + Helper.capitalize(fieldName);

				Method getterMethod = clazz.getDeclaredMethod(getterMethodName);

				value = getterMethod.invoke(newObj);
			}
		}

		Condition newCondition = new Condition();
		newCondition.setTableName(tableName);
		newCondition.setFieldName(pk);
		newCondition.setOperator(" = ");
		newCondition.setValue(value);
		newCondition.setConjuctiveOpt("");
		queryCond.put(1, newCondition);

		return queryCond;
	}

	private List<Join> constructJoin(List<Join> join, String classSimpleName, List<String> tableNames) {
		String tableName = yaml.getTableName(classSimpleName);

		Map<String, Map<String, String>> foreignKeys = yaml.getAllForeignKeyReferences(classSimpleName);

		if (foreignKeys != null) {
			for (Map.Entry<String, Map<String, String>> fkEntry : foreignKeys.entrySet()) {
				Map<String, String> fkDetails = fkEntry.getValue();

				String referencedTable = fkDetails.get("reference");
				String foreignKeyField = fkDetails.get("field");
				String referenceField = fkDetails.get("referenceField");

				if (tableNames.contains(referencedTable)) {
					Join joiner = new Join();
					joiner.setTableName(tableName);
					joiner.setFieldName(foreignKeyField);
					joiner.setReferenceTable(referencedTable);
					joiner.setReferenceField(referenceField);

					join.add(joiner);
				}
			}
		}
		System.out.println("Join built for : " + classSimpleName + " " + join.toString());
		return join;
	}

	private static boolean isNonEmptyValue(Object value) {
		if (value instanceof String) {
			return !((String) value).trim().isEmpty();
		} else if (value instanceof Integer) {
			return (Integer) value != 0;
		} else if (value instanceof Long) {
			return (Long) value != 0L;
		} else if (value instanceof Double) {
			return (Double) value != 0.0;
		} else if (value instanceof Float) {
			return (Float) value != 0.0f;
		} else if (value instanceof Boolean) {
			return value != null;
		}
		return true;
	}

	private <T> Fields<T> superClassFields(Class<?> childClazz, List<String> fields, T object) {
		Fields<T> newObj = new Fields<T>();

		Class<?> clazz = childClazz.getSuperclass();
		String classSimpleName = clazz.getSimpleName();
		String tableName = yaml.getTableName(classSimpleName);

		List<String> requiredFieldNames = new ArrayList<>();

		for (String str : fields) {
			String mappedField = yaml.getColumnName(classSimpleName, str);
			if (mappedField != null) {
				requiredFieldNames.add(mappedField);
			}
		}

		newObj.setTableName(tableName);
		newObj.setFieldNames(requiredFieldNames);

		return newObj;
	}

	private <T> List<List<Object>> objectConversion(Map<T, List<String>> objects, List<Map<String, Object>> result)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		List<List<Object>> output = new ArrayList<>();
		List<Object> rowOutput;
		int len = result.size();

		if (result.size() == 0) {
			return output;
		}

		for (int i = 0; i < len; i++) {
			Map<String, Object> row = result.get(i);
			rowOutput = new ArrayList<>();

			for (Map.Entry<T, List<String>> entry : objects.entrySet()) {
				T object = entry.getKey();
				List<String> fields = entry.getValue();

				if (fields == null) {
					continue;
				}
				Class<?> clazz = object.getClass();
				Object newInstance = clazz.getDeclaredConstructor().newInstance();
				String classSimpleName = clazz.getSimpleName();
				String columnName;
				String superClassName = clazz.getSuperclass().getSimpleName();
				for (String currField : fields) {
					boolean superClassField = false;

					columnName = yaml.getColumnName(classSimpleName, currField);

					if (columnName == null && !(superClassName.equals("Object"))) {
						columnName = yaml.getColumnName(superClassName, currField);
						superClassField = true;
					}

					if (row.keySet().contains(columnName)) {
						Object value = row.get(columnName);
						if (value != null) {
							String setterMethodName = "set" + Helper.capitalize(currField);

							Class<?> parameterClass = null;
							String parameterClassName = value.getClass().getSimpleName();

							switch (parameterClassName) {
							case "Integer":
								parameterClass = int.class;
								break;
							case "Long":
								parameterClass = long.class;
								break;
							case "Float":
								parameterClass = float.class;
								break;
							case "String":
								parameterClass = String.class;
								break;
							case "Boolean":
								parameterClass = Boolean.class;
								break;
							case "Double":
								parameterClass = double.class;
								break;
							default:
								throw new IllegalArgumentException("Unsupported type: " + parameterClassName);
							}
							Method setterMethod;

							if (superClassField) {
								setterMethod = clazz.getSuperclass().getDeclaredMethod(setterMethodName,
										parameterClass);
							} else {
								setterMethod = clazz.getDeclaredMethod(setterMethodName, parameterClass);
							}

							setterMethod.invoke(newInstance, value);
						}
					}
				}
				rowOutput.add(newInstance);
			}
			output.add(rowOutput);
		}
//		System.out.println(output);
		return output;
	}
}
