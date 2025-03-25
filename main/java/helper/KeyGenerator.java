package helper;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import crud.KeyOperation;
import exception.InvalidException;
import pojo.Key;

public class KeyGenerator 
{
	   public static void main(String[] args) throws Exception {
	        // Generate RSA Key Pair
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(2048);
	        KeyPair keyPair = keyPairGenerator.generateKeyPair();
	        
	        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
	        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

	        // Generate Key ID (kid)
	        String keyId = Helper.getMD5Hash(Helper.getRandomString());

	        // Convert Public Key to JWKS format using JSONObject
	        JSONObject jwk = new JSONObject();
	        jwk.put("kty", "RSA");
	        jwk.put("alg", "RS256");
	        jwk.put("use", "sig");
	        jwk.put("kid", keyId);
	        jwk.put("n", Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()));
	        jwk.put("e", Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));

	        // Create JSON array and object
	        JSONObject jwks = new JSONObject();
	        jwks.put("keys", new JSONArray().put(jwk));

	        // Save Public Key as JSON file
	        try (FileWriter fileWriter = new FileWriter("jwks.json")) {
	            fileWriter.write(jwks.toString(4)); // Pretty print JSON
	        }
	        
	        String encodedPubKey= Base64.getEncoder().encodeToString(publicKey.getEncoded());
	        String encodedPvtKey= Base64.getEncoder().encodeToString(privateKey.getEncoded());
	        
	        Key newKey= new Key();
	        newKey.setKeyId(keyId)
	        .setPublicKey(encodedPubKey)
	        .setPrivateKey(encodedPvtKey);
	        
	        try
	        {
	        	KeyOperation.createKeyEntry(newKey);
	        }
	        catch(InvalidException error)
	        {
	        	error.printStackTrace();
	        }
	   }
}
