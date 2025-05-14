package pojo;

import exception.InvalidException;
import helper.Validator;

public class User {
	
	private int userId;
	private String name;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String gender;
	
	public int getUserId() {
		return userId;
	}
	public User setUserId(int userId) {
		this.userId = userId;
		return this;
	}
	public String getName() {
		return name;
	}
	public User setName(String name) throws InvalidException {
		Validator.validate(name, "name");
		this.name = name;
		return this;
	}
	public String getFirstName() {
		return firstName;
	}
	public User setFirstName(String firstName) throws InvalidException {
		Validator.validate(firstName, "first_name");
		this.firstName = firstName;
		return this;
	}
	public String getLastName() {
		return lastName;
	}
	public User setLastName(String lastName) throws InvalidException {
		Validator.validate(lastName, "last_name");
		this.lastName = lastName;
		return this;
	}
	public String getEmail() {
		return email;
	}
	public User setEmail(String email) throws InvalidException {
		Validator.validate(email, "email");
		this.email = email;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public User setPassword(String password) throws InvalidException {
		Validator.validate(password, "password");
		this.password = password;
		return this;
	}
	public String getGender() {
		return gender;
	}
	public User setGender(String gender) throws InvalidException {
		Validator.validate(gender, "gender");
		this.gender = gender;
		return this;
	}	
}
