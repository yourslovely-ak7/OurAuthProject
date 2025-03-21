package pojo;

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
	public User setName(String name) {
		this.name = name;
		return this;
	}
	public String getFirstName() {
		return firstName;
	}
	public User setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	public String getLastName() {
		return lastName;
	}
	public User setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	public String getEmail() {
		return email;
	}
	public User setEmail(String email) {
		this.email = email;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public User setPassword(String password) {
		this.password = password;
		return this;
	}
	public String getGender() {
		return gender;
	}
	public User setGender(String gender) {
		this.gender = gender;
		return this;
	}	
}
