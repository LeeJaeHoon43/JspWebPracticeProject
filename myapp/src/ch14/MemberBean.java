package ch14;

public class MemberBean {
	private String id; // ȸ�� ID.
	private String pwd; // ȸ�� ��й�ȣ.
	private String name; // ȸ�� �̸�.
	private String gender; // ȸ�� ����.
	private String birthday; // ȸ�� ����.
	private String email; // ȸ�� �̸���.
	private String zipcode; // ȸ�� �����ȣ.
	private String address; // ȸ�� �ּ�.
	private String hobby[]; // ȸ�� ���.
	private String job; // ȸ�� ����.
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String[] getHobby() {
		return hobby;
	}
	public void setHobby(String[] hobby) {
		this.hobby = hobby;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
}
