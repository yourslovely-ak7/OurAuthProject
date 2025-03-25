package pojo;

public class Key {
	
	private String keyId;
	private String privateKey;
	private String publicKey;
	
	public String getKeyId() {
		return keyId;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public Key setKeyId(String keyId) {
		this.keyId = keyId;
		return this;
	}
	public Key setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
		return this;
	}
	public Key setPublicKey(String publicKey) {
		this.publicKey = publicKey;
		return this;
	}
}
